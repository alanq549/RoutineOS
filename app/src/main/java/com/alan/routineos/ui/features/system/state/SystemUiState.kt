package com.alan.routineos.ui.features.system.state

import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.ScheduleException

data class SystemUiState(
    val activeTab: Int = 0, // 0: Activities, 1: Planning
    val planningSubTab: PlanningSection = PlanningSection.ROUTINE_CHANGES,
    val searchQuery: String = "",
    val selectedDate: Long = System.currentTimeMillis(),
    val currentWeekStart: Long = System.currentTimeMillis(),
    val adaptations: List<ScheduleException> = emptyList(),
    val planningItems: List<PlanningItemUi> = emptyList(),
    val planningTargets: List<PlanningTargetUi> = emptyList(),
    val allNodes: List<Node> = emptyList(),
    val isLoading: Boolean = false
)

enum class PlanningSection {
    ROUTINE_CHANGES,
    TASKS_AND_NOTES
}

data class PlanningItemUi(
    val id: String,
    val type: PlanningItemType,
    val title: String,
    val description: String? = null,
    val dueDate: Long? = null,
    val dueTime: String? = null,
    val relatedNodeId: String? = null,
    val relatedNodePath: String? = null,
    val status: PlanningStatus = PlanningStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "LOCAL",
    val version: Int = 1
)

data class PlanningTargetUi(
    val nodeId: String,
    val path: String,
    val level: Int
)

enum class PlanningItemType { TASK, NOTE, REMINDER }
enum class PlanningStatus { PENDING, COMPLETED }
