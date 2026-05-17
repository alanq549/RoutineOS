package com.alan.routineos.ui.features.system.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.alan.routineos.ui.features.system.presentation.SystemScreen

const val SYSTEM_ROUTE = "system"

fun NavController.navigateToSystem(navOptions: NavOptions? = null) {
    this.navigate(SYSTEM_ROUTE, navOptions)
}

fun NavGraphBuilder.systemScreen(onNavigateToBuilder: (String) -> Unit) {
    composable(SYSTEM_ROUTE) {
        SystemScreen(onNavigateToBuilder = onNavigateToBuilder)
    }
}
