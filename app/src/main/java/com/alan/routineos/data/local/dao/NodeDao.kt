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

    @Query("SELECT * FROM nodes WHERE id = :id")
    suspend fun getById(id: String): Node?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(node: Node)

    @Update
    suspend fun update(node: Node)
}
