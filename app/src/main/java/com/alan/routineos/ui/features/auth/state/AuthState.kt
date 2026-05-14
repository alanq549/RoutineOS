package com.alan.routineos.ui.features.auth.state

import com.alan.routineos.domain.model.AuthSession

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data object Unauthenticated : AuthState()
    data class Authenticated(val session: AuthSession) : AuthState()
    data class Error(val message: String) : AuthState()
    data class EmailNotVerified(val email: String, val message: String) : AuthState()
    data class RateLimited(val message: String) : AuthState()
}
