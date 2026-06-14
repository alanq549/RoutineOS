package com.alan.routineos.data.local.dao

import androidx.room.*
import com.alan.routineos.data.local.entities.PlanningItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanningItemDao {
    @Query("SELECT * FROM planning_items ORDER BY createdAt DESC")
    fun getAll(): Flow<List<PlanningItemEntity>>

    @Upsert
    suspend fun upsert(item: PlanningItemEntity)

    @Delete
    suspend fun delete(item: PlanningItemEntity)

    @Query("DELETE FROM planning_items WHERE id = :id")
    suspend fun deleteById(id: String)
}
