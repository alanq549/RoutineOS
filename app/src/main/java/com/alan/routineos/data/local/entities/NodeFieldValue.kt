package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "node_field_values")
data class NodeFieldValue(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val nodeId: String,
    val schemaId: String,          // FK a NodeMetadataSchema
    val fieldName: String,         // redundante pero útil para queries
    val value: String,             // siempre String, parsear según FieldType
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC,
    val version: Int = 1
)
