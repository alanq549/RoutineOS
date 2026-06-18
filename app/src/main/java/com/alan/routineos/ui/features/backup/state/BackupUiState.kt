package com.alan.routineos.ui.features.backup.state

import com.alan.routineos.backup.BackupExportResult
import com.alan.routineos.backup.BackupFileInfo

data class BackupUiState(
    val isExporting: Boolean = false,
    val lastExportResult: BackupExportResult? = null,
    val backups: List<BackupFileInfo> = emptyList(),
    val errorMessage: String? = null
)
