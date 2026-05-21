package com.alan.routineos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alan.routineos.data.local.entities.NodeSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface NodeScheduleDao {
    @Query("SELECT * FROM node_schedules")
    fun getAll(): Flow<List<NodeSchedule>>

    @Query("SELECT * FROM node_schedules WHERE nodeId = :nodeId")
    fun getByNodeId(nodeId: String): Flow<List<NodeSchedule>>

    @Query("SELECT * FROM node_schedules WHERE nodeId IN (:nodeIds)")
    suspend fun getSchedulesForNodes(nodeIds: List<String>): List<NodeSchedule>

    @Query("SELECT * FROM node_schedules WHERE nodeId IN (:nodeIds)")
    fun getSchedulesForNodesFlow(nodeIds: List<String>): Flow<List<NodeSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(schedule: NodeSchedule)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(schedules: List<NodeSchedule>)

    @Query("DELETE FROM node_schedules WHERE nodeId = :nodeId")
    suspend fun deleteByNodeId(nodeId: String)
}
