package com.alan.routineos.ui.features.today.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.alan.routineos.notifications.NotificationPermissionHelper
import com.alan.routineos.ui.features.system.state.PlanningItemType
import com.alan.routineos.ui.features.today.components.TimelineIndicator
import com.alan.routineos.ui.features.today.components.TodayActivityCard
import com.alan.routineos.ui.features.today.state.PlanningLinkedItemUi
import com.alan.routineos.ui.features.today.state.TimelineEntryUi
import com.alan.routineos.ui.features.today.viewmodel.TodayViewModel
import com.alan.routineos.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onNavigateToExecute: (String) -> Unit,
    onNavigateToTemplateBuilder: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.updateNotificationPermissionStatus(isGranted)
    }

    var showQuickActions by remember { mutableStateOf(false) }
    var showEventSheet by remember { mutableStateOf(false) }
    var showCustomizeSheet by remember { mutableStateOf<TimelineEntryUi?>(null) }
    var editingEvent by remember { mutableStateOf<TimelineEntryUi?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<String?>(null) }
    var showPlanningSheet by remember { mutableStateOf(false) }
    var showActivityPicker by remember { mutableStateOf<String?>(null) }
    var planningType by remember { mutableStateOf(PlanningItemType.TASK) }

    LaunchedEffect(Unit) {
        viewModel.updateNotificationPermissionStatus(
            NotificationPermissionHelper.hasNotificationPermission(context)
        )
        viewModel.events.collectLatest { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        containerColor = ColorBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showQuickActions = true },
                containerColor = ColorExec,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Acciones Rápidas",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = uiState.dateLabel,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = ColorText
                    )
                    Text(
                        text = uiState.monthLabel,
                        style = MetaMono.copy(fontSize = 10.sp, letterSpacing = 2.sp),
                        color = ColorTextDim
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${uiState.completedCount}/${uiState.totalCount}",
                            style = MetaMono.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = ColorText
                        )
                        Text(text = "PASOS", style = MetaMono.copy(fontSize = 7.sp), color = ColorTextDim)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    CircularProgressIndicator(
                        progress = { if (uiState.totalCount == 0) 0f else uiState.completedCount.toFloat() / uiState.totalCount.toFloat() },
                        modifier = Modifier.size(32.dp),
                        color = ColorPlan,
                        trackColor = ColorBorder.copy(alpha = 0.3f),
                        strokeWidth = 3.dp
                    )
                }
            }

            // TIMELINE
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ColorExec)
                }
            } else if (uiState.timelineEntries.isEmpty() && uiState.unlinkedPlanningItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text(text = "No hay actividades para hoy", style = MetaMono, color = ColorTextDim)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    if (NotificationPermissionHelper.shouldRequestPermission() && !uiState.isNotificationPermissionGranted) {
                        item {
                            NotificationPermissionCard(
                                onRequestPermission = {
                                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                }
                            )
                        }
                    }

                    if (uiState.unlinkedPlanningItems.isNotEmpty()) {
                        item {
                            Text(
                                "PENDIENTES DEL DÍA",
                                style = MetaMono.copy(fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                                color = ColorTextMuted,
                                modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp)
                            )
                        }

                        val groupedItems = uiState.unlinkedPlanningItems.groupBy { it.type }
                        
                        // Orden específico: Tareas -> Recordatorios -> Notas
                        listOf(PlanningItemType.TASK, PlanningItemType.REMINDER, PlanningItemType.NOTE).forEach { type ->
                            groupedItems[type]?.let { items ->
                                items(items, key = { "unlinked_${it.id}" }) { item ->
                                    UnlinkedPlanningRow(item, onToggle = { viewModel.togglePlanningTask(item.id) })
                                }
                            }
                        }

                        item {
                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = ColorBorder.copy(alpha = 0.3f))
                            Spacer(Modifier.height(16.dp))
                        }
                    }

                    items(uiState.timelineEntries, key = { it.id }) { entry ->
                        if (entry.showTimeIndicatorBefore) TimelineIndicator()
                        TodayActivityCard(
                            id = entry.id,
                            time = entry.time,
                            title = entry.title,
                            subtitle = entry.subtitle,
                            statusLabel = entry.statusLabel,
                            statusColor = entry.statusColor,
                            barColor = entry.barColor,
                            isCancelled = entry.isCancelled,
                            isSkipped = entry.isSkipped,
                            hasConflict = entry.hasConflict,
                            isCurrent = entry.isCurrent,
                            wasShiftedByDomino = entry.wasShiftedByDomino,
                            dominoReason = entry.dominoReason,
                            conflictResolutionSuggestions = entry.conflictResolutionSuggestions,
                            planningInfo = entry.planningInfo,
                            resolvedNodes = entry.resolvedNodes,
                            isSpontaneousEvent = entry.isSpontaneousEvent,
                            onNodeToggle = { nodeId -> viewModel.toggleNodeCompletion(nodeId) },
                            onNodeClick = { nodeId -> onNavigateToExecute(nodeId) },
                            onComplete = { viewModel.toggleNodeCompletion(entry.id) },
                            onSkip = { nodeId -> viewModel.skipNode(nodeId) },
                            onPostpone = { nodeId, mins -> viewModel.postponeNode(nodeId, mins) },
                            onCustomizeSchedule = { _ -> showCustomizeSheet = entry },
                            onDurationChange = { nodeId, mins -> viewModel.changeDuration(nodeId, mins) },
                            onAdjustDuration = { nodeId, delta -> viewModel.adjustNodeDuration(nodeId, delta) },
                            onPlanningToggle = { taskId -> viewModel.togglePlanningTask(taskId) },
                            onResolveConflict = { resolution -> viewModel.resolveConflict(entry.id, resolution) },
                            onEditSpontaneous = { _ -> editingEvent = entry; showEventSheet = true },
                            onDeleteSpontaneous = { nodeId -> showDeleteConfirm = nodeId }
                        )
                    }
                }
            }
        }
    }

    // SHEETS PARA FIX 15
    if (showQuickActions) {
        TodayQuickActionsSheet(
            onDismiss = { showQuickActions = false },
            onAction = { action ->
                showQuickActions = false
                when (action) {
                    "event" -> showEventSheet = true
                    "task" -> { planningType = PlanningItemType.TASK; showPlanningSheet = true }
                    "note" -> { planningType = PlanningItemType.NOTE; showPlanningSheet = true }
                    "skip", "reschedule", "extend", "reduce" -> showActivityPicker = action
                }
            }
        )
    }

    if (showCustomizeSheet != null) {
        CustomizeScheduleSheet(
            entry = showCustomizeSheet!!,
            onDismiss = { showCustomizeSheet = null },
            onConfirm = { start, end ->
                viewModel.customizeSchedule(showCustomizeSheet!!.id, start, end)
                showCustomizeSheet = null
            }
        )
    }

    if (showActivityPicker != null) {
        ActivityPickerSheet(
            entries = uiState.timelineEntries,
            onDismiss = { showActivityPicker = null },
            onSelect = { entryId ->
                val action = showActivityPicker
                showActivityPicker = null
                when (action) {
                    "skip" -> viewModel.skipNode(entryId)
                    "reschedule" -> {
                        val entry = uiState.timelineEntries.find { it.id == entryId }
                        if (entry != null) showCustomizeSheet = entry
                    }
                    "extend" -> viewModel.adjustNodeDuration(entryId, 15)
                    "reduce" -> viewModel.adjustNodeDuration(entryId, -15)
                }
            }
        )
    }

    if (showEventSheet) {
        NewSpontaneousEventSheet(
            editingEvent = editingEvent,
            onDismiss = { 
                showEventSheet = false
                editingEvent = null
            },
            onConfirm = { title, time, dur ->
                if (editingEvent != null) {
                    viewModel.updateSpontaneousEvent(editingEvent!!.id, title, time, dur)
                } else {
                    viewModel.createSpontaneousEvent(title, time, dur)
                }
                showEventSheet = false
                editingEvent = null
            }
        )
    }

    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Eliminar evento espontáneo") },
            text = { Text("¿Estás seguro de que quieres eliminar este evento? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSpontaneousEvent(showDeleteConfirm!!)
                        showDeleteConfirm = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("ELIMINAR")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("CANCELAR")
                }
            },
            containerColor = ColorSurface,
            titleContentColor = ColorText,
            textContentColor = ColorTextDim
        )
    }

    if (showPlanningSheet) {
        QuickPlanningSheet(
            type = planningType,
            onDismiss = { showPlanningSheet = false },
            onConfirm = { title ->
                viewModel.createQuickPlanningItem(title, planningType)
                showPlanningSheet = false
            }
        )
    }
}

