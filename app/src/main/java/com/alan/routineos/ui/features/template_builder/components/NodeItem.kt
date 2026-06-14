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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.alan.routineos.data.local.entities.NodeSchedule
import com.alan.routineos.data.local.entities.NodeType
import com.alan.routineos.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NodeItem(
    name: String,
    onNameChange: (String) -> Unit,
    depth: Int,
    hasSchedules: Boolean = false,
    isInherited: Boolean = false,
    inheritedFrom: String? = null,
    effectiveSchedules: List<NodeSchedule> = emptyList(),
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
    
    // Jerarquía visual: indentación máxima de 4 niveles
    val visualDepth = depth.coerceAtMost(4)
    val indent = (visualDepth * 20).dp
    
    val bgColor = when (depth) {
        0 -> ColorSurface
        else -> ColorSurface2
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(vertical = 4.dp)
    ) {
        // Línea vertical de conexión jerárquica (solo si es hijo)
        if (depth > 0) {
            Box(
                modifier = Modifier
                    .padding(start = (indent - 10.dp))
                    .width(1.5.dp)
                    .fillMaxHeight()
                    .background(ColorBorder.copy(alpha = 0.3f))
            )
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 14.dp)
                .padding(start = indent)
                .fillMaxWidth()
                .background(bgColor, RoundedCornerShape(12.dp))
                .border(
                    width = if (depth == 0) 1.dp else 0.5.dp,
                    color = if (depth == 0) ColorBorder else ColorBorder.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
        ) {
            // Cabecera del Nodo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge de profundidad para niveles profundos (ej: Nivel 5+)
                if (depth > 4) {
                    Surface(
                        color = ColorExec.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "L$depth",
                            style = MetaMono.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                            color = ColorExec,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Icon(
                    imageVector = if (depth == 0) Icons.Default.Layers else Icons.Rounded.AccountTree,
                    contentDescription = null,
                    tint = if (depth == 0) ColorPlan else ColorTextDim,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                BasicTextField(
                    value = name,
                    onValueChange = onNameChange,
                    modifier = Modifier.weight(1f),
                    textStyle = TitleNode.copy(
                        fontSize = 14.sp,
                        fontWeight = if (depth == 0) FontWeight.Bold else FontWeight.Medium,
                        color = ColorText
                    ),
                    cursorBrush = SolidColor(ColorExec),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (name.isEmpty()) {
                                Text(
                                    "Nombre del bloque...",
                                    style = TitleNode.copy(fontSize = 14.sp, color = ColorTextMuted)
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                // Acciones con targets táctiles amplios
                Row(
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onScheduleClick) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Horario",
                            tint = when {
                                hasSchedules -> if (isOutsideRange) Color.Red else ColorExec
                                isInherited -> ColorTextDim
                                else -> ColorTextDim.copy(alpha = 0.3f)
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(onClick = onAddClick) {
                        Icon(
                            imageVector = Icons.Rounded.AddCircleOutline,
                            contentDescription = "Agregar elemento hijo",
                            tint = ColorPlan.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Eliminar",
                            tint = Color.Red.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp)
                ) {
                    HorizontalDivider(color = ColorBorder.copy(alpha = 0.2f), modifier = Modifier.padding(bottom = 12.dp))

                    // Información de Horario
                    if (isInherited) {
                        Text(
                            "HORARIO HEREDADO: ${inheritedFrom?.uppercase() ?: "PADRE"}",
                            style = MetaMono.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = ColorTextDim
                        )
                        effectiveSchedules.firstOrNull()?.let { s ->
                            Text(
                                "${s.startTime} — ${s.endTime}",
                                style = TitleNode.copy(fontSize = 12.sp),
                                color = ColorTextDim.copy(alpha = 0.8f)
                            )
                        }
                    } else if (hasSchedules) {
                        Text(
                            "HORARIO ESPECÍFICO",
                            style = MetaMono.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = ColorExec
                        )
                        effectiveSchedules.firstOrNull()?.let { s ->
                            Text(
                                "${s.startTime} — ${s.endTime}",
                                style = TitleNode.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                color = ColorText
                            )
                        }
                    }

                    if (isOutsideRange && hasSchedules) {
                        Text(
                            "Aviso: fuera de rango del nivel superior",
                            style = MetaMono.copy(fontSize = 9.sp, color = Color.Red.copy(alpha = 0.8f)),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Selección de Tipo
                    Text(
                        "CATEGORÍA",
                        style = MetaMono.copy(fontSize = 9.sp, letterSpacing = 1.sp),
                        color = ColorTextDim
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        nodeTypes.forEach { type ->
                            val isSelected = selectedTypeId == type.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { onTypeChange(type.id) },
                                label = { Text(type.name, style = MetaMono.copy(fontSize = 10.sp)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ColorPlan.copy(alpha = 0.2f),
                                    selectedLabelColor = ColorPlan,
                                    containerColor = ColorSurface,
                                    labelColor = ColorTextDim
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = ColorBorder,
                                    selectedBorderColor = ColorPlan
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = onManageDetailsClick,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Tune, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Personalizar tipos y métricas",
                            style = TitleNode.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = ColorExec
                        )
                    }

                    // Metadata
                    if (schemas.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "VALORES PREDETERMINADOS",
                            style = MetaMono.copy(fontSize = 9.sp, letterSpacing = 1.sp),
                            color = ColorTextDim
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        schemas.forEach { schema ->
                            val fieldValue = fieldValues.find { it.schemaId == schema.id }?.value ?: ""
                            Row(
                                modifier = Modifier
                                    .padding(vertical = 6.dp)
                                    .fillMaxWidth()
                                    .background(ColorSurface, RoundedCornerShape(8.dp))
                                    .border(0.5.dp, ColorBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    schema.fieldLabel,
                                    style = TitleNode.copy(fontSize = 12.sp, color = ColorTextDim),
                                    modifier = Modifier.width(100.dp)
                                )
                                BasicTextField(
                                    value = fieldValue,
                                    onValueChange = { onFieldValueChange(schema.id, schema.fieldName, it) },
                                    modifier = Modifier.weight(1f),
                                    textStyle = TitleNode.copy(fontSize = 12.sp, color = ColorText),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = if (schema.fieldType == FieldType.NUMBER) KeyboardType.Number else KeyboardType.Text
                                    ),
                                    cursorBrush = SolidColor(ColorExec)
                                )
                                if (schema.unit != null) {
                                    Text(
                                        schema.unit,
                                        style = MetaMono.copy(fontSize = 10.sp),
                                        color = ColorTextDim,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
