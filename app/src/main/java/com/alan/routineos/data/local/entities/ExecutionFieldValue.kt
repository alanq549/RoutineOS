package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "execution_field_values")
data class ExecutionFieldValue(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val dayInstanceId: String?,
    val nodeId: String,
    val sourceTemplateNodeId: String?,
    val schemaId: String,
    val fieldName: String,
    val plannedValue: String?,
    val actualValue: String,
    val date: Long,
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC,
    val version: Int = 1
)
