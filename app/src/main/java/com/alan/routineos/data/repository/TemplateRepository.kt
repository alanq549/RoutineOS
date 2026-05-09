package com.alan.routineos.data.repository

import com.alan.routineos.data.local.dao.TemplateDao
import com.alan.routineos.data.local.entities.RoutineTemplate
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplateRepository @Inject constructor(
    private val templateDao: TemplateDao
) {
    fun getAll(): Flow<List<RoutineTemplate>> = templateDao.getAll()
    
    suspend fun getById(id: String): RoutineTemplate? = templateDao.getById(id)
    
    suspend fun upsert(template: RoutineTemplate) = templateDao.upsert(template)
    
    suspend fun delete(template: RoutineTemplate) = templateDao.delete(template)
}
