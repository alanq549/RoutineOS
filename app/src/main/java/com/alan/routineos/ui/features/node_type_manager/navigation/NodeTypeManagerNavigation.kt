package com.alan.routineos.ui.features.node_type_manager.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.alan.routineos.ui.features.node_type_manager.presentation.NodeTypeManagerScreen
import com.alan.routineos.ui.features.node_type_manager.viewmodel.NodeTypeManagerViewModel
import com.alan.routineos.ui.navigation.Screen

fun NavGraphBuilder.nodeTypeManagerScreen(
    onBack: () -> Unit
) {
    composable(Screen.NodeTypeManager.route) {
        val viewModel: NodeTypeManagerViewModel = hiltViewModel()
        NodeTypeManagerScreen(
            onBack = onBack,
            viewModel = viewModel
        )
    }
}
