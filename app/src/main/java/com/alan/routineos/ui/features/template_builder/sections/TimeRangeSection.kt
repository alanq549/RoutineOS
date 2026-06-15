package com.alan.routineos.ui.features.template_builder.sections

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.data.local.entities.TemporalMode
import com.alan.routineos.ui.theme.ColorBorder
import com.alan.routineos.ui.theme.ColorExec
import com.alan.routineos.ui.theme.ColorTextDim
import com.alan.routineos.ui.theme.MetaMono
import com.alan.routineos.ui.theme.TitleNode
import java.util.Locale

@Composable
fun TimeRangeSection(
    temporalMode: TemporalMode,
    startTime: String,
    endTime: String,
    durationMinutes: Int,
    @Suppress("UNUSED_PARAMETER")
    hasNodeSchedules: Boolean = false,
    onTemporalModeChange: (TemporalMode) -> Unit,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit,
    onDurationChange: (Int) -> Unit
) {
    val context = LocalContext.current

    fun showPicker(currentTime: String, onTimeSelected: (String) -> Unit) {
        val parts = if (currentTime.contains(":")) currentTime.split(":") else listOf("08", "00")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        TimePickerDialog(
            context,
            { _, h, m ->
                onTimeSelected(String.format(Locale.US, "%02d:%02d", h, m))
            },
            hour,
            minute,
            true
        ).show()
    }

    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp)) {
        Text(
            text = "HORARIO DE LA ACTIVIDAD",
            style = MetaMono.copy(
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            ),
            color = ColorExec
        )

        Spacer(modifier = Modifier.height(12.dp))

        TemporalModeSelector(
            selectedMode = temporalMode,
            onModeSelected = onTemporalModeChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (temporalMode) {
            TemporalMode.NONE -> {
                Text(
                    text = "Sin horario definido",
                    style = TitleNode.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    color = ColorTextDim
                )
            }

            TemporalMode.START_ONLY -> {
                TimeValueDisplay(
                    label = "HORA",
                    value = if (startTime.isBlank() || startTime == "--:--") "08:00" else startTime,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showPicker(startTime, onStartTimeChange) }
                )
            }

            TemporalMode.START_END -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TimeValueDisplay(
                        label = "INICIO",
                        value = if (startTime.isBlank() || startTime == "--:--") "08:00" else startTime,
                        modifier = Modifier.weight(1f),
                        onClick = { showPicker(startTime, onStartTimeChange) }
                    )

                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = ColorBorder,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .size(20.dp)
                    )

                    TimeValueDisplay(
                        label = "FIN",
                        value = if (endTime.isBlank() || endTime == "--:--") "09:00" else endTime,
                        modifier = Modifier.weight(1f),
                        onClick = { showPicker(endTime, onEndTimeChange) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                DurationInput(
                    durationMinutes = durationMinutes,
                    onDurationChange = onDurationChange
                )
            }

            TemporalMode.SEQUENTIAL -> {
                DurationInput(
                    durationMinutes = durationMinutes,
                    onDurationChange = onDurationChange
                )
            }
        }
    }
}