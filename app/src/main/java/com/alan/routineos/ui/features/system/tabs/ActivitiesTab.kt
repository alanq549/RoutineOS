package com.alan.routineos.ui.features.system.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alan.routineos.ui.features.library.components.QuickCreateCard
import com.alan.routineos.ui.features.library.components.QuickCreateSheet
import com.alan.routineos.ui.features.library.components.SearchBar
import com.alan.routineos.ui.features.library.components.TemplateCard
import com.alan.routineos.ui.features.system.model.ExampleTemplate
import com.alan.routineos.ui.theme.*

@Composable
fun ActivitiesTab(
    onNavigateToBuilder: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showCreateSheet by remember { mutableStateOf(false) }

    val exampleActivities = listOf(
        ExampleTemplate("Semestre 8", "#2196F3", "6 materias · aula, profesor", listOf(0, 1, 2, 3, 4), "7:00–14:00"),
        ExampleTemplate("Push Day", "#F44336", "5 ejercicios · series, reps, peso", listOf(0, 3), null),
        ExampleTemplate("Pull Day", "#9C27B0", "5 ejercicios · series, reps, peso", listOf(1, 4), null),
        ExampleTemplate("Despertar", "#4CAF50", "sin nodos · hora fija", listOf(0, 1, 2, 3, 4, 5, 6), null),
        ExampleTemplate("Dormir", "#FF9800", "sin nodos · hora fija", listOf(0, 1, 2, 3, 4, 5, 6), null)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp)
        ) {
            items(exampleActivities) { item ->
                TemplateCard(
                    name = item.name,
                    colorHex = item.color,
                    metaSummary = item.meta,
                    activeDays = item.days,
                    timeRange = item.time,
                    onClick = { onNavigateToBuilder("id") },
                    onMoreClick = {}
                )
            }
            item {
                QuickCreateCard(onClick = { showCreateSheet = true })
            }
        }
    }

    if (showCreateSheet) {
        QuickCreateSheet(
            onDismiss = { showCreateSheet = false },
            onCreate = { _, _ ->
                showCreateSheet = false
                onNavigateToBuilder("new")
            }
        )
    }
}
