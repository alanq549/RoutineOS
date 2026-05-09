package com.alan.routineos.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alan.routineos.ui.screens.*
import com.alan.routineos.ui.state.AuthState
import com.alan.routineos.ui.theme.ColorSurface
import com.alan.routineos.ui.theme.NeonEmerald
import com.alan.routineos.ui.viewmodel.AuthViewModel
import com.alan.routineos.ui.viewmodel.UserViewModel

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val userViewModel: UserViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()

    val authState by authViewModel.authState.collectAsState()

    // Manejo de navegación global persistente
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Unauthenticated -> {
                if (currentDestination?.route != Screen.Auth.route) {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }

            is AuthState.Authenticated -> {
                if (currentDestination?.route == Screen.Auth.route) {
                    navController.navigate(Screen.Today.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            }
            else -> {}
        }
    }

    val showBottomBar = bottomNavItems.any { it.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar && authState is AuthState.Authenticated) {
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
        if (authState is AuthState.Loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonEmerald)
            }
        } else {
            NavHost(
                navController = navController,
                startDestination = if (authState is AuthState.Authenticated)
                    Screen.Today.route else Screen.Auth.route,
                modifier = Modifier.padding(innerPadding),
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                composable(Screen.Today.route) {
                    HomeScreen(
                        authViewModel = authViewModel,
                        userViewModel = userViewModel,
                        onNavigateToAccount = { navController.navigate(Screen.Account.route) }
                    )
                }
                composable(Screen.Planner.route) { PlannerScreen() }
                composable(Screen.Execute.route) { backStackEntry ->
                    val nodeId = backStackEntry.arguments?.getString("nodeId")
                    ExecuteScreen(nodeId)
                }
                composable(Screen.Library.route) { LibraryScreen() }
                composable(Screen.Stats.route) { StatsScreen() }

                composable(
                    route = Screen.Account.route,
                    enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
                ) {
                    AccountScreen(
                        userViewModel = userViewModel,
                        onBack = { navController.popBackStack() },
                        onLogout = { authViewModel.logout() }
                    )
                }

                composable(Screen.Auth.route) {
                    AuthScreen(
                        viewModel = authViewModel,
                        onLoginSuccess = {
                            navController.navigate(Screen.Today.route) {
                                popUpTo(Screen.Auth.route) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
