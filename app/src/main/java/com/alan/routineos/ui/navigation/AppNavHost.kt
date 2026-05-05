package com.alan.routineos.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alan.routineos.core.session.SessionManager
import com.alan.routineos.ui.components.NotificationOverlay
import com.alan.routineos.ui.events.UiEvent
import com.alan.routineos.ui.screens.AuthScreen
import com.alan.routineos.ui.screens.HomeScreen
import com.alan.routineos.ui.theme.BgDark
import com.alan.routineos.ui.viewmodel.AuthViewModel
import com.alan.routineos.ui.viewmodel.UserViewModel


@Composable
fun AppNavHost(
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    sessionManager: SessionManager,
) {

    val navController = rememberNavController()
    val session by sessionManager.session.collectAsState()

    var notification by remember { mutableStateOf<String?>(null) }

    // UI events SOLO visuales
    LaunchedEffect(Unit) {
        authViewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar,
                is UiEvent.ShowToast -> notification

                else -> Unit
            }
        }
    }

    // 🔥 navegación SOLO por sesión
    LaunchedEffect(session) {
        val target = if (session != null) "home" else "auth"

        navController.navigate(target) {
            popUpTo(0) // limpia todo stack
            launchSingleTop = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {

        NavHost(
            navController = navController,
            startDestination = "auth"
        ) {
            composable("auth") {
                AuthScreen(authViewModel)
            }

            composable("home") {
                HomeScreen(authViewModel, userViewModel,)
            }
        }

        NotificationOverlay(
            message = notification,
            onDismiss = { notification = null }
        )
    }
}