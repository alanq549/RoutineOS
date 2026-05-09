package com.alan.routineos.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Today : Screen("today", "Today", Icons.Default.Today)
    object Planner : Screen("planner", "Planner", Icons.Default.CalendarMonth)
    object Execute : Screen("execute/{nodeId}", "Execute", Icons.Default.PlayArrow) {
        fun createRoute(nodeId: String) = "execute/$nodeId"
    }
    object Library : Screen("library", "Library", Icons.Default.LibraryBooks)
    object Stats : Screen("stats", "Stats", Icons.Default.History)
    
    object Auth : Screen("auth", "Auth")
    object Account : Screen("account", "Cuenta", Icons.Default.Person)
}

val bottomNavItems = listOf(
    Screen.Today,
    Screen.Planner,
    Screen.Execute,
    Screen.Library,
    Screen.Stats,
)
