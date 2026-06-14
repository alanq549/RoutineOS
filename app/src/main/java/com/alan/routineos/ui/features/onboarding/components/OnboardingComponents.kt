package com.alan.routineos.ui.features.onboarding.components

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.data.local.entities.FieldType
import com.alan.routineos.ui.features.onboarding.state.NodeMetadataSchemaDraft
import com.alan.routineos.ui.features.onboarding.state.NodeTypeDraft
import com.alan.routineos.ui.theme.*
import java.util.Locale

@Composable
fun StepRoutineName(
    name: String,
    onNameChange: (String) -> Unit
) {
    Column {
        Text(
            "Hola. Vamos a construir algo sólido.",
            style = MaterialTheme.typography.headlineMedium,
            color = ColorText,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Toda rutina exitosa comienza con un propósito claro. ¿Cómo llamarás a este nuevo espacio de disciplina?",
            style = MaterialTheme.typography.bodyLarge,
            color = ColorTextDim
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "DALE UNA IDENTIDAD",
            style = MetaMono.copy(fontSize = 10.sp, letterSpacing = 1.sp),
            color = ColorExec
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "Ej: Mi semana de Uni, Mañanas de gimnasio...",
                    color = ColorTextMuted
                )
            },
            textStyle = TitleNode.copy(fontSize = 16.sp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ColorExec,
                unfocusedBorderColor = ColorBorder,
                focusedContainerColor = ColorSurface,
                unfocusedContainerColor = ColorSurface,
                focusedTextColor = ColorText,
                unfocusedTextColor = ColorText
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun StepTypeChoice(
    onChoice: (Boolean) -> Unit
) {
    Column {
        Text(
            "Personalización",
            style = MaterialTheme.typography.headlineMedium,
            color = ColorText,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "¿Quieres configurar tipos de actividad personalizados y métricas ahora, o prefieres empezar con algo simple?",
            style = MaterialTheme.typography.bodyLarge,
            color = ColorTextDim
        )

        Spacer(modifier = Modifier.height(32.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onChoice(false) },
            color = ColorSurface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ColorBorder)
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Bolt, null, tint = ColorExec, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Text("Ahora no", style = TitleNode.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold), color = ColorText)
                    Text("Empezar rápido con bloques estándar.", style = MetaMono.copy(fontSize = 12.sp), color = ColorTextDim)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onChoice(true) },
            color = ColorSurface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ColorBorder)
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Tune, null, tint = ColorPlan, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Text("Personalizar", style = TitleNode.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold), color = ColorText)
                    Text("Definir mis propios tipos y métricas.", style = MetaMono.copy(fontSize = 12.sp), color = ColorTextDim)
                }
            }
        }
    }
}

