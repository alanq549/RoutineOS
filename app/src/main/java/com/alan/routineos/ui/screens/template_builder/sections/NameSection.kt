package com.alan.routineos.ui.screens.template_builder.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.screens.template_builder.components.BuilderTextField
import com.alan.routineos.ui.theme.MetaMono

@Composable
fun NameSection(
    name: String,
    onNameChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
        Text(
            "NOMBRE",
            style = MetaMono.copy(fontSize = 9.sp),
            color = Color(0xFF555555)
        )
        Spacer(modifier = Modifier.height(4.dp))
        BuilderTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = "Ej: Push Day",
            isFocused = true
        )
    }
}
