package com.alan.routineos.data.local.dao

import androidx.room.*
import com.alan.routineos.data.local.entities.ScheduleException
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleExceptionDao {
    @Query("SELECT * FROM schedule_exceptions WHERE (dateFrom <= :date AND dateTo >= :date)")
    fun getActiveForDate(date: Long): Flow<List<ScheduleException>>

    @Query("SELECT * FROM schedule_exceptions WHERE (dateFrom <= :to AND dateTo >= :from)")
    fun getActiveForRange(from: Long, to: Long): Flow<List<ScheduleException>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(exception: ScheduleException)

    @Delete
    suspend fun delete(exception: ScheduleException)
}
