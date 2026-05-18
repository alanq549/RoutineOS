package com.alan.routineos.ui.features.system.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alan.routineos.ui.features.library.components.QuickCreateCard
import com.alan.routineos.ui.features.library.components.QuickCreateSheet
import com.alan.routineos.ui.features.library.components.SearchBar
import com.alan.routineos.ui.features.library.components.TemplateCard
import com.alan.routineos.ui.features.library.viewmodel.LibraryViewModel
import com.alan.routineos.ui.theme.*

@Composable
fun ActivitiesTab(
    viewModel: LibraryViewModel = hiltViewModel(),
    onNavigateToBuilder: (String, String?, String?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateSheet by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::updateSearchQuery
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ColorExec)
            }
        } else if (uiState.definitions.isEmpty() && uiState.searchQuery.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Aún no tienes actividades. Crea la primera.",
                        style = TitleNode,
                        color = ColorTextDim
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    QuickCreateCard(onClick = { showCreateSheet = true })
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp)
            ) {
                items(uiState.definitions, key = { it.id }) { def ->
                    TemplateCard(
                        name = def.name,
                        colorHex = def.colorHex,
                        metaSummary = def.blocksSummary,
                        activeDays = def.activeDays,
                        timeRange = def.timeLabel,
                        onClick = { onNavigateToBuilder(def.id, null, null) },
                        onEdit = { onNavigateToBuilder(def.id, null, null) },
                        onDelete = { viewModel.deleteTemplate(def.id) }
                    )
                }
                item {
                    QuickCreateCard(onClick = { showCreateSheet = true })
                }
            }
        }
    }

    if (showCreateSheet) {
        QuickCreateSheet(
            onDismiss = { showCreateSheet = false },
            onCreate = { name, color ->
                showCreateSheet = false
                onNavigateToBuilder("new", name, color)
            }
        )
    }
}
