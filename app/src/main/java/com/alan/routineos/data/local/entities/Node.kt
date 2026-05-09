package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "nodes")
data class Node(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val parentId: String? = null,
    val typeId: String,            // FK a NodeType (no un enum hardcoded)
    val templateId: String? = null,
    val instanceId: String? = null,
    val name: String,
    val position: Int = 0,
    val scheduledTime: String? = null,  // "07:00"
    val durationMinutes: Int? = null,
    val status: NodeStatus = NodeStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC,
    val version: Int = 1
)

enum class NodeStatus { PENDING, ACTIVE, COMPLETED, SKIPPED, POSTPONED }
enum class SyncStatus { SYNCED, PENDING_SYNC, CONFLICT }
