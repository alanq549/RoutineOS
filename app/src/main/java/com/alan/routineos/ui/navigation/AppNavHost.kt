package com.alan.routineos.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.alan.routineos.ui.core.startup.AppStartupState
import com.alan.routineos.ui.core.startup.AppStartupViewModel
import com.alan.routineos.ui.core.startup.navigation.LAUNCH_ROUTE
import com.alan.routineos.ui.core.startup.navigation.launchScreen
import com.alan.routineos.ui.features.account.navigation.accountScreen
import com.alan.routineos.ui.features.auth.navigation.AUTH_ROUTE
import com.alan.routineos.ui.features.auth.navigation.authScreen
import com.alan.routineos.ui.features.auth.navigation.navigateToAuth
import com.alan.routineos.ui.features.backup.navigation.backupScreen
import com.alan.routineos.ui.features.backup.navigation.navigateToBackup
import com.alan.routineos.ui.features.execute.navigation.executeScreen
import com.alan.routineos.ui.features.execute.navigation.navigateToExecute
import com.alan.routineos.ui.features.node_type_manager.navigation.nodeTypeManagerScreen
import com.alan.routineos.ui.features.onboarding.navigation.ONBOARDING_ROUTE
import com.alan.routineos.ui.features.onboarding.navigation.onboardingScreen
import com.alan.routineos.ui.features.stats.navigation.statsScreen
import com.alan.routineos.ui.features.system.navigation.systemScreen
import com.alan.routineos.ui.features.template_builder.navigation.navigateToTemplateBuilder
import com.alan.routineos.ui.features.template_builder.navigation.templateBuilderScreen
import com.alan.routineos.ui.features.today.navigation.todayScreen
import com.alan.routineos.ui.theme.ColorSurface

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startupViewModel: AppStartupViewModel = hiltViewModel()
) {
    val startupState by startupViewModel.state.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    // Use metadata from Screen object to decide UI state
    val currentScreen = Screen.fromRoute(currentRoute)
    val showBottomBar = currentScreen?.showBottomBar == true

    // Si todavía estamos cargando el estado inicial (DataStore), 
    // el Splash nativo se mantiene visible (configurado en MainActivity)
    if (startupState is AppStartupState.Loading) {
        return
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = ColorSurface,
                    tonalElevation = NavigationBarDefaults.Elevation
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected =
                            currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = {
                                screen.icon?.let { Icon(it, contentDescription = screen.title) }
                            },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = LAUNCH_ROUTE,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. Splash Animado (LaunchScreen) - Orchestration
            launchScreen(
                state = startupState,
                onFinish = { finalState ->
                    val navOptions = navOptions {
                        popUpTo(LAUNCH_ROUTE) { inclusive = true }
                    }
                    if (finalState is AppStartupState.ShowHome) {
                        navController.navigate(Screen.Today.route, navOptions)
                    } else {
                        navController.navigate(Screen.Onboarding.route, navOptions)
                    }
                }
            )

            // 2. Initial Flow Orchestration
            onboardingScreen(
                onFinish = {
                    navController.navigate(Screen.Today.route) {
                        popUpTo(ONBOARDING_ROUTE) { inclusive = true }
                    }
                }
            )

            authScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Today.route) {
                        popUpTo(AUTH_ROUTE) { inclusive = true }
                    }
                }
            )

            // 3. Feature Orchestration
            todayScreen(
                onNavigateToExecute = { nodeId ->
                    navController.navigateToExecute(nodeId)
                },
                onNavigateToTemplateBuilder = { templateId ->
                    navController.navigateToTemplateBuilder(templateId)
                }
            )

            executeScreen(
                onBack = { navController.popBackStack() }
            )

            systemScreen(
                onNavigateToBuilder = { templateId, initialName, initialColor ->
                    navController.navigateToTemplateBuilder(templateId, initialName, initialColor)
                }
            )

            templateBuilderScreen(
                onBack = { navController.popBackStack() },
                onNavigateToTypeManager = {
                    navController.navigate(Screen.NodeTypeManager.route)
                }
            )

            nodeTypeManagerScreen(
                onBack = { navController.popBackStack() }
            )

            statsScreen()

            backupScreen(
                onBack = { navController.popBackStack() }
            )

            accountScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    // Logout orchestration logic
                },
                onNavigateToAuth = {
                    navController.navigateToAuth()
                },
                onNavigateToBackup = {
                    navController.navigateToBackup()
                }
            )
        }
    }
}
