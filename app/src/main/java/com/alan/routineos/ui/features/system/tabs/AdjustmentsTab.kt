package com.alan.routineos.ui.features.system.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.features.system.components.*
import com.alan.routineos.ui.theme.*

@Composable
fun AdjustmentsTab() {
    var selectedDayIndex by remember { mutableIntStateOf(1) }
    var showNewAdjustment by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // SECCIÓN 1 — Week strip
        SystemWeekStrip(
            selectedDayIndex = selectedDayIndex,
            onDaySelected = { selectedDayIndex = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // SECCIÓN 2 — Excepciones activas
        Text(
            text = "AJUSTES ESTA SEMANA",
            style = MetaMono,
            color = ColorTextMuted,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp)
        ) {
            item {
                AdjustmentCard(
                    dateRange = "Lun 12 mayo",
                    title = "Semestre 8 — cancelado",
                    type = AdjustmentType.CANCEL_DAY,
                    onDelete = {}
                )
            }
            item {
                AdjustmentCard(
                    dateRange = "19–23 mayo",
                    title = "Vacaciones — toda la semana",
                    type = AdjustmentType.VACATION,
                    onDelete = {}
                )
            }
            item {
                AdjustmentCard(
                    dateRange = "Jue 15 mayo",
                    title = "Push Day → 18:00",
                    type = AdjustmentType.RESCHEDULE,
                    onDelete = {}
                )
            }

            // SECCIÓN 3 — Botón agregar ajuste
            item {
                Spacer(modifier = Modifier.height(12.dp))
                val borderColor = ColorBorder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable { showNewAdjustment = true }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRoundRect(
                            color = borderColor,
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
                            text = "+ NUEVO AJUSTE",
                            style = MetaMono.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = ColorTextMuted
                        )
                    }
                }
            }
        }
    }

    if (showNewAdjustment) {
        NewAdjustmentSheet(
            onDismiss = { showNewAdjustment = false },
            onConfirm = { _ -> showNewAdjustment = false }
        )
    }
}
