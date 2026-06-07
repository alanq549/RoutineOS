package com.alan.routineos.data.repository

import com.alan.routineos.data.local.dao.NodeOverrideDao
import com.alan.routineos.data.local.entities.NodeOverride
import com.alan.routineos.data.local.entities.OverrideType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NodeOverrideRepository @Inject constructor(
    private val nodeOverrideDao: NodeOverrideDao
) {
    fun getOverridesForNode(nodeId: String, instanceId: String): Flow<List<NodeOverride>> =
        nodeOverrideDao.getOverridesForNode(nodeId, instanceId)

    suspend fun getSpecificOverrideSync(nodeId: String, instanceId: String, type: OverrideType): NodeOverride? =
        nodeOverrideDao.getSpecificOverrideSync(nodeId, instanceId, type)

    fun getOverridesForInstance(instanceId: String): Flow<List<NodeOverride>> =
        nodeOverrideDao.getOverridesForInstance(instanceId)

    fun getAll(): Flow<List<NodeOverride>> = nodeOverrideDao.getAll()

    suspend fun upsert(override: NodeOverride) = nodeOverrideDao.upsert(override)

    suspend fun delete(override: NodeOverride) = nodeOverrideDao.delete(override)
    
    suspend fun deleteForNodeInstance(nodeId: String, instanceId: String) =
        nodeOverrideDao.deleteForNodeInstance(nodeId, instanceId)
}
