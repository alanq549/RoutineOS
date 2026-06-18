package com.alan.routineos.data.repository

import com.alan.routineos.data.local.dao.ExecutionFieldValueDao
import com.alan.routineos.data.local.entities.ExecutionFieldValue
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExecutionFieldValueRepository @Inject constructor(
    private val dao: ExecutionFieldValueDao
) {
    fun getAll(): Flow<List<ExecutionFieldValue>> = dao.getAll()

    fun getByNodeId(nodeId: String): Flow<List<ExecutionFieldValue>> = dao.getByNodeId(nodeId)
    
    suspend fun getByNodeAndDate(nodeId: String, date: Long): List<ExecutionFieldValue> =
        dao.getByNodeAndDate(nodeId, date)

    suspend fun upsert(value: ExecutionFieldValue) = dao.upsert(value)

    suspend fun insertAll(values: List<ExecutionFieldValue>) = dao.insertAll(values)

    suspend fun deleteById(id: String) = dao.deleteById(id)
}
