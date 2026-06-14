package com.alan.routineos.ui.features.system.tabs

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.util.DateUtils
import com.alan.routineos.data.local.entities.RecurrenceType
import com.alan.routineos.data.local.entities.ScheduleException
import com.alan.routineos.ui.features.system.components.AdaptationCard
import com.alan.routineos.ui.features.system.components.AdaptationType
import com.alan.routineos.ui.features.system.components.NewAdaptationSheet
import com.alan.routineos.ui.features.system.components.NewPlanningItemSheet
import com.alan.routineos.ui.features.system.components.PlanningItemCard
import com.alan.routineos.ui.features.system.components.SystemWeekStrip
import com.alan.routineos.ui.features.system.state.PlanningItemType
import com.alan.routineos.ui.features.system.state.PlanningItemUi
import com.alan.routineos.ui.features.system.state.PlanningSection
import com.alan.routineos.ui.features.system.state.PlanningStatus
import com.alan.routineos.ui.features.system.state.SystemUiState
import com.alan.routineos.ui.theme.ColorBg
import com.alan.routineos.ui.theme.ColorBorder
import com.alan.routineos.ui.theme.ColorSurface
import com.alan.routineos.ui.theme.ColorText
import com.alan.routineos.ui.theme.ColorTextDim
import com.alan.routineos.ui.theme.ColorTextMuted
import com.alan.routineos.ui.theme.MetaMono
import com.alan.routineos.ui.theme.TitleNode

