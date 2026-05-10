package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val templateId: String,
    val weekday: Int,           // 1=Lunes ... 7=Domingo
    val isActive: Boolean = true,
    val startTime: String? = null,
    val endTime: String? = null,
    val validFrom: Long? = null,
    val validUntil: Long? = null,
    val overrideDate: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC,
    val version: Int = 1
)
