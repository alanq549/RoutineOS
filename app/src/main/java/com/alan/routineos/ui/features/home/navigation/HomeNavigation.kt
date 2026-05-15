package com.alan.routineos.ui.features.home.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.alan.routineos.ui.features.home.presentation.HomeScreen
import com.alan.routineos.ui.features.home.viewmodel.HomeViewModel

fun NavGraphBuilder.homeScreen(
    onNavigateToAccount: () -> Unit
) {
    composable("home") {
        val viewModel: HomeViewModel = hiltViewModel()
        HomeScreen(
            viewModel = viewModel,
            onNavigateToAccount = onNavigateToAccount
        )
    }
}
