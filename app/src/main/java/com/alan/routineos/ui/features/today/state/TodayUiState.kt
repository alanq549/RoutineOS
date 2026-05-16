package com.alan.routineos.ui.features.today.state

import androidx.compose.ui.graphics.Color
import com.alan.routineos.data.local.entities.DayInstance
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeType

data class TodayUiState(
    val instance: DayInstance? = null,
    val nodes: List<Node> = emptyList(),
    val nodeTypes: List<NodeType> = emptyList(),
    val isLoading: Boolean = true,
    val currentTime: String = "",
    // Nuevos campos para UI
    val dateLabel: String = "Lunes, 12 de mayo",
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val timelineEntries: List<TimelineEntryUi> = emptyList()
)

data class TimelineEntryUi(
    val id: String,
    val time: String,
    val title: String,
    val subtitle: String? = null,
    val statusLabel: String,
    val statusColor: Color,
    val barColor: Color,
    val isCancelled: Boolean = false,
    val hasConflict: Boolean = false,
    val resolvedNodes: List<ResolvedNodeUi> = emptyList(),
    val showTimeIndicatorBefore: Boolean = false
)

data class ResolvedNodeUi(
    val name: String,
    val depth: Int,
    val valueSummary: String? = null
)