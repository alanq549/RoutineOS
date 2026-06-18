package com.alan.routineos.ui.features.account.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.core.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val remindersEnabled: Boolean = true,
    val showHeatmap: Boolean = true,
    val showInsights: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsDataStore.isRemindersEnabled,
        settingsDataStore.isShowHeatmapEnabled,
        settingsDataStore.isShowInsightsEnabled
    ) { reminders, heatmap, insights ->
        SettingsUiState(reminders, heatmap, insights)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun toggleReminders(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setRemindersEnabled(enabled) }
    }

    fun toggleHeatmap(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setShowHeatmapEnabled(enabled) }
    }

    fun toggleInsights(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setShowInsightsEnabled(enabled) }
    }
}
