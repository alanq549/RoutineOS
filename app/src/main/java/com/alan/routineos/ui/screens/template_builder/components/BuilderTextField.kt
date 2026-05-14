package com.alan.routineos.ui.screens.template_builder.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
fun BuilderTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isFocused: Boolean = false
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TitleNode.copy(color = Color(0xFFE0E0E0), fontSize = 13.sp),
        cursorBrush = SolidColor(Color(0xFF1565C0)),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
            .border(
                width = 0.5.dp,
                color = if (isFocused) Color(0xFF1565C0) else Color(0xFF2A2A2A),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 9.dp),
        decorationBox = { innerTextField ->
            if (value.isEmpty()) Text(placeholder, color = Color(0xFF444444), style = TitleNode.copy(fontSize = 13.sp))
            innerTextField()
        }
    )
}
