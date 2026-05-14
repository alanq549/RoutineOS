package com.alan.routineos.ui.features.execute.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.alan.routineos.ui.features.execute.presentation.ExecuteScreen
import com.alan.routineos.ui.features.execute.viewmodel.ExecuteViewModel
import com.alan.routineos.ui.navigation.Screen

fun NavGraphBuilder.executeScreen(
    onBack: () -> Unit
) {
    composable(
        route = Screen.Execute.route,
        arguments = listOf(
            navArgument("nodeId") { type = NavType.StringType }
        )
    ) {
        val viewModel: ExecuteViewModel = hiltViewModel()
        ExecuteScreen(
            onBack = onBack,
            viewModel = viewModel
        )
    }
}
