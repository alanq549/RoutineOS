package com.alan.routineos.ui.features.template_builder.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.theme.MetaMono
import com.alan.routineos.ui.theme.ColorTextDim

data class ActivityPreset(
    val name: String,
    val emoji: String,
    val colorHex: String,
    val category: ContextCategory
)

val PRESETS = listOf(
    ActivityPreset("Gym", "💪", "#F44336", ContextCategory.EJERCICIO),
    ActivityPreset("Trabajo", "💼", "#2196F3", ContextCategory.TRABAJO),
    ActivityPreset("Estudiar", "📚", "#9C27B0", ContextCategory.ESTUDIO),
    ActivityPreset("Dormir", "🌙", "#FF9800", ContextCategory.SALUD),
    ActivityPreset("Meditar", "🧘", "#4CAF50", ContextCategory.SALUD),
    ActivityPreset("Lectura", "📖", "#795548", ContextCategory.PERSONAL)
)

@Composable
fun QuickPresetsSection(
    onSelect: (ActivityPreset) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            "¿POR DÓNDE EMPEZAMOS?",
            modifier = Modifier.padding(horizontal = 14.dp),
            style = MetaMono.copy(fontSize = 9.sp, letterSpacing = 1.sp),
            color = ColorTextDim
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(PRESETS) { preset ->
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1A1A1A), RoundedCornerShape(12.dp))
                        .border(0.5.dp, Color(0xFF2A2A2A), RoundedCornerShape(12.dp))
                        .clickable { onSelect(preset) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(preset.emoji, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            preset.name,
                            style = MetaMono.copy(fontSize = 11.sp),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
