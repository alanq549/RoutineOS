package com.alan.routineos.data.local.dao

import androidx.room.*
import com.alan.routineos.data.local.entities.Node
import kotlinx.coroutines.flow.Flow

@Dao
interface NodeDao {
    @Query("SELECT * FROM nodes WHERE parentId = :parentId AND deletedAt IS NULL ORDER BY position ASC")
    fun getChildren(parentId: String): Flow<List<Node>>

    @Query("SELECT * FROM nodes WHERE instanceId = :instanceId AND deletedAt IS NULL ORDER BY position ASC")
    fun getByInstance(instanceId: String): Flow<List<Node>>

    @Query("SELECT * FROM nodes WHERE templateId = :templateId AND instanceId IS NULL AND deletedAt IS NULL")
    suspend fun getAllByTemplate(templateId: String): List<Node>

    @Query("SELECT * FROM nodes WHERE id = :id")
    suspend fun getById(id: String): Node?

    @Query("SELECT * FROM nodes WHERE instanceId IS NULL AND deletedAt IS NULL")
    fun getAllTemplateNodes(): Flow<List<Node>>

    @Query("SELECT * FROM nodes WHERE instanceId IS NULL AND deletedAt IS NULL")
    suspend fun getAllTemplateNodesSync(): List<Node>

    @Query("SELECT * FROM nodes WHERE templateId = :templateNodeId AND instanceId IS NOT NULL AND id != :currentNodeId ORDER BY createdAt DESC LIMIT 10")
    suspend fun getPreviousInstances(templateNodeId: String, currentNodeId: String): List<Node>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(node: Node)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(nodes: List<Node>)

    @Update
    suspend fun update(node: Node)

    @Query("DELETE FROM nodes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM nodes WHERE templateId = :templateId AND instanceId IS NULL")
    suspend fun deleteByTemplate(templateId: String)

    @Query("DELETE FROM nodes WHERE instanceId = :instanceId")
    suspend fun deleteByInstance(instanceId: String)

    @Query("DELETE FROM nodes WHERE instanceId IN (SELECT id FROM day_instances WHERE date = :date)")
    suspend fun deleteByDate(date: Long)
}
