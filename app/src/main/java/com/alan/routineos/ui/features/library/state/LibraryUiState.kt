package com.alan.routineos.ui.features.library.state

import com.alan.routineos.data.local.entities.RoutineTemplate

data class LibraryUiState(
    val templates: List<RoutineTemplate> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true
)
