package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.alan.routineos.ui.features.template_builder.sections.ContextCategory
import com.alan.routineos.ui.features.template_builder.sections.TimeMode
import java.util.UUID

@Entity(tableName = "routine_templates")
data class RoutineTemplate(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val rootNodeId: String,
    val name: String,
    val colorHex: String = "#3FB950",
    val category: ContextCategory = ContextCategory.FLEXIBLE,
    val timeMode: TimeMode = TimeMode.RANGE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC,
    val version: Int = 1
)
