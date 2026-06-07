package com.alan.routineos.data.repository

import android.util.Log
import com.alan.routineos.core.util.DateUtils
import com.alan.routineos.data.local.dao.DayInstanceDao
import com.alan.routineos.data.local.dao.FieldValueDao
import com.alan.routineos.data.local.dao.NodeDao
import com.alan.routineos.data.local.dao.NodeScheduleDao
import com.alan.routineos.data.local.dao.ScheduleDao
import com.alan.routineos.data.local.entities.DayInstance
import com.alan.routineos.data.local.entities.InstanceStatus
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeSchedule
import com.alan.routineos.data.local.entities.NodeStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    private val scheduleDao: ScheduleDao
) {
    private val instanceGenerationMutex = Mutex()

    fun getByDate(date: Long): Flow<List<DayInstance>> = dayInstanceDao.getAllByDate(date)

    suspend fun upsert(instance: DayInstance) = dayInstanceDao.upsert(instance)

    suspend fun update(instance: DayInstance) = dayInstanceDao.update(instance)

    suspend fun generateInstanceIfNeeded(
        templateId: String,
        date: Long
    ): DayInstance? = instanceGenerationMutex.withLock {
        val weekday = DateUtils.getDayOfWeek(Date(date))
        val isApplicable = hasApplicableSchedule(templateId, weekday)
        
        Log.d("TODAY_DEBUG", "TEMPLATE APPLICABLE TODAY templateId=$templateId today=$weekday result=$isApplicable")

        val existing = dayInstanceDao.getByTemplateAndDate(templateId, date)

        if (!isApplicable) {
            if (existing != null) {
                Log.d("TODAY_DEBUG", "DELETE NON_APPLICABLE INSTANCE templateId=$templateId date=$date")
                nodeDao.deleteByInstance(existing.id)
                dayInstanceDao.deleteById(existing.id)
            }
            Log.d("TODAY_DEBUG", "SKIP INSTANCE GENERATION templateId=$templateId reason=no_schedule_today")
            return@withLock null
        }

        if (existing != null) {
            val nodes = nodeDao.getByInstance(existing.id).first()

            if (nodes.isNotEmpty()) {
                return@withLock existing
            }

            Log.d(
                "TODAY_DEBUG",
                "EMPTY INSTANCE FOUND templateId=$templateId instanceId=${existing.id}, REGENERATING"
            )

            nodeDao.deleteByInstance(existing.id)
            dayInstanceDao.deleteById(existing.id)
        }

        return@withLock generateInstance(templateId, date)
    }

    suspend fun regenerateTemplateInstanceForDate(
        templateId: String,
        date: Long
    ) = instanceGenerationMutex.withLock {
        val weekday = DateUtils.getDayOfWeek(Date(date))
        val isApplicable = hasApplicableSchedule(templateId, weekday)
        
        Log.d("TODAY_DEBUG", "TEMPLATE APPLICABLE TODAY templateId=$templateId today=$weekday result=$isApplicable")

        val oldInstances = dayInstanceDao.getAllByTemplateAndDate(templateId, date)

        if (!isApplicable) {
            if (oldInstances.isNotEmpty()) {
                oldInstances.forEach { oldInstance ->
                    Log.d("TODAY_DEBUG", "DELETE NON_APPLICABLE INSTANCE templateId=$templateId date=$date")
                    nodeDao.deleteByInstance(oldInstance.id)
                    dayInstanceDao.deleteById(oldInstance.id)
                }
            }
            Log.d("TODAY_DEBUG", "SKIP REGENERATE templateId=$templateId reason=no_schedule_today")
            return@withLock
        }

        Log.d("TODAY_DEBUG", "REGENERATE TEMPLATE INSTANCE templateId=$templateId date=$date")

        oldInstances.forEach { oldInstance ->
            Log.d("TODAY_DEBUG", "DELETE OLD INSTANCE id=${oldInstance.id}")

            val nodesToDelete = nodeDao.getByInstance(oldInstance.id).first()

            nodeDao.deleteByInstance(oldInstance.id)
            Log.d("TODAY_DEBUG", "DELETE OLD INSTANCE NODES COUNT = ${nodesToDelete.size}")

            dayInstanceDao.deleteById(oldInstance.id)
            Log.d("TODAY_DEBUG", "OLD INSTANCE DELETED id=${oldInstance.id}")
        }

        generateInstance(templateId, date)
        Log.d("TODAY_DEBUG", "NEW INSTANCE GENERATED templateId=$templateId date=$date")
    }

    suspend fun useTemplateForDate(
        templateId: String,
        date: Long,
        forceRegenerate: Boolean = false
    ): DayInstance? = instanceGenerationMutex.withLock {
        Log.d("TODAY_DEBUG", "USE TEMPLATE FOR DATE START templateId=$templateId date=$date force=$forceRegenerate")

        val existing = dayInstanceDao.getByTemplateAndDate(templateId, date)

        if (existing != null && !forceRegenerate) {
            val nodes = nodeDao.getByInstance(existing.id).first()
            if (nodes.isNotEmpty()) {
                Log.d("TODAY_DEBUG", "USE TEMPLATE FOR DATE EXISTING FOUND instanceId=${existing.id}")
                Log.d("TODAY_DEBUG", "USE TEMPLATE FOR DATE SKIPPED existing valid instance")
                return@withLock existing
            }
        }

        if (forceRegenerate || (existing != null)) {
            val oldInstances = dayInstanceDao.getAllByTemplateAndDate(templateId, date)
            oldInstances.forEach { oldInstance ->
                Log.d("TODAY_DEBUG", "USE TEMPLATE FOR DATE DELETE OLD id=${oldInstance.id}")
                nodeDao.deleteByInstance(oldInstance.id)
                dayInstanceDao.deleteById(oldInstance.id)
            }
        }

        val instance = generateInstance(templateId, date)
        Log.d("TODAY_DEBUG", "USE TEMPLATE FOR DATE CREATED instanceId=${instance.id}")
        return@withLock instance
    }

    private suspend fun hasApplicableSchedule(templateId: String, todayWeekday: Int): Boolean {
        // 1. Validar Global Schedules (tabla schedules)
        val globalSchedules = scheduleDao.getByTemplateSync(templateId)
        val hasGlobalToday = globalSchedules.any { it.weekday == todayWeekday && it.isActive }
        if (hasGlobalToday) return true

        // 2. Validar Node Schedules (tabla node_schedules)
        val templateNodes = nodeDao.getAllByTemplate(templateId)
        if (templateNodes.isNotEmpty()) {
            val nodeIds = templateNodes.map { it.id }
            val nodeSchedules = nodeScheduleDao.getSchedulesForNodes(nodeIds)
            val hasNodeScheduleToday = nodeSchedules.any { it.dayOfWeek == todayWeekday }
            if (hasNodeScheduleToday) return true
        }
        
        return false
    }

    suspend fun dedupeInstancesForDate(date: Long) = instanceGenerationMutex.withLock {
        val instances = dayInstanceDao.getAllByDate(date).first()

        Log.d("TODAY_DEBUG", "INSTANCES TODAY = ${instances.size}")

        instances
            .groupBy { it.templateId }
            .forEach { (templateId, group) ->
                Log.d("TODAY_DEBUG", "TEMPLATE $templateId INSTANCE COUNT = ${group.size}")

                if (group.size <= 1) return@forEach

                val keep = group.first()
                val duplicates = group.filter { it.id != keep.id }

                Log.d("TODAY_DEBUG", "KEEP INSTANCE = ${keep.id}")

                duplicates.forEach { duplicate ->
                    val nodes = nodeDao.getByInstance(duplicate.id).first()

                    Log.d("TODAY_DEBUG", "DELETE DUPLICATE INSTANCE = ${duplicate.id}")
                    Log.d("TODAY_DEBUG", "DELETE DUPLICATE NODES COUNT = ${nodes.size}")

                    nodeDao.deleteByInstance(duplicate.id)
                    dayInstanceDao.deleteById(duplicate.id)
                }
            }
    }


    private suspend fun generateInstance(templateId: String, date: Long): DayInstance {
        val weekday = DateUtils.getDayOfWeek(Date(date))
        val instanceId = UUID.randomUUID().toString()
        val newInstance = DayInstance(
            id = instanceId,
            templateId = templateId,
            date = date,
            status = InstanceStatus.GENERATED
        )
        dayInstanceDao.upsert(newInstance)

        // FIX 1 — generateInstance() Refined
        val allNodes = nodeDao.getAllTemplateNodesSync()

        val templateNodesOnly = allNodes.filter {
            it.templateId == templateId && it.instanceId == null
        }

        val roots = templateNodesOnly.filter {
            it.parentId == null
        }

        Log.d("TODAY_DEBUG", "TEMPLATE NODES ONLY = ${templateNodesOnly.size}")
        Log.d("TODAY_DEBUG", "ROOTS ONLY = ${roots.size}")

        val templateTreeNodes = mutableListOf<Node>()
        val queue = ArrayDeque<Node>()
        queue.addAll(roots)

        val visited = mutableSetOf<String>()
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            if (node.id in visited) continue
            visited.add(node.id)
            templateTreeNodes.add(node)

            val children = templateNodesOnly.filter { it.parentId == node.id }
            queue.addAll(children)
        }

        Log.d("TODAY_DEBUG", "TREE TO CLONE = ${templateTreeNodes.size}")

        val idMap = mutableMapOf<String?, String?>()
        idMap[null] = null
        templateTreeNodes.forEach { idMap[it.id] = UUID.randomUUID().toString() }

        val nodeIds = templateTreeNodes.map { it.id }
        val allSchedules = nodeScheduleDao.getSchedulesForNodes(nodeIds)
        Log.d("TODAY_DEBUG", "NODE SCHEDULES TEMPLATE FOUND = ${allSchedules.size}")

        val instanceNodes = mutableListOf<Node>()
        val clonedSchedules = mutableListOf<NodeSchedule>()

        for (tNode in templateTreeNodes) {
            val newNodeId = idMap[tNode.id]!!

            // OBLIGATORY LOG: CLONED NODE
            Log.d(
                "TODAY_DEBUG",
                "CLONED NODE name=${tNode.name} oldParent=${tNode.parentId} newParent=${idMap[tNode.parentId]} instanceId=$instanceId"
            )

            // Clonar TODOS los horarios del nodo para la instancia
            val nodeSchedules = allSchedules.filter { it.nodeId == tNode.id }
            val todaySchedule = nodeSchedules.find { it.dayOfWeek == weekday }

            nodeSchedules.forEach { s ->
                val cloned = s.copy(
                    id = UUID.randomUUID().toString(),
                    nodeId = newNodeId,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                clonedSchedules.add(cloned)
                Log.d(
                    "TODAY_DEBUG",
                    "CLONED NODE SCHEDULE oldNode=${s.nodeId} newNode=$newNodeId day=${s.dayOfWeek} start=${s.startTime} end=${s.endTime}"
                )
            }

            // Copiar FieldValues (Usando fieldValueDao correctamente)
            val templateValues = fieldValueDao.getByNode(tNode.id).first()
            templateValues.forEach { tValue ->
                Log.d(
                    "TODAY_DEBUG",
                    "COPY FIELD TEMPLATE_TO_INSTANCE templateNode=${tNode.id} instanceNode=$newNodeId fieldName=${tValue.fieldName} value=${tValue.value}"
                )
                fieldValueDao.upsert(
                    tValue.copy(
                        id = UUID.randomUUID().toString(),
                        nodeId = newNodeId
                    )
                )
            }

            instanceNodes.add(
                tNode.copy(
                    id = newNodeId,
                    parentId = idMap[tNode.parentId], // Remapeo correcto
                    templateId = tNode.id,
                    instanceId = instanceId,
                    status = NodeStatus.PENDING,
                    scheduledTime = todaySchedule?.startTime ?: tNode.scheduledTime,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        nodeDao.insertAll(instanceNodes)
        Log.d("TODAY_DEBUG", "INSTANCE NODES COUNT GENERATED = ${instanceNodes.size}")

        if (clonedSchedules.isNotEmpty()) {
            nodeScheduleDao.insertAll(clonedSchedules)
            Log.d("TODAY_DEBUG", "INSTANCE NODE SCHEDULES GENERATED = ${clonedSchedules.size}")
        }

        return newInstance
    }

    fun getNodesForInstance(instanceId: String): Flow<List<Node>> =
        nodeDao.getByInstance(instanceId)

    suspend fun getCompletionRate(from: Long): Float {
        val totalList = dayInstanceDao.getInRange(from, System.currentTimeMillis()).first()
        if (totalList.isEmpty()) return 0f
        val completedCount = dayInstanceDao.countByStatus(InstanceStatus.COMPLETED, from)
        return completedCount.toFloat() / totalList.size
    }

    suspend fun calculateCurrentStreak(): Int {
        val instances = dayInstanceDao.getInRange(0, System.currentTimeMillis()).first()
            .sortedByDescending { it.date }

        var streak = 0
        val oneDayMs = 24 * 60 * 60 * 1000L
        var expectedDate = DateUtils.getStartOfDay()

        for (instance in instances) {
            if (instance.status == InstanceStatus.COMPLETED) {
                if (instance.date == expectedDate) {
                    streak++
                    expectedDate -= oneDayMs
                } else if (instance.date < expectedDate) {
                    break
                }
            } else {
                if (instance.date != DateUtils.getStartOfDay()) {
                    break
                }
            }
        }
        return streak
    }
}
