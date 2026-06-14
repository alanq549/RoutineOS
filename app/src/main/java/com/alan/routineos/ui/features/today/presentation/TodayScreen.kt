package com.alan.routineos.ui.features.today.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.features.today.components.TimelineIndicator
import com.alan.routineos.ui.features.today.components.TodayActivityCard
import com.alan.routineos.ui.features.today.viewmodel.TodayViewModel
import com.alan.routineos.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onNavigateToExecute: (String) -> Unit,
    onNavigateToTemplateBuilder: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        containerColor = ColorBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToTemplateBuilder("new") },
                containerColor = ColorExec,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nueva Actividad",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
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
                            progress = { if (uiState.totalCount == 0) 0f else uiState.completedCount.toFloat() / uiState.totalCount.toFloat() },
                            modifier = Modifier.size(32.dp),
                            color = ColorPlan,
                            trackColor = ColorBorder.copy(alpha = 0.3f),
                            strokeWidth = 3.dp
                        )
                    }
                }
            }

            // BANNER DE EXCEPCIONES (PLANNER INTELIGENTE 12.1)
            AnimatedVisibility(visible = uiState.activeExceptions.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    uiState.activeExceptions.forEach { ex ->
                        Surface(
                            color = ColorPlan.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorPlan.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, null, tint = ColorPlan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "RUTINA MODIFICADA POR: ${ex.label.uppercase()}",
                                    style = MetaMono.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                    color = ColorPlan
                                )
                            }
                        }
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
                        Text(
                            text = if (uiState.activeExceptions.isNotEmpty()) "Día de excepción" else "Día tranquilo",
                            style = TitleNode, 
                            color = ColorText
                        )
                        Text(
                            text = if (uiState.activeExceptions.isNotEmpty()) "Tus rutinas habituales están pausadas." else "No hay actividades para hoy", 
                            style = MetaMono, 
                            color = ColorTextDim,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp)
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
                            isSkipped = entry.isSkipped,
                            hasConflict = entry.hasConflict,
                            isCurrent = entry.isCurrent,
                            wasShiftedByDomino = entry.wasShiftedByDomino,
                            dominoReason = entry.dominoReason,
                            conflictResolutionSuggestions = entry.conflictResolutionSuggestions,
                            planningInfo = entry.planningInfo,
                            resolvedNodes = entry.resolvedNodes,
                            onNodeToggle = { nodeId -> viewModel.toggleNodeCompletion(nodeId) },
                            onNodeClick = { nodeId -> onNavigateToExecute(nodeId) },
                            onComplete = { viewModel.toggleNodeCompletion(entry.id) },
                            onSkip = { nodeId -> viewModel.skipNode(nodeId) },
                            onPostpone = { nodeId, mins -> viewModel.postponeNode(nodeId, mins) },
                            onReschedule = { nodeId -> viewModel.rescheduleNode(nodeId, "18:00") }, // Placeholder
                            onDurationChange = { nodeId, mins -> viewModel.changeDuration(nodeId, mins) },
                            onPlanningToggle = { taskId -> viewModel.togglePlanningTask(taskId) },
                            onResolveConflict = { resolution -> viewModel.resolveConflict(entry.id, resolution) }
                        )
                    }
                }
            }
        }
    }
}
