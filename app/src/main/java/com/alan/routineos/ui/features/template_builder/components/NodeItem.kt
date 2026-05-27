package com.alan.routineos.ui.features.template_builder.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.data.local.entities.FieldType
import com.alan.routineos.data.local.entities.NodeFieldValue
import com.alan.routineos.data.local.entities.NodeMetadataSchema
import com.alan.routineos.data.local.entities.NodeType
import com.alan.routineos.ui.theme.MetaMono
import com.alan.routineos.ui.theme.TitleNode
import com.alan.routineos.ui.theme.ColorExec

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NodeItem(
    name: String,
    onNameChange: (String) -> Unit,
    depth: Int,
    hasSchedules: Boolean = false,
    isInherited: Boolean = false,
    isOutsideRange: Boolean = false,
    nodeTypes: List<NodeType> = emptyList(),
    selectedTypeId: String? = null,
    onTypeChange: (String) -> Unit = {},
    schemas: List<NodeMetadataSchema> = emptyList(),
    fieldValues: List<NodeFieldValue> = emptyList(),
    onFieldValueChange: (schemaId: String, fieldName: String, value: String) -> Unit = { _, _, _ -> },
    onAddClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onScheduleClick: () -> Unit = {},
    onManageDetailsClick: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    val bgColor = when (depth) {
        0 -> Color(0xFF1E1E1E)
        1 -> Color(0xFF181818)
        else -> Color(0xFF161616)
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 14.dp, vertical = 2.5.dp)
            .padding(start = (depth * 20).dp)
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(
                0.5.dp,
                if (depth == 0) Color(0xFF2A2A2A) else Color(0xFF222222),
                RoundedCornerShape(8.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (depth == 0) Icons.Default.Folder else Icons.Default.SubdirectoryArrowRight,
                contentDescription = null,
                tint = if (depth == 0) Color(0xFF1565C0) else Color(0xFF444444),
                modifier = Modifier.size(14.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            BasicTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.weight(1f),
                textStyle = TitleNode.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE0E0E0)
                ),
                cursorBrush = SolidColor(Color(0xFF1565C0)),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                decorationBox = { innerTextField ->
                    if (name.isEmpty()) {
                        Text(
                            "Nombre del paso...",
                            style = TitleNode.copy(fontSize = 12.sp, color = Color(0xFF444444))
                        )
                    }
                    innerTextField()
                }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    modifier = Modifier.size(24.dp),
                    color = when {
                        hasSchedules -> Color(0xFF0D1F3A)
                        isInherited -> Color(0xFF1A1A1A)
                        else -> Color(0xFF1A1A1A)
                    },
                    shape = RoundedCornerShape(5.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        0.5.dp,
                        when {
                            hasSchedules -> Color(0xFF1565C0)
                            isInherited -> Color(0xFF444444).copy(alpha = 0.5f)
                            else -> Color(0xFF2A2A2A)
                        }
                    ),
                    onClick = onScheduleClick
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Schedule,
                            null,
                            tint = when {
                                hasSchedules -> if (isOutsideRange) Color.Red.copy(alpha = 0.8f) else Color(0xFF42A5F5)
                                isInherited -> Color(0xFF777777)
                                else -> Color(0xFF333333)
                            },
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                if (depth == 0) {
                    Surface(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF1A1A1A),
                        shape = RoundedCornerShape(5.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF2A2A2A)),
                        onClick = onAddClick
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Add,
                                null,
                                tint = Color(0xFF1565C0),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier.size(24.dp),
                    color = Color(0xFF1A1A1A),
                    shape = RoundedCornerShape(5.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF2A2A2A)),
                    onClick = onDeleteClick
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Close,
                            null,
                            tint = Color(0xFF553333),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(10.dp)) {
                if (isInherited) {
                    Text(
                        "HORARIO HEREDADO",
                        style = MetaMono.copy(fontSize = 7.sp, fontWeight = FontWeight.Bold),
                        color = Color(0xFF777777),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (isOutsideRange && hasSchedules) {
                    Text(
                        "Este horario está fuera del rango del paso superior.",
                        style = MetaMono.copy(fontSize = 7.sp, fontWeight = FontWeight.Bold),
                        color = Color.Red.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Text(
                    "ESTRUCTURA DEL PASO",
                    style = MetaMono.copy(fontSize = 8.sp),
                    color = Color(0xFF555555)
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    nodeTypes.forEach { type ->
                        val isSelected = selectedTypeId == type.id
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) Color(0xFF1565C0).copy(alpha = 0.2f) else Color(0xFF1A1A1A),
                                    RoundedCornerShape(4.dp)
                                )
                                .border(
                                    0.5.dp,
                                    if (isSelected) Color(0xFF1565C0) else Color(0xFF2A2A2A),
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable { onTypeChange(type.id) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                type.name,
                                style = MetaMono.copy(fontSize = 9.sp),
                                color = if (isSelected) Color.White else Color(0xFF666666)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .clickable { onManageDetailsClick() }
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        "+ crear o editar estructuras",
                        style = TitleNode.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = ColorExec
                    )
                }

                if (schemas.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "DATOS QUE REGISTRARÉ",
                        style = MetaMono.copy(fontSize = 8.sp),
                        color = Color(0xFF555555)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    schemas.forEach { schema ->
                        val fieldValue = fieldValues.find { it.schemaId == schema.id }?.value ?: ""
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                schema.fieldLabel,
                                style = TitleNode.copy(fontSize = 11.sp),
                                color = Color(0xFF888888),
                                modifier = Modifier.width(80.dp)
                            )
                            BasicTextField(
                                value = fieldValue,
                                onValueChange = { onFieldValueChange(schema.id, schema.fieldName, it) },
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0xFF141414), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                textStyle = TitleNode.copy(fontSize = 11.sp, color = Color.White),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = if (schema.fieldType == FieldType.NUMBER) KeyboardType.Number else KeyboardType.Text
                                ),
                                cursorBrush = SolidColor(Color(0xFF1565C0))
                            )
                            if (schema.unit != null) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    schema.unit,
                                    style = MetaMono.copy(fontSize = 9.sp),
                                    color = Color(0xFF444444)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
