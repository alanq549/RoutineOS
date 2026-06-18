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
    fun getAll(): Flow<List<NodeFieldValue>> = fieldValueDao.getAll()

    fun getByNode(nodeId: String): Flow<List<NodeFieldValue>> = fieldValueDao.getByNode(nodeId)
    
    suspend fun getByNodeSync(nodeId: String): List<NodeFieldValue> = 
        fieldValueDao.getByNodeSync(nodeId)

    suspend fun getByNodeAndField(nodeId: String, fieldName: String): NodeFieldValue? = 
        fieldValueDao.getByNodeAndField(nodeId, fieldName)

    suspend fun getByNodeAndSchema(nodeId: String, schemaId: String): NodeFieldValue? =
        fieldValueDao.getByNodeAndSchema(nodeId, schemaId)
    
    suspend fun upsert(value: NodeFieldValue) = fieldValueDao.upsert(value)

    suspend fun update(value: NodeFieldValue) = fieldValueDao.update(value)

    suspend fun deleteByIds(ids: List<String>) = fieldValueDao.deleteByIds(ids)
    
    fun getHistory(nodeId: String, fieldName: String): Flow<List<NodeFieldValue>> = 
        fieldValueDao.getHistoryForNodeAndField(nodeId, fieldName)

    fun getHistoryByTemplateNode(templateNodeId: String, fieldName: String): Flow<List<NodeFieldValue>> =
        fieldValueDao.getHistoryByTemplateNode(templateNodeId, fieldName)

    fun getHistoryByTemplateNodes(templateNodeIds: List<String>, fieldName: String): Flow<List<NodeFieldValue>> =
        fieldValueDao.getHistoryByTemplateNodes(templateNodeIds, fieldName)
}
