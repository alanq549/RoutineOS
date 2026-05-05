package com.alan.routineos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alan.routineos.ui.components.RoutineFAB
import com.alan.routineos.ui.components.RoutineTopBar
import com.alan.routineos.ui.components.TimelineHeader
import com.alan.routineos.ui.components.mockActivities
import com.alan.routineos.ui.theme.*
import com.alan.routineos.ui.viewmodel.AuthViewModel
import androidx.compose.foundation.lazy.items // IMPORTANTE para el LazyColumn
import com.alan.routineos.ui.components.ActivityTimelineItem
import com.alan.routineos.ui.components.DailyProgressSection
import com.alan.routineos.ui.state.UserState
import com.alan.routineos.ui.viewmodel.UserViewModel


@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel
) {

    val userState by userViewModel.userState.collectAsState()

    val user = when (val state = userState) {
        is UserState.Success -> state.user
        else -> null
    }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            RoutineTopBar(
                userName = user?.name,
                onLogout = { authViewModel.logout() }
            )
        },
        floatingActionButton = {
            RoutineFAB(onClick = { /* ... */ })
        }
    ) { innerPadding ->
        HomeScreenContent(innerPadding)
    }
}

@Composable
private fun HomeScreenContent(padding: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            // ❌ QUITAMOS el padding(padding) de aquí
            .padding(horizontal = 24.dp),

        // ✅ PASAMOS el padding al contentPadding
        // Esto hace que la lista empiece debajo de la TopBar,
        // pero que al hacer scroll, los items suban y se vean tras el cristal.
        contentPadding = PaddingValues(
            top = padding.calculateTopPadding() + 24.dp, // El espacio de la TopBar + aire extra
            bottom = 100.dp
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { DailyProgressSection(completed = 3, total = 5) }

        item { TimelineHeader() }

        items(mockActivities) { activity ->
            ActivityTimelineItem(activity = activity)
        }
    }
}