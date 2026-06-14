package com.alan.routineos.ui.features.template_builder.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.features.template_builder.components.BuilderTextField
import com.alan.routineos.ui.theme.ColorTextDim
import com.alan.routineos.ui.theme.MetaMono

@Composable
fun ActivityIdentitySection(
    name: String,
    onNameChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp)) {
        Text(
            "LO PRIMERO, EL PROPÓSITO",
            style = MetaMono.copy(fontSize = 9.sp, letterSpacing = 1.sp),
            color = ColorTextDim
        )

        Spacer(modifier = Modifier.height(16.dp))

        BuilderTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = "Dale un nombre a este espacio...",
            isFocused = name.isEmpty()
        )
    }
}
