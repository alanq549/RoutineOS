package com.alan.routineos.ui.features.stats.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.alan.routineos.ui.features.stats.presentation.StatsScreen
import com.alan.routineos.ui.features.stats.viewmodel.StatsViewModel

const val STATS_ROUTE = "stats"

fun NavController.navigateToStats(navOptions: NavOptions? = null) {
    this.navigate(STATS_ROUTE, navOptions)
}

fun NavGraphBuilder.statsScreen() {
    composable(STATS_ROUTE) {
        val viewModel: StatsViewModel = hiltViewModel()
        StatsScreen(viewModel = viewModel)
    }
}
