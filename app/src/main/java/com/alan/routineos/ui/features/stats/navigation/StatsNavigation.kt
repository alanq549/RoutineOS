package com.alan.routineos.ui.features.stats.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.alan.routineos.ui.features.stats.presentation.StatsScreen
import com.alan.routineos.ui.features.stats.viewmodel.StatsViewModel
import com.alan.routineos.ui.navigation.Screen

fun NavGraphBuilder.statsScreen() {
    composable(Screen.Stats.route) {
        val viewModel: StatsViewModel = hiltViewModel()
        StatsScreen(viewModel = viewModel)
    }
}
