package com.alan.routineos.data.repository

import com.alan.routineos.data.local.dao.DayInstanceDao
import com.alan.routineos.data.local.entities.DayInstance
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstanceRepository @Inject constructor(
    private val dayInstanceDao: DayInstanceDao
) {
    fun getByDate(date: Long): Flow<DayInstance?> = dayInstanceDao.getByDate(date)
    
    suspend fun upsert(instance: DayInstance) = dayInstanceDao.upsert(instance)
    
    suspend fun update(instance: DayInstance) = dayInstanceDao.update(instance)
}
