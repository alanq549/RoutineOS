package com.alan.routineos.data.repository

import com.alan.routineos.data.local.dao.ScheduleExceptionDao
import com.alan.routineos.data.local.entities.ScheduleException
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleExceptionRepository @Inject constructor(
    private val scheduleExceptionDao: ScheduleExceptionDao
) {
    fun getActiveForDate(date: Long): Flow<List<ScheduleException>> = 
        scheduleExceptionDao.getActiveForDate(date)

    suspend fun upsert(exception: ScheduleException) = scheduleExceptionDao.upsert(exception)

    suspend fun delete(exception: ScheduleException) = scheduleExceptionDao.delete(exception)
}