@Composable
fun NotificationPermissionCard(onRequestPermission: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        color = ColorSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = ColorPlan.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Notifications, null, tint = ColorPlan, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Activar recordatorios",
                    style = TitleNode.copy(fontSize = 14.sp),
                    color = ColorText
                )
                Text(
                    "Recibe avisos al iniciar tus actividades",
                    style = MetaMono.copy(fontSize = 10.sp),
                    color = ColorTextDim
                )
            }
            TextButton(onClick = onRequestPermission) {
                Text("ACTIVAR", style = MetaMono.copy(fontWeight = FontWeight.Bold), color = ColorExec)
            }
        }
    }
}

@Composable
fun UnlinkedPlanningRow(item: PlanningLinkedItemUi, onToggle: () -> Unit) {
    val isCompleted = item.status == com.alan.routineos.ui.features.system.state.PlanningStatus.COMPLETED
    val icon = when (item.type) {
        PlanningItemType.TASK -> if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked
        PlanningItemType.NOTE -> Icons.Default.Note
        PlanningItemType.REMINDER -> Icons.Default.Notifications
    }
    val iconColor = when (item.type) {
        PlanningItemType.TASK -> if (isCompleted) ColorExec else ColorTextDim
        PlanningItemType.NOTE -> ColorTextDim
        PlanningItemType.REMINDER -> Color(0xFF2196F3)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp)
            .background(ColorSurface, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (item.type == PlanningItemType.TASK) {
            IconButton(onClick = onToggle, modifier = Modifier.size(24.dp)) {
                Icon(
                    icon,
                    null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(16.dp))
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = TitleNode.copy(
                    fontSize = 13.sp,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                ),
                color = if (isCompleted) ColorTextDim else ColorText
            )
            if (item.dueTime != null) {
                Text(
                    text = item.dueTime,
                    style = MetaMono.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = if (item.type == PlanningItemType.REMINDER) Color(0xFF2196F3) else ColorTextDim
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayQuickActionsSheet(onDismiss: () -> Unit, onAction: (String) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = ColorSurface) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState())) {
            Text("Modificar agenda de hoy", style = TitleNode, color = ColorText)
            Spacer(Modifier.height(16.dp))
            QuickActionRow("Evento espontáneo", Icons.Default.Event, "Solo para hoy") { onAction("event") }
            QuickActionRow("Nueva tarea", Icons.Default.AddTask, "Añadir a pendientes") { onAction("task") }
            QuickActionRow("Nota rápida", Icons.Default.NoteAdd, "Recordatorio fugaz") { onAction("note") }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ColorBorder.copy(alpha = 0.5f))
            
            Text("Sobre actividades planeadas", style = MetaMono.copy(fontSize = 10.sp), color = ColorTextDim)
            Spacer(Modifier.height(8.dp))
            QuickActionRow("Omitir actividad", Icons.Default.Block, "Saltar algo de hoy") { onAction("skip") }
            QuickActionRow("Personalizar horario", Icons.Default.Schedule, "Define inicio y fin") { onAction("reschedule") }
            QuickActionRow("Extender tiempo", Icons.Default.Add, "Suma 15 minutos") { onAction("extend") }
            QuickActionRow("Reducir tiempo", Icons.Default.Remove, "Resta 15 minutos") { onAction("reduce") }
        }
    }
}

