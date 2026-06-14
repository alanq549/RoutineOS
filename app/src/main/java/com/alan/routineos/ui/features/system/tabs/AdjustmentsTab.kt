package com.alan.routineos.ui.features.system.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.util.DateUtils
import com.alan.routineos.data.local.entities.RecurrenceType
import com.alan.routineos.data.local.entities.ScheduleException
import com.alan.routineos.ui.features.system.components.*
import com.alan.routineos.ui.features.system.state.SystemUiState
import com.alan.routineos.ui.theme.*

@Composable
fun AdjustmentsTab(
    state: SystemUiState,
    onDateSelected: (Long) -> Unit,
    onNextWeek: () -> Unit,
    onPrevWeek: () -> Unit,
    onCreateAdaptation: (String, Int, RecurrenceType) -> Unit,
    onDeleteAdaptation: (ScheduleException) -> Unit
) {
    var showNewAdaptation by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // SECCIÓN 1 — Week strip dinámico
        SystemWeekStrip(
            selectedDate = state.selectedDate,
            currentWeekStart = state.currentWeekStart,
            onDateSelected = onDateSelected,
            onNextWeek = onNextWeek,
            onPrevWeek = onPrevWeek
        )

        Spacer(modifier = Modifier.height(24.dp))

        // SECCIÓN 2 — Adaptaciones activas
        Text(
            text = "ADAPTACIONES ACTIVAS",
            style = MetaMono,
            color = ColorTextMuted,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (state.adaptations.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Tu semana está en modo normal.",
                        style = TitleNode.copy(fontSize = 14.sp),
                        color = ColorTextDim
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Agrega vacaciones, días ligeros o cambios temporales cuando tu rutina real cambie.",
                        style = MetaMono.copy(fontSize = 10.sp),
                        color = ColorTextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp)
            ) {
                items(state.adaptations, key = { it.id }) { adaptation ->
                    AdaptationCard(
                        dateRange = DateUtils.formatRange(adaptation.dateFrom, adaptation.dateTo),
                        title = adaptation.label,
                        type = mapIntentionToType(adaptation.label),
                        recurrence = adaptation.recurrenceType,
                        onDelete = { onDeleteAdaptation(adaptation) }
                    )
                }
            }
        }

        // SECCIÓN 3 — Botón agregar adaptación (Siempre visible al final)
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(56.dp)
                .clickable { showNewAdaptation = true }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRoundRect(
                    color = ColorBorder,
                    style = Stroke(
                        width = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
                )
            }
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "+ NUEVA ADAPTACIÓN",
                    style = MetaMono.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    color = ColorTextMuted
                )
            }
        }
    }

    if (showNewAdaptation) {
        NewAdaptationSheet(
            onDismiss = { showNewAdaptation = false },
            onConfirm = { label, rangeType, recurrenceType -> 
                onCreateAdaptation(label, rangeType, recurrenceType)
                showNewAdaptation = false 
            }
        )
    }
}

private fun mapIntentionToType(label: String): AdaptationType {
    return when {
        label.contains("Vacaciones", ignoreCase = true) -> AdaptationType.VACATION
        label.contains("Cancelado", ignoreCase = true) -> AdaptationType.CANCEL_DAY
        label.contains("Reprogramar", ignoreCase = true) -> AdaptationType.RESCHEDULE
        label.contains("Reducida", ignoreCase = true) -> AdaptationType.REDUCED
        else -> AdaptationType.SPECIAL
    }
}
