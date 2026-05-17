package com.alan.routineos.ui.features.system.state

data class SystemUiState(
    val activeTab: Int = 0,
    val searchQuery: String = "",
    val selectedDayIndex: Int = 1,
    val isLoading: Boolean = false
)
