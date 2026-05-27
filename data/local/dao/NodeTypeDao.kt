package com.alan.routineos.data.local.dao

import androidx.room.*
import com.alan.routineos.data.local.entities.NodeType
import kotlinx.coroutines.flow.Flow

@Dao
interface NodeTypeDao {
    @Query("SELECT * FROM node_types WHERE isActive = 1 ORDER BY position ASC")
    fun getAllActive(): Flow<List<NodeType>>

    @Query("SELECT * FROM node_types WHERE id = :id")
    suspend fun getById(id: String): NodeType?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(nodeType: NodeType)

    @Delete
    suspend fun delete(nodeType: NodeType)
}
