package com.alan.routineos.data.repository

import com.alan.routineos.core.util.DateUtils
import com.alan.routineos.data.local.dao.DayInstanceDao
import com.alan.routineos.data.local.dao.NodeDao
import com.alan.routineos.data.local.dao.NodeScheduleDao
import com.alan.routineos.data.local.dao.ScheduleDao
import com.alan.routineos.data.local.dao.ScheduleExceptionDao
import com.alan.routineos.data.local.entities.DayInstance
import com.alan.routineos.data.local.entities.InstanceStatus
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeStatus
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
    private val nodeScheduleDao: NodeScheduleDao
) {
    fun getByDate(date: Long): Flow<DayInstance?> = dayInstanceDao.getByDate(date)
    
    suspend fun upsert(instance: DayInstance) = dayInstanceDao.upsert(instance)
    
    suspend fun update(instance: DayInstance) = dayInstanceDao.update(instance)

    suspend fun generateInstanceIfNeeded(date: Long, weekday: Int): Boolean {
        val existing = dayInstanceDao.getByDate(date).first()
        if (existing != null) return false

        val exceptions = exceptionDao.getActiveForDate(date).first()
        if (exceptions.any { it.affectsGeneration }) return false

        val activeSchedules = scheduleDao.getActiveForWeekday(weekday, date).first()
        if (activeSchedules.isEmpty()) return false

        activeSchedules.forEach { schedule ->
            generateInstance(schedule.templateId, date)
        }
        
        return true
    }

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

        // FILTRO: Solo incluimos nodos secuenciales o que tengan horario para hoy
        val filteredNodes = templateNodes.filter { tNode ->
            val schedulesForNode = allSchedules.filter { it.nodeId == tNode.id }
            if (schedulesForNode.isEmpty()) {
                true // Nodo secuencial (Gym)
            } else {
                schedulesForNode.any { it.dayOfWeek == weekday } // Solo si aplica hoy
            }
        }

        val idMap = mutableMapOf<String, String>()
        filteredNodes.forEach { idMap[it.id] = UUID.randomUUID().toString() }

        val instanceNodes = filteredNodes.map { tNode ->
            val todaySchedule = allSchedules.find { it.nodeId == tNode.id && it.dayOfWeek == weekday }
            
            tNode.copy(
                id = idMap[tNode.id]!!,
                parentId = idMap[tNode.parentId],
                templateId = tNode.id,
                instanceId = instanceId,
                status = NodeStatus.PENDING,
                scheduledTime = todaySchedule?.startTime ?: tNode.scheduledTime, // Aplicar horario específico si existe
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        }.sortedBy { it.scheduledTime ?: "99:99" } // Ordenación natural por tiempo

        nodeDao.insertAll(instanceNodes)
        
        return newInstance
    }

    suspend fun getCompletionRate(from: Long): Float {
        val total = dayInstanceDao.getInRange(from, System.currentTimeMillis()).first().size
        if (total == 0) return 0f
        val completed = dayInstanceDao.countByStatus(InstanceStatus.COMPLETED, from)
        return completed.toFloat() / total
    }

    suspend fun calculateCurrentStreak(): Int {
        val instances = dayInstanceDao.getInRange(0, System.currentTimeMillis()).first()
            .sortedByDescending { it.date }
        
        var streak = 0
        val oneDayMs = 24 * 60 * 60 * 1000L
        var expectedDate = com.alan.routineos.core.util.DateUtils.getStartOfDay()

        for (instance in instances) {
            if (instance.status == InstanceStatus.COMPLETED) {
                if (instance.date == expectedDate) {
                    streak++
                    expectedDate -= oneDayMs
                } else if (instance.date < expectedDate) {
                    break
                }
            } else {
                if (instance.date != com.alan.routineos.core.util.DateUtils.getStartOfDay()) {
                    break
                }
            }
        }
        return streak
    }
}
