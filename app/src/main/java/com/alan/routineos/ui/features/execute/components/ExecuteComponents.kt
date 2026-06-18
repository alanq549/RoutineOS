package com.alan.routineos.ui.features.execute.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.data.local.entities.FieldType
import com.alan.routineos.data.local.entities.NodeMetadataSchema
import com.alan.routineos.ui.features.execute.state.HistorySession
import com.alan.routineos.ui.theme.*

@Composable
fun DynamicField(
    schema: NodeMetadataSchema,
    currentValue: String,
    readonly: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = schema.fieldLabel.uppercase(),
            style = MetaMono,
            color = ColorTextDim
        )
        Spacer(modifier = Modifier.height(8.dp))

        when (schema.fieldType) {
            FieldType.NUMBER -> {
                NumericPicker(
                    value = currentValue,
                    unit = schema.unit,
                    stepSize = schema.stepSize ?: 1f,
                    readonly = readonly,
                    onValueChange = onValueChange
                )
            }

            FieldType.DURATION -> {
                NumericPicker(
                    value = currentValue,
                    unit = "min",
                    stepSize = 1f,
                    readonly = readonly,
                    onValueChange = onValueChange
                )
            }

            FieldType.BOOLEAN -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (currentValue.toBoolean()) "SÍ" else "NO",
                        style = TitleNode,
                        color = if (readonly) ColorTextDim else ColorText
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Switch(
                        checked = currentValue.toBoolean(),
                        onCheckedChange = { onValueChange(it.toString()) },
                        enabled = !readonly,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = if (readonly) ColorExec.copy(alpha = 0.5f) else ColorExec,
                            disabledCheckedThumbColor = ColorExec.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            else -> {
                OutlinedTextField(
                    value = currentValue,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TitleNode,
                    readOnly = readonly,
                    enabled = !readonly,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ColorText,
                        unfocusedTextColor = ColorText,
                        disabledTextColor = ColorTextDim,
                        focusedBorderColor = ColorExec,
                        unfocusedBorderColor = ColorBorder,
                        disabledBorderColor = ColorBorder.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}

@Composable
fun NumericPicker(
    value: String,
    unit: String?,
    stepSize: Float,
    readonly: Boolean = false,
    onValueChange: (String) -> Unit
) {
    val numericValue = value.toFloatOrNull() ?: 0f

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (!readonly) {
            Surface(
                onClick = { onValueChange((numericValue - stepSize).toString()) },
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp),
                color = ColorSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ColorBorder)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("-", color = ColorText, fontSize = 24.sp)
                }
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = if (readonly) 0.dp else 24.dp),
            horizontalAlignment = if (readonly) Alignment.Start else Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MonoTimer.copy(fontSize = if (readonly) 24.sp else 32.sp),
                color = if (readonly) ColorTextDim else ColorText
            )
            if (unit != null) {
                Text(text = unit, style = MetaMono, color = ColorTextDim)
            }
        }

        if (!readonly) {
            Surface(
                onClick = { onValueChange((numericValue + stepSize).toString()) },
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp),
                color = ColorSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ColorExec)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("+", color = ColorExec, fontSize = 24.sp)
                }
            }
        }
    }
}

@Composable
fun SessionHistoryCard(session: HistorySession) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = ColorSurface2)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            session.values.forEach { valItem ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(valItem.fieldName, style = MetaMono, color = ColorTextDim)
                    Text(valItem.value, style = TitleNode, color = ColorText)
                }
            }
        }
    }
}
