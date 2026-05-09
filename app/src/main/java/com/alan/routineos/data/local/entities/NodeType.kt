package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "node_types")
data class NodeType(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,              // "Ejercicio", "Materia", "Hábito", lo que sea
    val icon: String? = null,      // nombre del icono Material
    val colorHex: String? = null,  // color para identificar visualmente
    val hasMetricFields: Boolean = false,  // si tiene campos de tracking (pesos, reps, etc.)
    val allowsChildren: Boolean = true,    // si puede tener nodos hijo
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
