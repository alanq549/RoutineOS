package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "nodes")
data class Node(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val parentId: String? = null,
    val typeId: String,            // FK a NodeType
    val templateId: String? = null, // ID del RoutineTemplate
    val sourceTemplateNodeId: String? = null, // ID del nodo plantilla original (si es una instancia)
    val instanceId: String? = null, // ID de la instancia diaria (si es una instancia)
    val name: String,
    val position: Int = 0,
    val scheduledTime: String? = null,  // "07:00"
    val durationMinutes: Int? = null,
    val status: NodeStatus = NodeStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC,
    val version: Int = 1,
    val isSequential: Boolean = true // true si no depende de horarios específicos
)

enum class NodeStatus { PENDING, ACTIVE, COMPLETED, SKIPPED, POSTPONED }
enum class SyncStatus { SYNCED, PENDING_SYNC, CONFLICT }
