package com.alan.routineos.ui.features.home.state

import com.alan.routineos.domain.model.UserProfile

data class HomeUiState(
    val user: UserProfile? = null,
    val isLoading: Boolean = false
)
