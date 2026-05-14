package com.alan.routineos.ui.features.planner.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.features.planner.components.PlannerActivityCard
import com.alan.routineos.ui.features.planner.viewmodel.PlannerViewModel
import com.alan.routineos.ui.theme.*

@Composable
fun PlannerScreen(
    viewModel: PlannerViewModel
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
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "PLANNER",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ColorText,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Mayo 2025",
                        style = TitleNode,
                        color = ColorTextDim
                    )
                }
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = ColorTextDim,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Week Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val weekDays = listOf(
                    "L" to "12", "MA" to "13", "MI" to "14",
                    "J" to "15", "V" to "16", "S" to "17", "D" to "18"
                )

                weekDays.forEach { (day, num) ->
                    val isSelected = day == "MA"
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(40.dp)
                            .then(
                                if (isSelected) Modifier
                                    .background(ColorPlan, RoundedCornerShape(12.dp))
                                    .padding(vertical = 8.dp)
                                else Modifier.padding(vertical = 8.dp)
                            )
                    ) {
                        Text(
                            day,
                            style = MetaMono.copy(fontSize = 12.sp),
                            color = if (isSelected) Color.White else ColorTextDim
                        )
                        Text(
                            num,
                            style = TitleNode.copy(fontSize = 18.sp),
                            color = if (isSelected) Color.White else ColorText
                        )
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(4.dp)
                                .background(if (isSelected) Color.White else ColorPlan, CircleShape)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "MARTES 13 MAYO · 4 ACTIVIDADES",
                style = MetaMono,
                color = ColorTextDim
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    PlannerActivityCard(
                        time = "5:30 am",
                        title = "Despertar",
                        color = HabitGreen,
                        tags = listOf("fijo L-V")
                    )
                }
                item {
                    PlannerActivityCard(
                        time = "7:00 am - 2:00 pm",
                        title = "Semestre 8",
                        color = ColorPlan,
                        tags = listOf("Desarrollo Móvil 7-9", "Análisis Emp. 9-11", "+ 2 bloques"),
                        tagColors = listOf(HabitGreen, HabitGreen, ColorPending)
                    )
                }
                item {
                    PlannerActivityCard(
                        time = "4:00 pm - 5:30 pm",
                        title = "Pull Day",
                        color = Color(0xFFBC3FA3),
                        tags = listOf("Ma / V")
                    )
                }
                item {
                    PlannerActivityCard(
                        time = "10:30 pm",
                        title = "Dormir",
                        color = ColorPending,
                        tags = listOf("todos los días")
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ColorBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorText)
                    ) {
                        Text(" agregar actividad a este día", style = TitleNode)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
