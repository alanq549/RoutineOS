package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "node_overrides")
data class NodeOverride(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val nodeId: String,
    val instanceId: String,
    val overrideType: OverrideType,
    val newTime: String? = null,
    val newDurationMinutes: Int? = null,
    val postponeMinutes: Int? = null,
    val reason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC,
    val version: Int = 1
)

enum class OverrideType { POSTPONE, SKIP, RESCHEDULE, DURATION_CHANGE, CANCEL }
