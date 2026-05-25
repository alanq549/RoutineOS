package com.alan.routineos.ui.features.template_builder.sections

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.theme.MetaMono
import com.alan.routineos.ui.theme.TitleNode
import com.alan.routineos.ui.theme.ColorTextDim
import java.util.Locale

@Composable
fun TimeRangeSection(
    selectedMode: TimeMode,
    startTime: String,
    endTime: String,
    durationMinutes: Int,
    onModeChange: (TimeMode) -> Unit,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit,
    onDurationChange: (Int) -> Unit
) {
    val context = LocalContext.current

    fun showPicker(currentTime: String, onTimeSelected: (String) -> Unit) {
        val parts = currentTime.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        
        TimePickerDialog(context, { _, h, m ->
            onTimeSelected(String.format(Locale.US, "%02d:%02d", h, m))
        }, hour, minute, true).show()
    }

    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp)) {
        Text(
            "¿CUÁNDO OCURRE?",
            style = MetaMono.copy(fontSize = 9.sp, letterSpacing = 1.sp),
            color = ColorTextDim
        )
        Spacer(modifier = Modifier.height(12.dp))

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
                        .clickable { onModeChange(mode) }
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

        AnimatedContent(targetState = selectedMode, label = "time_content") { mode ->
            when (mode) {
                TimeMode.FIXED_START -> {
                    TimeValueDisplay(
                        label = "INICIA A LAS", 
                        value = startTime,
                        onClick = { showPicker(startTime, onStartTimeChange) }
                    )
                }
                TimeMode.RANGE -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TimeValueDisplay(
                            label = "DESDE LAS", 
                            value = startTime, 
                            modifier = Modifier.weight(1f),
                            onClick = { showPicker(startTime, onStartTimeChange) }
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF2A2A2A),
                            modifier = Modifier.padding(horizontal = 16.dp).size(16.dp)
                        )
                        TimeValueDisplay(
                            label = "HASTA LAS", 
                            value = endTime, 
                            modifier = Modifier.weight(1f),
                            onClick = { showPicker(endTime, onEndTimeChange) }
                        )
                    }
                }
                TimeMode.DURATION -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TimeValueDisplay(
                            label = "INICIA A LAS", 
                            value = startTime, 
                            modifier = Modifier.weight(1f),
                            onClick = { showPicker(startTime, onStartTimeChange) }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text("DURACIÓN (MIN)", style = MetaMono.copy(fontSize = 8.sp), color = ColorTextDim)
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E1E1E), RoundedCornerShape(10.dp))
                                    .border(0.5.dp, Color(0xFF2A2A2A), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                BasicTextField(
                                    value = durationMinutes.toString(),
                                    onValueChange = { 
                                        it.toIntOrNull()?.let { mins -> onDurationChange(mins) }
                                    },
                                    textStyle = TitleNode.copy(fontSize = 16.sp, letterSpacing = 1.sp, color = Color.White),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    cursorBrush = SolidColor(Color.White),
                                    decorationBox = { innerTextField ->
                                        if (durationMinutes == 0) {
                                            Text("0", style = TitleNode.copy(fontSize = 16.sp, color = Color(0xFF444444)))
                                        }
                                        innerTextField()
                                    }
                                )
                            }
                        }
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
fun TimeValueDisplay(
    label: String, 
    value: String, 
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Column(modifier = modifier.clickable { onClick() }) {
        Text(label, style = MetaMono.copy(fontSize = 8.sp), color = ColorTextDim)
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E), RoundedCornerShape(10.dp))
                .border(0.5.dp, Color(0xFF2A2A2A), RoundedCornerShape(10.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                value,
                style = TitleNode.copy(fontSize = 16.sp, letterSpacing = 1.sp),
                color = Color.White
            )
        }
    }
}
