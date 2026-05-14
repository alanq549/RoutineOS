package com.alan.routineos.ui.features.auth.state

sealed class VerifyEmailState {
    data object Idle : VerifyEmailState()
    data object Loading : VerifyEmailState()
    data object Success : VerifyEmailState()
    data class Error(val message: String) : VerifyEmailState()
}
