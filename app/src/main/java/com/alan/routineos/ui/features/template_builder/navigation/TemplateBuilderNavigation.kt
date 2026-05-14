package com.alan.routineos.ui.features.template_builder.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.alan.routineos.ui.features.template_builder.presentation.TemplateBuilderScreen
import com.alan.routineos.ui.features.template_builder.viewmodel.TemplateBuilderViewModel
import com.alan.routineos.ui.navigation.Screen

fun NavGraphBuilder.templateBuilderScreen(
    onBack: () -> Unit,
    onNavigateToTypeManager: () -> Unit
) {
    composable(
        route = Screen.TemplateBuilder.route,
        arguments = listOf(
            navArgument("templateId") { type = NavType.StringType }
        )
    ) {
        val viewModel: TemplateBuilderViewModel = hiltViewModel()
        TemplateBuilderScreen(
            onBack = onBack,
            onNavigateToTypeManager = onNavigateToTypeManager,
            viewModel = viewModel
        )
    }
}
