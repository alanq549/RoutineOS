package com.alan.routineos.ui.features.account.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.core.session.SessionManager
import com.alan.routineos.core.session.UserManager
import com.alan.routineos.ui.features.account.state.UserState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userManager: UserManager,
    private val sessionManager: SessionManager
) : ViewModel() {

    val userState: StateFlow<UserState> = combine(
        userManager.user,
        userManager.isLoading,
        userManager.error
    ) { user, isLoading, error ->
        when {
            isLoading -> UserState.Loading
            error != null -> UserState.Error(error)
            user != null -> UserState.Success(user)
            else -> UserState.Idle
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserState.Idle
    )

    init {
        viewModelScope.launch {
            userManager.loadLocal()
        }
    }

    /**
     * Executes logout process and triggers callback when finished.
     * Running it in viewModelScope but ensuring we don't navigate away 
     * before the request is at least sent.
     */
    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            sessionManager.logout() // This sends the request to backend
            userManager.clear()
            onComplete()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            userManager.fetchUser()
        }
    }
}
