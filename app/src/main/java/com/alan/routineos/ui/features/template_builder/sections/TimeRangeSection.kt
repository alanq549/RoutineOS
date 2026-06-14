package com.alan.routineos.ui.features.template_builder.sections

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.theme.*
import java.util.Locale

@Composable
fun TimeRangeSection(
    startTime: String,
    endTime: String,
    durationMinutes: Int,
    hasNodeSchedules: Boolean = false,
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
            "HORARIO DE LA ACTIVIDAD",
            style = MetaMono.copy(fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
            color = ColorExec
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        Column {
            Text(
                "DURACIÓN TOTAL ESTIMADA (MIN)",
                style = MetaMono.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                color = ColorTextDim
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ColorSurface, RoundedCornerShape(12.dp))
                    .border(1.dp, ColorBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                BasicTextField(
                    value = durationMinutes.toString(),
                    onValueChange = {
                        it.toIntOrNull()?.let { mins -> onDurationChange(mins) }
                    },
                    textStyle = TitleNode.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorText
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(ColorExec),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (durationMinutes == 0) {
                                Text(
                                    "0",
                                    style = TitleNode.copy(fontSize = 18.sp, color = ColorTextMuted)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
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
        Text(label, style = MetaMono.copy(fontSize = 9.sp, fontWeight = FontWeight.Medium), color = ColorTextDim)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ColorSurface, RoundedCornerShape(12.dp))
                .border(1.dp, ColorBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                value,
                style = TitleNode.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = ColorText
            )
        }
    }
}
