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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import com.alan.routineos.ui.theme.ColorPending
import com.alan.routineos.ui.theme.ColorPlan
import com.alan.routineos.ui.theme.ColorSurface
import com.alan.routineos.ui.theme.ColorText
import com.alan.routineos.ui.theme.ColorTextDim
import com.alan.routineos.ui.theme.HabitGreen
import com.alan.routineos.ui.theme.MetaMono
import com.alan.routineos.ui.theme.TitleNode
import com.alan.routineos.ui.viewmodel.PlannerViewModel

@Composable
fun PlannerScreen(
    viewModel: PlannerViewModel = hiltViewModel()
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
                    imageVector = Icons.Default.Add, // Placeholder icon
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
                        color = Color(0xFFBC3FA3), // Purple
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
                        onClick = { /* TODO */ },
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

@Composable
fun PlannerActivityCard(
    time: String,
    title: String,
    color: Color,
    tags: List<String>,
    tagColors: List<Color>? = null
) {
    Surface(
        color = ColorSurface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorBorder)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(color)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(time, style = MetaMono, color = ColorTextDim)
                Text(title, style = TitleNode.copy(fontSize = 18.sp), color = ColorText)

                if (tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tags.forEachIndexed { index, tag ->
                            val currentTagColor = tagColors?.getOrNull(index) ?: color
                            Surface(
                                color = currentTagColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = tag,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MetaMono.copy(fontSize = 10.sp),
                                    color = currentTagColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