@Composable
fun QuickActionRow(title: String, icon: ImageVector, desc: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(color = ColorExec.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = ColorExec, modifier = Modifier.size(20.dp)) }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, style = TitleNode.copy(fontSize = 14.sp), color = ColorText)
            Text(desc, style = MetaMono.copy(fontSize = 10.sp), color = ColorTextDim)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityPickerSheet(entries: List<TimelineEntryUi>, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = ColorSurface) {
        Column(Modifier.padding(24.dp).padding(bottom = 40.dp)) {
            Text("Selecciona actividad", style = TitleNode)
            Spacer(Modifier.height(16.dp))
            if (entries.isEmpty()) {
                Text("No hay actividades planeadas para hoy", style = MetaMono, color = ColorTextDim)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(entries) { entry ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { onSelect(entry.id) },
                            color = ColorBg,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder)
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(entry.time, style = MetaMono.copy(fontSize = 10.sp), color = ColorExec)
                                Spacer(Modifier.width(12.dp))
                                Text(entry.title, style = TitleNode.copy(fontSize = 14.sp), color = ColorText)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizeScheduleSheet(
    entry: TimelineEntryUi,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    val initialStart = remember(entry) { entry.time.split("-").firstOrNull()?.trim() ?: "12:00" }
    val initialEnd = remember(entry) { entry.time.split("-").getOrNull(1)?.trim() ?: entry.endTime ?: "13:00" }
    
    var startTime by remember { mutableStateOf(initialStart) }
    var endTime by remember { mutableStateOf(initialEnd) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = ColorSurface) {
        Column(Modifier.padding(24.dp).padding(bottom = 40.dp)) {
            Text("Personalizar horario", style = TitleNode)
            Text(entry.title, style = MetaMono.copy(fontSize = 12.sp), color = ColorTextDim)
            Spacer(Modifier.height(24.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = startTime, 
                    onValueChange = { startTime = it }, 
                    label = { Text("Inicio") }, 
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = endTime, 
                    onValueChange = { endTime = it }, 
                    label = { Text("Fin") }, 
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = { onConfirm(startTime, endTime) }, 
                modifier = Modifier.fillMaxWidth().height(56.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
                shape = RoundedCornerShape(12.dp)
            ) { Text("GUARDAR HORARIO") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSpontaneousEventSheet(
    editingEvent: TimelineEntryUi? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int) -> Unit
) {
    var title by remember { mutableStateOf(editingEvent?.title ?: "") }
    var time by remember { mutableStateOf(editingEvent?.time?.split(" ")?.firstOrNull() ?: "12:00") }
    
    // Attempt to parse duration from time label if it's "HH:MM - HH:MM"
    val initialDuration = remember(editingEvent) {
        if (editingEvent?.time?.contains("-") == true) {
             try {
                 val parts = editingEvent.time.split("-").map { it.trim() }
                 val startMins = com.alan.routineos.core.util.ScheduleResolver.timeToMinutes(parts[0])
                 val endMins = com.alan.routineos.core.util.ScheduleResolver.timeToMinutes(parts[1])
                 var diff = endMins - startMins
                 if (diff < 0) diff += 1440
                 diff
             } catch (e: Exception) { 60 }
        } else {
            60
        }
    }
    
    var duration by remember { mutableIntStateOf(initialDuration) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = ColorSurface) {
        Column(Modifier.padding(24.dp).padding(bottom = 40.dp)) {
            Text(if (editingEvent != null) "Editar Evento" else "Nuevo Evento Hoy", style = TitleNode)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = title, 
                onValueChange = { title = it }, 
                label = { Text("¿Qué evento es?") }, 
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorExec, focusedLabelColor = ColorExec)
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = time, 
                    onValueChange = { time = it }, 
                    label = { Text("Hora") }, 
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = duration.toString(), 
                    onValueChange = { duration = it.toIntOrNull() ?: 60 }, 
                    label = { Text("Minutos") }, 
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onConfirm(title, time, duration) }, 
                modifier = Modifier.fillMaxWidth().height(56.dp), 
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
                shape = RoundedCornerShape(12.dp)
            ) { Text(if (editingEvent != null) "GUARDAR CAMBIOS" else "CREAR EVENTO SOLO HOY") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickPlanningSheet(type: PlanningItemType, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var title by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(24.dp).padding(bottom = 40.dp)) {
            Text(if (type == PlanningItemType.TASK) "Nueva Tarea para Hoy" else "Nueva Nota de Hoy", style = TitleNode)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = title, 
                onValueChange = { title = it }, 
                label = { Text("Título") }, 
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorExec, focusedLabelColor = ColorExec)
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onConfirm(title) }, 
                modifier = Modifier.fillMaxWidth().height(56.dp), 
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
                shape = RoundedCornerShape(12.dp)
            ) { Text("GUARDAR") }
        }
    }
}
