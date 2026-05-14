package com.alan.routineos.ui.features.library.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alan.routineos.ui.features.library.components.SearchBar
import com.alan.routineos.ui.features.library.components.TemplateCard
import com.alan.routineos.ui.features.library.viewmodel.LibraryViewModel
import com.alan.routineos.ui.theme.*

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onNavigateToBuilder: (String) -> Unit,
    onNavigateToTypes: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = ColorBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "LIBRERÍA",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ColorText,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.templates.size} definiciones",
                        style = MetaMono,
                        color = ColorTextDim
                    )
                }
                Text(
                    text = "+ Nueva",
                    style = TitleNode,
                    color = ColorPlan,
                    modifier = Modifier
                        .clickable { onNavigateToBuilder("new") }
                        .padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::updateSearchQuery
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "MIS DEFINICIONES",
                style = MetaMono,
                color = ColorTextDim
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ColorExec)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(uiState.templates, key = { it.id }) { template ->
                        TemplateCard(
                            template = template,
                            onEdit = { onNavigateToBuilder(template.id) }
                        )
                    }

                    item {
                        OutlinedButton(
                            onClick = { onNavigateToBuilder("new") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ColorBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorText)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("agregar definición", style = TitleNode)
                        }
                    }
                }
            }
        }
    }
}
