package com.alan.routineos.ui.features.system.components

import android.util.Log
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
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.util.DateUtils
import com.alan.routineos.data.local.entities.RecurrenceType
import com.alan.routineos.ui.features.system.state.PlanningItemType
import com.alan.routineos.ui.features.system.state.PlanningItemUi
import com.alan.routineos.ui.features.system.state.PlanningStatus
import com.alan.routineos.ui.features.system.state.PlanningTargetUi
import com.alan.routineos.ui.theme.*
import java.util.Date

/**
 * REDESIGNED SYSTEM COMPONENTS — INTENTION-FIRST UX
 * Human-centric adaptations and planning.
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
    recurrence: RecurrenceType = RecurrenceType.NONE,
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
                    if (recurrence != RecurrenceType.NONE) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Repeat, null, tint = ColorTextDim, modifier = Modifier.size(10.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(recurrence.name, style = MetaMono.copy(fontSize = 8.sp), color = ColorTextDim)
                    }
                    Spacer(Modifier.weight(1f))
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
                    style = TitleNode.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
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

@Composable
fun PlanningItemCard(
    item: PlanningItemUi,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isCompleted = item.status == PlanningStatus.COMPLETED && item.type == PlanningItemType.TASK

    LaunchedEffect(item.type) {
        Log.d("PLANNING_DEBUG", "PLANNING TYPE RENDERED type=${item.type}")
    }

    val icon = when (item.type) {
        PlanningItemType.TASK -> Icons.Default.TaskAlt
        PlanningItemType.NOTE -> Icons.AutoMirrored.Filled.Note
        PlanningItemType.REMINDER -> Icons.Default.NotificationsNone
    }
    val iconColor = when (item.type) {
        PlanningItemType.TASK -> ColorExec
        PlanningItemType.NOTE -> Color.Gray
        PlanningItemType.REMINDER -> Color(0xFF2196F3)
    }
    val typeLabel = when (item.type) {
        PlanningItemType.TASK -> "TAREA"
        PlanningItemType.NOTE -> "NOTA"
        PlanningItemType.REMINDER -> "RECORDATORIO"
    }

    val (dueLabel, dueColor) = remember(item.dueDate, item.dueTime, isCompleted, item.type) {
        item.dueDate?.let { due ->
            val startOfToday = DateUtils.getStartOfDay()
            val startOfDue = DateUtils.getStartOfDay(due)
            val isOverdue =
                startOfDue < startOfToday && !isCompleted && item.type == PlanningItemType.TASK
            val isToday = startOfDue == startOfToday

            val dateLabel = when {
                isToday -> "Hoy"
                startOfDue == startOfToday + 24 * 3600 * 1000L -> "Mañana"
                else -> DateUtils.formatShortDate(due)
            }

            val statusLabel = when (item.type) {
                PlanningItemType.TASK -> when {
                    isOverdue -> "Atrasada: "
                    isToday -> "Vence hoy: "
                    else -> "Vence: "
                }

                PlanningItemType.NOTE -> "Referencia: "
                PlanningItemType.REMINDER -> "Recordar: "
            }

            val label =
                if (item.dueTime != null) "$statusLabel$dateLabel ${item.dueTime}" else "$statusLabel$dateLabel"
            val color = when {
                isCompleted -> ColorTextMuted
                isOverdue -> Color(0xFFEF5350)
                isToday && item.type == PlanningItemType.TASK -> ColorExec
                item.type == PlanningItemType.REMINDER -> Color(0xFF2196F3)
                else -> ColorTextMuted
            }

            if (isOverdue && !isCompleted) {
                Log.d("PLANNING_DEBUG", "PLANNING TASK OVERDUE id=${item.id}")
            }

            label to color
        } ?: (null to ColorTextMuted)
    }

    Surface(
        color = ColorSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (isCompleted) ColorExec.copy(alpha = 0.2f) else ColorBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isCompleted) 0.5f else 1f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.type == PlanningItemType.TASK) {
                IconButton(onClick = onToggle, modifier = Modifier.size(24.dp)) {
                    Icon(
                        if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        null,
                        tint = if (isCompleted) ColorExec else ColorTextDim,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        null,
                        tint = iconColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = typeLabel,
                        style = MetaMono.copy(fontSize = 7.sp, fontWeight = FontWeight.Bold),
                        color = iconColor
                    )
                }

                if (item.title.isNotBlank()) {
                    Text(
                        text = item.title,
                        style = TitleNode.copy(
                            fontSize = 14.sp,
                            fontWeight = if (item.type == PlanningItemType.NOTE && item.description.isNullOrBlank()) FontWeight.Bold else FontWeight.Medium,
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                        ),
                        color = if (isCompleted) ColorTextMuted else ColorText
                    )
                }

                if (!item.description.isNullOrBlank()) {
                    Text(
                        text = item.description,
                        style = if (item.type == PlanningItemType.NOTE)
                            TitleNode.copy(fontSize = 13.sp, color = ColorText)
                        else MetaMono.copy(fontSize = 10.sp, color = ColorTextDim),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                if (dueLabel != null) {
                    Text(
                        text = dueLabel,
                        style = MetaMono.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = dueColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (item.relatedNodePath != null) {
                    Text(
                        text = "Relacionado con: ${item.relatedNodePath}",
                        style = MetaMono.copy(fontSize = 9.sp),
                        color = ColorTextMuted,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            var showMenu by remember { mutableStateOf(false) }

            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.MoreVert,
                        null,
                        tint = ColorTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, null)
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Eliminar") },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, null)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewAdaptationSheet(
    onDismiss: () -> Unit,
    onConfirm: (label: String, rangeType: Int, recurrence: RecurrenceType) -> Unit
) {
    var intention by remember { mutableStateOf<AdaptationIntention?>(null) }
    var strategy by remember { mutableStateOf(AdjustmentStrategy.PAUSE_ALL) }
    var rangeType by remember { mutableIntStateOf(0) } // 0: Only this day, 1: All week
    var recurrence by remember { mutableStateOf(RecurrenceType.NONE) }

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
                text = "¿Qué cambio de rutina necesitas?",
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
                            if (recurrence != RecurrenceType.NONE) recurrence = RecurrenceType.NONE
                        }
                        DateModeButton("Toda esta semana", rangeType == 1, Modifier.weight(1f)) {
                            rangeType = 1
                            if (recurrence != RecurrenceType.NONE) recurrence = RecurrenceType.NONE
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 2.5 Recurrence (Subfix 12.2)
                    Text(
                        text = "¿Se repite?",
                        style = TitleNode.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                        color = ColorText
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RecurrenceChip("No se repite", recurrence == RecurrenceType.NONE) {
                            recurrence = RecurrenceType.NONE
                        }
                        RecurrenceChip("Cada semana", recurrence == RecurrenceType.WEEKLY) {
                            recurrence = RecurrenceType.WEEKLY
                            rangeType = 0 // Recurrence usually applies to single day patterns
                        }
                        RecurrenceChip("Cada mes", recurrence == RecurrenceType.MONTHLY) {
                            recurrence = RecurrenceType.MONTHLY
                            rangeType = 0
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
                            onConfirm(
                                intention?.label ?: "Adaptación",
                                rangeType,
                                recurrence
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("APLICAR CAMBIO", style = TitleNode.copy(color = Color.White))
                    }
                }
            }
        }
    }
}

@Composable
private fun RecurrenceChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) ColorPlan.copy(alpha = 0.1f) else ColorSurface,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (isSelected) ColorPlan else ColorBorder
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = TitleNode.copy(
                fontSize = 11.sp,
                color = if (isSelected) ColorText else ColorTextDim
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPlanningItemSheet(
    targets: List<PlanningTargetUi>,
    editingItem: PlanningItemUi? = null,
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String?, type: PlanningItemType, nodeId: String?, nodePath: String?, dueDate: Long?, dueTime: String?) -> Unit
) {
    val isEditMode = editingItem != null

    var title by remember(editingItem?.id) {
        mutableStateOf(editingItem?.title.orEmpty())
    }

    var desc by remember(editingItem?.id) {
        mutableStateOf(editingItem?.description.orEmpty())
    }

    var type by remember(editingItem?.id) {
        mutableStateOf(editingItem?.type ?: PlanningItemType.TASK)
    }

    var dueDate by remember(editingItem?.id) {
        mutableStateOf(editingItem?.dueDate)
    }

    var dueTime by remember(editingItem?.id) {
        mutableStateOf(editingItem?.dueTime)
    }

    var selectedTarget by remember(editingItem?.id, targets) {
        mutableStateOf(
            editingItem?.relatedNodeId?.let { nodeId ->
                targets.find { it.nodeId == nodeId }
            }
        )
    }

    val titleLabel = when (type) {
        PlanningItemType.TASK -> "¿Qué tienes que hacer?"
        PlanningItemType.NOTE -> "¿Qué quieres recordar o anotar?"
        PlanningItemType.REMINDER -> "¿Qué necesitas recordar?"
    }

    val dateLabel = when (type) {
        PlanningItemType.TASK -> "FECHA LÍMITE"
        PlanningItemType.NOTE -> "FECHA DE REFERENCIA"
        PlanningItemType.REMINDER -> "DÍA DEL RECORDATORIO"
    }

    val timeLabel =
        if (type == PlanningItemType.REMINDER) "HORA DEL RECORDATORIO" else "HORA (OPCIONAL)"

    val canConfirm = when (type) {
        PlanningItemType.TASK -> title.isNotBlank()
        PlanningItemType.NOTE -> title.isNotBlank() || desc.isNotBlank()
        PlanningItemType.REMINDER -> title.isNotBlank()
    }

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
                text = if (isEditMode) "Editar elemento" else "Nuevo elemento",
                style = TitleNode.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                color = ColorText
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (isEditMode) {
                Text(
                    text = "Tipo: ${type.name}",
                    style = MetaMono.copy(fontSize = 9.sp),
                    color = ColorTextMuted
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlanningTypeChip("Tarea", type == PlanningItemType.TASK) {
                        type = PlanningItemType.TASK
                        Log.d("PLANNING_DEBUG", "PLANNING TYPE SELECTED type=TASK")
                    }
                    PlanningTypeChip("Nota", type == PlanningItemType.NOTE) {
                        type = PlanningItemType.NOTE
                        Log.d("PLANNING_DEBUG", "PLANNING TYPE SELECTED type=NOTE")
                    }
                    PlanningTypeChip("Recordatorio", type == PlanningItemType.REMINDER) {
                        type = PlanningItemType.REMINDER
                        Log.d("PLANNING_DEBUG", "PLANNING TYPE SELECTED type=REMINDER")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(titleLabel, style = MetaMono) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = ColorBorder,
                    focusedBorderColor = ColorExec,
                    focusedLabelColor = ColorExec
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text("Detalle opcional", style = MetaMono) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = ColorBorder,
                    focusedBorderColor = ColorExec,
                    focusedLabelColor = ColorExec
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(dateLabel, style = MetaMono.copy(fontSize = 9.sp), color = ColorTextMuted)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val startOfToday = DateUtils.getStartOfDay()
                val startOfTomorrow = startOfToday + 24 * 3600 * 1000L

                SelectableChip("Sin fecha", dueDate == null) {
                    dueDate = null
                }
                SelectableChip("Hoy", dueDate == startOfToday) {
                    dueDate = startOfToday
                }
                SelectableChip("Mañana", dueDate == startOfTomorrow) {
                    dueDate = startOfTomorrow
                }
            }

            if (type == PlanningItemType.REMINDER && dueDate == null) {
                Text(
                    text = "Se recomienda asignar una fecha para el recordatorio.",
                    style = MetaMono.copy(fontSize = 8.sp, color = ColorExec.copy(alpha = 0.7f)),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            AnimatedVisibility(visible = dueDate != null) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(timeLabel, style = MetaMono.copy(fontSize = 9.sp), color = ColorTextMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SelectableChip("Sin hora", dueTime == null) {
                            dueTime = null
                        }
                        SelectableChip("23:59", dueTime == "23:59") {
                            dueTime = "23:59"
                        }
                        SelectableChip("08:00", dueTime == "08:00") {
                            dueTime = "08:00"
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SelectableChip("12:00", dueTime == "12:00") {
                            dueTime = "12:00"
                        }
                        SelectableChip("18:00", dueTime == "18:00") {
                            dueTime = "18:00"
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("RELACIONADO CON", style = MetaMono.copy(fontSize = 9.sp), color = ColorTextMuted)
            Spacer(modifier = Modifier.height(8.dp))

            SelectableChip("Sin vincular", selectedTarget == null) {
                selectedTarget = null
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (targets.isNotEmpty()) {
                targets.forEach { target ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clickable {
                                selectedTarget = target
                            },
                        color = if (selectedTarget?.nodeId == target.nodeId) ColorExec.copy(alpha = 0.1f) else ColorSurface,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            if (selectedTarget?.nodeId == target.nodeId) ColorExec else ColorBorder
                        )
                    ) {
                        Text(
                            text = target.path,
                            modifier = Modifier.padding(12.dp),
                            style = TitleNode.copy(fontSize = 11.sp),
                            color = if (selectedTarget?.nodeId == target.nodeId) ColorExec else ColorText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    onConfirm(
                        title,
                        desc.takeIf { it.isNotBlank() },
                        type,
                        selectedTarget?.nodeId,
                        selectedTarget?.path,
                        dueDate,
                        dueTime
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
                shape = RoundedCornerShape(12.dp),
                enabled = canConfirm
            ) {
                Text(
                    if (isEditMode) "GUARDAR CAMBIOS" else "CREAR",
                    style = TitleNode.copy(color = Color.White)
                )
            }
        }
    }
}

@Composable
private fun SelectableChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) ColorExec.copy(alpha = 0.1f) else ColorSurface,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (isSelected) ColorExec else ColorBorder
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = TitleNode.copy(
                fontSize = 12.sp,
                color = if (isSelected) ColorText else ColorTextDim
            )
        )
    }
}

@Composable
private fun PlanningTypeChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) ColorExec.copy(alpha = 0.1f) else ColorSurface,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (isSelected) ColorExec else ColorBorder
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = TitleNode.copy(
                fontSize = 12.sp,
                color = if (isSelected) ColorText else ColorTextDim
            )
        )
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
    var selectedAction by remember { mutableStateOf(2) } // 0: Cancel, 1: Move, 2: Keep

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
    selectedDate: Long,
    currentWeekStart: Long,
    onDateSelected: (Long) -> Unit,
    onNextWeek: () -> Unit,
    onPrevWeek: () -> Unit,
    adaptationCount: Int = 0,
    taskCount: Int = 0,
    noteCount: Int = 0
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
                val isSelected =
                    DateUtils.getStartOfDay(timestamp) == DateUtils.getStartOfDay(selectedDate)
                val isToday =
                    DateUtils.getStartOfDay(timestamp) == DateUtils.getStartOfDay(System.currentTimeMillis())

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

                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        if (adaptationCount > 0) Dot(Color(0xFFFF9800))
                        if (taskCount > 0) Dot(Color(0xFF2196F3))
                        if (noteCount > 0) Dot(Color.Gray)
                    }

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

@Composable
private fun Dot(color: Color) {
    Box(
        modifier = Modifier
            .size(3.dp)
            .background(color, CircleShape)
    )
}
