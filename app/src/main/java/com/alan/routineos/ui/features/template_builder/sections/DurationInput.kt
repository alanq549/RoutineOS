package com.alan.routineos.ui.features.template_builder.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.theme.ColorBorder
import com.alan.routineos.ui.theme.ColorExec
import com.alan.routineos.ui.theme.ColorSurface
import com.alan.routineos.ui.theme.ColorText
import com.alan.routineos.ui.theme.ColorTextDim
import com.alan.routineos.ui.theme.MetaMono
import com.alan.routineos.ui.theme.TitleNode

@Composable
fun DurationInput(
    durationMinutes: Int,
    onDurationChange: (Int) -> Unit
) {
    Column {
        Text(
            text = "DURACIÓN ESTIMADA (MIN)",
            style = MetaMono.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
            color = ColorTextDim
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ColorSurface, RoundedCornerShape(12.dp))
                .border(1.dp, ColorBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            BasicTextField(
                value = durationMinutes.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let { mins ->
                        onDurationChange(mins)
                    }
                },
                textStyle = TitleNode.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorText
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                cursorBrush = SolidColor(ColorExec),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        innerTextField()
                    }
                }
            )
        }
    }
}