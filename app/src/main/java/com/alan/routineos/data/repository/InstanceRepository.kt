package com.alan.routineos.data.repository

import com.alan.routineos.core.util.DateUtils
import com.alan.routineos.data.local.dao.*
import com.alan.routineos.data.local.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstanceRepository @Inject constructor(
    private val dayInstanceDao: DayInstanceDao,
    private val nodeDao: NodeDao,
    private val scheduleDao: ScheduleDao,
    private val exceptionDao: ScheduleExceptionDao,
    private val nodeScheduleDao: NodeScheduleDao,
    private val fieldValueDao: FieldValueDao
) {
    fun getByDate(date: Long): Flow<List<DayInstance>> = dayInstanceDao.getAllByDate(date)
    
    suspend fun upsert(instance: DayInstance) = dayInstanceDao.upsert(instance)
    
    suspend fun update(instance: DayInstance) = dayInstanceDao.update(instance)

    suspend fun generateInstanceIfNeeded(date: Long, weekday: Int): Boolean {
        // Check for exceptions
        val exceptions = exceptionRepo().getActiveForDate(date).first()
        if (exceptions.any { it.affectsGeneration }) return false

        // Get active schedules for this day
        val activeSchedules = scheduleDao.getActiveForWeekday(weekday, date).first()
        if (activeSchedules.isEmpty()) return false

        // Get existing instances for today
        val existingInstances = dayInstanceDao.getAllByDate(date).first()
        val existingTemplateIds = existingInstances.map { it.templateId }.toSet()

        var generated = false
        activeSchedules.forEach { schedule ->
            if (!existingTemplateIds.contains(schedule.templateId)) {
                generateInstance(schedule.templateId, date)
                generated = true
            }
        }
        
        return generated
    }
    
    private fun exceptionRepo() = exceptionDao // Helper for clarity

    suspend fun generateInstance(templateId: String, date: Long): DayInstance {
        val weekday = DateUtils.getDayOfWeek(Date(date)) 
        val instanceId = UUID.randomUUID().toString()
        val newInstance = DayInstance(
            id = instanceId,
            templateId = templateId,
            date = date,
            status = InstanceStatus.GENERATED
        )
        dayInstanceDao.upsert(newInstance)

        val templateNodes = nodeDao.getAllByTemplate(templateId)
        val nodeIds = templateNodes.map { it.id }
        val allSchedules = nodeScheduleDao.getSchedulesForNodes(nodeIds)

        val idMap = mutableMapOf<String, String>()
        templateNodes.forEach { idMap[it.id] = UUID.randomUUID().toString() }

        val instanceNodes = templateNodes.map { tNode ->
            val todaySchedule = allSchedules.find { it.nodeId == tNode.id && it.dayOfWeek == weekday }
            
            // Copiar valores de campos (Metadata) de la plantilla a la instancia
            val templateValues = fieldValueDao.getByNode(tNode.id).first()
            templateValues.forEach { tValue ->
                fieldValueDao.upsert(tValue.copy(
                    id = UUID.randomUUID().toString(),
                    nodeId = idMap[tNode.id]!!
                ))
            }

            tNode.copy(
                id = idMap[tNode.id]!!,
                parentId = idMap[tNode.parentId],
                templateId = templateId, 
                instanceId = instanceId,
                status = NodeStatus.PENDING,
                scheduledTime = todaySchedule?.startTime ?: tNode.scheduledTime,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        }.sortedBy { it.scheduledTime ?: "99:99" }

        nodeDao.insertAll(instanceNodes)
        
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
