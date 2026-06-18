package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "node_metadata_schemas")
data class NodeMetadataSchema(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val typeId: String,            // FK a NodeType
    val fieldName: String,         // "peso_kg", "reps", "aula", "hora_salida"
    val fieldLabel: String,        // "Peso (kg)", "Repeticiones", "Aula"
    val fieldType: FieldType,      // NUMBER, TEXT, DURATION, TIME, BOOLEAN
    val defaultValue: String? = null,
    val unit: String? = null,      // "kg", "min", "rep"
    val stepSize: Float? = null,   // para campos numéricos con incremento fijo
    val isRequired: Boolean = false,
    val position: Int = 0,
    val editableInTemplate: Boolean = true,
    val editableInExecution: Boolean = false,
    val executionTrackingMode: ExecutionTrackingMode = ExecutionTrackingMode.NONE,
    val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC,
    val version: Int = 1
)

enum class FieldType { NUMBER, TEXT, DURATION, TIME, BOOLEAN, SELECT }

enum class ExecutionTrackingMode {
    NONE,
    OVERRIDE_VALUE,
    RECORD_ACTUAL
}
