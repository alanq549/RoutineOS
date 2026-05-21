package com.alan.routineos.data.repository

import android.util.Log
import com.alan.routineos.data.local.dao.NodeDao
import com.alan.routineos.data.local.dao.NodeScheduleDao
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeSchedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NodeRepository @Inject constructor(
    private val nodeDao: NodeDao,
    private val nodeScheduleDao: NodeScheduleDao
) {
    fun getChildren(parentId: String): Flow<List<Node>> = nodeDao.getChildren(parentId)

    // FIX 2 — Log en getByInstance
    fun getByInstance(instanceId: String): Flow<List<Node>> = nodeDao.getByInstance(instanceId)
        .onEach { Log.d("TODAY_DEBUG", "GET BY INSTANCE COUNT = ${it.size}") }

    suspend fun getAllByTemplate(templateId: String): List<Node> =
        nodeDao.getAllByTemplate(templateId)

    suspend fun getById(id: String): Node? = nodeDao.getById(id)

    fun getAllTemplateNodes(): Flow<List<Node>> = nodeDao.getAllTemplateNodes()

    suspend fun upsert(node: Node) = nodeDao.upsert(node)

    suspend fun insertAll(nodes: List<Node>) = nodeDao.insertAll(nodes)

    suspend fun update(node: Node) = nodeDao.update(node)

    suspend fun deleteByTemplate(templateId: String) = nodeDao.deleteByTemplate(templateId)

    // NodeSchedule methods
    fun getAllNodeSchedules(): Flow<List<NodeSchedule>> = nodeScheduleDao.getAll()

    fun getSchedulesForNode(nodeId: String): Flow<List<NodeSchedule>> =
        nodeScheduleDao.getByNodeId(nodeId)

    suspend fun upsertSchedule(schedule: NodeSchedule) =
        nodeScheduleDao.upsert(schedule) /// Function "upsertSchedule" is never used

    suspend fun deleteSchedulesByNodeId(nodeId: String) =
        nodeScheduleDao.deleteByNodeId(nodeId) //Function "deleteSchedulesByNodeId" is never used

    suspend fun saveSchedules(nodeId: String, schedules: List<NodeSchedule>) {
        Log.d("TODAY_DEBUG", "SAVE SCHEDULES nodeId=$nodeId count=${schedules.size}")

        nodeScheduleDao.deleteByNodeId(nodeId)

        val fixedSchedules = schedules.map { schedule ->
            schedule.copy(
                nodeId = nodeId
            )
        }

        fixedSchedules.forEach {
            Log.d(
                "TODAY_DEBUG",
                "INSERT NODE SCHEDULE nodeId=${it.nodeId} dayOfWeek=${it.dayOfWeek} start=${it.startTime} end=${it.endTime}"
            )
        }

        nodeScheduleDao.insertAll(fixedSchedules)
    }
}
