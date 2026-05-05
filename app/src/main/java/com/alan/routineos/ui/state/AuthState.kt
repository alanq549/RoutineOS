package com.alan.routineos.ui.state

import com.alan.routineos.domain.model.AuthSession

sealed class AuthState {

    object Idle : AuthState()
    object Loading : AuthState()

    object Unauthenticated : AuthState()

    data class Authenticated(
        val session: AuthSession
    ) : AuthState()

    data class Error(
        val message: String
    ) : AuthState()
}