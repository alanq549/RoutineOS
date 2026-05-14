package com.alan.routineos.ui.features.today.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.alan.routineos.ui.features.today.presentation.TodayScreen
import com.alan.routineos.ui.features.today.viewmodel.TodayViewModel
import com.alan.routineos.ui.navigation.Screen

fun NavGraphBuilder.todayScreen(
    onNavigateToExecute: (String) -> Unit
) {
    composable(Screen.Today.route) {
        val viewModel: TodayViewModel = hiltViewModel()
        TodayScreen(
            viewModel = viewModel,
            onNavigateToExecute = onNavigateToExecute
        )
    }
}
