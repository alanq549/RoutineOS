package com.alan.routineos.data.local.dao

import androidx.room.*
import com.alan.routineos.data.local.entities.*

@Dao
interface SyncDao {
    @Query("SELECT * FROM nodes WHERE syncStatus = 'PENDING_SYNC'")
    suspend fun getPendingNodes(): List<Node>

    @Query("SELECT * FROM node_types WHERE syncStatus = 'PENDING_SYNC'")
    suspend fun getPendingNodeTypes(): List<NodeType>

    @Query("SELECT * FROM node_metadata_schemas WHERE syncStatus = 'PENDING_SYNC'")
    suspend fun getPendingSchemas(): List<NodeMetadataSchema>

    @Query("SELECT * FROM node_field_values WHERE syncStatus = 'PENDING_SYNC'")
    suspend fun getPendingFieldValues(): List<NodeFieldValue>

    @Query("SELECT * FROM routine_templates WHERE syncStatus = 'PENDING_SYNC'")
    suspend fun getPendingTemplates(): List<RoutineTemplate>

    @Query("SELECT * FROM schedules WHERE syncStatus = 'PENDING_SYNC'")
    suspend fun getPendingSchedules(): List<Schedule>

    @Query("SELECT * FROM day_instances WHERE syncStatus = 'PENDING_SYNC'")
    suspend fun getPendingInstances(): List<DayInstance>

    @Query("SELECT * FROM schedule_exceptions WHERE syncStatus = 'PENDING_SYNC'")
    suspend fun getPendingExceptions(): List<ScheduleException>
    
    @Query("SELECT * FROM node_overrides WHERE syncStatus = 'PENDING_SYNC'")
    suspend fun getPendingOverrides(): List<NodeOverride>

    @Transaction
    suspend fun markAsSynced(
        nodes: List<String>,
        types: List<String>,
        schemas: List<String>,
        values: List<String>,
        templates: List<String>,
        schedules: List<String>,
        instances: List<String>,
        exceptions: List<String>,
        overrides: List<String>
    ) {
        updateNodesSyncStatus(nodes, SyncStatus.SYNCED)
        updateTypesSyncStatus(types, SyncStatus.SYNCED)
        updateSchemasSyncStatus(schemas, SyncStatus.SYNCED)
        updateValuesSyncStatus(values, SyncStatus.SYNCED)
        updateTemplatesSyncStatus(templates, SyncStatus.SYNCED)
        updateSchedulesSyncStatus(schedules, SyncStatus.SYNCED)
        updateInstancesSyncStatus(instances, SyncStatus.SYNCED)
        updateExceptionsSyncStatus(exceptions, SyncStatus.SYNCED)
        updateOverridesSyncStatus(overrides, SyncStatus.SYNCED)
    }

    @Query("UPDATE nodes SET syncStatus = :status WHERE id IN (:ids)")
    suspend fun updateNodesSyncStatus(ids: List<String>, status: SyncStatus)

    @Query("UPDATE node_types SET syncStatus = :status WHERE id IN (:ids)")
    suspend fun updateTypesSyncStatus(ids: List<String>, status: SyncStatus)

    @Query("UPDATE node_metadata_schemas SET syncStatus = :status WHERE id IN (:ids)")
    suspend fun updateSchemasSyncStatus(ids: List<String>, status: SyncStatus)

    @Query("UPDATE node_field_values SET syncStatus = :status WHERE id IN (:ids)")
    suspend fun updateValuesSyncStatus(ids: List<String>, status: SyncStatus)

    @Query("UPDATE routine_templates SET syncStatus = :status WHERE id IN (:ids)")
    suspend fun updateTemplatesSyncStatus(ids: List<String>, status: SyncStatus)

    @Query("UPDATE schedules SET syncStatus = :status WHERE id IN (:ids)")
    suspend fun updateSchedulesSyncStatus(ids: List<String>, status: SyncStatus)

    @Query("UPDATE day_instances SET syncStatus = :status WHERE id IN (:ids)")
    suspend fun updateInstancesSyncStatus(ids: List<String>, status: SyncStatus)

    @Query("UPDATE schedule_exceptions SET syncStatus = :status WHERE id IN (:ids)")
    suspend fun updateExceptionsSyncStatus(ids: List<String>, status: SyncStatus)
    
    @Query("UPDATE node_overrides SET syncStatus = :status WHERE id IN (:ids)")
    suspend fun updateOverridesSyncStatus(ids: List<String>, status: SyncStatus)
}
