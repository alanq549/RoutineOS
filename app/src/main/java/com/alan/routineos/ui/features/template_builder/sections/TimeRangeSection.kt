package com.alan.routineos.ui.features.template_builder.sections

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.theme.MetaMono
import com.alan.routineos.ui.theme.TitleNode

enum class TimeMode { FIXED_START, RANGE, DURATION, FLEXIBLE }

@Composable
fun TimeRangeSection() {
    var selectedMode by remember { mutableStateOf(TimeMode.RANGE) }

    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp)) {
        Text(
            "PLANIFICACIÓN TEMPORAL",
            style = MetaMono.copy(fontSize = 9.sp),
            color = Color(0xFF555555)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Mode Selector - Segmented Control Style
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TimeMode.entries.forEach { mode ->
                val isSelected = selectedMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) Color(0xFF2A2A2A) else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { selectedMode = mode }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when(mode) {
                            TimeMode.FIXED_START -> "Inicio"
                            TimeMode.RANGE -> "Rango"
                            TimeMode.DURATION -> "Duración"
                            TimeMode.FLEXIBLE -> "Flexible"
                        },
                        style = TitleNode.copy(fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                        color = if (isSelected) Color.White else Color(0xFF555555)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Time Values - Futuristic Layout
        AnimatedContent(targetState = selectedMode, label = "time_content") { mode ->
            when (mode) {
                TimeMode.FIXED_START -> {
                    TimeValueDisplay(label = "INICIO", value = "08:00 AM")
                }
                TimeMode.RANGE -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TimeValueDisplay(label = "DESDE", value = "08:00 AM", modifier = Modifier.weight(1f))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF2A2A2A),
                            modifier = Modifier.padding(horizontal = 16.dp).size(16.dp)
                        )
                        TimeValueDisplay(label = "HASTA", value = "10:00 AM", modifier = Modifier.weight(1f))
                    }
                }
                TimeMode.DURATION -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TimeValueDisplay(label = "INICIO", value = "08:00 AM", modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(16.dp))
                        TimeValueDisplay(label = "DURACIÓN", value = "2h 30m", modifier = Modifier.weight(1f))
                    }
                }
                TimeMode.FLEXIBLE -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1A1A1A), RoundedCornerShape(12.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "SIN HORARIO FIJO",
                            style = MetaMono.copy(letterSpacing = 2.sp),
                            color = Color(0xFF444444)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimeValueDisplay(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MetaMono.copy(fontSize = 8.sp), color = Color(0xFF444444))
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E), RoundedCornerShape(10.dp))
                .border(0.5.dp, Color(0xFF2A2A2A), RoundedCornerShape(10.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                value,
                style = TitleNode.copy(fontSize = 16.sp, letterSpacing = 1.sp),
                color = Color(0xFFE0E0E0)
            )
        }
    }
}
