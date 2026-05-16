package com.alan.routineos.ui.features.node_type_manager.components.schema

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.theme.TitleNode

@Composable
fun ManagerTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isFocused: Boolean = false
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
            .border(
                width = if (isFocused) 1.dp else 0.5.dp,
                color  = if (isFocused) Color(0xFF1565C0) else Color(0xFF2A2A2A),
                shape  = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        textStyle = TitleNode.copy(color = Color(0xFFE0E0E0), fontSize = 13.sp),
        cursorBrush = SolidColor(Color(0xFF1565C0)),
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text(placeholder, color = Color(0xFF3A3A3A), style = TitleNode.copy(fontSize = 13.sp))
            }
            inner()
        }
    )
}
