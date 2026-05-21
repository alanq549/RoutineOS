package com.alan.routineos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.alan.routineos.data.local.entities.DayInstance
import com.alan.routineos.data.local.entities.InstanceStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DayInstanceDao {
    @Query("SELECT * FROM day_instances WHERE date = :date")
    fun getAllByDate(date: Long): Flow<List<DayInstance>>

    @Query(""" SELECT * FROM day_instances WHERE templateId = :templateId AND date = :date """)
    suspend fun getAllByTemplateAndDate(templateId: String, date: Long): List<DayInstance>

    @Query("SELECT * FROM day_instances WHERE templateId = :templateId AND date = :date LIMIT 1")
    suspend fun getByTemplateAndDate(templateId: String, date: Long): DayInstance?

    @Query("SELECT * FROM day_instances WHERE date >= :from AND date <= :to ORDER BY date ASC")
    fun getInRange(from: Long, to: Long): Flow<List<DayInstance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(instance: DayInstance)

    @Update
    suspend fun update(instance: DayInstance)

    @Query("SELECT COUNT(*) FROM day_instances WHERE status = :status AND date >= :from")
    suspend fun countByStatus(status: InstanceStatus, from: Long): Int

    @Query("DELETE FROM day_instances WHERE date = :date")
    suspend fun deleteByDate(date: Long)

    @Query("DELETE FROM day_instances WHERE id = :id")
    suspend fun deleteById(id: String)
}
