package com.alan.routineos.data.local.dao

import androidx.room.*
import com.alan.routineos.data.local.entities.ExecutionFieldValue
import kotlinx.coroutines.flow.Flow

@Dao
interface ExecutionFieldValueDao {
    @Query("SELECT * FROM execution_field_values WHERE nodeId = :nodeId")
    fun getByNodeId(nodeId: String): Flow<List<ExecutionFieldValue>>

    @Query("SELECT * FROM execution_field_values WHERE nodeId = :nodeId AND date = :date")
    suspend fun getByNodeAndDate(nodeId: String, date: Long): List<ExecutionFieldValue>

    @Query("SELECT * FROM execution_field_values")
    fun getAll(): Flow<List<ExecutionFieldValue>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(value: ExecutionFieldValue)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(values: List<ExecutionFieldValue>)

    @Query("DELETE FROM execution_field_values WHERE id = :id")
    suspend fun deleteById(id: String)
}
