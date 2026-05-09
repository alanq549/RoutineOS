package com.alan.routineos.data.local.dao

import androidx.room.*
import com.alan.routineos.data.local.entities.RoutineTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {
    @Query("SELECT * FROM routine_templates ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<RoutineTemplate>>

    @Query("SELECT * FROM routine_templates WHERE id = :id")
    suspend fun getById(id: String): RoutineTemplate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(template: RoutineTemplate)

    @Delete
    suspend fun delete(template: RoutineTemplate)
}
