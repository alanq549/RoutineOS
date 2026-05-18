package com.alan.routineos.ui.features.today.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.features.today.components.TimelineIndicator
import com.alan.routineos.ui.features.today.components.TodayActivityCard
import com.alan.routineos.ui.features.today.viewmodel.TodayViewModel
import com.alan.routineos.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onNavigateToExecute: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = ColorBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // HEADER DINÁMICO
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = uiState.dateLabel,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = ColorText
                    )
                    Text(
                        text = uiState.monthLabel,
                        style = MetaMono.copy(fontSize = 10.sp, letterSpacing = 2.sp),
                        color = ColorTextDim
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${uiState.completedCount}/${uiState.totalCount}",
                            style = MetaMono.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = ColorText
                        )
                        Text(
                            text = "PASOS",
                            style = MetaMono.copy(fontSize = 7.sp),
                            color = ColorTextDim
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { if (uiState.totalCount == 0) 0f else uiState.completedCount.toFloat() / uiState.totalCount },
                            modifier = Modifier.size(32.dp),
                            color = ColorPlan,
                            trackColor = ColorBorder.copy(alpha = 0.3f),
                            strokeWidth = 3.dp
                        )
                    }
                }
            }

            // LÍNEA DE TIEMPO REAL
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ColorExec)
                }
            } else if (uiState.timelineEntries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(40.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Día tranquilo", style = TitleNode, color = ColorText)
                        Text("No hay actividades para hoy", style = MetaMono, color = ColorTextDim)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(uiState.timelineEntries, key = { it.id }) { entry ->
                        if (entry.showTimeIndicatorBefore) {
                            TimelineIndicator()
                        }
                        TodayActivityCard(
                            id = entry.id,
                            time = entry.time,
                            title = entry.title,
                            subtitle = entry.subtitle,
                            statusLabel = entry.statusLabel,
                            statusColor = entry.statusColor,
                            barColor = entry.barColor,
                            isCancelled = entry.isCancelled,
                            hasConflict = entry.hasConflict,
                            resolvedNodes = entry.resolvedNodes,
                            onNodeToggle = { nodeId -> viewModel.toggleNodeCompletion(nodeId) },
                            onComplete = { viewModel.toggleNodeCompletion(entry.id) }
                        )
                    }
                }
            }
        }
    }
}
