package com.alan.routineos.data.repository

import com.alan.routineos.data.local.dao.NodeDao
import com.alan.routineos.data.local.entities.Node
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NodeRepository @Inject constructor(
    private val nodeDao: NodeDao
) {
    fun getChildren(parentId: String): Flow<List<Node>> = nodeDao.getChildren(parentId)
    
    fun getByInstance(instanceId: String): Flow<List<Node>> = nodeDao.getByInstance(instanceId)

    suspend fun getAllByTemplate(templateId: String): List<Node> = nodeDao.getAllByTemplate(templateId)
    
    suspend fun getById(id: String): Node? = nodeDao.getById(id)

    fun getAllTemplateNodes(): Flow<List<Node>> = nodeDao.getAllTemplateNodes()
    
    suspend fun upsert(node: Node) = nodeDao.upsert(node)

    suspend fun insertAll(nodes: List<Node>) = nodeDao.insertAll(nodes)
    
    suspend fun update(node: Node) = nodeDao.update(node)
}
