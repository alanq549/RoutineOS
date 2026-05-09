package com.alan.routineos.data.repository

import com.alan.routineos.data.local.dao.DayInstanceDao
import com.alan.routineos.data.local.dao.NodeDao
import com.alan.routineos.data.local.dao.ScheduleDao
import com.alan.routineos.data.local.dao.ScheduleExceptionDao
import com.alan.routineos.data.local.entities.DayInstance
import com.alan.routineos.data.local.entities.InstanceStatus
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstanceRepository @Inject constructor(
    private val dayInstanceDao: DayInstanceDao,
    private val nodeDao: NodeDao,
    private val scheduleDao: ScheduleDao,
    private val exceptionDao: ScheduleExceptionDao
) {
    fun getByDate(date: Long): Flow<DayInstance?> = dayInstanceDao.getByDate(date)
    
    suspend fun upsert(instance: DayInstance) = dayInstanceDao.upsert(instance)
    
    suspend fun update(instance: DayInstance) = dayInstanceDao.update(instance)

    suspend fun generateInstanceIfNeeded(date: Long, weekday: Int): Boolean {
        // 1. Check if instance already exists
        val existing = dayInstanceDao.getByDate(date).first()
        if (existing != null) return false

        // 2. Check for active exceptions
        val exceptions = exceptionDao.getActiveForDate(date).first()
        if (exceptions.any { it.affectsGeneration }) return false

        // 3. Find active schedules for this weekday
        val activeSchedules = scheduleDao.getActiveForWeekday(weekday, date).first()
        if (activeSchedules.isEmpty()) return false

        // 4. Generate instances for each schedule
        activeSchedules.forEach { schedule ->
            generateInstance(schedule.templateId, date)
        }
        
        return true
    }

    suspend fun generateInstance(templateId: String, date: Long): DayInstance {
        val instanceId = UUID.randomUUID().toString()
        val newInstance = DayInstance(
            id = instanceId,
            templateId = templateId,
            date = date,
            status = InstanceStatus.GENERATED
        )
        dayInstanceDao.upsert(newInstance)

        val templateNodes = nodeDao.getAllByTemplate(templateId)
        val idMap = mutableMapOf<String, String>()
        
        // Generate new IDs for instance nodes
        templateNodes.forEach { idMap[it.id] = UUID.randomUUID().toString() }

        val instanceNodes = templateNodes.map { tNode ->
            tNode.copy(
                id = idMap[tNode.id]!!,
                parentId = idMap[tNode.parentId],
                templateId = tNode.id, // Reference to original template node
                instanceId = instanceId,
                status = NodeStatus.PENDING,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        }
        nodeDao.insertAll(instanceNodes)
        
        return newInstance
    }
}
