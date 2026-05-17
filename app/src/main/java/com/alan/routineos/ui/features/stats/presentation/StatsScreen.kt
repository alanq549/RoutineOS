package com.alan.routineos.ui.features.stats.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.features.stats.components.*
import com.alan.routineos.ui.features.stats.viewmodel.StatsViewModel
import com.alan.routineos.ui.theme.*

/**
 * STATS SCREEN — REFACTOR VISUAL/CONCEPTUAL
 * Focused on personal historical evolution and longitudinal progress.
 * Design inspiration: Linear, Raycast, Cron.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showNodePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            StatsTopBar()
        },
        containerColor = ColorBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // 1. CORE HUMAN METRICS (Integrated hierarchy)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    label = "Consistencia",
                    value = "${(uiState.completionRate * 100).toInt()}%",
                    trend = "↑ 4%",
                    modifier = Modifier.weight(1f)
                )
                MetricItem(
                    label = "Racha Actual",
                    value = "${uiState.currentStreak}D",
                    color = ColorPending,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 2. ACTIVITY EXPLORER (Selection as exploration)
            Text(
                text = "ANÁLISIS DE ACTIVIDAD", 
                style = MetaMono.copy(fontSize = 9.sp, letterSpacing = 1.sp), 
                color = ColorTextMuted
            )
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showNodePicker = true },
                color = ColorSurface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp), 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.selectedNode?.name ?: "Explorar rutina...", 
                            style = TitleNode.copy(fontSize = 15.sp), 
                            color = if (uiState.selectedNode == null) ColorTextMuted else ColorText
                        )
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = ColorTextMuted, modifier = Modifier.size(16.dp))
                }
            }

            // 3. DYNAMIC EVOLUTION SECTION
            if (uiState.selectedNode != null) {
                Spacer(modifier = Modifier.height(32.dp))
                
                // Field Selector using custom Premium Pills/Segmented Control
                if (uiState.availableFields.isNotEmpty()) {
                    CustomSegmentedControl(
                        options = uiState.availableFields.map { it.fieldLabel },
                        selectedIndex = uiState.availableFields.indexOf(uiState.selectedField),
                        onOptionSelected = { viewModel.selectField(uiState.availableFields[it]) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Organic Integrated Chart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(ColorSurface, RoundedCornerShape(16.dp))
                        .padding(vertical = 32.dp, horizontal = 4.dp)
                ) {
                    if (uiState.selectedField != null && uiState.dataPoints.size >= 2) {
                        SparklineChart(
                            dataPoints = uiState.dataPoints,
                            modifier = Modifier.fillMaxSize(),
                            color = ColorPlan
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Sin datos suficientes",
                                style = TitleNode.copy(fontSize = 13.sp),
                                color = ColorTextMuted
                            )
                            Text(
                                text = "Se requieren al menos 2 registros",
                                style = MetaMono.copy(fontSize = 8.sp),
                                color = ColorTextMuted
                            )
                        }
                    }
                }
                
                if (uiState.dataPoints.size >= 2) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "EVOLUCIÓN BASADA EN ÚLTIMOS ${uiState.dataPoints.size} REGISTROS",
                        style = MetaMono.copy(fontSize = 8.sp, letterSpacing = 1.sp),
                        color = ColorTextMuted,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            } else {
                // Empty state or summary view
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "SELECCIONA UNA ACTIVIDAD PARA VER SU PROGRESIÓN",
                        style = MetaMono.copy(fontSize = 9.sp, letterSpacing = 0.5.sp),
                        color = ColorTextMuted
                    )
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

@Composable
private fun StatsTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "EVOLUCIÓN",
                style = TitleNode.copy(fontSize = 14.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold),
                color = ColorText
            )
            Text(
                text = "HISTORIAL OPERATIVO",
                style = MetaMono.copy(fontSize = 10.sp, color = ColorTextDim)
            )
        }
        Surface(
            color = ColorSurface,
            shape = CircleShape,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder),
            onClick = { /* General system history */ }
        ) {
            Icon(
                Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.padding(8.dp).size(16.dp),
                tint = ColorTextDim
            )
        }
    }
}
