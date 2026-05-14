package com.alan.routineos.ui.features.stats.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alan.routineos.ui.features.stats.components.MetricCard
import com.alan.routineos.ui.features.stats.components.NodePickerSheet
import com.alan.routineos.ui.features.stats.components.SparklineChart
import com.alan.routineos.ui.features.stats.viewmodel.StatsViewModel
import com.alan.routineos.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showNodePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ESTADÍSTICAS", style = MetaMono) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = ColorSurface)
            )
        },
        containerColor = ColorBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Summary Cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    label = "CUMPLIMIENTO",
                    value = "${(uiState.completionRate * 100).toInt()}%",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "RACHA ACTUAL",
                    value = "${uiState.currentStreak} DÍAS",
                    color = ColorPending,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Node & Field Selector
            Text("PROGRESO HISTÓRICO", style = MetaMono, color = ColorTextDim)
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showNodePicker = true },
                color = ColorSurface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ColorBorder)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Actividad", style = MetaMono, color = ColorTextMuted)
                        Text(
                            uiState.selectedNode?.name ?: "Seleccionar actividad...", 
                            style = TitleNode, 
                            color = if (uiState.selectedNode == null) ColorTextMuted else ColorText
                        )
                    }
                    Icon(Icons.Default.Search, contentDescription = null, tint = ColorTextDim)
                }
            }

            if (uiState.availableFields.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.availableFields.forEach { field ->
                        FilterChip(
                            selected = uiState.selectedField?.id == field.id,
                            onClick = { viewModel.selectField(field) },
                            label = { Text(field.fieldLabel, style = MetaMono) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ColorExec.copy(alpha = 0.2f),
                                selectedLabelColor = ColorExec
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Chart
            if (uiState.selectedField != null) {
                if (uiState.dataPoints.size >= 2) {
                    SparklineChart(
                        dataPoints = uiState.dataPoints,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(ColorSurface, RoundedCornerShape(12.dp))
                            .border(1.dp, ColorBorder, RoundedCornerShape(12.dp))
                            .padding(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Mostrando últimos ${uiState.dataPoints.size} registros", 
                        style = MetaMono, 
                        color = ColorTextMuted,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(ColorSurface, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Necesitas al menos 2 registros para ver el gráfico", color = ColorTextMuted, style = TitleNode)
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        if (showNodePicker) {
            NodePickerSheet(
                nodes = uiState.availableNodes,
                searchQuery = uiState.nodeSearchQuery,
                onSearchChange = viewModel::updateSearchQuery,
                onDismiss = { showNodePicker = false },
                onSelect = {
                    viewModel.selectNode(it)
                    showNodePicker = false
                }
            )
        }
    }
}
