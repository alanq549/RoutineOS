package com.alan.routineos.ui.features.template_builder.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.theme.TitleNode

@Composable
fun TimeField(
    time: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(100.dp)
            .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
            .border(0.5.dp, Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Text(
            time,
            style = TitleNode.copy(color = Color(0xFFE0E0E0), fontSize = 13.sp)
        )
    }
}
