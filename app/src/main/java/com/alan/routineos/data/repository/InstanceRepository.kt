package com.alan.routineos.data.repository

import android.util.Log
import com.alan.routineos.core.util.DateUtils
import com.alan.routineos.data.local.dao.DayInstanceDao
import com.alan.routineos.data.local.dao.FieldValueDao
import com.alan.routineos.data.local.dao.NodeDao
import com.alan.routineos.data.local.dao.NodeOverrideDao
import com.alan.routineos.data.local.dao.NodeScheduleDao
import com.alan.routineos.data.local.dao.ScheduleDao
import com.alan.routineos.data.local.dao.ScheduleExceptionDao
import com.alan.routineos.data.local.entities.DayInstance
import com.alan.routineos.data.local.entities.InstanceStatus
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeFieldValue
import com.alan.routineos.data.local.entities.NodeSchedule
import com.alan.routineos.data.local.entities.NodeStatus
import com.alan.routineos.data.local.entities.RecurrenceType
import com.alan.routineos.data.local.entities.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Calendar
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstanceRepository @Inject constructor(
    private val dayInstanceDao: DayInstanceDao,
    private val nodeDao: NodeDao,
    private val nodeScheduleDao: NodeScheduleDao,
    private val fieldValueDao: FieldValueDao,
    private val scheduleDao: ScheduleDao,
    private val nodeOverrideDao: NodeOverrideDao,
    private val scheduleExceptionDao: ScheduleExceptionDao
) {
    private val instanceGenerationMutex = Mutex()

    fun getByDate(date: Long): Flow<List<DayInstance>> = dayInstanceDao.getAllByDate(date)

    suspend fun upsert(instance: DayInstance) = dayInstanceDao.upsert(instance)

    suspend fun update(instance: DayInstance) = dayInstanceDao.update(instance)

    suspend fun generateInstanceIfNeeded(
        templateId: String,
        date: Long
    ): DayInstance? = instanceGenerationMutex.withLock {
        generateInstanceIfNeededInternal(templateId, date)
    }

    private suspend fun generateInstanceIfNeededInternal(
        templateId: String,
        date: Long
    ): DayInstance? {
        val weekday = DateUtils.getDayOfWeek(Date(date))
        val isBlocked = isTemplateBlockedByException(templateId, date)
        val isApplicable = !isBlocked && hasApplicableSchedule(templateId, weekday)
        
        Log.d("TODAY_DEBUG", "GENERATE_CHECK templateId=$templateId date=$date applicable=$isApplicable blocked=$isBlocked")

        val existing = dayInstanceDao.getByTemplateAndDate(templateId, date)

        if (!isApplicable) {
            if (existing != null) {
                Log.d("TODAY_DEBUG", "CLEANUP NON_APPLICABLE instanceId=${existing.id}")
                cleanupInstanceData(existing.id)
                dayInstanceDao.deleteById(existing.id)
            }
            return null
        }

        if (existing != null) {
            val nodes = nodeDao.getByInstance(existing.id).first()
            if (nodes.isNotEmpty()) return existing
            
            cleanupInstanceData(existing.id)
            dayInstanceDao.deleteById(existing.id)
        }

        return generateInstance(templateId, date)
    }

    suspend fun refreshInstancesForDate(date: Long) = instanceGenerationMutex.withLock {
        val todayStart = DateUtils.getStartOfDay(date)
        val weekday = DateUtils.getDayOfWeek(Date(todayStart))
        
        // 1. Templates con horario activo hoy
        val allSchedules = scheduleDao.getAll().first()
        val scheduledTemplateIds = allSchedules
            .filter { it.weekday == weekday && it.isActive }
            .map { it.templateId }
            .toSet()
            
        // 2. Templates que ya tienen instancia
        val existingInstances = dayInstanceDao.getAllByDate(todayStart).first()
        val existingTemplateIds = existingInstances.mapNotNull { it.templateId }.toSet()
        
        val allTemplateIds = scheduledTemplateIds + existingTemplateIds
        
        Log.d("TODAY_DEBUG", "REFRESH_DATE date=$todayStart templates_to_check=${allTemplateIds.size}")
        
        allTemplateIds.forEach { tId ->
            generateInstanceIfNeededInternal(tId, todayStart)
        }
    }

    suspend fun regenerateTemplateInstanceForDate(
        templateId: String,
        date: Long
    ) = instanceGenerationMutex.withLock {
        val weekday = DateUtils.getDayOfWeek(Date(date))
        val isBlocked = isTemplateBlockedByException(templateId, date)
        val isApplicable = !isBlocked && hasApplicableSchedule(templateId, weekday)
        
        val oldInstances = dayInstanceDao.getAllByTemplateAndDate(templateId, date)

        if (!isApplicable) {
            if (oldInstances.isNotEmpty()) {
                oldInstances.forEach { oldInstance ->
                    cleanupInstanceData(oldInstance.id)
                    dayInstanceDao.deleteById(oldInstance.id)
                }
            }
            return@withLock
        }

        oldInstances.forEach { oldInstance ->
            cleanupInstanceData(oldInstance.id)
            dayInstanceDao.deleteById(oldInstance.id)
        }

        generateInstance(templateId, date)
    }

    private suspend fun isTemplateBlockedByException(templateId: String, date: Long): Boolean {
        val exceptions = scheduleExceptionDao.getActiveForDateSync(date)
        val weekday = DateUtils.getDayOfWeek(Date(date))
        val dayOfMonth = Calendar.getInstance().apply { timeInMillis = date }.get(Calendar.DAY_OF_MONTH)

        return exceptions.any { ex ->
            if (!ex.affectsGeneration) return@any false
            if (ex.templateId != null && ex.templateId != templateId) return@any false

            when (ex.recurrenceType) {
                RecurrenceType.NONE -> true
                RecurrenceType.WEEKLY -> ex.weekday == weekday
                RecurrenceType.MONTHLY -> {
                    // Si weekday está presente en MONTHLY, lo tratamos como día del mes por ahora
                    // o implementamos lógica más compleja si se requiere "Primer lunes"
                    ex.weekday == dayOfMonth 
                }
            }
        }
    }

    private suspend fun cleanupInstanceData(instanceId: String) {
        nodeOverrideDao.deleteByInstanceId(instanceId)
        val nodes = nodeDao.getByInstance(instanceId).first()
        val nodeIds = nodes.map { it.id }
        if (nodeIds.isNotEmpty()) {
            val fieldValues = fieldValueDao.getByNodes(nodeIds).first()
            fieldValueDao.deleteByIds(fieldValues.map { it.id })
        }
        nodeDao.deleteByInstance(instanceId)
    }

    private suspend fun hasApplicableSchedule(templateId: String, todayWeekday: Int): Boolean {
        val globalSchedules = scheduleDao.getByTemplateSync(templateId)
        if (globalSchedules.any { it.weekday == todayWeekday && it.isActive }) return true

        val templateNodes = nodeDao.getAllByTemplate(templateId)
        if (templateNodes.isNotEmpty()) {
            val nodeIds = templateNodes.map { it.id }
            val nodeSchedules = nodeScheduleDao.getSchedulesForNodes(nodeIds)
            if (nodeSchedules.any { it.dayOfWeek == todayWeekday }) return true
        }
        
        return false
    }

    suspend fun dedupeInstancesForDate(date: Long) = instanceGenerationMutex.withLock {
        val instances = dayInstanceDao.getAllByDate(date).first()
        instances.groupBy { it.templateId }.forEach { (_, group) ->
            if (group.size <= 1) return@forEach
            val keep = group.first()
            val duplicates = group.filter { it.id != keep.id }
            duplicates.forEach { duplicate ->
                cleanupInstanceData(duplicate.id)
                dayInstanceDao.deleteById(duplicate.id)
            }
        }
    }

    private suspend fun generateInstance(templateId: String, date: Long): DayInstance? {
        val allNodes = nodeDao.getAllTemplateNodesSync()
        val templateNodesOnly = allNodes.filter { it.templateId == templateId && it.instanceId == null }
        
        if (templateNodesOnly.isEmpty()) return null

        val weekday = DateUtils.getDayOfWeek(Date(date))
        val instanceId = UUID.randomUUID().toString()
        val newInstance = DayInstance(
            id = instanceId,
            templateId = templateId,
            date = date,
            status = InstanceStatus.GENERATED
        )

        val roots = templateNodesOnly.filter { it.parentId == null }
        val templateTreeNodes = mutableListOf<Node>()
        val queue = ArrayDeque<Node>()
        queue.addAll(roots)
        val visited = mutableSetOf<String>()
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            if (node.id in visited) continue
            visited.add(node.id)
            templateTreeNodes.add(node)
            queue.addAll(templateNodesOnly.filter { it.parentId == node.id })
        }

        val idMap = mutableMapOf<String?, String?>(null to null)
        templateTreeNodes.forEach { idMap[it.id] = UUID.randomUUID().toString() }

        val allSchedules = nodeScheduleDao.getSchedulesForNodes(templateTreeNodes.map { it.id })
        val instanceNodes = mutableListOf<Node>()
        val clonedSchedules = mutableListOf<NodeSchedule>()
        val clonedFieldValues = mutableListOf<NodeFieldValue>()

        for (tNode in templateTreeNodes) {
            val newNodeId = idMap[tNode.id]!!
            val nodeSchedules = allSchedules.filter { it.nodeId == tNode.id }
            val todaySchedule = nodeSchedules.find { it.dayOfWeek == weekday }

            nodeSchedules.forEach { s ->
                clonedSchedules.add(s.copy(
                    id = UUID.randomUUID().toString(),
                    nodeId = newNodeId,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                ))
            }

            val templateValues = fieldValueDao.getByNodeSync(tNode.id)
            templateValues.forEach { tValue ->
                clonedFieldValues.add(tValue.copy(
                    id = UUID.randomUUID().toString(),
                    nodeId = newNodeId
                ))
            }

            instanceNodes.add(tNode.copy(
                id = newNodeId,
                parentId = idMap[tNode.parentId],
                templateId = templateId,
                sourceTemplateNodeId = tNode.id,
                instanceId = instanceId,
                status = NodeStatus.PENDING,
                scheduledTime = todaySchedule?.startTime ?: tNode.scheduledTime,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING_SYNC,
                version = 1
            ))
        }

        dayInstanceDao.upsert(newInstance)
        nodeDao.insertAll(instanceNodes)
        if (clonedSchedules.isNotEmpty()) nodeScheduleDao.insertAll(clonedSchedules)
        clonedFieldValues.forEach { fieldValueDao.upsert(it) }

        return newInstance
    }

    fun getNodesForInstance(instanceId: String): Flow<List<Node>> = nodeDao.getByInstance(instanceId)

    suspend fun getCompletionRate(from: Long): Float {
        val totalList = dayInstanceDao.getInRange(from, System.currentTimeMillis()).first()
        if (totalList.isEmpty()) return 0f
        val completedCount = dayInstanceDao.countByStatus(InstanceStatus.COMPLETED, from)
        return completedCount.toFloat() / totalList.size
    }

    suspend fun calculateCurrentStreak(): Int {
        val instances = dayInstanceDao.getInRange(0, System.currentTimeMillis()).first().sortedByDescending { it.date }
        var streak = 0
        var expectedDate = DateUtils.getStartOfDay()
        for (instance in instances) {
            if (instance.status == InstanceStatus.COMPLETED) {
                if (instance.date == expectedDate) {
                    streak++
                    expectedDate -= 24 * 60 * 60 * 1000L
                } else if (instance.date < expectedDate) break
            } else if (instance.date != DateUtils.getStartOfDay()) break
        }
        return streak
    }
}
