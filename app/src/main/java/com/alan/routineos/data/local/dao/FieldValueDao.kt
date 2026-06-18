package com.alan.routineos.data.local.dao

import androidx.room.*
import com.alan.routineos.data.local.entities.NodeFieldValue
import kotlinx.coroutines.flow.Flow

@Dao
interface FieldValueDao {
    @Query("SELECT * FROM node_field_values")
    fun getAll(): Flow<List<NodeFieldValue>>

    @Query("SELECT * FROM node_field_values WHERE nodeId = :nodeId")
    fun getByNode(nodeId: String): Flow<List<NodeFieldValue>>

    @Query("SELECT * FROM node_field_values WHERE nodeId = :nodeId")
    suspend fun getByNodeSync(nodeId: String): List<NodeFieldValue>

    @Query("SELECT * FROM node_field_values WHERE nodeId IN (:nodeIds)")
    fun getByNodes(nodeIds: List<String>): Flow<List<NodeFieldValue>>

    @Query("SELECT * FROM node_field_values WHERE nodeId = :nodeId AND fieldName = :fieldName LIMIT 1")
    suspend fun getByNodeAndField(nodeId: String, fieldName: String): NodeFieldValue?

    @Query("SELECT * FROM node_field_values WHERE nodeId = :nodeId AND schemaId = :schemaId LIMIT 1")
    suspend fun getByNodeAndSchema(nodeId: String, schemaId: String): NodeFieldValue?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(value: NodeFieldValue)

    @Update
    suspend fun update(value: NodeFieldValue)

    @Delete
    suspend fun delete(value: NodeFieldValue)

    @Query("DELETE FROM node_field_values WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("SELECT * FROM node_field_values WHERE nodeId = :nodeId AND fieldName = :fieldName ORDER BY updatedAt DESC")
    fun getHistoryForNodeAndField(nodeId: String, fieldName: String): Flow<List<NodeFieldValue>>

    @Query("""
        SELECT * FROM node_field_values 
        WHERE fieldName = :fieldName 
        AND nodeId IN (
            SELECT id FROM nodes 
            WHERE sourceTemplateNodeId = :templateNodeId 
            AND instanceId IS NOT NULL
        )
        ORDER BY updatedAt ASC
    """)
    fun getHistoryByTemplateNode(templateNodeId: String, fieldName: String): Flow<List<NodeFieldValue>>

    @Query("""
        SELECT * FROM node_field_values 
        WHERE fieldName = :fieldName 
        AND nodeId IN (
            SELECT id FROM nodes 
            WHERE sourceTemplateNodeId IN (:templateNodeIds) 
            AND instanceId IS NOT NULL
        )
        ORDER BY updatedAt ASC
    """)
    fun getHistoryByTemplateNodes(templateNodeIds: List<String>, fieldName: String): Flow<List<NodeFieldValue>>
}
