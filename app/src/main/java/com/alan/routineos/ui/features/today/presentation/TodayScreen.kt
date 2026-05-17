package com.alan.routineos.ui.features.today.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.features.today.components.TimelineIndicator
import com.alan.routineos.ui.features.today.components.TodayActivityCard
import com.alan.routineos.ui.features.today.state.ResolvedNodeUi
import com.alan.routineos.ui.features.today.state.TimelineEntryUi
import com.alan.routineos.ui.features.today.viewmodel.TodayViewModel
import com.alan.routineos.ui.theme.*

/**
 * UI REFACTOR: Today Screen
 * Focused on an operational temporal timeline.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onNavigateToExecute: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    // Mock data for UI development
    val entries = if (uiState.timelineEntries.isEmpty()) exampleTimeline else uiState.timelineEntries

    Scaffold(
        containerColor = ColorBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // COMPACT HEADER
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
                        text = "MAYO 2024",
                        style = MetaMono.copy(fontSize = 10.sp, letterSpacing = 2.sp),
                        color = ColorTextDim
                    )
                }

                // Compact Progress
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${uiState.completedCount}/${uiState.totalCount}",
                            style = MetaMono.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = ColorText
                        )
                        Text(
                            text = "COMPLETADO",
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

            // CONTINUOUS TIMELINE
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    if (entry.showTimeIndicatorBefore) {
                        TimelineIndicator()
                    }
                    TodayActivityCard(
                        time = entry.time,
                        title = entry.title,
                        subtitle = entry.subtitle,
                        statusLabel = entry.statusLabel,
                        statusColor = entry.statusColor,
                        barColor = entry.barColor,
                        isCancelled = entry.isCancelled,
                        hasConflict = entry.hasConflict,
                        resolvedNodes = entry.resolvedNodes
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        OutlinedButton(
                            onClick = { showAddSheet = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ColorBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorTextDim)
                        ) {
                            Text("＋ AGREGAR ACTIVIDAD", style = TitleNode.copy(fontSize = 11.sp, letterSpacing = 1.sp))
                        }
                    }
                }
            }
        }

        if (showAddSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddSheet = false },
                containerColor = ColorSurface
            ) {
                QuickAddSheetContent(onAdd = { showAddSheet = false })
            }
        }
    }
}

@Composable
private fun QuickAddSheetContent(onAdd: () -> Unit) {
    var name by remember { mutableStateOf("") }
    Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp)) {
        Text("NUEVA ACTIVIDAD", style = MetaMono, color = ColorTextDim)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("¿Qué vas a hacer?") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ColorExec,
                unfocusedBorderColor = ColorBorder
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAdd,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("AGREGAR AL TIMELINE")
        }
    }
}

private val exampleTimeline = listOf(
    TimelineEntryUi("1","05:30 AM","Despertar",null,"completado", Color(0xFF4CAF50), Color(0xFF4CAF50)),
    TimelineEntryUi("2","07:00 AM","Semestre 8","5 bloques hoy", "en progreso", Color(0xFF42A5F5), Color(0xFF1565C0),
        showTimeIndicatorBefore = true,
        resolvedNodes = listOf(
            ResolvedNodeUi("Aplicaciones Empresariales",0,"07:00"),
            ResolvedNodeUi("Desarrollo Móvil",0,"09:00"),
            ResolvedNodeUi("Análisis Empresarial",0,"10:00")
        )),
    TimelineEntryUi("3","03:00 PM","Snack",null,"pendiente", Color(0xFF555555), Color(0xFF424242)),
    TimelineEntryUi("4","04:00 PM","Push Day", "Pecho · Hombro · Tríceps","pendiente", Color(0xFF555555), Color(0xFF424242),
        resolvedNodes = listOf(
            ResolvedNodeUi("Pecho",0),
            ResolvedNodeUi("Press banca",1,"4×8"),
            ResolvedNodeUi("Press inclinado",1,"3×10"),
            ResolvedNodeUi("Hombro",0),
            ResolvedNodeUi("Elevaciones laterales",1,"3×15")
        )),
    TimelineEntryUi("5","06:00 PM","Tarea",null,"cancelado hoy", Color(0xFF424242), Color(0xFF757575), isCancelled = true),
    TimelineEntryUi("6","10:30 PM","Dormir",null,"pendiente", Color(0xFF555555), Color(0xFF424242))
)