@Composable
fun StepNodeTypes(
    nodeTypes: List<NodeTypeDraft>,
    onAdd: (String, Boolean) -> Unit,
    onRemove: (String) -> Unit
) {
    var newTypeName by remember { mutableStateOf("") }
    var hasMetrics by remember { mutableStateOf(false) }

    Column {
        Text(
            "Define tus bloques",
            style = MaterialTheme.typography.headlineSmall,
            color = ColorText,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Divide tu rutina en bloques claros (ej: Mañana, Tarde, Pasos). Esto te ayudará a visualizar mejor tu día.",
            style = MaterialTheme.typography.bodyMedium,
            color = ColorTextDim
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            color = ColorSurface,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ColorBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = newTypeName,
                    onValueChange = { newTypeName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ej: Bloque 1, Teoría, Cardio...") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorExec,
                        focusedTextColor = ColorText,
                        unfocusedTextColor = ColorText
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = hasMetrics,
                        onCheckedChange = { hasMetrics = it },
                        colors = CheckboxDefaults.colors(checkedColor = ColorExec)
                    )
                    Text(
                        "Quiero registrar datos en este bloque",
                        color = ColorTextDim,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            if (newTypeName.isNotBlank()) {
                                onAdd(newTypeName, hasMetrics)
                                newTypeName = ""
                                hasMetrics = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Text("Añadir")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(nodeTypes, key = { it.id }) { type ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF2A2A2A))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            type.name,
                            modifier = Modifier.weight(1f),
                            style = TitleNode.copy(fontSize = 14.sp),
                            color = ColorText
                        )
                        if (type.hasMetrics) {
                            Badge(
                                containerColor = ColorExec.copy(alpha = 0.2f),
                                contentColor = ColorExec
                            ) {
                                Text(
                                    "DATOS",
                                    modifier = Modifier.padding(4.dp),
                                    style = MetaMono.copy(fontSize = 8.sp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        IconButton(
                            onClick = { onRemove(type.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color.Red.copy(alpha = 0.3f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StepMetadataFields(
    nodeTypes: List<NodeTypeDraft>,
    onUpdateSchemas: (String, List<NodeMetadataSchemaDraft>) -> Unit
) {
    val typesWithMetrics = nodeTypes.filter { it.hasMetrics }

    Column {
        Text(
            "Mide lo que importa",
            style = MaterialTheme.typography.headlineMedium,
            color = ColorText,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Define qué datos específicos quieres registrar para cada bloque. Esto te permitirá ver tu progreso real.",
            color = ColorTextDim,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (typesWithMetrics.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No has marcado bloques con métricas.\nPuedes continuar si no necesitas registrar datos específicos por ahora.",
                    color = ColorTextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(typesWithMetrics, key = { it.id }) { type ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ColorSurface, RoundedCornerShape(16.dp))
                            .border(1.dp, ColorBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            type.name.uppercase(),
                            style = MetaMono.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = ColorExec
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        type.schemas.forEachIndexed { index, schema ->
                            Row(
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .fillMaxWidth()
                                    .background(Color(0xFF141414), RoundedCornerShape(10.dp))
                                    .border(0.5.dp, Color(0xFF222222), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    null,
                                    tint = ColorExec.copy(alpha = 0.4f),
                                    modifier = Modifier.size(16.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                BasicTextField(
                                    value = schema.fieldLabel,
                                    onValueChange = { newLabel ->
                                        val newSchemas = type.schemas.toMutableList()
                                        newSchemas[index] = schema.copy(fieldLabel = newLabel)
                                        onUpdateSchemas(type.id, newSchemas)
                                    },
                                    modifier = Modifier.weight(1f),
                                    textStyle = TitleNode.copy(color = ColorText, fontSize = 14.sp),
                                    cursorBrush = SolidColor(ColorExec),
                                    decorationBox = { innerTextField ->
                                        Box(contentAlignment = Alignment.CenterStart) {
                                            if (schema.fieldLabel.isEmpty()) {
                                                Text(
                                                    "Nombre del dato...",
                                                    style = TitleNode.copy(
                                                        color = ColorTextMuted,
                                                        fontSize = 14.sp
                                                    )
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )

                                IconButton(
                                    onClick = {
                                        val newSchemas = type.schemas.toMutableList()
                                        newSchemas.removeAt(index)
                                        onUpdateSchemas(type.id, newSchemas)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        null,
                                        tint = Color.Red.copy(alpha = 0.3f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Sugerencias rápidas (Quick Add)
                        val suggestions =
                            listOf("Cantidad", "Meta", "Avance", "Tiempo", "Observación")
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            suggestions.forEach { label ->
                                Box(
                                    modifier = Modifier
                                        .background(Color.Transparent, RoundedCornerShape(8.dp))
                                        .border(0.5.dp, ColorBorder, RoundedCornerShape(8.dp))
                                        .clickable {
                                            val schemas = type.schemas.toMutableList()
                                            schemas.add(
                                                NodeMetadataSchemaDraft(
                                                    fieldName = "field_${System.currentTimeMillis()}_${schemas.size}",
                                                    fieldLabel = label,
                                                    fieldType = if (label == "Observación") FieldType.TEXT else FieldType.NUMBER
                                                )
                                            )
                                            onUpdateSchemas(type.id, schemas)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        label,
                                        style = MetaMono.copy(fontSize = 10.sp),
                                        color = ColorTextDim
                                    )
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val schemas = type.schemas.toMutableList()
                                    schemas.add(
                                        NodeMetadataSchemaDraft(
                                            fieldName = "field_${System.currentTimeMillis()}_${schemas.size}",
                                            fieldLabel = "",
                                            fieldType = FieldType.NUMBER
                                        )
                                    )
                                    onUpdateSchemas(type.id, schemas)
                                },
                            color = Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                0.5.dp,
                                Color(0xFF2A2A2A)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    null,
                                    tint = ColorTextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Añadir campo personalizado",
                                    style = TitleNode.copy(fontSize = 11.sp),
                                    color = ColorTextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepSchedule(
    selectedDays: List<Int>,
    startTime: String,
    endTime: String,
    onToggleDay: (Int) -> Unit,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit
) {
    val context = LocalContext.current
    val days = listOf("L", "M", "M", "J", "V", "S", "D")

    fun showPicker(currentTime: String, onSelected: (String) -> Unit) {
        val parts = currentTime.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        TimePickerDialog(context, { _, h, m ->
            onSelected(String.format(Locale.US, "%02d:%02d", h, m))
        }, hour, minute, true).show()
    }

    Column {
        Text(
            "Dales un lugar en tu día",
            style = MaterialTheme.typography.headlineSmall,
            color = ColorText,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("¿Cuándo vas a dedicarle tiempo a esta nueva rutina?", color = ColorTextDim)

        Spacer(modifier = Modifier.height(32.dp))

        val isRangeInvalid = try {
            val s = startTime.split(":").map { it.toInt() }
            val e = endTime.split(":").map { it.toInt() }
            (e[0] * 60 + e[1]) <= (s[0] * 60 + s[1])
        } catch (ex: Exception) {
            false
        }

        if (isRangeInvalid) {
            Surface(
                color = Color.Red.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    "El horario de fin debe ser posterior al de inicio.",
                    color = Color.Red.copy(alpha = 0.8f),
                    modifier = Modifier.padding(12.dp),
                    style = TitleNode.copy(fontSize = 12.sp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Text("DÍAS ASIGNADOS", style = MetaMono.copy(fontSize = 9.sp), color = ColorExec)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            days.forEachIndexed { index, day ->
                val dayNum = index + 1
                val isSelected = selectedDays.contains(dayNum)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .background(
                            if (isSelected) ColorExec else Color(0xFF1A1A1A),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) ColorExec else ColorBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onToggleDay(dayNum) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        day,
                        color = if (isSelected) Color.White else ColorTextDim,
                        style = TitleNode.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("INICIO", style = MetaMono.copy(fontSize = 9.sp), color = ColorExec)
                Spacer(modifier = Modifier.height(12.dp))
                TimeDisplayBox(startTime) { showPicker(startTime, onStartTimeChange) }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("FIN", style = MetaMono.copy(fontSize = 9.sp), color = ColorExec)
                Spacer(modifier = Modifier.height(12.dp))
                TimeDisplayBox(endTime) { showPicker(endTime, onEndTimeChange) }
            }
        }
    }
}

@Composable
private fun TimeDisplayBox(time: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Schedule, null, tint = ColorExec, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                time,
                style = TitleNode.copy(fontSize = 16.sp, letterSpacing = 1.sp),
                color = Color.White
            )
        }
    }
}
