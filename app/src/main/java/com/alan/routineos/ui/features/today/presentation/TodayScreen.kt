package com.alan.routineos.ui.features.today.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.features.today.components.TimelineIndicator
import com.alan.routineos.ui.features.today.components.TodayActivityCard
import com.alan.routineos.ui.features.today.viewmodel.TodayViewModel
import com.alan.routineos.ui.theme.*

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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Lunes, 12",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ColorText,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "mayo",
                        style = MaterialTheme.typography.headlineSmall,
                        color = ColorText,
                        fontWeight = FontWeight.Light
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { 0.33f },
                            modifier = Modifier.size(56.dp),
                            color = ColorPlan,
                            trackColor = ColorBorder,
                            strokeWidth = 4.dp
                        )
                        Text(
                            text = "33%",
                            style = MetaMono.copy(fontSize = 12.sp),
                            color = ColorText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("PROGRESO", style = MetaMono, color = ColorTextDim)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    TodayActivityCard(
                        time = "5:30 am",
                        title = "Despertar",
                        statusLabel = "completado",
                        statusColor = ColorExec,
                        borderColor = ColorExec
                    )
                }

                item {
                    TimelineIndicator()
                }

                item {
                    TodayActivityCard(
                        time = "7:00 am - 2:00 pm",
                        title = "Semestre 8",
                        statusLabel = "en progreso",
                        statusColor = ColorPlan,
                        borderColor = ColorPlan,
                        subItems = listOf(
                            "07:00 Aplicaciones Empresariales",
                            "09:00 Desarrollo Móvil",
                            "10:00 Inteligencia de Negocios"
                        )
                    )
                }

                item {
                    TodayActivityCard(
                        time = "3:00 pm",
                        title = "Snack",
                        statusLabel = "pendiente",
                        statusColor = ColorTextDim,
                        borderColor = ColorBorder
                    )
                }

                item {
                    TodayActivityCard(
                        time = "4:00 pm - 5:30 pm",
                        title = "Push Day",
                        statusLabel = "pendiente",
                        statusColor = ColorTextDim,
                        borderColor = ColorBorder
                    )
                }

                item {
                    TodayActivityCard(
                        time = "6:00 pm - 8:00 pm",
                        title = "Tarea",
                        statusLabel = "cancelado hoy",
                        statusColor = ColorPending,
                        borderColor = ColorPending.copy(alpha = 0.5f)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ColorBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorText)
                    ) {
                        Text(" agregar actividad imprevista", style = TitleNode)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
