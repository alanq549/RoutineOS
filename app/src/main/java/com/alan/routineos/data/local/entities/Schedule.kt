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
    val endTime: String? = null,    // hora de fin (útil para ciclos con salida variable)
    val validFrom: Long? = null,    // desde cuándo aplica este schedule
    val validUntil: Long? = null,   // hasta cuándo aplica (semestre, contrato, etc.)
    val overrideDate: Long? = null
)
