package com.alan.routineos.ui.features.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.core.session.SessionManager
import com.alan.routineos.core.session.UserManager
import com.alan.routineos.ui.features.home.state.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val userManager: UserManager
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        userManager.user,
        userManager.isLoading
    ) { user, isLoading ->
        HomeUiState(
            user = user,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    init {
        viewModelScope.launch {
            userManager.loadLocal()
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.logout()
            userManager.clear()
        }
    }
}
