package com.alan.routineos.ui.features.template_builder.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.alan.routineos.ui.theme.ColorTextDim
import com.alan.routineos.ui.theme.ColorExec

@Composable
fun RepeatSection(
    selectedDays: Set<Int>,
    timeMode: TimeMode,
    onToggleDay: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
        Text(
            "¿QUÉ DÍAS PUEDE APARECER?",
            style = MetaMono.copy(fontSize = 9.sp, letterSpacing = 1.sp),
            color = ColorTextDim
        )
        Text(
            "Esto define en qué días RoutineOS puede generar esta actividad.",
            style = MetaMono.copy(fontSize = 8.sp),
            color = Color(0xFF555555),
            modifier = Modifier.padding(top = 4.dp)
        )
        
        if (timeMode == TimeMode.FLEXIBLE && selectedDays.isNotEmpty()) {
            Text(
                "Se generará estos días, pero sus pasos decidirán el horario.",
                style = MetaMono.copy(fontSize = 8.sp, fontWeight = FontWeight.Medium),
                color = ColorExec.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val days = listOf("L", "M", "M", "J", "V", "S", "D")

            days.forEachIndexed { index, day ->
                val dayValue = index + 1
                val isSelected = selectedDays.contains(dayValue)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .background(
                            if (isSelected) ColorExec.copy(alpha = 0.1f) else Color(0xFF1A1A1A),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            0.5.dp,
                            if (isSelected) ColorExec else Color(0xFF2A2A2A),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            onToggleDay(dayValue)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        day,
                        style = TitleNode.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) Color.White else Color(0xFF777777)
                    )
                }
            }
        }
    }
}
