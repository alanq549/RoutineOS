package com.alan.routineos.ui.features.library.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alan.routineos.ui.features.library.components.QuickCreateCard
import com.alan.routineos.ui.features.library.components.QuickCreateSheet
import com.alan.routineos.ui.features.library.components.SearchBar
import com.alan.routineos.ui.features.library.components.TemplateCard
import com.alan.routineos.ui.features.library.viewmodel.LibraryViewModel
import com.alan.routineos.ui.theme.ColorBg
import com.alan.routineos.ui.theme.ColorText
import com.alan.routineos.ui.theme.ColorTextDim
import com.alan.routineos.ui.theme.MetaMono
import com.alan.routineos.ui.theme.TitleNode

data class ExampleTemplate(
    val name: String,
    val color: String,
    val meta: String,
    val days: List<Int>,
    val time: String?
)

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onNavigateToBuilder: (String) -> Unit,
    onNavigateToTypes: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateSheet by remember { mutableStateOf(false) }

    val exampleTemplates = listOf(
        ExampleTemplate(
            "Semestre 8",
            "#2196F3",
            "6 materias · aula, profesor",
            listOf(0, 1, 2, 3, 4),
            "7:00 – 14:00"
        ),
        ExampleTemplate(
            "Push Day",
            "#F44336",
            "5 ejercicios · series, reps, peso",
            listOf(0, 3),
            null
        ),
        ExampleTemplate(
            "Despertar",
            "#4CAF50",
            "sin nodos · hora fija",
            listOf(0, 1, 2, 3, 4, 5, 6),
            null
        ),
        ExampleTemplate(
            "Dormir",
            "#FF9800",
            "sin nodos · hora fija",
            listOf(0, 1, 2, 3, 4, 5, 6),
            null
        ),
        ExampleTemplate("Ciclismo", "#9C27B0", "1 nodo · km, tiempo", listOf(6), null)
    )

    Scaffold(
        containerColor = ColorBg,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("LIBRERÍA", style = TitleNode, fontWeight = FontWeight.Bold, color = ColorText)
                Text(
                    "${uiState.templates.size.coerceAtLeast(exampleTemplates.size)} definiciones",
                    style = MetaMono,
                    color = ColorTextDim
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::updateSearchQuery
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Si la DB está vacía usamos los ejemplos para la UI Task
                val listToShow =
                    if (uiState.templates.isEmpty()) exampleTemplates else uiState.templates.map {
                        ExampleTemplate(
                            it.name,
                            it.colorHex,
                            "Definición activa",
                            emptyList(),
                            null
                        )
                    }

                items(listToShow) { template ->
                    TemplateCard(
                        name = template.name,
                        colorHex = template.color,
                        metaSummary = template.meta,
                        activeDays = template.days,
                        timeRange = template.time,
                        onClick = { /* onNavigateToBuilder(id) */ },
                        onMoreClick = { /* Menu */ }
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
                onCreate = { name, color ->
                    showCreateSheet = false
                    onNavigateToBuilder("new") // En un flujo real pasaríamos name/color vía ViewModel o Params
                }
            )
        }
    }
}