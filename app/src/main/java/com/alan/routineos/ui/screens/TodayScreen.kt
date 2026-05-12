package com.alan.routineos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alan.routineos.ui.theme.ColorBg
import com.alan.routineos.ui.theme.ColorBorder
import com.alan.routineos.ui.theme.ColorExec
import com.alan.routineos.ui.theme.ColorPending
import com.alan.routineos.ui.theme.ColorPlan
import com.alan.routineos.ui.theme.ColorSurface
import com.alan.routineos.ui.theme.ColorText
import com.alan.routineos.ui.theme.ColorTextDim
import com.alan.routineos.ui.theme.MetaMono
import com.alan.routineos.ui.theme.TitleNode
import com.alan.routineos.ui.viewmodel.TodayViewModel

@Composable
fun TodayScreen(
    viewModel: TodayViewModel = hiltViewModel(),
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
                        shape = RoundedCornerShape(8.dp),
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

@Composable
fun TodayActivityCard(
    time: String,
    title: String,
    statusLabel: String,
    statusColor: Color,
    borderColor: Color,
    subItems: List<String> = emptyList()
) {
    Surface(
        color = ColorSurface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorBorder)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Left color indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(borderColor)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(time, style = MetaMono, color = ColorTextDim)
                Text(title, style = TitleNode.copy(fontSize = 18.sp), color = ColorText)
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = statusLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MetaMono,
                        color = statusColor
                    )
                }

                if (subItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    subItems.forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier
                                .size(4.dp)
                                .background(statusColor, CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                item,
                                style = MetaMono.copy(fontSize = 12.sp),
                                color = ColorTextDim
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(ColorPlan, CircleShape)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 2.dp,
            color = ColorPlan
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("AHORA", style = MetaMono, color = ColorPlan)
    }
}
