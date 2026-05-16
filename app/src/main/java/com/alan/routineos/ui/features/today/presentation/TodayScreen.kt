package com.alan.routineos.ui.features.today.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.features.today.components.TimelineIndicator
import com.alan.routineos.ui.features.today.components.TodayActivityCard
import com.alan.routineos.ui.features.today.state.ResolvedNodeUi
import com.alan.routineos.ui.features.today.state.TimelineEntryUi
import com.alan.routineos.ui.features.today.viewmodel.TodayViewModel
import com.alan.routineos.ui.theme.ColorBg
import com.alan.routineos.ui.theme.ColorBorder
import com.alan.routineos.ui.theme.ColorExec
import com.alan.routineos.ui.theme.ColorPlan
import com.alan.routineos.ui.theme.ColorSurface
import com.alan.routineos.ui.theme.ColorText
import com.alan.routineos.ui.theme.ColorTextDim
import com.alan.routineos.ui.theme.MetaMono
import com.alan.routineos.ui.theme.TitleNode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onNavigateToExecute: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    // Datos de ejemplo para desarrollo
    val entries =
        if (uiState.timelineEntries.isEmpty()) exampleTimeline else uiState.timelineEntries

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

            // HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = uiState.dateLabel,
                        style = MaterialTheme.typography.headlineMedium,
                        color = ColorText,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val progress =
                        if (uiState.totalCount == 0) 0f else uiState.completedCount.toFloat() / uiState.totalCount
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(56.dp),
                            color = ColorPlan,
                            trackColor = ColorBorder,
                            strokeWidth = 4.dp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${uiState.completedCount}/${uiState.totalCount}",
                        style = MetaMono.copy(fontSize = 12.sp),
                        color = ColorTextDim
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // TIMELINE
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    if (entry.showTimeIndicatorBefore) {
                        TimelineIndicator()
                        Spacer(modifier = Modifier.height(4.dp))
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
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showAddSheet = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333333)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorTextDim)
                    ) {
                        Text("＋ Agregar actividad imprevista", style = TitleNode)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        if (showAddSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddSheet = false },
                containerColor = ColorSurface
            ) {
                var name by remember { mutableStateOf("") }
                Column(modifier = Modifier
                    .padding(24.dp)
                    .padding(bottom = 32.dp)) {
                    Text("Nueva Actividad", style = TitleNode, color = ColorText)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nombre") }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showAddSheet = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorExec)
                    ) {
                        Text("Agregar")
                    }
                }
            }
        }
    }
}

private val exampleTimeline = listOf(
    TimelineEntryUi(
        "1",
        "5:30 am",
        "Despertar",
        null,
        "completado",
        Color(0xFF4CAF50),
        Color(0xFF4CAF50)
    ),
    TimelineEntryUi(
        "2",
        "7:00 am – 2:00 pm",
        "Semestre 8",
        "5 bloques hoy",
        "en progreso",
        Color(0xFF42A5F5),
        Color(0xFF1565C0),
        showTimeIndicatorBefore = true,
        resolvedNodes = listOf(
            ResolvedNodeUi("Aplicaciones Empresariales", 0, "07:00"),
            ResolvedNodeUi("Desarrollo Móvil", 0, "09:00"),
            ResolvedNodeUi("Análisis Empresarial", 0, "10:00")
        )
    ),
    TimelineEntryUi(
        "3",
        "3:00 pm",
        "Snack",
        null,
        "pendiente",
        Color(0xFF555555),
        Color(0xFF424242)
    ),
    TimelineEntryUi(
        "4",
        "4:00 pm – 5:30 pm",
        "Push Day",
        "Pecho · Hombro · Tríceps",
        "pendiente",
        Color(0xFF555555),
        Color(0xFF424242),
        resolvedNodes = listOf(
            ResolvedNodeUi("Pecho", 0),
            ResolvedNodeUi("Press banca", 1, "4×8"),
            ResolvedNodeUi("Press inclinado", 1, "3×10"),
            ResolvedNodeUi("Hombro", 0),
            ResolvedNodeUi("Elevaciones laterales", 1, "3×15")
        )
    ),
    TimelineEntryUi(
        "5",
        "6:00 pm",
        "Tarea",
        null,
        "cancelado hoy",
        Color(0xFF424242),
        Color(0xFF757575),
        isCancelled = true
    ),
    TimelineEntryUi(
        "6",
        "10:30 pm",
        "Dormir",
        null,
        "pendiente",
        Color(0xFF555555),
        Color(0xFF424242)
    )
)