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
            // 1. RESUMEN PRINCIPAL (Debe leerse primero)
            uiState.weeklySummary?.let { weekly ->
                WeeklyProgressCard(
                    averageCompletionRate = weekly.averageCompletionRate,
                    totalSkippedCount = weekly.totalSkippedCount,
                    totalModifiedCount = weekly.totalModifiedCount,
                    hasEnoughData = uiState.hasEnoughWeeklyData,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            uiState.consistency?.let { consistency ->
                ConsistencyCard(
                    consistency = consistency,
                    hasEnoughData = uiState.hasAnyActivityData,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 2. EXPLORADOR INTEGRADO (Acceso rápido a actividad humana)
            Text(
                text = "ANÁLISIS DE ACTIVIDAD",
                style = MetaMono.copy(fontSize = 9.sp, letterSpacing = 1.sp),
                color = ColorTextMuted
            )
            Spacer(modifier = Modifier.height(12.dp))

            InlineActivityExplorer(
                query = uiState.nodeSearchQuery,
                onQueryChange = viewModel::updateSearchQuery,
                selectedActivity = uiState.selectedActivity,
                onSelectActivity = viewModel::selectActivity,
                availableActivities = uiState.availableActivities,
                onOpenFullList = { showNodePicker = true }
            )

            // 3. ACTIVITY DETAIL (Deep dive)
            if (uiState.selectedActivity != null && uiState.activityDetail != null) {
                Spacer(modifier = Modifier.height(24.dp))
                ActivityDetailCard(
                    activityName = uiState.selectedActivity!!.displayName,
                    stats = uiState.activityDetail!!,
                    hasEnoughHistory = uiState.hasEnoughActivityHistory,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 4. EVOLUCIÓN DINÁMICA (Si hay selección y campos de métricas)
            if (uiState.selectedActivity != null) {
                Spacer(modifier = Modifier.height(24.dp))
                
                if (uiState.availableFields.isNotEmpty()) {
                    CustomSegmentedControl(
                        options = uiState.availableFields.map { it.fieldLabel },
                        selectedIndex = uiState.availableFields.indexOf(uiState.selectedField),
                        onOptionSelected = { viewModel.selectField(uiState.availableFields[it]) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(ColorSurface, RoundedCornerShape(16.dp))
                        .padding(vertical = 24.dp, horizontal = 4.dp)
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
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 5. HEATMAP REAL (Evolución de aportaciones)
            ContributionHeatmapCard(
                heatmapData = uiState.heatmapData,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 6. INSIGHTS OPERATIVOS
            Text(
                text = "INSIGHTS OPERATIVOS",
                style = MetaMono.copy(fontSize = 9.sp, letterSpacing = 1.sp),
                color = ColorTextMuted
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.dailyInsights.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.dailyInsights.take(2).forEach { insight ->
                        InsightCard(insight = insight, modifier = Modifier.fillMaxWidth())
                    }
                }
            } else {
                Text(
                    text = "Aún no hay suficientes datos para generar insights.",
                    style = MetaMono.copy(fontSize = 11.sp),
                    color = ColorTextMuted
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 7. FRICCIÓN (Ajustes)
            MostAdjustedActivitiesCard(
                activities = uiState.mostAdjustedActivities,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 8. DURACIÓN PLANEADA VS REAL (Solo si hay datos de hoy)
            uiState.dailySummary?.let { daily ->
                if (daily.actualDurationMinutes > 0) {
                    PlannedVsActualCard(
                        plannedDurationMinutes = daily.plannedDurationMinutes,
                        actualDurationMinutes = daily.actualDurationMinutes,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        if (showNodePicker) {
            NodePickerSheet(
                activities = uiState.availableActivities,
                searchQuery = uiState.nodeSearchQuery,
                onSearchChange = viewModel::updateSearchQuery,
                onDismiss = { showNodePicker = false },
                onSelect = {
                    viewModel.selectActivity(it)
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
                text = "CÓMO SE COMPORTAN TUS DÍAS",
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
