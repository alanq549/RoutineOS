package com.alan.routineos.ui.features.planner.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.alan.routineos.ui.features.planner.presentation.PlannerScreen
import com.alan.routineos.ui.features.planner.viewmodel.PlannerViewModel

const val PLANNER_ROUTE = "planner"

fun NavController.navigateToPlanner(navOptions: NavOptions? = null) {
    this.navigate(PLANNER_ROUTE, navOptions)
}

fun NavGraphBuilder.plannerScreen() {
    composable(PLANNER_ROUTE) {
        val viewModel: PlannerViewModel = hiltViewModel()
        PlannerScreen(viewModel = viewModel)
    }
}
