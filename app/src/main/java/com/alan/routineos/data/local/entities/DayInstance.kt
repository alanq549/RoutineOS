package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "day_instances")
data class DayInstance(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val templateId: String,
    val date: Long,
    val status: InstanceStatus = InstanceStatus.GENERATED,
    val overrideReason: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC,
    val version: Int = 1
)

enum class InstanceStatus { GENERATED, IN_PROGRESS, COMPLETED, CANCELLED, OVERRIDDEN }
