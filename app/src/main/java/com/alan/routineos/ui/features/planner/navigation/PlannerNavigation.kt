package com.alan.routineos.ui.features.planner.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.alan.routineos.ui.features.planner.presentation.PlannerScreen
import com.alan.routineos.ui.features.planner.viewmodel.PlannerViewModel
import com.alan.routineos.ui.navigation.Screen

fun NavGraphBuilder.plannerScreen() {
    composable(Screen.Planner.route) {
        val viewModel: PlannerViewModel = hiltViewModel()
        PlannerScreen(viewModel = viewModel)
    }
}
