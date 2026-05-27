package com.alan.routineos.data.repository

import com.alan.routineos.data.local.dao.NodeTypeDao
import com.alan.routineos.data.local.entities.NodeType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NodeTypeRepository @Inject constructor(
    private val nodeTypeDao: NodeTypeDao
) {
    fun getAllActive(): Flow<List<NodeType>> = nodeTypeDao.getAllActive()
    
    suspend fun getById(id: String): NodeType? = nodeTypeDao.getById(id)
    
    suspend fun upsert(nodeType: NodeType) = nodeTypeDao.upsert(nodeType)
    
    suspend fun archive(nodeType: NodeType) {
        nodeTypeDao.upsert(nodeType.copy(isActive = false, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deletePhysical(nodeType: NodeType) = nodeTypeDao.delete(nodeType)
}
