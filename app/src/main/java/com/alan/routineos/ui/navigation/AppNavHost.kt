package com.alan.routineos.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.alan.routineos.ui.screens.*
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
    val isOnboardingCompleted by navViewModel.settingsDataStore.isOnboardingCompleted.collectAsState(initial = null)
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    if (isOnboardingCompleted == null) return

    val startDestination = if (isOnboardingCompleted == true) Screen.Today.route else "onboarding"

    val currentRoute = currentDestination?.route
    val showBottomBar = bottomNavItems.any { it.route == currentRoute } &&
            currentRoute != Screen.Execute.route &&
            currentRoute != "onboarding"

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = ColorSurface,
                    tonalElevation = NavigationBarDefaults.Elevation
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
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
            composable("onboarding") {
                OnboardingScreen(onFinish = {
                    navController.navigate(Screen.Today.route) {
                        popUpTo("onboarding") { inclusive = true }
                    }
                })
            }
            composable(Screen.Today.route) { 
                TodayScreen(onNavigateToExecute = { nodeId ->
                    navController.navigate(Screen.Execute.createRoute(nodeId))
                }) 
            }
            composable(Screen.Planner.route) { PlannerScreen() }
            composable(Screen.Execute.route) {
                ExecuteScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Library.route) { 
                LibraryScreen(
                    onNavigateToBuilder = { templateId ->
                        navController.navigate(Screen.TemplateBuilder.createRoute(templateId))
                    },
                    onNavigateToTypes = {
                        navController.navigate(Screen.NodeTypeManager.route)
                    }
                ) 
            }
            composable(Screen.TemplateBuilder.route) {
                TemplateBuilderScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToTypeManager = { navController.navigate(Screen.NodeTypeManager.route) }
                )
            }
            composable(Screen.NodeTypeManager.route) {
                NodeTypeManagerScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Stats.route) { StatsScreen() }
        }
    }
}
