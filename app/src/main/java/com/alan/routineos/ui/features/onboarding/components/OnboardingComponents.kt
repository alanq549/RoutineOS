package com.alan.routineos.ui.features.onboarding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.data.local.entities.FieldType
import com.alan.routineos.ui.features.onboarding.state.NodeTypeDraft
import com.alan.routineos.ui.features.onboarding.state.NodeMetadataSchemaDraft
import com.alan.routineos.ui.theme.*

@Composable
fun StepRoutineName(name: String, onNameChange: (String) -> Unit) {
    Column {
        Text(
            "¿Qué quieres organizar?",
            style = MaterialTheme.typography.headlineMedium,
            color = ColorText,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Escribe el nombre de tu primera rutina principal.",
            style = MaterialTheme.typography.bodyMedium,
            color = ColorTextDim
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Ej: Mis días de trabajo, Mi semana de gym...") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ColorText,
                unfocusedTextColor = ColorText,
                focusedBorderColor = ColorExec,
                unfocusedBorderColor = ColorBorder
            )
        )
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
        Text("¿Qué tipos de actividad tiene?", style = MaterialTheme.typography.headlineSmall, color = ColorText)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Define los bloques que componen tu rutina.", style = MaterialTheme.typography.bodyMedium, color = ColorTextDim)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newTypeName,
                onValueChange = { newTypeName = it },
                modifier = Modifier.weight(1f),
                label = { Text("Nombre del tipo (ej: Ejercicio)") },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorExec)
            )
            IconButton(onClick = { 
                if(newTypeName.isNotBlank()) {
                    onAdd(newTypeName, hasMetrics)
                    newTypeName = ""
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = ColorExec)
            }
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = hasMetrics, onCheckedChange = { hasMetrics = it })
            Text("¿Tiene campos de seguimiento? (pesos, reps, etc.)", color = ColorTextDim, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(nodeTypes) { type ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = ColorSurface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(type.name, modifier = Modifier.weight(1f), color = ColorText)
                        if (type.hasMetrics) {
                            Badge(containerColor = ColorPending) { Text("Métricas", color = ColorBg) }
                        }
                        IconButton(onClick = { onRemove(type.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepMetadataFields(
    nodeTypes: List<NodeTypeDraft>,
    onUpdateSchemas: (String, List<NodeMetadataSchemaDraft>) -> Unit
) {
    val typesWithMetrics = nodeTypes.filter { it.hasMetrics }

    Column {
        Text("Campos de seguimiento", style = MaterialTheme.typography.headlineSmall, color = ColorText)
        Text("Define qué datos quieres registrar para cada tipo.", color = ColorTextDim)
        
        if (typesWithMetrics.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No has marcado tipos con métricas. Puedes continuar.", color = ColorTextMuted)
            }
        } else {
            LazyColumn {
                items(typesWithMetrics) { type ->
                    Text(type.name, style = TitleNode, color = ColorExec, modifier = Modifier.padding(top = 16.dp))
                    
                    Button(onClick = {
                        val schemas = type.schemas.toMutableList()
                        schemas.add(NodeMetadataSchemaDraft("campo_${schemas.size}", "Nuevo Campo", FieldType.NUMBER))
                        onUpdateSchemas(type.id, schemas)
                    }, modifier = Modifier.padding(vertical = 8.dp)) {
                        Text("Agregar campo a ${type.name}")
                    }
                    
                    type.schemas.forEach { schema ->
                        Text("- ${schema.fieldLabel} (${schema.fieldType})", color = ColorText, fontSize = 12.sp)
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
    onToggleDay: (Int) -> Unit,
    onTimeChange: (String) -> Unit
) {
    val days = listOf("L", "M", "X", "J", "V", "S", "D")

    Column {
        Text("¿En qué días se repite?", style = MaterialTheme.typography.headlineSmall, color = ColorText)
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            days.forEachIndexed { index, day ->
                val dayNum = index + 1
                val isSelected = selectedDays.contains(dayNum)
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (isSelected) ColorExec else ColorSurface,
                            RoundedCornerShape(8.dp)
                        )
                        .border(1.dp, ColorBorder, RoundedCornerShape(8.dp))
                        .clickable { onToggleDay(dayNum) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        day,
                        color = if (isSelected) Color.White else ColorTextDim,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Hora de inicio", style = TitleNode, color = ColorText)
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = startTime,
            onValueChange = onTimeChange,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorExec)
        )
    }
}
