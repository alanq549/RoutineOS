package com.alan.routineos.ui.features.node_type_manager.components.sheets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.data.local.entities.ExecutionTrackingMode
import com.alan.routineos.data.local.entities.FieldType
import com.alan.routineos.ui.features.node_type_manager.components.schema.BoolOption
import com.alan.routineos.ui.features.node_type_manager.components.schema.FieldLabel
import com.alan.routineos.ui.features.node_type_manager.components.schema.ManagerTextField
import com.alan.routineos.ui.features.node_type_manager.internal.colors
import com.alan.routineos.ui.features.node_type_manager.internal.displayName
import com.alan.routineos.ui.features.node_type_manager.internal.shortLabel
import com.alan.routineos.ui.theme.ColorExec
import com.alan.routineos.ui.theme.MetaMono
import com.alan.routineos.ui.theme.TitleNode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSchemaSheet(
    onDismiss: () -> Unit,
    onAdd: (
        fieldName: String,
        label: String,
        type: FieldType,
        default: String?,
        unit: String?,
        editableInTemplate: Boolean,
        editableInExecution: Boolean,
        trackingMode: ExecutionTrackingMode
    ) -> Unit
) {
    var name         by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(FieldType.NUMBER) }

    // Flags FIX 18
    var editableInTemplate by remember { mutableStateOf(true) }
    var editableInExecution by remember { mutableStateOf(false) }
    var trackingMode by remember { mutableStateOf(ExecutionTrackingMode.NONE) }

    // Número / Texto
    var defaultText by remember { mutableStateOf("") }
    var unitText    by remember { mutableStateOf("") }

    // Booleano
    var boolDefault by remember { mutableStateOf(true) }   // true = Sí

    // Lista (SELECT)
    var optionInput  by remember { mutableStateOf("") }
    var selectOptions by remember { mutableStateOf(listOf<String>()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 14.dp)
                    .size(36.dp, 3.dp)
                    .background(Color(0xFF333333), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "NUEVO CAMPO",
                style = MetaMono.copy(fontSize = 11.sp),
                color = Color(0xFFE0E0E0)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // ── Nombre ──────────────────────────────────
            FieldLabel("Nombre del campo")
            Spacer(modifier = Modifier.height(4.dp))
            ManagerTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "ej: peso, aula, profesor…",
                isFocused = true
            )
            Spacer(modifier = Modifier.height(14.dp))

            // ── Tipo ────────────────────────────────────
            FieldLabel("Tipo de campo")
            Spacer(modifier = Modifier.height(8.dp))

            val types = listOf(FieldType.NUMBER, FieldType.TEXT, FieldType.BOOLEAN, FieldType.SELECT)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                types.forEach { ft ->
                    val isSel = selectedType == ft
                    val (bg, fg) = ft.colors()
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedType = ft },
                        color = if (isSel) bg else Color(0xFF1A1A1A),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSel) 1.dp else 0.5.dp,
                            if (isSel) fg.copy(alpha = .6f) else Color(0xFF2A2A2A)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 9.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                ft.shortLabel(),
                                style = MetaMono.copy(fontSize = 9.sp),
                                color = if (isSel) fg else Color(0xFF444444)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                ft.displayName(),
                                style = TitleNode.copy(fontSize = 10.sp),
                                color = if (isSel) fg else Color(0xFF777777)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // ── Sección condicional por tipo ────────────
            AnimatedVisibility(
                visible = selectedType == FieldType.NUMBER || selectedType == FieldType.TEXT,
                enter = expandVertically(),
                exit  = shrinkVertically()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FieldLabel("Valor por defecto")
                        Spacer(modifier = Modifier.height(4.dp))
                        ManagerTextField(
                            value = defaultText,
                            onValueChange = { defaultText = it },
                            placeholder = "—"
                        )
                    }
                    if (selectedType == FieldType.NUMBER) {
                        Column(modifier = Modifier.weight(1f)) {
                            FieldLabel("Unidad")
                            Spacer(modifier = Modifier.height(4.dp))
                            ManagerTextField(
                                value = unitText,
                                onValueChange = { unitText = it },
                                placeholder = "kg, rep, min…"
                            )
                        }
                    }
                }
            }

            // Booleano: toggle Sí / No
            AnimatedVisibility(
                visible = selectedType == FieldType.BOOLEAN,
                enter = expandVertically(),
                exit  = shrinkVertically()
            ) {
                Column {
                    FieldLabel("Valor por defecto")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BoolOption(
                            label = "Sí",
                            selected = boolDefault,
                            color = Color(0xFF4CAF50),
                            onClick = { boolDefault = true }
                        )
                        BoolOption(
                            label = "No",
                            selected = !boolDefault,
                            color = Color(0xFFEF5350),
                            onClick = { boolDefault = false }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "El usuario podrá cambiar este valor al registrar la actividad.",
                        style = TitleNode.copy(fontSize = 10.sp),
                        color = Color(0xFF444444)
                    )
                }
            }

            // Select: lista de opciones
            AnimatedVisibility(
                visible = selectedType == FieldType.SELECT,
                enter = expandVertically(),
                exit  = shrinkVertically()
            ) {
                Column {
                    FieldLabel("Opciones de la lista")
                    Spacer(modifier = Modifier.height(8.dp))

                    if (selectOptions.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF181818), RoundedCornerShape(8.dp))
                                .border(0.5.dp, Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            selectOptions.forEachIndexed { index, option ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${index + 1}.",
                                        style = TitleNode.copy(fontSize = 11.sp),
                                        color = Color(0xFF555555),
                                        modifier = Modifier.width(20.dp)
                                    )
                                    Text(
                                        option,
                                        modifier = Modifier.weight(1f),
                                        style = TitleNode.copy(fontSize = 12.sp),
                                        color = Color(0xFFE0E0E0)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(Color(0xFF1A1A1A), RoundedCornerShape(4.dp))
                                            .clickable { selectOptions = selectOptions - option },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Close, null,
                                            tint = Color(0xFF884444),
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                                if (index < selectOptions.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 10.dp),
                                        color = Color(0xFF222222),
                                        thickness = 0.5.dp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ManagerTextField(
                            value = optionInput,
                            onValueChange = { optionInput = it },
                            placeholder = "Nueva opción…",
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (optionInput.isNotBlank()) Color(0xFF1565C0) else Color(0xFF1A1A1A),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable(enabled = optionInput.isNotBlank()) {
                                    selectOptions = selectOptions + optionInput.trim()
                                    optionInput = ""
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add, null,
                                tint = if (optionInput.isNotBlank()) Color.White else Color(0xFF333333),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (selectOptions.isEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Agrega al menos una opción para poder guardar.",
                            style = TitleNode.copy(fontSize = 10.sp),
                            color = Color(0xFF444444)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color(0xFF2A2A2A))
            Spacer(modifier = Modifier.height(24.dp))

            // ── Configuración de Ejecución (FIX 18) ──────
            FieldLabel("Uso del campo")
            Spacer(modifier = Modifier.height(12.dp))

            UsageOption(
                title = "Solo dato base",
                desc = "Estructural, no se edita al ejecutar (ej: Aula, Profesor)",
                isSelected = !editableInExecution && trackingMode == ExecutionTrackingMode.NONE,
                onClick = {
                    editableInExecution = false
                    trackingMode = ExecutionTrackingMode.NONE
                }
            )

            UsageOption(
                title = "Ajuste del día",
                desc = "Se puede cambiar el valor base para hoy (ej: Prioridad)",
                isSelected = editableInExecution && trackingMode == ExecutionTrackingMode.OVERRIDE_VALUE,
                onClick = {
                    editableInExecution = true
                    trackingMode = ExecutionTrackingMode.OVERRIDE_VALUE
                }
            )

            UsageOption(
                title = "Registro de ejecución",
                desc = "Registra lo realizado hoy (ej: Repeticiones, Peso)",
                isSelected = editableInExecution && trackingMode == ExecutionTrackingMode.RECORD_ACTUAL,
                onClick = {
                    editableInExecution = true
                    trackingMode = ExecutionTrackingMode.RECORD_ACTUAL
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            val canSave = name.isNotBlank() && when (selectedType) {
                FieldType.SELECT -> selectOptions.isNotEmpty()
                else             -> true
            }

            Button(
                onClick = {
                    val default = when (selectedType) {
                        FieldType.BOOLEAN -> if (boolDefault) "true" else "false"
                        FieldType.SELECT  -> selectOptions.firstOrNull()
                        else              -> defaultText.ifBlank { null }
                    }
                    val unit = if (selectedType == FieldType.NUMBER) unitText.ifBlank { null } else null
                    onAdd(name, name, selectedType, default, unit, editableInTemplate, editableInExecution, trackingMode)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1565C0),
                    disabledContainerColor = Color(0xFF1A1A1A)
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = canSave
            ) {
                Text(
                    "AGREGAR CAMPO",
                    style = TitleNode.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                    color = if (canSave) Color.White else Color(0xFF444444)
                )
            }
        }
    }
}

@Composable
fun UsageOption(
    title: String,
    desc: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        color = if (isSelected) Color(0xFF1565C0).copy(alpha = 0.1f) else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 1.dp else 0.5.dp,
            if (isSelected) Color(0xFF1565C0) else Color(0xFF2A2A2A)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                title,
                style = TitleNode.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                color = if (isSelected) Color.White else Color(0xFFB0B0B0)
            )
            Text(
                desc,
                style = MetaMono.copy(fontSize = 9.sp),
                color = if (isSelected) Color.White.copy(alpha = 0.7f) else Color(0xFF666666)
            )
        }
    }
}
