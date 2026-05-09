package com.alan.routineos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.core.network.toApiError
import com.alan.routineos.core.session.SessionManager
import com.alan.routineos.core.session.UserManager
import com.alan.routineos.data.remote.auth.login.LoginRequest
import com.alan.routineos.data.remote.auth.register.RegisterRequest
import com.alan.routineos.domain.usecase.LoginUseCase
import com.alan.routineos.domain.usecase.RegisterUseCase
import com.alan.routineos.domain.usecase.VerifyEmailCodeUseCase
import com.alan.routineos.ui.events.UiEvent
import com.alan.routineos.ui.state.AuthState
import com.alan.routineos.ui.state.VerifyEmailState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val loginUseCase: LoginUseCase,
    private val verifyEmailUseCase: VerifyEmailCodeUseCase,
    private val sessionManager: SessionManager,
    private val userManager: UserManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState = _authState.asStateFlow()

    private val _verifyState = MutableStateFlow<VerifyEmailState>(VerifyEmailState.Idle)
    val verifyState = _verifyState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var pendingEmail: String? = null

    init {
        checkSessionAndObserve()
    }

    private fun checkSessionAndObserve() {
        viewModelScope.launch {
            // 1. Cargamos la sesión desde el almacenamiento local
            sessionManager.loadSession()

            // 2. Observamos cambios de sesión
            sessionManager.session.collect { session ->
                _authState.value = if (session != null) {
                    AuthState.Authenticated(session)
                } else {
                    AuthState.Unauthenticated
                }
            }
        }
    }

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            runCatching {
                loginUseCase(request)
            }.onSuccess { session ->
                sessionManager.saveSession(session)
                userManager.syncUser()
                _uiEvent.emit(UiEvent.ShowSnackbar("Login exitoso"))
            }
                .onFailure { throwable ->

                    val error = throwable.toApiError()

                    when (error.code) {

                        "EMAIL_NOT_VERIFIED" -> {
                            _authState.value =
                                AuthState.EmailNotVerified(
                                    email = request.email,
                                    message = error.message
                                )
                        }

                        "RATE_LIMITED" -> {
                            _authState.value =
                                AuthState.RateLimited(
                                    error.message
                                )
                        }

                        else -> {
                            _authState.value =
                                AuthState.Error(
                                    error.message
                                )
                        }
                    }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.logout()
            userManager.clear()
            _uiEvent.emit(UiEvent.Navigate("auth"))
        }
    }

    fun register(request: RegisterRequest, onCodeSent: () -> Unit) {
        viewModelScope.launch {
            runCatching {
                registerUseCase(request)
            }.onSuccess {
                pendingEmail = request.email
                onCodeSent()
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Error")
            }
        }
    }

    fun verifyEmailCode(code: String) {
        val email = pendingEmail ?: run {
            _verifyState.value = VerifyEmailState.Error("Email inválido")
            return
        }

        viewModelScope.launch {
            _verifyState.value = VerifyEmailState.Loading

            runCatching {
                verifyEmailUseCase(email, code)
            }.onSuccess {
                _verifyState.value = VerifyEmailState.Success
            }.onFailure {
                _verifyState.value = VerifyEmailState.Error(it.message ?: "Error")
            }
        }
    }
}
