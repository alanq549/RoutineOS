package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "node_schedules",
    foreignKeys = [
        ForeignKey(
            entity = Node::class,
            parentColumns = ["id"],
            childColumns = ["nodeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("nodeId")]
)
data class NodeSchedule(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val nodeId: String,
    val dayOfWeek: Int, // 1 (Lunes) a 7 (Domingo)
    val startTime: String, // "HH:mm"
    val endTime: String,   // "HH:mm"
    val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC,
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
