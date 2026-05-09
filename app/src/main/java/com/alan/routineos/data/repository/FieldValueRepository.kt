package com.alan.routineos.data.repository

import com.alan.routineos.data.local.dao.FieldValueDao
import com.alan.routineos.data.local.entities.NodeFieldValue
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FieldValueRepository @Inject constructor(
    private val fieldValueDao: FieldValueDao
) {
    fun getByNode(nodeId: String): Flow<List<NodeFieldValue>> = fieldValueDao.getByNode(nodeId)
    
    suspend fun getByNodeAndField(nodeId: String, fieldName: String): NodeFieldValue? = 
        fieldValueDao.getByNodeAndField(nodeId, fieldName)
    
    suspend fun upsert(value: NodeFieldValue) = fieldValueDao.upsert(value)
    
    fun getHistory(nodeId: String, fieldName: String): Flow<List<NodeFieldValue>> = 
        fieldValueDao.getHistoryForNodeAndField(nodeId, fieldName)
}
