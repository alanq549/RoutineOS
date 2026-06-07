package com.alan.routineos.ui.features.template_builder.sections

import android.app.TimePickerDialog
import android.util.Log
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
import androidx.compose.material3.Surface
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
import com.alan.routineos.ui.theme.ColorExec
import java.util.Locale

@Composable
fun TimeRangeSection(
    selectedMode: TimeMode,
    startTime: String,
    endTime: String,
    durationMinutes: Int,
    hasNodeSchedules: Boolean = false,
    onModeChange: (TimeMode) -> Unit,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit,
    onDurationChange: (Int) -> Unit
) {
    val context = LocalContext.current

    fun showPicker(currentTime: String, onTimeSelected: (String) -> Unit) {
        val parts = if (currentTime.contains(":")) currentTime.split(":") else listOf("08", "00")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        
        TimePickerDialog(context, { _, h, m ->
            onTimeSelected(String.format(Locale.US, "%02d:%02d", h, m))
        }, hour, minute, true).show()
    }

    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp)) {
        Text(
            "¿QUÉ TAN FIJA ES ESTA ACTIVIDAD?",
            style = MetaMono.copy(fontSize = 9.sp, letterSpacing = 1.sp),
            color = ColorTextDim
        )
        Text(
            "Elige si esta actividad vive atada al reloj o si puede adaptarse.",
            style = MetaMono.copy(fontSize = 8.sp),
            color = Color(0xFF555555),
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

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
                        .clickable { 
                            Log.d("TEMPLATE_UX_DEBUG", "TIME MODE SELECTED mode=$mode")
                            onModeChange(mode) 
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when(mode) {
                            TimeMode.FIXED_START -> "Hora fija"
                            TimeMode.RANGE -> "Ventana"
                            TimeMode.DURATION -> "Duración"
                            TimeMode.FLEXIBLE -> "Sin hora fija"
                        },
                        style = TitleNode.copy(fontSize = 9.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                        color = if (isSelected) Color.White else Color(0xFF555555)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        AnimatedContent(targetState = selectedMode, label = "time_content") { mode ->
            Column {
                val description = when (mode) {
                    TimeMode.FIXED_START -> "Empieza a una hora concreta. Ejemplo: Despertar 05:25"
                    TimeMode.RANGE -> "Puede ocurrir dentro de un rango. Ejemplo: Estudiar entre 18:00 y 21:00"
                    TimeMode.DURATION -> "Importa cuánto dura, no exactamente cuándo. Ejemplo: Leer 30 min"
                    TimeMode.FLEXIBLE -> "Sus pasos o adaptaciones deciden el horario. Ejemplo: Universidad con bloques por día"
                }
                
                Text(
                    text = description,
                    style = MetaMono.copy(fontSize = 9.sp, fontWeight = FontWeight.Medium),
                    color = ColorExec.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                when (mode) {
                    TimeMode.FIXED_START -> {
                        Column {
                            Text(
                                "Esta actividad aparecerá en Today a la hora indicada.",
                                style = TitleNode.copy(fontSize = 11.sp),
                                color = ColorTextDim,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            TimeValueDisplay(
                                label = "HORA DE INICIO", 
                                value = if (startTime.isBlank() || startTime == "--:--") "--:--" else startTime,
                                onClick = { showPicker(startTime, onStartTimeChange) }
                            )
                            if (startTime.isBlank() || startTime == "--:--") {
                                Text("Define una hora de inicio.", color = Color.Red.copy(alpha = 0.7f), style = MetaMono.copy(fontSize = 8.sp), modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                    TimeMode.RANGE -> {
                        Column {
                            Text(
                                "RoutineOS intentará acomodarla dentro de esta ventana.",
                                style = TitleNode.copy(fontSize = 11.sp),
                                color = ColorTextDim,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TimeValueDisplay(
                                    label = "DESDE LAS", 
                                    value = if (startTime.isBlank() || startTime == "--:--") "--:--" else startTime, 
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
                                    value = if (endTime.isBlank() || endTime == "--:--") "--:--" else endTime, 
                                    modifier = Modifier.weight(1f),
                                    onClick = { showPicker(endTime, onEndTimeChange) }
                                )
                            }
                        }
                    }
                    TimeMode.DURATION -> {
                        Column {
                            Text(
                                "Se usará como bloque de tiempo flexible.",
                                style = TitleNode.copy(fontSize = 11.sp),
                                color = ColorTextDim,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TimeValueDisplay(
                                    label = "INICIA A LAS (Opcional)", 
                                    value = if (startTime.isBlank() || startTime == "--:--") "--:--" else startTime,
                                    modifier = Modifier.weight(1f),
                                    onClick = { showPicker(startTime, onStartTimeChange) }
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("DURACIÓN ESTIMADA (MIN)", style = MetaMono.copy(fontSize = 8.sp), color = ColorTextDim)
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
                    }
                    TimeMode.FLEXIBLE -> {
                        Column {
                            Text(
                                "Sin hora global. Puedes asignar horarios a bloques como Lunes, Materia 1 o Gym.",
                                style = TitleNode.copy(fontSize = 11.sp),
                                color = ColorTextDim,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                "Útil para actividades que se organizan por pasos, días o contexto.",
                                style = MetaMono.copy(fontSize = 8.sp),
                                color = Color(0xFF444444)
                            )
                            if (!hasNodeSchedules) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    color = Color.Yellow.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.Yellow.copy(alpha = 0.2f))
                                ) {
                                    Text(
                                        "Esta actividad podrá aparecer en Today, pero sin una hora clara hasta que configures horarios en sus bloques.",
                                        style = MetaMono.copy(fontSize = 8.sp, color = Color.Yellow.copy(alpha = 0.7f)),
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                                LaunchedEffect(Unit) {
                                    Log.d("TEMPLATE_UX_DEBUG", "SOFT WARNING flexibleWithoutNodeSchedules")
                                }
                            }
                        }
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
