package com.alan.routineos.ui.features.auth.state

sealed class RegisterState {
    data object Idle : RegisterState()
    data object Loading : RegisterState()
    data object CodeSent : RegisterState()
    data class Error(val message: String) : RegisterState()
}
