package com.alan.routineos.data.local.dao

import androidx.room.*
import com.alan.routineos.data.local.entities.DayInstance
import kotlinx.coroutines.flow.Flow

@Dao
interface DayInstanceDao {
    @Query("SELECT * FROM day_instances WHERE date = :date LIMIT 1")
    fun getByDate(date: Long): Flow<DayInstance?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(instance: DayInstance)

    @Update
    suspend fun update(instance: DayInstance)
}
