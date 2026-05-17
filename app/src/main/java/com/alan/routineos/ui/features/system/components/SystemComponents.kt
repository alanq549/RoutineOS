package com.alan.routineos.ui.features.system.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.features.node_type_manager.components.schema.ManagerTextField
import com.alan.routineos.ui.theme.*

/**
 * REDESIGNED SYSTEM COMPONENTS — INTENTION-FIRST UX
 * A single reactive sheet replaces the multi-step wizard.
 * Focused on human intention, calm tech, and progressive disclosure.
 */

enum class AdjustmentType { CANCEL_DAY, VACATION, RESCHEDULE }

// --- Intentions (What's happening?) ---
enum class AdjustmentIntention(val label: String, val icon: ImageVector) {
    VACATION("Vacaciones", Icons.Default.BeachAccess),
    DAY_OFF("Día libre", Icons.Default.Nightlight),
    SICKNESS("Enfermedad", Icons.Default.Sick),
    EXAM("Examen", Icons.Default.School),
    EVENT("Evento", Icons.Default.Celebration),
    CUSTOM("Otro", Icons.Default.Tune)
}

// --- Strategies (How to handle it?) ---
enum class AdjustmentStrategy(val label: String, val description: String, val icon: ImageVector) {
    PAUSE_ALL("Pausar todo", "Nada se genera estos días", Icons.Default.PauseCircle),
    KEEP_IMPORTANT("Solo importante", "Se mantiene lo crítico", Icons.Default.PriorityHigh),
    AUTO_RESCHEDULE("Reacomodar", "El sistema busca huecos", Icons.Default.AutoMode),
    MANUAL("Elegir manualmente", "Configura cada actividad", Icons.Default.EditAttributes)
}

@Composable
fun AdjustmentCard(
    dateRange: String,
    title: String,
    type: AdjustmentType,
    onDelete: () -> Unit
) {
    val barColor = when (type) {
        AdjustmentType.CANCEL_DAY -> Color(0xFFEF5350)
        AdjustmentType.VACATION -> Color(0xFFFF9800)
        AdjustmentType.RESCHEDULE -> Color(0xFF1565C0)
    }

    val label = when (type) {
        AdjustmentType.CANCEL_DAY -> "Cancelado"
        AdjustmentType.VACATION -> "Vacaciones"
        AdjustmentType.RESCHEDULE -> "Reprogramado"
    }

    Surface(
        color = ColorSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(barColor)
            )
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(dateRange, style = MetaMono.copy(fontSize = 10.sp), color = ColorTextDim)
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = barColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = label.uppercase(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MetaMono.copy(fontSize = 7.sp, fontWeight = FontWeight.Bold),
                            color = barColor
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    title,
                    style = TitleNode.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                    color = ColorText
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.Top)
            ) {
                Icon(
                    Icons.Default.Close,
                    null,
                    modifier = Modifier.size(14.dp),
                    tint = ColorTextDim
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewAdjustmentSheet(
    onDismiss: () -> Unit,
    onConfirm: (title: String) -> Unit
) {
    var intention by remember { mutableStateOf<AdjustmentIntention?>(null) }
    var strategy by remember { mutableStateOf(AdjustmentStrategy.PAUSE_ALL) }
    var isRange by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ColorBg,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            Text(
                text = "¿Qué está pasando?",
                style = TitleNode.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                color = ColorText
            )
            
            Spacer(modifier = Modifier.height(20.dp))

            // 1. Intention Grid
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdjustmentIntention.entries.forEach { item ->
                    val isSelected = intention == item
                    IntentionChip(
                        item = item,
                        isSelected = isSelected,
                        onClick = { intention = item }
                    )
                }
            }

            AnimatedVisibility(
                visible = intention != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // 2. Human Date Selection
                    Text(
                        text = "¿Cuándo ocurre?",
                        style = TitleNode.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                        color = ColorText
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ColorSurface, RoundedCornerShape(10.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        DateModeButton("Solo un día", !isRange, Modifier.weight(1f)) { isRange = false }
                        DateModeButton("Varios días", isRange, Modifier.weight(1f)) { isRange = true }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (!isRange) {
                        DatePickerTrigger("Lunes, 12 de mayo")
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DatePickerTrigger("19 mayo", Modifier.weight(1f))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.padding(horizontal = 8.dp).size(16.dp), tint = ColorTextMuted)
                            DatePickerTrigger("23 mayo", Modifier.weight(1f))
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // 3. Simple Strategy Selector
                    Text(
                        text = "¿Cómo manejamos tu rutina?",
                        style = TitleNode.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                        color = ColorText
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AdjustmentStrategy.entries.forEach { item ->
                        val isSelected = strategy == item
                        StrategyOption(item, isSelected) { strategy = item }
                    }

                    // 4. Advanced disclosure
                    AnimatedVisibility(visible = strategy == AdjustmentStrategy.MANUAL) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            Text("AJUSTES GRANULARES", style = MetaMono, color = ColorTextMuted)
                            Spacer(modifier = Modifier.height(12.dp))
                            ManualActivityRow("Semestre 8", "07:00 AM")
                            ManualActivityRow("Push Day", "04:00 PM")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    ManagerTextField(
                        value = note,
                        onValueChange = { note = it },
                        placeholder = "Razón o nota (opcional)"
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // 5. REACTIVE PREVIEW
                    ReactivePreviewSection(strategy)

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { onConfirm(intention?.label ?: "Ajuste") },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("APLICAR CAMBIOS", style = TitleNode.copy(color = Color.White))
                    }
                }
            }
        }
    }
}

@Composable
private fun IntentionChip(
    item: AdjustmentIntention,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) ColorExec.copy(alpha = 0.1f) else ColorSurface,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, if (isSelected) ColorExec else ColorBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon, 
                contentDescription = null, 
                modifier = Modifier.size(16.dp), 
                tint = if (isSelected) ColorExec else ColorTextDim
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = item.label, 
                style = TitleNode.copy(fontSize = 13.sp), 
                color = if (isSelected) ColorText else ColorTextDim
            )
        }
    }
}

