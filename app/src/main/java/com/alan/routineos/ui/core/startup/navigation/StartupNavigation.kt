package com.alan.routineos.ui.core.startup.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.alan.routineos.ui.core.startup.AppStartupState
import com.alan.routineos.ui.core.startup.LaunchScreen

const val LAUNCH_ROUTE = "launch"

fun NavController.navigateToLaunch(navOptions: NavOptions? = null) {
    this.navigate(LAUNCH_ROUTE, navOptions)
}

fun NavGraphBuilder.launchScreen(
    state: AppStartupState,
    onFinish: (AppStartupState) -> Unit
) {
    composable(LAUNCH_ROUTE) {
        LaunchScreen(
            state = state,
            onFinish = onFinish
        )
    }
}
