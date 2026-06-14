package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "schedule_exceptions")
data class ScheduleException(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val templateId: String? = null,   // null = aplica a todos los templates
    val label: String,                // "Vacaciones de verano", "Semana de exámenes"
    val dateFrom: Long,
    val dateTo: Long,
    val affectsGeneration: Boolean = true,  // si bloquea la generación de instancias
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val weekday: Int? = null,               // 1=Lunes ... 7=Domingo (para WEEKLY)
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC,
    val version: Int = 1
)

enum class RecurrenceType { NONE, WEEKLY, MONTHLY }
