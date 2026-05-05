package com.alan.routineos.ui.state

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object CodeSent : RegisterState()
    data class Error(val message: String) : RegisterState()
}