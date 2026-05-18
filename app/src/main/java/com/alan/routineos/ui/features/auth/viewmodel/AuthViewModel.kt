package com.alan.routineos.ui.features.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.core.network.toApiError
import com.alan.routineos.core.session.SessionManager
import com.alan.routineos.core.session.UserManager
import com.alan.routineos.core.util.DeviceInfoProvider
import com.alan.routineos.data.remote.auth.login.LoginRequest
import com.alan.routineos.data.remote.auth.register.RegisterRequest
import com.alan.routineos.domain.usecase.LoginUseCase
import com.alan.routineos.domain.usecase.RegisterUseCase
import com.alan.routineos.domain.usecase.VerifyEmailCodeUseCase
import com.alan.routineos.ui.events.UiEvent
import com.alan.routineos.ui.features.auth.state.AuthState
import com.alan.routineos.ui.features.auth.state.VerifyEmailState
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
    private val userManager: UserManager,
    private val deviceInfoProvider: DeviceInfoProvider
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
            try {
                sessionManager.loadSession()
                sessionManager.session.collect { session ->
                    if (session != null) {
                        userManager.loadLocal()
                        if (userManager.user.value != null) {
                            _authState.value = AuthState.Authenticated(session)
                        } else {
                            val success = runCatching { userManager.syncUser() }.isSuccess
                            if (success && userManager.user.value != null) {
                                _authState.value = AuthState.Authenticated(session)
                            } else {
                                sessionManager.clear()
                                _authState.value = AuthState.Unauthenticated
                            }
                        }
                    } else {
                        _authState.value = AuthState.Unauthenticated
                    }
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val request = LoginRequest(
                email = email,
                password = password,
                timezone = deviceInfoProvider.getTimezone(),
                device = deviceInfoProvider.getDeviceRequest()
            )

            runCatching {
                loginUseCase(request)
            }.onSuccess { session ->
                sessionManager.saveSession(session)
                userManager.syncUser()
                _uiEvent.emit(UiEvent.ShowSnackbar("Login exitoso"))
            }.onFailure { throwable ->
                val error = throwable.toApiError()
                when (error.code) {
                    "EMAIL_NOT_VERIFIED" -> {
                        _authState.value = AuthState.EmailNotVerified(
                            email = email,
                            message = error.message
                        )
                    }
                    "RATE_LIMITED" -> {
                        _authState.value = AuthState.RateLimited(error.message)
                    }
                    else -> {
                        _authState.value = AuthState.Error(error.message)
                    }
                }
            }
        }
    }

    fun register(name: String, email: String, password: String, onCodeSent: () -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val request = RegisterRequest(
                email = email,
                password = password,
                name = name,
                timezone = deviceInfoProvider.getTimezone(),
                device = deviceInfoProvider.getDeviceRequest()
            )

            runCatching {
                registerUseCase(request)
            }.onSuccess {
                pendingEmail = email
                onCodeSent()
            }.onFailure { throwable ->
                val error = throwable.toApiError()
                _authState.value = AuthState.Error(error.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.logout()
            userManager.clear()
            _authState.value = AuthState.Unauthenticated
            _uiEvent.emit(UiEvent.Navigate("auth"))
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
