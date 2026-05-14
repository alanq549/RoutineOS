package com.alan.routineos.ui.features.account.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.alan.routineos.ui.features.account.presentation.AccountScreen
import com.alan.routineos.ui.features.account.viewmodel.UserViewModel
import com.alan.routineos.ui.navigation.Screen

fun NavGraphBuilder.accountScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToAuth: () -> Unit
) {
    composable(Screen.Account.route) {
        val userViewModel: UserViewModel = hiltViewModel()
        AccountScreen(
            userViewModel = userViewModel,
            onBack = onBack,
            onLogout = onLogout,
            onNavigateToAuth = onNavigateToAuth
        )
    }
}
