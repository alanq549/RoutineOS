package com.alan.routineos.ui.features.library.viewmodel

import com.alan.routineos.data.local.entities.RoutineTemplate

data class LibraryUiState(
    val templates: List<RoutineTemplate> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true
)
