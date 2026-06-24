package com.alan.routineos.ui.features.today.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.util.DateUtils
import com.alan.routineos.ui.features.system.state.PlanningItemType
import com.alan.routineos.ui.features.system.state.PlanningStatus
import com.alan.routineos.ui.features.today.state.ConflictResolutionUi
import com.alan.routineos.ui.features.today.state.PlanningIndicatorUi
import com.alan.routineos.ui.features.today.state.PlanningLinkedItemUi
import com.alan.routineos.ui.features.today.state.ResolvedNodeUi
import com.alan.routineos.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayActivityCard(
    id: String,
    time: String,
    title: String,
    subtitle: String? = null,
    statusLabel: String,
    statusColor: Color,
    barColor: Color,
    isCancelled: Boolean = false,
    isSkipped: Boolean = false,
    hasConflict: Boolean = false,
    isCurrent: Boolean = false,
    wasShiftedByDomino: Boolean = false,
    dominoReason: String? = null,
    conflictResolutionSuggestions: List<ConflictResolutionUi> = emptyList(),
    planningInfo: PlanningIndicatorUi? = null,
    resolvedNodes: List<ResolvedNodeUi> = emptyList(),
    isSpontaneousEvent: Boolean = false,
    onNodeToggle: (String) -> Unit = {},
    onNodeClick: (String) -> Unit = {},
    onComplete: () -> Unit = {},
    onSkip: (String) -> Unit = {},
    onPostpone: (String, Int) -> Unit = { _, _ -> },
    onCustomizeSchedule: (String) -> Unit = {},
    onDurationChange: (String, Int) -> Unit = { _, _ -> },
    onAdjustDuration: (String, Int) -> Unit = { _, _ -> },
    onPlanningToggle: (String) -> Unit = {},
    onResolveConflict: (ConflictResolutionUi) -> Unit = {},
    onEditSpontaneous: (String) -> Unit = {},
    onDeleteSpontaneous: (String) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(true) }
    var showMenu by remember { mutableStateOf(false) }
    var menuNodeId by remember { mutableStateOf<String?>(null) }
    
    // Planning Detail Sheet State
    var showPlanningSheet by remember { mutableStateOf(false) }
    var activePlanningNodeId by remember { mutableStateOf<String?>(null) }
    var activePlanningNodeName by remember { mutableStateOf("") }
    var activePlanningItems by remember { mutableStateOf<List<PlanningLinkedItemUi>>(emptyList()) }

    // Update activePlanningItems reactively if sheet is open
    LaunchedEffect(planningInfo, resolvedNodes, showPlanningSheet, activePlanningNodeId) {
        if (showPlanningSheet && activePlanningNodeId != null) {
             if (activePlanningNodeId == id) {
                 activePlanningItems = planningInfo?.items ?: emptyList()
             } else {
                 val subNode = resolvedNodes.find { it.id == activePlanningNodeId }
                 activePlanningItems = subNode?.planningInfo?.items ?: emptyList()
             }
             if (activePlanningItems.isEmpty()) {
                 showPlanningSheet = false
             }
        }
    }

    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd) {
            onComplete()
            dismissState.reset()
        } else if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onSkip(id)
            dismissState.reset()
        }
    }

    val effectiveAlpha = when {
        isCancelled -> 0.4f
        isSkipped || statusLabel.lowercase() == "skipped" -> 0.5f
        else -> 1f
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .alpha(effectiveAlpha)
    ) {
        // 1. TIEMPO
        Column(
            modifier = Modifier
                .width(64.dp)
                .padding(top = 16.dp, end = 8.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = time,
                style = MetaMono.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = if (isCurrent) ColorExec else if (wasShiftedByDomino) ColorExec else ColorText
            )
            if (wasShiftedByDomino && dominoReason != null) {
                Icon(
                    Icons.AutoMirrored.Filled.TrendingFlat,
                    contentDescription = null,
                    tint = ColorExec.copy(alpha = 0.7f),
                    modifier = Modifier.size(10.dp).padding(top = 2.dp)
                )
            }
        }

        // 2. EJE
        Column(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .size(if (isCurrent) 14.dp else 10.dp)
                    .background(if (isCurrent) ColorExec else barColor, CircleShape)
                    .border(2.dp, ColorBg, CircleShape)
            )
            Box(
                modifier = Modifier
                    .width(if (isCurrent) 2.dp else 1.dp)
                    .fillMaxHeight()
                    .background(if (isCurrent) ColorExec.copy(alpha = 0.6f) else ColorBorder.copy(alpha = 0.4f))
            )
        }

        // 3. CONTENIDO
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                val direction = dismissState.dismissDirection
                val bgColor = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50)
                    SwipeToDismissBoxValue.EndToStart -> Color(0xFF333333)
                    else -> Color.Transparent
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bgColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                ) {
                    Icon(
                        if (direction == SwipeToDismissBoxValue.StartToEnd) Icons.Default.Check else Icons.Default.Close,
                        null, tint = Color.White
                    )
                }
            },
            modifier = Modifier.weight(1f).padding(bottom = 12.dp, end = 16.dp)
        ) {
            Surface(
                color = if (isCurrent) ColorSurface2 else ColorSurface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNodeClick(id) },
                border = if (isCurrent) {
                    androidx.compose.foundation.BorderStroke(2.dp, ColorExec)
                } else {
                    androidx.compose.foundation.BorderStroke(0.5.dp, if (wasShiftedByDomino) ColorExec.copy(alpha = 0.3f) else ColorBorder)
                }
            ) {
                Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                    // Highlight bar for current activity
                    if (isCurrent) {
                        Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(ColorExec))
                    }

                    Column(modifier = Modifier.padding(14.dp).weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (hasConflict) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Conflict",
                                    tint = Color.Yellow,
                                    modifier = Modifier.size(16.dp).padding(end = 6.dp)
                                )
                            }
                            
                            if (isCurrent) {
                                Surface(
                                    color = ColorExec,
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = "AHORA",
                                        style = MetaMono.copy(fontSize = 8.sp, fontWeight = FontWeight.ExtraBold),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = title,
                                style = TitleNode.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                                color = ColorText,
                                modifier = Modifier.weight(1f),
                                textDecoration = if (isSkipped || statusLabel.lowercase() == "skipped") TextDecoration.LineThrough else null
                            )
                            
                            Surface(
                                color = statusColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = (if (isSkipped) "SKIPPED" else statusLabel).uppercase(),
                                    style = MetaMono.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }

                            Box {
                                IconButton(onClick = { 
                                    menuNodeId = id
                                    showMenu = true 
                                }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.MoreVert, null, tint = ColorTextDim, modifier = Modifier.size(20.dp))
                                }
                                
                                QuickActionsMenu(
                                    expanded = showMenu && menuNodeId == id,
                                    onDismiss = { showMenu = false },
                                    onPostpone = { onPostpone(id, it) },
                                    onSkip = { onSkip(id) },
                                    onCustomizeSchedule = { onCustomizeSchedule(id) },
                                    onDurationChange = { onDurationChange(id, it) },
                                    onAdjustDuration = { onAdjustDuration(id, it) },
                                    isSpontaneousEvent = isSpontaneousEvent,
                                    onEditSpontaneous = { onEditSpontaneous(id) },
                                    onDeleteSpontaneous = { onDeleteSpontaneous(id) }
                                )
                            }

                            if (resolvedNodes.isNotEmpty()) {
                                IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(24.dp)) {
                                    Icon(
                                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        null, tint = ColorTextDim, modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        if (dominoReason != null) {
                            Text(
                                text = dominoReason,
                                style = MetaMono.copy(fontSize = 9.sp, color = if (hasConflict) Color.Yellow else ColorExec.copy(alpha = 0.8f), fontWeight = FontWeight.Medium),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        } else if (subtitle != null) {
                            Text(subtitle, style = MetaMono.copy(fontSize = 10.sp), color = ColorTextDim)
                        }

                        // Conflict Resolution Section (Subfix 12.3)
                        if (hasConflict && conflictResolutionSuggestions.isNotEmpty()) {
                            ConflictResolutionSection(
                                suggestions = conflictResolutionSuggestions,
                                onResolve = onResolveConflict
                            )
                        }

                        // INDICADORES DE PLANIFICACIÓN (ROOT)
                        if (planningInfo != null && planningInfo.totalCount > 0) {
                            PlanningBadge(
                                info = planningInfo,
                                onClick = {
                                    Log.d("TODAY_PLANNING_DEBUG", "TODAY PLANNING BADGE CLICKED nodeId=$id")
                                    activePlanningNodeId = id
                                    activePlanningNodeName = title
                                    activePlanningItems = planningInfo.items
                                    showPlanningSheet = true
                                }
                            )
                            
                            PlanningPreview(planningInfo, id)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // DESGLOSE DE BLOQUES INTERNOS
                        AnimatedVisibility(visible = expanded && resolvedNodes.isNotEmpty()) {
                            Column(
                                modifier = Modifier.padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                resolvedNodes.forEach { node ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = ((node.depth - 1) * 12).dp)
                                            .background(if (node.isCompleted) Color.White.copy(alpha = 0.03f) else Color.Transparent, RoundedCornerShape(6.dp))
                                            .clickable(enabled = node.fields.isNotEmpty()) {
                                                onNodeClick(node.id)
                                            }
                                            .padding(8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                if (node.isCompleted) Icons.Default.CheckCircle else if (node.isSkipped) Icons.Default.Block else Icons.Default.RadioButtonUnchecked,
                                                null, 
                                                tint = if (node.isCompleted) ColorExec else if (node.isSkipped) Color.Gray else ColorTextDim, 
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clickable { onNodeToggle(node.id) }
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = node.name,
                                                    style = TitleNode.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium, textDecoration = if (node.isCompleted || node.isSkipped) TextDecoration.LineThrough else null),
                                                    color = if (node.isCompleted || node.isSkipped) ColorTextDim else ColorText
                                                )
                                                // INDICADORES DE PLANIFICACIÓN (SUB-NODE)
                                                if (node.planningInfo != null && node.planningInfo.totalCount > 0) {
                                                    PlanningBadge(
                                                        info = node.planningInfo,
                                                        onClick = {
                                                            Log.d("TODAY_PLANNING_DEBUG", "TODAY PLANNING BADGE CLICKED nodeId=${node.id}")
                                                            activePlanningNodeId = node.id
                                                            activePlanningNodeName = node.name
                                                            activePlanningItems = node.planningInfo.items
                                                            showPlanningSheet = true
                                                        }
                                                    )
                                                    
                                                    PlanningPreview(node.planningInfo, node.id)
                                                }
                                            }
                                            if (node.timeLabel != null) {
                                                Text(node.timeLabel, style = MetaMono.copy(fontSize = 9.sp), color = ColorExec)
                                            }
                                            
                                            Box {
                                                IconButton(onClick = { 
                                                    menuNodeId = node.id
                                                    showMenu = true 
                                                }, modifier = Modifier.size(20.dp)) {
                                                    Icon(Icons.Default.MoreVert, null, tint = ColorTextDim, modifier = Modifier.size(16.dp))
                                                }
                                                QuickActionsMenu(
                                                    expanded = showMenu && menuNodeId == node.id,
                                                    onDismiss = { showMenu = false },
                                                    onPostpone = { onPostpone(node.id, it) },
                                                    onSkip = { onSkip(node.id) },
                                                    onCustomizeSchedule = { onCustomizeSchedule(node.id) },
                                                    onDurationChange = { onDurationChange(node.id, it) },
                                                    onAdjustDuration = { onAdjustDuration(node.id, it) }
                                                )
                                            }
                                        }
                                        
                                        if (node.fields.isNotEmpty()) {
                                            Row(
                                                modifier = Modifier.padding(top = 6.dp, start = 26.dp),
                                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                                            ) {
                                                node.fields.forEach { field ->
                                                    Column {
                                                        Text(field.label.uppercase(), style = MetaMono.copy(fontSize = 7.sp, letterSpacing = 0.5.sp), color = ColorTextDim)
                                                        Text(field.value, style = MetaMono.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = if (node.isCompleted) ColorTextDim else Color.White)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPlanningSheet && activePlanningItems.isNotEmpty()) {
        ModalBottomSheet(
            onDismissRequest = { showPlanningSheet = false },
            containerColor = ColorBg,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            LaunchedEffect(Unit) {
                Log.d("TODAY_PLANNING_DEBUG", "TODAY PLANNING SHEET OPENED count=${activePlanningItems.size}")
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 48.dp)
            ) {
                Text(
                    text = "Pendientes vinculados",
                    style = TitleNode.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = ColorText
                )
                Text(
                    text = activePlanningNodeName,
                    style = MetaMono.copy(fontSize = 12.sp),
                    color = ColorTextDim
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(activePlanningItems, key = { it.id }) { item ->
                        Log.d("TODAY_PLANNING_DEBUG", "TODAY PLANNING ITEM RENDERED id=${item.id}")
                        PlanningLinkedItemRow(item, onToggle = { onPlanningToggle(item.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun ConflictResolutionSection(
    suggestions: List<ConflictResolutionUi>,
    onResolve: (ConflictResolutionUi) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(Color.Yellow.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .border(0.5.dp, Color.Yellow.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, null, tint = Color.Yellow, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(8.dp))
            Text("Conflicto detectado", style = TitleNode.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold), color = Color.Yellow)
        }
        Spacer(Modifier.height(8.dp))
        suggestions.forEach { suggestion ->
            Button(
                onClick = { onResolve(suggestion) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorSurface, contentColor = ColorText),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder)
            ) {
                Text(suggestion.label, style = MetaMono.copy(fontSize = 10.sp))
            }
        }
    }
}

@Composable
fun PlanningPreview(info: PlanningIndicatorUi, nodeId: String) {
    val previewLimit = 2
    val previewItems = info.items.take(previewLimit)
    val hiddenCount = (info.totalCount - previewLimit).coerceAtLeast(0)

    LaunchedEffect(nodeId, info.totalCount) {
        Log.d("TODAY_PLANNING_DEBUG", "TODAY PREVIEW GENERATED nodeId=$nodeId count=${info.totalCount}")
        Log.d("TODAY_PLANNING_DEBUG", "TODAY PREVIEW DISPLAYED nodeId=$nodeId")
        if (hiddenCount > 0) {
            Log.d("TODAY_PLANNING_DEBUG", "TODAY PREVIEW TRUNCATED nodeId=$nodeId hidden=$hiddenCount")
        }
    }

    Column(modifier = Modifier.padding(top = 4.dp, start = 4.dp)) {
        previewItems.forEach { item ->
            val icon = when(item.type) {
                PlanningItemType.TASK -> "📌"
                PlanningItemType.NOTE -> "📝"
                PlanningItemType.REMINDER -> "🔔"
            }
            Text(
                text = "$icon ${item.title}",
                style = MetaMono.copy(fontSize = 10.sp),
                color = ColorTextDim,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (hiddenCount > 0) {
            Text(
                text = "+$hiddenCount más",
                style = MetaMono.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                color = ColorTextMuted,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun PlanningLinkedItemRow(
    item: PlanningLinkedItemUi,
    onToggle: () -> Unit
) {
    val isCompleted = item.status == PlanningStatus.COMPLETED
    val (icon, color) = when(item.urgency) {
        0 -> Icons.Default.Error to Color(0xFFEF5350)
        1 -> Icons.Default.PriorityHigh to ColorExec
        else -> Icons.Default.PushPin to ColorTextDim
    }
    
    val dateLabel = remember(item.dueDate, item.dueTime) {
        item.dueDate?.let { due ->
            val startOfToday = DateUtils.getStartOfDay()
            val startOfDue = DateUtils.getStartOfDay(due)
            val dateLabel = when {
                startOfDue == startOfToday -> "Hoy"
                startOfDue == startOfToday + 24 * 3600 * 1000L -> "Mañana"
                startOfDue < startOfToday -> DateUtils.formatShortDate(due)
                else -> DateUtils.formatShortDate(due)
            }
            if (item.dueTime != null) "$dateLabel ${item.dueTime}" else dateLabel
        }
    }

    Surface(
        color = ColorSurface,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, if (isCompleted) ColorExec.copy(alpha = 0.2f) else ColorBorder),
        modifier = Modifier.fillMaxWidth().alpha(if (isCompleted) 0.5f else 1f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
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
                Icon(icon, null, tint = color.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = TitleNode.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium, textDecoration = if (isCompleted) TextDecoration.LineThrough else null),
                    color = if (isCompleted) ColorTextMuted else ColorText
                )
                if (dateLabel != null) {
                    Text(
                        text = (if (item.type == PlanningItemType.NOTE) "Referencia: " else "Vence: ") + dateLabel,
                        style = MetaMono.copy(fontSize = 10.sp),
                        color = if (isCompleted) ColorTextMuted else color
                    )
                }
            }
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = item.type.name,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MetaMono.copy(fontSize = 7.sp, fontWeight = FontWeight.Bold),
                    color = if (isCompleted) ColorTextMuted else color
                )
            }
        }
    }
}

@Composable
fun PlanningBadge(
    info: PlanningIndicatorUi,
    onClick: () -> Unit
) {
    val (text, color, icon) = when {
        info.overdueCount > 0 -> Triple(
            "${info.overdueCount} atrasada${if (info.overdueCount > 1) "s" else ""}",
            Color(0xFFEF5350),
            Icons.Default.Error
        )
        info.todayCount > 0 -> Triple(
            "${info.todayCount} vence${if (info.todayCount > 1) "n" else ""} hoy",
            ColorExec,
            Icons.Default.PriorityHigh
        )
        else -> Triple(
            "${info.totalCount} pendiente${if (info.totalCount > 1) "s" else ""}",
            ColorTextDim,
            Icons.Default.PushPin
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(top = 4.dp)
            .clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(10.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            style = MetaMono.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

@Composable
fun QuickActionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onPostpone: (Int) -> Unit,
    onSkip: () -> Unit,
    onCustomizeSchedule: () -> Unit,
    onDurationChange: (Int) -> Unit,
    onAdjustDuration: (Int) -> Unit,
    isSpontaneousEvent: Boolean = false,
    onEditSpontaneous: () -> Unit = {},
    onDeleteSpontaneous: () -> Unit = {}
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(ColorSurface).border(0.5.dp, ColorBorder, RoundedCornerShape(8.dp))
    ) {
        if (isSpontaneousEvent) {
            DropdownMenuItem(
                text = { Text("Editar evento", style = TitleNode.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold)) },
                onClick = { onEditSpontaneous(); onDismiss() },
                leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp), tint = ColorExec) }
            )
            DropdownMenuItem(
                text = { Text("Eliminar evento", style = TitleNode.copy(fontSize = 12.sp, color = Color.Red)) },
                onClick = { onDeleteSpontaneous(); onDismiss() },
                leadingIcon = { Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp), tint = Color.Red) }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = ColorBorder.copy(alpha = 0.5f))
        }

        DropdownMenuItem(
            text = { Text("Posponer 15 min", style = TitleNode.copy(fontSize = 12.sp)) },
            onClick = { onPostpone(15); onDismiss() },
            leadingIcon = { Icon(Icons.Default.History, null, modifier = Modifier.size(18.dp)) }
        )
        DropdownMenuItem(
            text = { Text("Posponer 30 min", style = TitleNode.copy(fontSize = 12.sp)) },
            onClick = { onPostpone(30); onDismiss() },
            leadingIcon = { Icon(Icons.Default.History, null, modifier = Modifier.size(18.dp)) }
        )
        DropdownMenuItem(
            text = { Text("Extender 15 min (+)", style = TitleNode.copy(fontSize = 12.sp)) },
            onClick = { onAdjustDuration(15); onDismiss() },
            leadingIcon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp)) }
        )
        DropdownMenuItem(
            text = { Text("Reducir 15 min (-)", style = TitleNode.copy(fontSize = 12.sp)) },
            onClick = { onAdjustDuration(-15); onDismiss() },
            leadingIcon = { Icon(Icons.Default.Remove, null, modifier = Modifier.size(18.dp)) }
        )
        DropdownMenuItem(
            text = { Text("Ajustar duración a 30 min", style = TitleNode.copy(fontSize = 12.sp)) },
            onClick = { onDurationChange(30); onDismiss() },
            leadingIcon = { Icon(Icons.Default.Timer, null, modifier = Modifier.size(18.dp)) }
        )
        DropdownMenuItem(
            text = { Text("Saltar hoy", style = TitleNode.copy(fontSize = 12.sp)) },
            onClick = { onSkip(); onDismiss() },
            leadingIcon = { Icon(Icons.Default.Block, null, modifier = Modifier.size(18.dp)) }
        )
        DropdownMenuItem(
            text = { Text("Personalizar horario", style = TitleNode.copy(fontSize = 12.sp)) },
            onClick = { onCustomizeSchedule(); onDismiss() },
            leadingIcon = { Icon(Icons.Default.Event, null, modifier = Modifier.size(18.dp)) }
        )
    }
}
