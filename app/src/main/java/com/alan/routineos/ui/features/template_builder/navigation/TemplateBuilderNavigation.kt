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
const val INITIAL_NAME_ARG = "initialName"
const val INITIAL_COLOR_ARG = "initialColor"

const val TEMPLATE_BUILDER_ROUTE = "$TEMPLATE_BUILDER_BASE_ROUTE/{$TEMPLATE_ID_ARG}?$INITIAL_NAME_ARG={$INITIAL_NAME_ARG}&$INITIAL_COLOR_ARG={$INITIAL_COLOR_ARG}"

fun NavController.navigateToTemplateBuilder(
    templateId: String = "new", 
    initialName: String? = null,
    initialColor: String? = null,
    navOptions: NavOptions? = null
) {
    val route = buildString {
        append("$TEMPLATE_BUILDER_BASE_ROUTE/$templateId")
        val params = mutableListOf<String>()
        if (initialName != null) params.add("$INITIAL_NAME_ARG=$initialName")
        if (initialColor != null) params.add("$INITIAL_COLOR_ARG=$initialColor")
        if (params.isNotEmpty()) {
            append("?")
            append(params.joinToString("&"))
        }
    }
    this.navigate(route, navOptions)
}

fun NavGraphBuilder.templateBuilderScreen(
    onBack: () -> Unit,
    onNavigateToTypeManager: () -> Unit
) {
    composable(
        route = TEMPLATE_BUILDER_ROUTE,
        arguments = listOf(
            navArgument(TEMPLATE_ID_ARG) { type = NavType.StringType },
            navArgument(INITIAL_NAME_ARG) { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument(INITIAL_COLOR_ARG) { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
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
