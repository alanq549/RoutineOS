package com.alan.routineos.ui.features.node_type_manager.components.schema

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.theme.TitleNode

@Composable
fun TypeChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) Color(0xFF0D1F3A) else Color(0xFF1A1A1A),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (isSelected) Color(0xFF1565C0) else Color(0xFF2A2A2A)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = TitleNode.copy(fontSize = 10.sp),
            color = if (isSelected) Color(0xFF42A5F5) else Color(0xFF555555)
        )
    }
}
