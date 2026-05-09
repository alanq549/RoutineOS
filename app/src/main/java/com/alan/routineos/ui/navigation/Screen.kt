package com.alan.routineos.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Today : Screen("today", "Today", Icons.Default.Today)
    object Planner : Screen("planner", "Planner", Icons.Default.CalendarMonth)
    object Execute : Screen("execute/{nodeId}", "Execute", Icons.Default.PlayArrow) {
        fun createRoute(nodeId: String) = "execute/$nodeId"
    }
    object Library : Screen("library", "Library", Icons.Default.LibraryBooks)
    object Stats : Screen("stats", "Stats", Icons.Default.History)
    
    // Screens without bottom nav
    object TemplateBuilder : Screen("library/builder/{templateId}", "Editor") {
        fun createRoute(templateId: String = "new") = "library/builder/$templateId"
    }
    object NodeTypeManager : Screen("settings/types", "Tipos de Actividad")
    
    object Auth : Screen("auth", "Auth")
}

val bottomNavItems = listOf(
    Screen.Today,
    Screen.Planner,
    Screen.Library,
    Screen.Stats
)
