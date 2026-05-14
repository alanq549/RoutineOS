package com.alan.routineos.ui.features.template_builder.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.theme.MetaMono

@Composable
fun ColorSection(
    selectedColorHex: String,
    colors: List<Color>,
    onColorChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
        Text("COLOR", style = MetaMono.copy(fontSize = 9.sp), color = Color(0xFF555555))
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            colors.forEach { color ->
                val colorHex = String.format("#%06X", color.toArgb() and 0xFFFFFF).uppercase()
                val isSelected = selectedColorHex.uppercase() == colorHex
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(color, CircleShape)
                        .then(
                            if (isSelected) Modifier
                                .border(2.dp, Color(0xFF121212), CircleShape)
                                .border(4.dp, Color.White, CircleShape)
                                .padding(4.dp)
                            else Modifier
                        )
                        .clickable { onColorChange(colorHex) }
                )
            }
        }
    }
}
