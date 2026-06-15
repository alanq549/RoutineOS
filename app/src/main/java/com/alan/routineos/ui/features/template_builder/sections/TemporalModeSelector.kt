package com.alan.routineos.ui.features.template_builder.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.data.local.entities.TemporalMode
import com.alan.routineos.ui.theme.ColorBorder
import com.alan.routineos.ui.theme.ColorExec
import com.alan.routineos.ui.theme.ColorSurface
import com.alan.routineos.ui.theme.ColorText
import com.alan.routineos.ui.theme.ColorTextDim
import com.alan.routineos.ui.theme.ColorTextMuted
import com.alan.routineos.ui.theme.TitleNode

@Composable
fun TemporalModeSelector(
    selectedMode: TemporalMode,
    onModeSelected: (TemporalMode) -> Unit
) {
    val options = listOf(
        TemporalMode.NONE to "Sin horario",
        TemporalMode.START_ONLY to "Hora específica",
        TemporalMode.START_END to "Rango horario",
        TemporalMode.SEQUENTIAL to "Secuencial"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (mode, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (selectedMode == mode) ColorExec.copy(alpha = 0.14f) else ColorSurface,
                        RoundedCornerShape(10.dp)
                    )
                    .border(
                        1.dp,
                        if (selectedMode == mode) ColorExec else ColorBorder,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onModeSelected(mode) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedMode == mode,
                    onClick = { onModeSelected(mode) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = ColorExec,
                        unselectedColor = ColorTextMuted
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = label,
                    style = TitleNode.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                    color = if (selectedMode == mode) ColorText else ColorTextDim
                )
            }
        }
    }
}