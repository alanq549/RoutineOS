package com.alan.routineos.ui.features.node_type_manager.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.alan.routineos.ui.features.node_type_manager.presentation.NodeTypeManagerScreen
import com.alan.routineos.ui.features.node_type_manager.viewmodel.NodeTypeManagerViewModel

const val NODE_TYPE_MANAGER_ROUTE = "settings/types"

fun NavController.navigateToNodeTypeManager(navOptions: NavOptions? = null) {
    this.navigate(NODE_TYPE_MANAGER_ROUTE, navOptions)
}

fun NavGraphBuilder.nodeTypeManagerScreen(
    onBack: () -> Unit
) {
    composable(NODE_TYPE_MANAGER_ROUTE) {
        val viewModel: NodeTypeManagerViewModel = hiltViewModel()
        NodeTypeManagerScreen(
            onBack = onBack,
            viewModel = viewModel
        )
    }
}
