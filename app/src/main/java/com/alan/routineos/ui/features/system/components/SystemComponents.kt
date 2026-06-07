package com.alan.routineos.ui.features.system.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.util.DateUtils
import com.alan.routineos.ui.theme.*
import java.util.Date

/**
 * REDESIGNED SYSTEM COMPONENTS — INTENTION-FIRST UX
 * Human-centric adaptations replace technical configurations.
 */

enum class AdaptationType { CANCEL_DAY, VACATION, RESCHEDULE, REDUCED, SPECIAL }

enum class AdaptationIntention(val label: String, val icon: ImageVector) {
    VACATION("Vacaciones", Icons.Default.BeachAccess),
    CANCEL_DAY("Día cancelado", Icons.Default.EventBusy),
    SPECIAL_WEEK("Semana especial", Icons.Default.Star),
    RESCHEDULE("Reprogramar", Icons.Default.History),
    REDUCED("Rutina reducida", Icons.Default.ReduceCapacity),
    LIGHT_DAY("Día ligero", Icons.Default.LightMode),
    SUNDAY_MODE("Modo domingo", Icons.Default.Nightlight),
    EXAMS("Semana de exámenes", Icons.Default.School)
}

enum class AdjustmentStrategy(val label: String, val description: String, val icon: ImageVector) {
    PAUSE_ALL("Pausar todo", "Nada se genera estos días", Icons.Default.PauseCircle),
    KEEP_IMPORTANT("Solo importante", "Se mantiene lo crítico", Icons.Default.PriorityHigh),
    AUTO_RESCHEDULE("Reacomodar", "El sistema ajusta los horarios", Icons.Default.AutoMode),
    MANUAL("Elegir manualmente", "Configura actividad por actividad", Icons.Default.EditAttributes)
}

@Composable
fun AdaptationCard(
    dateRange: String,
    title: String,
    type: AdaptationType,
    onDelete: () -> Unit
) {
    val barColor = when (type) {
        AdaptationType.CANCEL_DAY -> Color(0xFFEF5350)
        AdaptationType.VACATION -> Color(0xFFFF9800)
        AdaptationType.RESCHEDULE -> Color(0xFF1565C0)
        AdaptationType.REDUCED -> Color(0xFFAB47BC)
        AdaptationType.SPECIAL -> Color(0xFF26A69A)
    }

    val label = when (type) {
        AdaptationType.CANCEL_DAY -> "Cancelado"
        AdaptationType.VACATION -> "Vacaciones"
        AdaptationType.RESCHEDULE -> "Reprogramado"
        AdaptationType.REDUCED -> "Reducida"
        AdaptationType.SPECIAL -> "Especial"
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
fun NewAdaptationSheet(
    onDismiss: () -> Unit,
    onConfirm: (label: String, type: String, rangeType: Int) -> Unit
) {
    var intention by remember { mutableStateOf<AdaptationIntention?>(null) }
    var strategy by remember { mutableStateOf(AdjustmentStrategy.PAUSE_ALL) }
    var rangeType by remember { mutableIntStateOf(0) } // 0: Only this day, 1: All week, 2: Custom

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
                text = "¿Qué adaptación necesitas?",
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
                AdaptationIntention.entries.forEach { item ->
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
                        DateModeButton("Solo este día", rangeType == 0, Modifier.weight(1f)) {
                            rangeType = 0
                        }
                        DateModeButton("Toda esta semana", rangeType == 1, Modifier.weight(1f)) {
                            rangeType = 1
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // 3. Strategy Selector
                    Text(
                        text = "¿Cómo quieres adaptar tu rutina?",
                        style = TitleNode.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                        color = ColorText
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AdjustmentStrategy.entries.forEach { item ->
                        val isSelected = strategy == item
                        StrategyOption(item, isSelected) { strategy = item }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // 5. Reactive Preview
                    ReactivePreviewBox(strategy)

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { 
                            onConfirm(intention?.label ?: "Adaptación", intention?.name ?: "CUSTOM", rangeType) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("APLICAR ADAPTACIÓN", style = TitleNode.copy(color = Color.White))
                    }
                }
            }
        }
    }
}

@Composable
private fun IntentionChip(
    item: AdaptationIntention,
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
    selectedDate: Long,
    currentWeekStart: Long,
    onDateSelected: (Long) -> Unit,
    onNextWeek: () -> Unit,
    onPrevWeek: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevWeek, modifier = Modifier.size(32.dp)) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = ColorTextDim)
            }
            Text(
                text = DateUtils.formatHeaderMonth(Date(currentWeekStart)),
                style = MetaMono.copy(fontSize = 11.sp, letterSpacing = 1.sp),
                color = ColorText
            )
            IconButton(onClick = onNextWeek, modifier = Modifier.size(32.dp)) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = ColorTextDim)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val days = DateUtils.getDaysOfWeek(currentWeekStart)
            val dayLabels = listOf("L", "MA", "MI", "J", "V", "S", "D")

            days.forEachIndexed { index, timestamp ->
                val isSelected = DateUtils.getStartOfDay(timestamp) == DateUtils.getStartOfDay(selectedDate)
                val isToday = DateUtils.getStartOfDay(timestamp) == DateUtils.getStartOfDay(System.currentTimeMillis())
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) ColorExec.copy(alpha = 0.12f) else Color.Transparent)
                        .border(
                            width = if (isToday) 1.dp else 0.dp,
                            color = if (isToday) ColorExec.copy(alpha = 0.3f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onDateSelected(timestamp) }
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = dayLabels[index],
                        style = MetaMono.copy(
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) ColorExec else ColorTextDim
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val cal = java.util.Calendar.getInstance()
                    cal.timeInMillis = timestamp
                    Text(
                        text = cal.get(java.util.Calendar.DAY_OF_MONTH).toString(),
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
}
