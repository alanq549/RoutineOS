package com.alan.routineos.ui.features.account.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.alan.routineos.ui.features.account.presentation.AccountScreen
import com.alan.routineos.ui.features.account.viewmodel.UserViewModel

const val ACCOUNT_ROUTE = "account"

fun NavController.navigateToAccount(navOptions: NavOptions? = null) {
    this.navigate(ACCOUNT_ROUTE, navOptions)
}

fun NavGraphBuilder.accountScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToAuth: () -> Unit
) {
    composable(ACCOUNT_ROUTE) {
        val userViewModel: UserViewModel = hiltViewModel()
        AccountScreen(
            userViewModel = userViewModel,
            onBack = onBack,
            onLogout = onLogout,
            onNavigateToAuth = onNavigateToAuth
        )
    }
}
