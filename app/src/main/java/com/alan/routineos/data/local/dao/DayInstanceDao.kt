package com.alan.routineos.data.local.dao

import androidx.room.*
import com.alan.routineos.data.local.entities.DayInstance
import com.alan.routineos.data.local.entities.InstanceStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DayInstanceDao {
    @Query("SELECT * FROM day_instances WHERE date = :date")
    fun getAllByDate(date: Long): Flow<List<DayInstance>>

    @Query("SELECT * FROM day_instances WHERE date >= :from AND date <= :to ORDER BY date ASC")
    fun getInRange(from: Long, to: Long): Flow<List<DayInstance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(instance: DayInstance)

    @Update
    suspend fun update(instance: DayInstance)

    @Query("SELECT COUNT(*) FROM day_instances WHERE status = :status AND date >= :from")
    suspend fun countByStatus(status: InstanceStatus, from: Long): Int
}
