package com.alan.routineos.ui.features.backup.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.alan.routineos.ui.features.backup.presentation.BackupScreen
import com.alan.routineos.ui.features.backup.viewmodel.BackupViewModel

const val BACKUP_ROUTE = "settings/backup"

fun NavController.navigateToBackup(navOptions: NavOptions? = null) {
    this.navigate(BACKUP_ROUTE, navOptions)
}

fun NavGraphBuilder.backupScreen(
    onBack: () -> Unit
) {
    composable(BACKUP_ROUTE) {
        val viewModel: BackupViewModel = hiltViewModel()
        BackupScreen(
            viewModel = viewModel,
            onBack = onBack
        )
    }
}
