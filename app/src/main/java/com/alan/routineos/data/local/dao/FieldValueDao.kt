package com.alan.routineos.data.local.dao

import androidx.room.*
import com.alan.routineos.data.local.entities.NodeFieldValue
import kotlinx.coroutines.flow.Flow

@Dao
interface FieldValueDao {
    @Query("SELECT * FROM node_field_values WHERE nodeId = :nodeId")
    fun getByNode(nodeId: String): Flow<List<NodeFieldValue>>

    @Query("SELECT * FROM node_field_values WHERE nodeId = :nodeId AND fieldName = :fieldName LIMIT 1")
    suspend fun getByNodeAndField(nodeId: String, fieldName: String): NodeFieldValue?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(value: NodeFieldValue)

    @Query("SELECT * FROM node_field_values WHERE nodeId = :nodeId AND fieldName = :fieldName ORDER BY updatedAt DESC")
    fun getHistoryForNodeAndField(nodeId: String, fieldName: String): Flow<List<NodeFieldValue>>

    @Query("""
        SELECT * FROM node_field_values 
        WHERE fieldName = :fieldName 
        AND nodeId IN (SELECT id FROM nodes WHERE templateId = :templateNodeId)
        ORDER BY updatedAt ASC
    """)
    fun getHistoryByTemplateNode(templateNodeId: String, fieldName: String): Flow<List<NodeFieldValue>>
}
