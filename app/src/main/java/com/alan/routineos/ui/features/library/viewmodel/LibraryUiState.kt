package com.alan.routineos.ui.features.library.viewmodel

data class LibraryUiState(
    val definitions: List<ActivityDefinitionUi> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

data class ActivityDefinitionUi(
    val id: String,
    val name: String,
    val colorHex: String,
    val blocksSummary: String,
    val activeDays: List<Int>,
    val timeLabel: String?
)
