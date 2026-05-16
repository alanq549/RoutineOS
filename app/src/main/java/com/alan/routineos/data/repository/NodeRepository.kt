package com.alan.routineos.data.repository

import com.alan.routineos.data.local.dao.NodeDao
import com.alan.routineos.data.local.dao.NodeScheduleDao
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeSchedule
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NodeRepository @Inject constructor(
    private val nodeDao: NodeDao,
    private val nodeScheduleDao: NodeScheduleDao
) {
    fun getChildren(parentId: String): Flow<List<Node>> = nodeDao.getChildren(parentId)
    
    fun getByInstance(instanceId: String): Flow<List<Node>> = nodeDao.getByInstance(instanceId)

    suspend fun getAllByTemplate(templateId: String): List<Node> = nodeDao.getAllByTemplate(templateId)
    
    suspend fun getById(id: String): Node? = nodeDao.getById(id)

    fun getAllTemplateNodes(): Flow<List<Node>> = nodeDao.getAllTemplateNodes()
    
    suspend fun upsert(node: Node) = nodeDao.upsert(node)

    suspend fun insertAll(nodes: List<Node>) = nodeDao.insertAll(nodes)
    
    suspend fun update(node: Node) = nodeDao.update(node)

    // NodeSchedule methods
    fun getSchedulesForNode(nodeId: String): Flow<List<NodeSchedule>> = nodeScheduleDao.getByNodeId(nodeId)
    
    suspend fun upsertSchedule(schedule: NodeSchedule) = nodeScheduleDao.upsert(schedule)
    
    suspend fun deleteSchedulesByNodeId(nodeId: String) = nodeScheduleDao.deleteByNodeId(nodeId)

    suspend fun saveSchedules(nodeId: String, schedules: List<NodeSchedule>) {
        nodeScheduleDao.deleteByNodeId(nodeId)
        nodeScheduleDao.insertAll(schedules)
    }
}
