package com.alan.routineos.data.local.dao

import androidx.room.*
import com.alan.routineos.data.local.entities.NodeOverride
import com.alan.routineos.data.local.entities.OverrideType
import kotlinx.coroutines.flow.Flow

@Dao
interface NodeOverrideDao {
    @Query("SELECT * FROM node_overrides WHERE nodeId = :nodeId AND instanceId = :instanceId")
    fun getOverridesForNode(nodeId: String, instanceId: String): Flow<List<NodeOverride>>

    @Query("SELECT * FROM node_overrides WHERE nodeId = :nodeId AND instanceId = :instanceId AND overrideType = :type LIMIT 1")
    suspend fun getSpecificOverrideSync(nodeId: String, instanceId: String, type: OverrideType): NodeOverride?

    @Query("SELECT * FROM node_overrides WHERE instanceId = :instanceId")
    fun getOverridesForInstance(instanceId: String): Flow<List<NodeOverride>>

    @Query("SELECT * FROM node_overrides")
    fun getAll(): Flow<List<NodeOverride>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(override: NodeOverride)

    @Delete
    suspend fun delete(override: NodeOverride)
    
    @Query("DELETE FROM node_overrides WHERE nodeId = :nodeId AND instanceId = :instanceId")
    suspend fun deleteForNodeInstance(nodeId: String, instanceId: String)
}
