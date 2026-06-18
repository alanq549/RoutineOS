package com.alan.routineos.ui.features.backup.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.backup.BackupManager
import com.alan.routineos.ui.features.backup.state.BackupUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupManager: BackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    init {
        loadBackups()
    }

    fun loadBackups() {
        viewModelScope.launch {
            val list = backupManager.listLocalBackups()
            _uiState.update { it.copy(backups = list) }
        }
    }

    fun exportBackup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, errorMessage = null) }
            val result = backupManager.exportBackup()
            _uiState.update { 
                it.copy(
                    isExporting = false, 
                    lastExportResult = result,
                    errorMessage = if (!result.success) result.errorMessage else null
                ) 
            }
            if (result.success) {
                loadBackups()
            }
        }
    }
}
