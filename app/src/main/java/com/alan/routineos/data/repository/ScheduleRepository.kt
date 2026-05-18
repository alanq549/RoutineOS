package com.alan.routineos.data.repository

import com.alan.routineos.data.local.dao.ScheduleDao
import com.alan.routineos.data.local.entities.Schedule
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepository @Inject constructor(
    private val scheduleDao: ScheduleDao
) {
    fun getActiveForWeekday(weekday: Int, date: Long): Flow<List<Schedule>> = 
        scheduleDao.getActiveForWeekday(weekday, date)

    fun getByTemplate(templateId: String): Flow<List<Schedule>> = 
        scheduleDao.getByTemplate(templateId)

    fun getAll(): Flow<List<Schedule>> = scheduleDao.getAll()

    suspend fun deleteByTemplate(templateId: String) = 
        scheduleDao.deleteByTemplate(templateId)

    suspend fun upsert(schedule: Schedule) = scheduleDao.upsert(schedule)

    suspend fun delete(schedule: Schedule) = scheduleDao.delete(schedule)
}
