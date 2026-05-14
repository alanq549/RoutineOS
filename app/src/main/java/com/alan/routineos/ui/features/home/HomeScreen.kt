package com.alan.routineos.ui.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alan.routineos.ui.components.RoutineFAB
import com.alan.routineos.ui.components.RoutineTopBar
import com.alan.routineos.ui.components.TimelineHeader
import com.alan.routineos.ui.components.mockActivities
import com.alan.routineos.ui.theme.*
import com.alan.routineos.ui.features.auth.viewmodel.AuthViewModel
import com.alan.routineos.ui.features.account.viewmodel.UserViewModel
import com.alan.routineos.ui.features.account.state.UserState
import com.alan.routineos.ui.components.ActivityTimelineItem
import com.alan.routineos.ui.components.DailyProgressSection

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    onNavigateToAccount: () -> Unit
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
                onLogout = { authViewModel.logout() },
                onProfileClick = onNavigateToAccount
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
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(
            top = padding.calculateTopPadding() + 24.dp,
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
