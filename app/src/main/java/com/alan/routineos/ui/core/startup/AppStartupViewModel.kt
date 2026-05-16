package com.alan.routineos.ui.core.startup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.core.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppStartupViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _state = MutableStateFlow<AppStartupState>(AppStartupState.Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // Obtenemos el estado real del DataStore para decidir el flujo
            val completed = settingsDataStore.isOnboardingCompleted.first()

            _state.value = if (completed) {
                AppStartupState.ShowHome
            } else {
                AppStartupState.ShowOnboarding
            }
        }
    }
}
