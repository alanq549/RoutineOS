package com.alan.routineos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alan.routineos.data.local.entities.RoutineTemplate
import com.alan.routineos.ui.theme.ColorBg
import com.alan.routineos.ui.theme.ColorBorder
import com.alan.routineos.ui.theme.ColorExec
import com.alan.routineos.ui.theme.ColorPlan
import com.alan.routineos.ui.theme.ColorSurface
import com.alan.routineos.ui.theme.ColorText
import com.alan.routineos.ui.theme.ColorTextDim
import com.alan.routineos.ui.theme.MetaMono
import com.alan.routineos.ui.theme.TitleNode
import com.alan.routineos.ui.viewmodel.LibraryViewModel

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
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

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    Surface(
        color = ColorSurface,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (query.isEmpty()) "Buscar actividad..." else query,
                style = TitleNode.copy(fontSize = 16.sp),
                color = if (query.isEmpty()) ColorTextDim else ColorText,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TemplateCard(
    template: RoutineTemplate,
    onEdit: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        color = ColorSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = try {
                                Color(android.graphics.Color.parseColor(template.colorHex))
                            } catch (e: Exception) {
                                ColorPlan
                            },
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(template.name, style = TitleNode.copy(fontSize = 16.sp), color = ColorText)
                    Text(
                        "6 materias · metadatos: aula, profesor", // Placeholder as requested "que se vean asi"
                        style = MetaMono,
                        color = ColorTextDim
                    )
                }
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = ColorPlan,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val days = listOf("L", "Ma", "Mi", "J", "V", "S", "D")
                // In a real app we'd check template.daysOfWeek, for now let's just highlight some to match img
                days.forEachIndexed { index, day ->
                    val isActive = index < 5 // Placeholder: highlight first 5 days
                    DayBadge(day, isActive)
                }
            }
        }
    }
}

@Composable
fun DayBadge(day: String, isActive: Boolean) {
    Box(
        modifier = Modifier
            .size(32.dp, 24.dp)
            .background(
                color = if (isActive) ColorPlan.copy(alpha = 0.2f) else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
            .border(
                width = 1.dp,
                color = if (isActive) ColorPlan else ColorBorder,
                shape = RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day,
            style = MetaMono.copy(fontSize = 10.sp),
            color = if (isActive) ColorPlan else ColorTextDim
        )
    }
}
