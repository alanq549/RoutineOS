package com.alan.routineos.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Screen metadata definition.
 * Used for BottomBar orchestration and global UI state.
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null,
    val showBottomBar: Boolean = false
) {
    object Today : Screen("today", "Today", Icons.Default.Today, showBottomBar = true)
    object Planner : Screen("planner", "Planner", Icons.Default.CalendarMonth, showBottomBar = true)
    object Execute : Screen("execute/{nodeId}", "Execute", Icons.Default.PlayArrow)
    object Library : Screen("library", "Library", Icons.Default.LibraryBooks, showBottomBar = true)
    object Stats : Screen("stats", "Stats", Icons.Default.History, showBottomBar = true)
    object Account : Screen("account", "Account", Icons.Default.AccountCircle, showBottomBar = true)
    
    object TemplateBuilder : Screen("library/builder/{templateId}", "Editor")
    object NodeTypeManager : Screen("settings/types", "Tipos de Actividad")
    
    object Auth : Screen("auth", "Auth")
    object Onboarding : Screen("onboarding", "Onboarding")
    object Launch : Screen("launch", "Launch")

    companion object {
        private val allScreens = listOf(
            Today, Planner, Execute, Library, Stats, Account, 
            TemplateBuilder, NodeTypeManager, Auth, Onboarding, Launch
        )
        
        /**
         * Resolves a Screen object from a route pattern.
         */
        fun fromRoute(route: String?): Screen? = allScreens.find { it.route == route }
    }
}

val bottomNavItems = listOf(
    Screen.Today,
    Screen.Planner,
    Screen.Library,
    Screen.Stats,
    Screen.Account
)
