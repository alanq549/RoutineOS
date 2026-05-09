package com.alan.routineos.data.local.dao

import androidx.room.*
import com.alan.routineos.data.local.entities.NodeMetadataSchema
import kotlinx.coroutines.flow.Flow

@Dao
interface MetadataSchemaDao {
    @Query("SELECT * FROM node_metadata_schemas WHERE typeId = :typeId ORDER BY position ASC")
    fun getByTypeId(typeId: String): Flow<List<NodeMetadataSchema>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(schema: NodeMetadataSchema)

    @Delete
    suspend fun delete(schema: NodeMetadataSchema)
}
