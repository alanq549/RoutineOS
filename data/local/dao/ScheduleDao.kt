package com.alan.routineos.data.local.dao

import androidx.room.*
import com.alan.routineos.data.local.entities.Schedule
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("""
        SELECT * FROM schedules 
        WHERE weekday = :weekday 
        AND isActive = 1 
        AND (validFrom IS NULL OR validFrom <= :date) 
        AND (validUntil IS NULL OR validUntil >= :date)
    """)
    fun getActiveForWeekday(weekday: Int, date: Long): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE templateId = :templateId")
    fun getByTemplate(templateId: String): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE templateId = :templateId")
    suspend fun getByTemplateSync(templateId: String): List<Schedule>

    @Query("SELECT * FROM schedules")
    fun getAll(): Flow<List<Schedule>>

    @Query("DELETE FROM schedules WHERE templateId = :templateId")
    suspend fun deleteByTemplate(templateId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(schedule: Schedule)

    @Delete
    suspend fun delete(schedule: Schedule)
}
