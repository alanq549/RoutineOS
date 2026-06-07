package com.alan.routineos.ui.features.system.state

import com.alan.routineos.data.local.entities.ScheduleException

data class SystemUiState(
    val activeTab: Int = 0,
    val searchQuery: String = "",
    val selectedDate: Long = System.currentTimeMillis(),
    val currentWeekStart: Long = System.currentTimeMillis(),
    val adaptations: List<ScheduleException> = emptyList(),
    val isLoading: Boolean = false
)
