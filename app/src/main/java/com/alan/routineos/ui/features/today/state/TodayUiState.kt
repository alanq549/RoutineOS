package com.alan.routineos.ui.features.today.state

import androidx.compose.ui.graphics.Color
import com.alan.routineos.data.local.entities.FieldType
import com.alan.routineos.data.local.entities.ScheduleException
import com.alan.routineos.ui.features.system.state.PlanningItemType
import com.alan.routineos.ui.features.system.state.PlanningStatus

data class TodayUiState(
    val isLoading: Boolean = true,
    val currentTime: String = "",
    val dateLabel: String = "",
    val monthLabel: String = "",
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val timelineEntries: List<TimelineEntryUi> = emptyList(),
    val activeExceptions: List<ScheduleException> = emptyList(),
    val unlinkedPlanningItems: List<PlanningLinkedItemUi> = emptyList()
)

data class TimelineEntryUi(
    val id: String, 
    val time: String,
    val sortTime: String,
    val endTime: String? = null,
    val title: String,
    val subtitle: String? = null,
    val statusLabel: String,
    val statusColor: Color,
    val barColor: Color,
    val isCancelled: Boolean = false,
    val isSkipped: Boolean = false,
    val hasConflict: Boolean = false,
    val isCurrent: Boolean = false,
    val conflictResolutionSuggestions: List<ConflictResolutionUi> = emptyList(),
    val fields: List<ResolvedFieldUi> = emptyList(),
    val resolvedNodes: List<ResolvedNodeUi> = emptyList(),
    val showTimeIndicatorBefore: Boolean = false,
    val wasShiftedByDomino: Boolean = false,
    val dominoReason: String? = null,
    val planningInfo: PlanningIndicatorUi? = null
)

data class ConflictResolutionUi(
    val label: String,
    val type: ConflictResolutionType,
    val newValue: String? = null
)

enum class ConflictResolutionType { RESCHEDULE, SKIP, REDUCE }

data class ResolvedNodeUi(
    val id: String,
    val name: String,
    val depth: Int,
    val timeLabel: String? = null,
    val isCompleted: Boolean = false,
    val isSkipped: Boolean = false,
    val fields: List<ResolvedFieldUi> = emptyList(),
    val planningInfo: PlanningIndicatorUi? = null
)

data class PlanningIndicatorUi(
    val pendingCount: Int = 0,
    val todayCount: Int = 0,
    val overdueCount: Int = 0,
    val totalCount: Int = 0,
    val items: List<PlanningLinkedItemUi> = emptyList()
)

data class PlanningLinkedItemUi(
    val id: String,
    val type: PlanningItemType,
    val title: String,
    val dueDate: Long? = null,
    val dueTime: String? = null,
    val status: PlanningStatus = PlanningStatus.PENDING,
    val urgency: Int // 0: Overdue, 1: Today, 2: Future/No date
)

data class ResolvedFieldUi(
    val schemaId: String,
    val fieldName: String,
    val label: String,
    val value: String,
    val type: FieldType
)
