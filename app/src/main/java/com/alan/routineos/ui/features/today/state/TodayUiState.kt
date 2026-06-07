package com.alan.routineos.ui.features.today.state

import androidx.compose.ui.graphics.Color
import com.alan.routineos.data.local.entities.FieldType

data class TodayUiState(
    val isLoading: Boolean = true,
    val currentTime: String = "",
    val dateLabel: String = "",
    val monthLabel: String = "",
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val timelineEntries: List<TimelineEntryUi> = emptyList()
)

data class TimelineEntryUi(
    val id: String, 
    val time: String,
    val sortTime: String,
    val title: String,
    val subtitle: String? = null,
    val statusLabel: String,
    val statusColor: Color,
    val barColor: Color,
    val isCancelled: Boolean = false,
    val isSkipped: Boolean = false,
    val hasConflict: Boolean = false,
    val fields: List<ResolvedFieldUi> = emptyList(),
    val resolvedNodes: List<ResolvedNodeUi> = emptyList(),
    val showTimeIndicatorBefore: Boolean = false,
    val wasShiftedByDomino: Boolean = false,
    val dominoReason: String? = null
)

data class ResolvedNodeUi(
    val id: String,
    val name: String,
    val depth: Int,
    val timeLabel: String? = null,
    val isCompleted: Boolean = false,
    val isSkipped: Boolean = false,
    val fields: List<ResolvedFieldUi> = emptyList()
)

data class ResolvedFieldUi(
    val schemaId: String,
    val fieldName: String,
    val label: String,
    val value: String,
    val type: FieldType
)
