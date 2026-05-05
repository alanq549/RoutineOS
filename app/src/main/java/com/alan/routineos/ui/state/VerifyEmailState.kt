package com.alan.routineos.ui.state

sealed class VerifyEmailState {
    object Idle : VerifyEmailState()
    object Loading : VerifyEmailState()
    object Success : VerifyEmailState()
    data class Error(val message: String) : VerifyEmailState()
}