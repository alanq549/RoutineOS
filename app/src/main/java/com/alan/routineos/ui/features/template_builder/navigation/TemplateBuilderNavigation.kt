package com.alan.routineos.ui.features.template_builder.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.alan.routineos.ui.features.template_builder.presentation.TemplateBuilderScreen
import com.alan.routineos.ui.features.template_builder.viewmodel.TemplateBuilderViewModel

const val TEMPLATE_BUILDER_BASE_ROUTE = "library/builder"
const val TEMPLATE_ID_ARG = "templateId"
const val TEMPLATE_BUILDER_ROUTE = "$TEMPLATE_BUILDER_BASE_ROUTE/{$TEMPLATE_ID_ARG}"

fun NavController.navigateToTemplateBuilder(templateId: String = "new", navOptions: NavOptions? = null) {
    this.navigate("$TEMPLATE_BUILDER_BASE_ROUTE/$templateId", navOptions)
}

fun NavGraphBuilder.templateBuilderScreen(
    onBack: () -> Unit,
    onNavigateToTypeManager: () -> Unit
) {
    composable(
        route = TEMPLATE_BUILDER_ROUTE,
        arguments = listOf(
            navArgument(TEMPLATE_ID_ARG) { type = NavType.StringType }
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
