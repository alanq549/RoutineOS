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
import com.alan.routineos.ui.features.template_builder.components.BuilderTextField
import com.alan.routineos.ui.theme.MetaMono

enum class ActivityType { WORKOUT, TASK, HABIT, EVENT, ROUTINE }

@Composable
fun ActivityIdentitySection(
    name: String,
    onNameChange: (String) -> Unit
) {
    var selectedType by remember { mutableStateOf(ActivityType.ROUTINE) }

    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp)) {
        Text(
            "IDENTIDAD",
            style = MetaMono.copy(fontSize = 9.sp),
            color = Color(0xFF555555)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Name Input - Minimal
        BuilderTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = "Nombre de la actividad...",
            isFocused = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Type Selector - Premium Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActivityType.entries.forEach { type ->
                val isSelected = selectedType == type
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) Color(0xFF1565C0).copy(alpha = 0.1f) else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        )
                        .border(
                            0.5.dp,
                            if (isSelected) Color(0xFF1565C0) else Color(0xFF2A2A2A),
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { selectedType = type }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = type.name,
                        style = MetaMono.copy(fontSize = 8.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                        color = if (isSelected) Color(0xFF42A5F5) else Color(0xFF555555)
                    )
                }
            }
        }
    }
}
