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
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alan.routineos.core.datastore.SettingsDataStore
import com.alan.routineos.ui.features.account.navigation.accountScreen
import com.alan.routineos.ui.features.auth.navigation.authScreen
import com.alan.routineos.ui.features.auth.viewmodel.AuthViewModel
import com.alan.routineos.ui.features.execute.navigation.executeScreen
import com.alan.routineos.ui.features.library.navigation.libraryScreen
import com.alan.routineos.ui.features.node_type_manager.navigation.nodeTypeManagerScreen
import com.alan.routineos.ui.features.onboarding.navigation.onboardingScreen
import com.alan.routineos.ui.features.planner.navigation.plannerScreen
import com.alan.routineos.ui.features.stats.navigation.statsScreen
import com.alan.routineos.ui.features.template_builder.navigation.templateBuilderScreen
import com.alan.routineos.ui.features.today.navigation.todayScreen
import com.alan.routineos.ui.theme.ColorSurface
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavViewModel @Inject constructor(
    val settingsDataStore: SettingsDataStore
) : ViewModel()

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    navViewModel: NavViewModel = hiltViewModel()
) {
    val isOnboardingCompleted by navViewModel.settingsDataStore.isOnboardingCompleted.collectAsState(
        initial = null
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    if (isOnboardingCompleted == null) return

    val startDestination = if (isOnboardingCompleted == true) Screen.Today.route else "onboarding"

    val currentRoute = currentDestination?.route
    val showBottomBar = bottomNavItems.any { it.route == currentRoute } &&
            currentRoute != Screen.Execute.route &&
            currentRoute != "onboarding" &&
            currentRoute != Screen.Auth.route

    val authViewModel: AuthViewModel = hiltViewModel()

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
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            onboardingScreen(
                onFinish = {
                    navController.navigate(Screen.Today.route) {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
            
            todayScreen(
                onNavigateToExecute = { nodeId ->
                    navController.navigate(Screen.Execute.createRoute(nodeId))
                }
            )
            
            plannerScreen()

            executeScreen(
                onBack = { navController.popBackStack() }
            )

            libraryScreen(
                onNavigateToBuilder = { templateId ->
                    navController.navigate(Screen.TemplateBuilder.createRoute(templateId))
                },
                onNavigateToTypes = {
                    navController.navigate(Screen.NodeTypeManager.route)
                }
            )

            templateBuilderScreen(
                onBack = { navController.popBackStack() },
                onNavigateToTypeManager = { navController.navigate(Screen.NodeTypeManager.route) }
            )

            nodeTypeManagerScreen(
                onBack = { navController.popBackStack() }
            )

            statsScreen()

            accountScreen(
                onBack = { navController.popBackStack() },
                onLogout = { 
                    authViewModel.logout()
                },
                onNavigateToAuth = { navController.navigate(Screen.Auth.route) }
            )

            authScreen(
                onLoginSuccess = {
                    navController.popBackStack()
                }
            )
        }
    }
}
