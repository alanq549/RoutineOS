package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "node_types")
data class NodeType(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: String? = null,
    val colorHex: String? = null,
    val hasMetricFields: Boolean = false,
    val allowsChildren: Boolean = true,
    val position: Int = 0,
    val isActive: Boolean = true, // Soft delete flag
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC,
    val version: Int = 1
)