@Composable
private fun DateModeButton(label: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) ColorBg else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = TitleNode.copy(fontSize = 12.sp, fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal), color = if (isSelected) ColorText else ColorTextDim)
    }
}

@Composable
private fun DatePickerTrigger(value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = ColorSurface,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CalendarToday, null, Modifier.size(14.dp), tint = ColorTextDim)
            Spacer(Modifier.width(10.dp))
            Text(value, style = TitleNode.copy(fontSize = 14.sp), color = ColorText)
        }
    }
}

@Composable
private fun StrategyOption(item: AdjustmentStrategy, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        color = if (isSelected) ColorExec.copy(alpha = 0.05f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, if (isSelected) ColorExec else ColorBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isSelected) ColorExec else ColorTextMuted
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = item.label, 
                    style = TitleNode.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold), 
                    color = if (isSelected) ColorText else ColorTextDim
                )
                Text(
                    text = item.description, 
                    style = MetaMono.copy(fontSize = 10.sp), 
                    color = ColorTextMuted
                )
            }
        }
    }
}

@Composable
private fun ManualActivityRow(name: String, time: String) {
    var selectedAction by remember { mutableIntStateOf(2) } // 0: Pause, 1: Move, 2: Keep

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = TitleNode.copy(fontSize = 14.sp), color = ColorText)
            Text(time, style = MetaMono.copy(fontSize = 9.sp), color = ColorTextMuted)
        }
        
        Row(
            modifier = Modifier.background(ColorSurface, RoundedCornerShape(8.dp)).padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            ActionToggleIcon(Icons.Default.Pause, selectedAction == 0) { selectedAction = 0 }
            ActionToggleIcon(Icons.Default.Schedule, selectedAction == 1) { selectedAction = 1 }
            ActionToggleIcon(Icons.Default.Check, selectedAction == 2) { selectedAction = 2 }
        }
    }
}

@Composable
private fun ActionToggleIcon(icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(32.dp)
            .clickable { onClick() },
        color = if (isSelected) ColorBg else Color.Transparent,
        shape = RoundedCornerShape(6.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                modifier = Modifier.size(16.dp), 
                tint = if (isSelected) ColorExec else ColorTextMuted
            )
        }
    }
}

@Composable
private fun ReactivePreviewSection(strategy: AdjustmentStrategy) {
    Surface(
        color = ColorSurface.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("VISTA PREVIA", style = MetaMono.copy(fontSize = 8.sp, letterSpacing = 1.sp), color = ColorTextMuted)
            Spacer(modifier = Modifier.height(12.dp))
            
            val lines = when(strategy) {
                AdjustmentStrategy.PAUSE_ALL -> listOf("• Todas las actividades pausadas")
                AdjustmentStrategy.KEEP_IMPORTANT -> listOf("• Rutinas críticas activas", "• Actividades secundarias pausadas")
                AdjustmentStrategy.AUTO_RESCHEDULE -> listOf("• El sistema re-calcula tu tiempo", "• Horarios movidos automáticamente")
                AdjustmentStrategy.MANUAL -> listOf("• Cambios aplicados según tu selección")
            }
            
            lines.forEach { line ->
                Text(
                    text = line,
                    style = TitleNode.copy(fontSize = 12.sp, lineHeight = 18.sp),
                    color = ColorTextDim
                )
            }
        }
    }
}

@Composable
fun SystemWeekStrip(
    selectedDayIndex: Int,
    onDaySelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val weekDays = listOf(
            "L" to "12", "MA" to "13", "MI" to "14",
            "J" to "15", "V" to "16", "S" to "17", "D" to "18"
        )

        weekDays.forEachIndexed { index, (day, num) ->
            val isSelected = index == selectedDayIndex
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) ColorExec.copy(alpha = 0.12f) else Color.Transparent)
                    .clickable { onDaySelected(index) }
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = day,
                    style = MetaMono.copy(
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isSelected) ColorExec else ColorTextDim
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = num,
                    style = TitleNode.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    color = if (isSelected) ColorExec else ColorText
                )
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(3.dp)
                            .background(ColorExec, CircleShape)
                    )
                }
            }
        }
    }
}
