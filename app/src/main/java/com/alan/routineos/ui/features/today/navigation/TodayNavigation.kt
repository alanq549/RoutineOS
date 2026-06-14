package com.alan.routineos.ui.features.today.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.alan.routineos.ui.features.today.presentation.TodayScreen
import com.alan.routineos.ui.features.today.viewmodel.TodayViewModel

const val TODAY_ROUTE = "today"

fun NavController.navigateToToday(navOptions: NavOptions? = null) {
    this.navigate(TODAY_ROUTE, navOptions)
}

fun NavGraphBuilder.todayScreen(
    onNavigateToExecute: (String) -> Unit,
    onNavigateToTemplateBuilder: (String) -> Unit
) {
    composable(TODAY_ROUTE) {
        val viewModel: TodayViewModel = hiltViewModel()
        TodayScreen(
            viewModel = viewModel,
            onNavigateToExecute = onNavigateToExecute,
            onNavigateToTemplateBuilder = onNavigateToTemplateBuilder
        )
    }
}
