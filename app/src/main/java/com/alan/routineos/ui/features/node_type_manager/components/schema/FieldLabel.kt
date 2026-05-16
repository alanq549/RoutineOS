package com.alan.routineos.ui.features.node_type_manager.components.schema

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.theme.MetaMono

@Composable
fun FieldLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MetaMono.copy(fontSize = 9.sp),
        color = Color(0xFF555555)
    )
}
