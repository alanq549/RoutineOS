package com.alan.routineos.ui.features.execute.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.alan.routineos.ui.features.execute.presentation.ExecuteScreen
import com.alan.routineos.ui.features.execute.viewmodel.ExecuteViewModel

const val EXECUTE_BASE_ROUTE = "execute"
const val NODE_ID_ARG = "nodeId"
const val EXECUTE_ROUTE = "$EXECUTE_BASE_ROUTE/{$NODE_ID_ARG}"

fun NavController.navigateToExecute(nodeId: String, navOptions: NavOptions? = null) {
    this.navigate("$EXECUTE_BASE_ROUTE/$nodeId", navOptions)
}

fun NavGraphBuilder.executeScreen(
    onBack: () -> Unit
) {
    composable(
        route = EXECUTE_ROUTE,
        arguments = listOf(
            navArgument(NODE_ID_ARG) { type = NavType.StringType }
        )
    ) {
        val viewModel: ExecuteViewModel = hiltViewModel()
        ExecuteScreen(
            onBack = onBack,
            viewModel = viewModel
        )
    }
}