@Composable
fun PlanningTab(
    state: SystemUiState,
    onSubTabSelected: (PlanningSection) -> Unit,
    onDateSelected: (Long) -> Unit,
    onNextWeek: () -> Unit,
    onPrevWeek: () -> Unit,
    onCreateAdaptation: (String, Int, RecurrenceType) -> Unit,
    onDeleteAdaptation: (ScheduleException) -> Unit,
    onCreatePlanningItem: (String, String?, PlanningItemType, String?, String?, Long?, String?) -> Unit,
    onUpdatePlanningItem: (String, String, String?, String?, String?, Long?, String?) -> Unit,
    onTogglePlanningItem: (String) -> Unit,
    onDeletePlanningItem: (String) -> Unit
) {
    var showNewAdaptation by remember { mutableStateOf(false) }
    var showNewPlanningItem by remember { mutableStateOf(false) }
    var editingPlanningItem by remember { mutableStateOf<PlanningItemUi?>(null) }
    var planningItemToDelete by remember { mutableStateOf<PlanningItemUi?>(null) }

    LaunchedEffect(Unit) {
        Log.d("PLANNING_DEBUG", "PLANNING SCREEN OPENED")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // SECCIÓN 1 — Calendario con indicadores
        SystemWeekStrip(
            selectedDate = state.selectedDate,
            currentWeekStart = state.currentWeekStart,
            onDateSelected = onDateSelected,
            onNextWeek = onNextWeek,
            onPrevWeek = onPrevWeek,
            adaptationCount = state.adaptations.size,
            taskCount = state.planningItems.count { it.type == PlanningItemType.TASK },
            noteCount = state.planningItems.count { it.type == PlanningItemType.NOTE }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Texto introductorio
        Text(
            text = "Organiza cambios temporales, tareas, notas y recordatorios alrededor de tus rutinas.",
            style = MetaMono.copy(fontSize = 9.sp, lineHeight = 14.sp),
            color = ColorTextDim,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // SECCIÓN 2 — Subtabs
        PlanningSubTabSelector(
            selectedSection = state.planningSubTab,
            onSelected = onSubTabSelected
        )

        Spacer(modifier = Modifier.height(24.dp))

        // SECCIÓN 3 — Lista de contenido
        Box(modifier = Modifier.weight(1f)) {
            when (state.planningSubTab) {
                PlanningSection.ROUTINE_CHANGES -> {
                    RoutineChangesList(
                        adaptations = state.adaptations,
                        onDelete = onDeleteAdaptation,
                        onAddClick = { showNewAdaptation = true }
                    )
                }

                PlanningSection.TASKS_AND_NOTES -> {
                    TasksAndNotesList(
                        items = state.planningItems,
                        onToggle = onTogglePlanningItem,
                        onEdit = { item ->
                            Log.d("PLANNING_DB_DEBUG", "PLANNING ITEM EDIT OPENED id=${item.id}")
                            editingPlanningItem = item
                        },
                        onDelete = { item ->
                            planningItemToDelete = item
                        },
                        onAddClick = { showNewPlanningItem = true }
                    )
                }
            }
        }
    }

    if (showNewAdaptation) {
        NewAdaptationSheet(
            onDismiss = { showNewAdaptation = false },
            onConfirm = { label, rangeType, recurrenceType ->
                onCreateAdaptation(label, rangeType, recurrenceType)
                showNewAdaptation = false
            }
        )
    }

    if (showNewPlanningItem || editingPlanningItem != null) {
        NewPlanningItemSheet(
            targets = state.planningTargets,
            editingItem = editingPlanningItem,
            onDismiss = {
                showNewPlanningItem = false
                editingPlanningItem = null
            },
            onConfirm = { title, desc, type, nodeId, nodePath, dueDate, dueTime ->
                val editing = editingPlanningItem
                if (editing != null) {
                    onUpdatePlanningItem(
                        editing.id,
                        title,
                        desc,
                        nodeId,
                        nodePath,
                        dueDate,
                        dueTime
                    )
                    editingPlanningItem = null
                } else {
                    onCreatePlanningItem(title, desc, type, nodeId, nodePath, dueDate, dueTime)
                    showNewPlanningItem = false
                }
            }
        )
    }

    planningItemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { planningItemToDelete = null },
            title = { Text("Eliminar") },
            text = { Text("¿Eliminar este elemento de planificación?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePlanningItem(item.id)
                        planningItemToDelete = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { planningItemToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun PlanningSubTabSelector(
    selectedSection: PlanningSection,
    onSelected: (PlanningSection) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .background(ColorSurface, RoundedCornerShape(10.dp))
            .padding(4.dp)
    ) {
        PlanningSection.entries.forEach { section ->
            val isSelected = selectedSection == section
            val title = when (section) {
                PlanningSection.ROUTINE_CHANGES -> "Cambios de rutina"
                PlanningSection.TASKS_AND_NOTES -> "Tareas y notas"
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (isSelected) ColorBg else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelected(section) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = TitleNode.copy(
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isSelected) ColorText else ColorTextDim
                )
            }
        }
    }
}

@Composable
private fun RoutineChangesList(
    adaptations: List<ScheduleException>,
    onDelete: (ScheduleException) -> Unit,
    onAddClick: () -> Unit
) {
    LaunchedEffect(adaptations.size) {
        Log.d("PLANNING_DEBUG", "ROUTINE CHANGES CONTENT SHOWN count=${adaptations.size}")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "CAMBIOS DE RUTINA",
            style = MetaMono.copy(fontSize = 9.sp, letterSpacing = 1.sp),
            color = ColorTextMuted,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Text(
            text = "Usa esto cuando tu horario real cambie por uno o varios días.",
            style = MetaMono.copy(fontSize = 8.sp),
            color = Color(0xFF555555),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 2.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (adaptations.isEmpty()) {
            EmptyPlanningState(
                title = "No hay cambios de rutina para este día.",
                subtitle = "Cuando tu rutina cambie, aparecerán aquí."
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp)
            ) {
                items(adaptations, key = { it.id }) { item ->
                    AdaptationCard(
                        dateRange = DateUtils.formatRange(item.dateFrom, item.dateTo),
                        title = item.label,
                        type = mapIntentionToType(item.label),
                        recurrence = item.recurrenceType,
                        onDelete = { onDelete(item) }
                    )
                }
            }
        }

        AddPlanningButton(text = "+ CAMBIO DE RUTINA", onClick = onAddClick)
    }
}

@Composable
private fun TasksAndNotesList(
    items: List<PlanningItemUi>,
    onToggle: (String) -> Unit,
    onEdit: (PlanningItemUi) -> Unit,
    onDelete: (PlanningItemUi) -> Unit,
    onAddClick: () -> Unit
) {
    val pendingTasks =
        items.filter { it.type == PlanningItemType.TASK && it.status == PlanningStatus.PENDING }
    val reminders = items.filter { it.type == PlanningItemType.REMINDER }
    val notes = items.filter { it.type == PlanningItemType.NOTE }
    val completedTasks =
        items.filter { it.type == PlanningItemType.TASK && it.status == PlanningStatus.COMPLETED }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "TAREAS Y NOTAS",
            style = MetaMono.copy(fontSize = 9.sp, letterSpacing = 1.sp),
            color = ColorTextMuted,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Text(
            text = "Planea pendientes asociados a actividades, materias o bloques.",
            style = MetaMono.copy(fontSize = 8.sp),
            color = Color(0xFF555555),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 2.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (items.isEmpty()) {
            EmptyPlanningState(
                title = "Todavía no tienes tareas ni notas para este día.",
                subtitle = "Pronto podrás vincular tareas, recordatorios y notas a una actividad o materia."
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp)
            ) {
                if (pendingTasks.isNotEmpty()) {
                    item {
                        SectionHeader("PENDIENTES (${pendingTasks.size})", ColorTextDim)
                    }
                    items(pendingTasks, key = { it.id }) { item ->
                        PlanningItemCard(
                            item = item,
                            onToggle = { onToggle(item.id) },
                            onEdit = { onEdit(item) },
                            onDelete = { onDelete(item) }
                        )
                    }
                }

                if (reminders.isNotEmpty()) {
                    item {
                        SectionHeader("RECORDATORIOS (${reminders.size})", Color(0xFF2196F3))
                    }
                    items(reminders, key = { it.id }) { item ->
                        PlanningItemCard(
                            item = item,
                            onToggle = { onToggle(item.id) },
                            onEdit = { onEdit(item) },
                            onDelete = { onDelete(item) }
                        )
                    }
                }

                if (notes.isNotEmpty()) {
                    item {
                        SectionHeader("NOTAS (${notes.size})", Color.Gray)
                    }
                    items(notes, key = { it.id }) { item ->
                        PlanningItemCard(
                            item = item,
                            onToggle = { onToggle(item.id) },
                            onEdit = { onEdit(item) },
                            onDelete = { onDelete(item) }
                        )
                    }
                }

                if (completedTasks.isNotEmpty()) {
                    item {
                        SectionHeader("COMPLETADAS (${completedTasks.size})", ColorTextMuted)
                    }
                    items(completedTasks, key = { it.id }) { item ->
                        PlanningItemCard(
                            item = item,
                            onToggle = { onToggle(item.id) },
                            onEdit = { onEdit(item) },
                            onDelete = { onDelete(item) }
                        )
                    }
                }
            }
        }

        AddPlanningButton(text = "+ TAREA O NOTA", onClick = onAddClick)
    }
}

@Composable
private fun SectionHeader(text: String, color: Color) {
    Text(
        text = text,
        style = MetaMono.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
        color = color,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun EmptyPlanningState(title: String, subtitle: String) {
    LaunchedEffect(title) {
        Log.d("PLANNING_DEBUG", "PLANNING EMPTY STATE SHOWN")
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                title,
                style = TitleNode.copy(fontSize = 14.sp),
                color = ColorTextDim,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                subtitle,
                style = MetaMono.copy(fontSize = 10.sp),
                color = ColorTextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AddPlanningButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = ColorBorder,
                style = Stroke(
                    width = 2f,
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
            Text(
                text = text,
                style = MetaMono.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = ColorTextMuted
            )
        }
    }
}

private fun mapIntentionToType(label: String): AdaptationType {
    return when {
        label.contains("Vacaciones", ignoreCase = true) -> AdaptationType.VACATION
        label.contains("Cancelado", ignoreCase = true) -> AdaptationType.CANCEL_DAY
        label.contains("Reprogramar", ignoreCase = true) -> AdaptationType.RESCHEDULE
        label.contains("Reducida", ignoreCase = true) -> AdaptationType.REDUCED
        else -> AdaptationType.SPECIAL
    }
}
