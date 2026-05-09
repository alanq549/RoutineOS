package com.alan.routineos.data.local.dao

import androidx.room.*
import com.alan.routineos.data.local.entities.NodeOverride
import kotlinx.coroutines.flow.Flow

@Dao
interface NodeOverrideDao {
    @Query("SELECT * FROM node_overrides WHERE nodeId = :nodeId AND instanceId = :instanceId LIMIT 1")
    fun getOverride(nodeId: String, instanceId: String): Flow<NodeOverride?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(override: NodeOverride)

    @Delete
    suspend fun delete(override: NodeOverride)
}
