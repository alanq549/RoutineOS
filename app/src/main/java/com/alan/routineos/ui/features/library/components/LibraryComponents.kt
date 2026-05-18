package com.alan.routineos.ui.features.library.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.features.node_type_manager.components.schema.ManagerTextField
import com.alan.routineos.ui.theme.*

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    Surface(
        color = ColorSurface,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (query.isEmpty()) "Buscar actividad..." else query,
                style = TitleNode.copy(fontSize = 14.sp),
                color = if (query.isEmpty()) ColorTextDim else ColorText,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TemplateCard(
    name: String,
    colorHex: String,
    metaSummary: String,
    activeDays: List<Int>,
    timeRange: String? = null,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = ColorSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = try {
                                Color(android.graphics.Color.parseColor(colorHex))
                            } catch (e: Exception) {
                                ColorPlan
                            },
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = name,
                    style = TitleNode.copy(fontSize = 16.sp),
                    color = ColorText,
                    modifier = Modifier.weight(1f)
                )
                
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.MoreVert, null, tint = ColorTextDim, modifier = Modifier.size(20.dp))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar", style = MetaMono) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp)) }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar", color = Color.Red, style = MetaMono) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(18.dp)) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = metaSummary,
                    style = MetaMono.copy(fontSize = 10.sp),
                    color = ColorTextDim
                )
                if (!timeRange.isNullOrBlank()) {
                    Text(
                        text = "  •  $timeRange",
                        style = MetaMono.copy(fontSize = 10.sp, color = ColorExec)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val days = listOf("L", "M", "M", "J", "V", "S", "D")
                days.forEachIndexed { index, day ->
                    val isActive = activeDays.contains(index)
                    DayBadge(day, isActive)
                }
            }
        }
    }
}

@Composable
fun DayBadge(day: String, isActive: Boolean) {
    Box(
        modifier = Modifier
            .size(28.dp, 20.dp)
            .background(
                color = if (isActive) ColorExec.copy(alpha = 0.1f) else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
            .border(
                width = 0.5.dp,
                color = if (isActive) ColorExec else Color(0xFF222222),
                shape = RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day,
            style = MetaMono.copy(fontSize = 9.sp),
            color = if (isActive) Color.White else Color(0xFF444444)
        )
    }
}

@Composable
fun QuickCreateCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = Color(0xFF222222),
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
            )
        }
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp), tint = ColorTextDim)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Nueva actividad", style = TitleNode.copy(fontSize = 14.sp), color = ColorTextDim)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCreateSheet(
    onDismiss: () -> Unit,
    onCreate: (name: String, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ColorSurface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
            Text("¿QUÉ VAMOS A ORGANIZAR?", style = MetaMono.copy(fontSize = 10.sp), color = ColorTextDim)
            Spacer(modifier = Modifier.height(16.dp))
            ManagerTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "Ej. Gym, Estudiar, Meditación...",
                isFocused = true
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { onCreate(name, "#2196F3") },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
                shape = RoundedCornerShape(12.dp),
                enabled = name.isNotBlank()
            ) {
                Text("CONTINUAR", style = TitleNode.copy(color = Color.White))
            }
        }
    }
}
