package com.alan.routineos.ui.features.system.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditAttributes
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sick
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.theme.ColorBg
import com.alan.routineos.ui.theme.ColorBorder
import com.alan.routineos.ui.theme.ColorExec
import com.alan.routineos.ui.theme.ColorSurface
import com.alan.routineos.ui.theme.ColorText
import com.alan.routineos.ui.theme.ColorTextDim
import com.alan.routineos.ui.theme.ColorTextMuted
import com.alan.routineos.ui.theme.MetaMono
import com.alan.routineos.ui.theme.TitleNode

/**
 * REDESIGNED SYSTEM COMPONENTS — INTENTION-FIRST UX
 * A single reactive sheet replaces the multi-step wizard.
 */

enum class AdjustmentType { CANCEL_DAY, VACATION, RESCHEDULE }

enum class AdjustmentIntention(val label: String, val icon: ImageVector) {
    VACATION("Vacaciones", Icons.Default.BeachAccess),
    DAY_OFF("Día libre", Icons.Default.Nightlight),
    SICKNESS("Enfermedad", Icons.Default.Sick),
    EXAM("Examen", Icons.Default.School),
    EVENT("Evento", Icons.Default.Celebration),
    CUSTOM("Otro", Icons.Default.Tune)
}

enum class AdjustmentStrategy(val label: String, val description: String, val icon: ImageVector) {
    PAUSE_ALL("Pausar todo", "Nada se genera estos días", Icons.Default.PauseCircle),
    KEEP_IMPORTANT("Solo importante", "Se mantiene lo crítico", Icons.Default.PriorityHigh),
    AUTO_RESCHEDULE("Reacomodar", "El sistema ajusta los horarios", Icons.Default.AutoMode),
    MANUAL("Elegir manualmente", "Configura actividad por actividad", Icons.Default.EditAttributes)
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

                    // 2. Dates
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
                        DateModeButton("Solo un día", !isRange, Modifier.weight(1f)) {
                            isRange = false
                        }
                        DateModeButton("Varios días", isRange, Modifier.weight(1f)) {
                            isRange = true
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    DatePickerTrigger(if (isRange) "19 - 23 de mayo" else "Lunes, 12 de mayo")

                    Spacer(modifier = Modifier.height(32.dp))

                    // 3. Strategy Selector
                    Text(
                        text = "¿Cómo quieres manejar esos días?",
                        style = TitleNode.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                        color = ColorText
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AdjustmentStrategy.entries.forEach { item ->
                        val isSelected = strategy == item
                        StrategyOption(item, isSelected) { strategy = item }
                    }

                    // 4. Advanced Options (Disclosure)
                    AnimatedVisibility(visible = strategy == AdjustmentStrategy.MANUAL) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            Text(
                                "CONFIGURACIÓN POR ACTIVIDAD",
                                style = MetaMono,
                                color = ColorTextMuted
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            ManualActivityRow("Semestre 8")
                            ManualActivityRow("Push Day")
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // 5. Reactive Preview
                    ReactivePreviewBox(strategy)

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { onConfirm(intention?.label ?: "Ajuste") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
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
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (isSelected) ColorExec else ColorBorder
        )
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
private fun DateModeButton(
    label: String,
    isSelected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) ColorBg else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = TitleNode.copy(
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (isSelected) ColorText else ColorTextDim
        )
    }
}

@Composable
private fun DatePickerTrigger(value: String) {
    Surface(
        color = ColorSurface,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder),
        modifier = Modifier.fillMaxWidth()
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
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (isSelected) ColorExec else ColorBorder
        )
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
private fun ManualActivityRow(name: String) {
    var selectedAction by remember { mutableIntStateOf(2) } // 0: Cancel, 1: Move, 2: Keep

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name, style = TitleNode.copy(fontSize = 14.sp), color = ColorText)

        Row(
            modifier = Modifier
                .background(ColorSurface, RoundedCornerShape(8.dp))
                .padding(2.dp),
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
private fun ReactivePreviewBox(strategy: AdjustmentStrategy) {
    Surface(
        color = ColorSurface.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "ESTO PASARÁ:",
                style = MetaMono.copy(letterSpacing = 1.sp),
                color = ColorTextMuted
            )
            Spacer(modifier = Modifier.height(12.dp))

            val text = when (strategy) {
                AdjustmentStrategy.PAUSE_ALL -> "• Todas las actividades pausadas\n• Tu racha se mantendrá congelada"
                AdjustmentStrategy.KEEP_IMPORTANT -> "• Rutinas críticas permanecen activas\n• El resto no se generará"
                AdjustmentStrategy.AUTO_RESCHEDULE -> "• El sistema buscará nuevos horarios\n• Se optimizarán los espacios vacíos"
                AdjustmentStrategy.MANUAL -> "• Cambios personalizados según tu selección"
            }

            Text(
                text = text,
                style = TitleNode.copy(fontSize = 12.sp, lineHeight = 18.sp),
                color = ColorTextDim
            )
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
