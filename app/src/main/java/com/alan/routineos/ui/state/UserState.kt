package com.alan.routineos.ui.state

import com.alan.routineos.domain.model.UserProfile

sealed class UserState {
    data object Idle : UserState()
    data object Loading : UserState()
    data class Success(val user: UserProfile) : UserState()
    data class Error(val message: String) : UserState()
}