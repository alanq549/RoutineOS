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

@Composable
fun RepeatSection() {
    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
        Text(
            "REPETICIÓN",
            style = MetaMono.copy(fontSize = 9.sp),
            color = Color(0xFF555555)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val days = listOf("L", "M", "M", "J", "V", "S", "D")
            var selectedDays by remember { mutableStateOf(setOf<Int>()) }

            days.forEachIndexed { index, day ->
                val isSelected = selectedDays.contains(index + 1)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            if (isSelected) Color(0xFF1565C0) else Color(0xFF1A1A1A),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            0.5.dp,
                            if (isSelected) Color(0xFF42A5F5) else Color(0xFF2A2A2A),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            selectedDays =
                                if (isSelected) selectedDays - (index + 1) else selectedDays + (index + 1)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        day,
                        style = TitleNode.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isSelected) Color.White else Color(0xFF777777)
                    )
                }
            }
        }
    }
}
