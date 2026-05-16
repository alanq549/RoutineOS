package com.alan.routineos.ui.features.auth.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.alan.routineos.ui.features.auth.presentation.AuthScreen
import com.alan.routineos.ui.features.auth.viewmodel.AuthViewModel

const val AUTH_ROUTE = "auth"

fun NavController.navigateToAuth(navOptions: NavOptions? = null) {
    this.navigate(AUTH_ROUTE, navOptions)
}

fun NavGraphBuilder.authScreen(
    onLoginSuccess: () -> Unit
) {
    composable(AUTH_ROUTE) {
        val viewModel: AuthViewModel = hiltViewModel()
        AuthScreen(
            viewModel = viewModel,
            onLoginSuccess = onLoginSuccess
        )
    }
}
