package com.alan.routineos.ui.features.auth.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.alan.routineos.ui.features.auth.presentation.AuthScreen
import com.alan.routineos.ui.features.auth.viewmodel.AuthViewModel
import com.alan.routineos.ui.navigation.Screen

fun NavGraphBuilder.authScreen(
    onLoginSuccess: () -> Unit
) {
    composable(Screen.Auth.route) {
        val viewModel: AuthViewModel = hiltViewModel()
        AuthScreen(
            viewModel = viewModel,
            onLoginSuccess = onLoginSuccess
        )
    }
}
