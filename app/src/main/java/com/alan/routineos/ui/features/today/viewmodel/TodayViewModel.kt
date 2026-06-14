package com.alan.routineos.ui.features.today.viewmodel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.core.util.DateUtils
import com.alan.routineos.core.util.ScheduleResolver
import com.alan.routineos.data.local.entities.FieldType
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeFieldValue
import com.alan.routineos.data.local.entities.NodeMetadataSchema
import com.alan.routineos.data.local.entities.NodeOverride
import com.alan.routineos.data.local.entities.NodeSchedule
import com.alan.routineos.data.local.entities.NodeStatus
import com.alan.routineos.data.local.entities.OverrideType
import com.alan.routineos.data.local.entities.PlanningItemEntity
import com.alan.routineos.data.local.entities.RoutineTemplate
import com.alan.routineos.data.local.entities.Schedule
import com.alan.routineos.data.repository.FieldValueRepository
import com.alan.routineos.data.repository.InstanceRepository
import com.alan.routineos.data.repository.MetadataSchemaRepository
import com.alan.routineos.data.repository.NodeOverrideRepository
import com.alan.routineos.data.repository.NodeRepository
import com.alan.routineos.data.repository.PlanningRepository
import com.alan.routineos.data.repository.ScheduleExceptionRepository
import com.alan.routineos.data.repository.ScheduleRepository
import com.alan.routineos.data.repository.TemplateRepository
import com.alan.routineos.ui.features.system.state.PlanningItemType
import com.alan.routineos.ui.features.system.state.PlanningStatus
import com.alan.routineos.ui.features.today.state.ConflictResolutionType
import com.alan.routineos.ui.features.today.state.ConflictResolutionUi
import com.alan.routineos.ui.features.today.state.PlanningIndicatorUi
import com.alan.routineos.ui.features.today.state.PlanningLinkedItemUi
import com.alan.routineos.ui.features.today.state.ResolvedFieldUi
import com.alan.routineos.ui.features.today.state.ResolvedNodeUi
import com.alan.routineos.ui.features.today.state.TimelineEntryUi
import com.alan.routineos.ui.features.today.state.TodayUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val instanceRepo: InstanceRepository,
    private val nodeRepo: NodeRepository,
    private val fieldValueRepo: FieldValueRepository,
    private val templateRepo: TemplateRepository,
    private val scheduleRepo: ScheduleRepository,
    private val exceptionRepo: ScheduleExceptionRepository,
    private val schemaRepo: MetadataSchemaRepository,
    private val nodeOverrideRepo: NodeOverrideRepository,
    private val planningRepo: PlanningRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TodayUiState(
            dateLabel = DateUtils.formatHeaderDate(),
            monthLabel = DateUtils.formatHeaderMonth(),
            isLoading = true
        )
    )

    val uiState: StateFlow<TodayUiState> = _uiState

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val today = DateUtils.getStartOfDay()
            val weekday = DateUtils.getDayOfWeek()

            Log.d("TODAY_DEBUG", "INITIALIZING TODAY - Date: $today, Weekday: $weekday")
            instanceRepo.dedupeInstancesForDate(today)
            generateInstanceIfNeeded(today, weekday)

            observeTodayData()
        }

        updateTimeTicker()
    }

    private data class ResolvedTimeBlock(
        val node: Node,
        val template: RoutineTemplate,
        val originalStart: String?,
        val originalEnd: String?,
        var effectiveStart: String?,
        var effectiveEnd: String?,
        var durationMinutes: Int,
        var isSkipped: Boolean = false,
        var appliedShiftMinutes: Int = 0,
        var shiftReason: String? = null
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeTodayData() {
        val today = DateUtils.getStartOfDay()
        val weekday = DateUtils.getDayOfWeek()

        viewModelScope.launch {
            combine(
                instanceRepo.getByDate(today),
                exceptionRepo.getActiveForDate(today),
                _uiState.map { it.currentTime }.distinctUntilChanged()
            ) { instances, exceptions, currentTime ->
                Triple(instances, exceptions, currentTime)
            }.flatMapLatest { (instances, exceptions, currentTime) ->
                if (instances.isEmpty()) {
                    Log.d(
                        "TODAY_DEBUG",
                        "OBSERVE: No instances found for today. Exceptions: ${exceptions.size}"
                    )
                    flowOf(emptyList<TimelineEntryUi>() to exceptions)
                } else {
                    Log.d(
                        "TODAY_DEBUG",
                        "OBSERVE: Found ${instances.size} instances. Resolving data..."
                    )
                    val templatesFlow = templateRepo.getAll()
                    val schedulesFlow = scheduleRepo.getAll()
                    val nodeSchedulesFlow = nodeRepo.getAllNodeSchedules()
                    val fieldValuesFlow = fieldValueRepo.getAll()
                    val schemasFlow = schemaRepo.getAll()
                    val overridesFlow = nodeOverrideRepo.getAll()
                    val planningFlow = planningRepo.getAllPlanningItems()

                    val nodeFlows = instances.map { instance ->
                        val tId = instance.templateId
                        nodeRepo.getByInstance(instance.id).map { nodes ->
                            nodes.map { node -> node to tId }
                        }
                    }

                    val allNodesFlow = if (nodeFlows.isEmpty()) {
                        flowOf(emptyList<Pair<Node, String>>())
                    } else {
                        combine(nodeFlows) { arrays ->
                            arrays.toList().flatten()
                        }
                    }

                    combine(
                        allNodesFlow,
                        templatesFlow,
                        schedulesFlow,
                        nodeSchedulesFlow,
                        fieldValuesFlow,
                        schemasFlow,
                        overridesFlow,
                        planningFlow
                    ) { args: Array<*> ->
                        @Suppress("UNCHECKED_CAST")
                        val nodesWithId = args[0] as List<Pair<Node, String>>

                        @Suppress("UNCHECKED_CAST")
                        val templates = args[1] as List<RoutineTemplate>

                        @Suppress("UNCHECKED_CAST")
                        val schedules = args[2] as List<Schedule>

                        @Suppress("UNCHECKED_CAST")
                        val nodeSchedules = args[3] as List<NodeSchedule>

                        @Suppress("UNCHECKED_CAST")
                        val fieldValues = args[4] as List<NodeFieldValue>

                        @Suppress("UNCHECKED_CAST")
                        val schemas = args[5] as List<NodeMetadataSchema>

                        @Suppress("UNCHECKED_CAST")
                        val overrides = args[6] as List<NodeOverride>

                        @Suppress("UNCHECKED_CAST")
                        val planningItems = args[7] as List<PlanningItemEntity>

                        val timeline = buildTimeline(
                            nodesWithId,
                            templates,
                            schedules,
                            nodeSchedules,
                            fieldValues,
                            schemas,
                            overrides,
                            planningItems,
                            weekday,
                            currentTime
                        )
                        timeline to exceptions
                    }
                }
            }
                .collect { (entries, exceptions) ->
                    _uiState.update { state ->
                        state.copy(
                            timelineEntries = entries,
                            activeExceptions = exceptions,
                            totalCount = entries.sumOf { it.resolvedNodes.size + 1 },
                            completedCount = entries.sumOf { entry ->
                                val rootDone =
                                    if (entry.statusLabel == NodeStatus.COMPLETED.name.lowercase()) 1 else 0
                                val childrenDone = entry.resolvedNodes.count { it.isCompleted }
                                rootDone + childrenDone
                            },
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun buildTimeline(
        nodesWithId: List<Pair<Node, String>>,
        templates: List<RoutineTemplate>,
        schedules: List<Schedule>,
        nodeSchedules: List<NodeSchedule>,
        fieldValues: List<NodeFieldValue>,
        schemas: List<NodeMetadataSchema>,
        overrides: List<NodeOverride>,
        planningItems: List<PlanningItemEntity>,
        weekday: Int,
        currentTime: String
    ): List<TimelineEntryUi> {

        val activeSchedules = schedules.filter { it.isActive }

        val allNodes = nodesWithId.map { it.first }
        val rootNodesWithTemplate = nodesWithId.filter { it.first.parentId == null }
        val nodeSchedulesMap = nodeSchedules.groupBy { it.nodeId }
        val overridesMap = overrides.groupBy { it.nodeId }

        val planningMap = planningItems
            .filter { it.status != PlanningStatus.COMPLETED.name }
            .groupBy { it.relatedNodeId }

        val blocks = rootNodesWithTemplate.mapNotNull { (rootNode, templateId) ->
            val template = templates.find { it.id == templateId } ?: return@mapNotNull null
            val templateSchedules = activeSchedules.filter { it.templateId == templateId }
            val globalSchedule = templateSchedules.find { it.weekday == weekday }

            val baseStart = globalSchedule?.startTime ?: rootNode.scheduledTime
            val baseEnd = globalSchedule?.endTime ?: baseStart?.let {
                addMinutes(
                    it,
                    rootNode.durationMinutes ?: 60
                )
            }

            val duration = if (baseStart != null && baseEnd != null) {
                val diff = ScheduleResolver.timeToMinutes(baseEnd) - ScheduleResolver.timeToMinutes(
                    baseStart
                )
                if (diff <= 0) rootNode.durationMinutes ?: 60 else diff
            } else rootNode.durationMinutes ?: 60

            ResolvedTimeBlock(
                node = rootNode,
                template = template,
                originalStart = baseStart,
                originalEnd = baseEnd,
                effectiveStart = baseStart,
                effectiveEnd = baseEnd,
                durationMinutes = duration
            )
        }.sortedBy { it.originalStart ?: "99:99" }

        var accumulatedShift = 0
        var shiftSource: String? = null
        var lastBlockEndTime: String? = null

        val resolvedBlocks = blocks.map { block ->
            val nodeOverrides = overridesMap[block.node.id].orEmpty()

            // Subfix 12.5: Temporal Dependencies (Roots)
            if (block.node.isSequential && lastBlockEndTime != null) {
                block.effectiveStart = lastBlockEndTime
            } else if (accumulatedShift != 0 && block.originalStart != null) {
                block.effectiveStart = addMinutes(block.originalStart, accumulatedShift)
                block.appliedShiftMinutes = accumulatedShift
                block.shiftReason =
                    "Movido por $shiftSource ${if (accumulatedShift > 0) "+" else ""}${accumulatedShift}m"
            }

            var nodeSpecificDelta = 0
            nodeOverrides.forEach { ov ->
                when (ov.overrideType) {
                    OverrideType.SKIP -> block.isSkipped = true
                    OverrideType.POSTPONE -> {
                        val mins = ov.postponeMinutes ?: 0
                        block.effectiveStart = addMinutes(block.effectiveStart, mins)
                        nodeSpecificDelta += mins
                    }

                    OverrideType.RESCHEDULE -> {
                        val newTime = ov.newTime
                        if (newTime != null && block.originalStart != null) {
                            block.effectiveStart = newTime
                            val totalShiftFromOriginal =
                                ScheduleResolver.timeToMinutes(newTime) - ScheduleResolver.timeToMinutes(
                                    block.originalStart
                                )
                            nodeSpecificDelta = totalShiftFromOriginal - block.appliedShiftMinutes
                        }
                    }

                    OverrideType.DURATION_CHANGE -> {
                        val newDur = ov.newDurationMinutes ?: block.durationMinutes
                        nodeSpecificDelta += (newDur - block.durationMinutes)
                        block.durationMinutes = newDur
                    }

                    else -> {}
                }
            }

            block.effectiveEnd = addMinutes(block.effectiveStart, block.durationMinutes)

            if (nodeSpecificDelta != 0) {
                accumulatedShift += nodeSpecificDelta
                shiftSource = block.node.name
            }

            lastBlockEndTime = block.effectiveEnd
            block
        }

        val currentMins = ScheduleResolver.timeToMinutes(currentTime)

        return resolvedBlocks.map { block ->
            val rootNode = block.node
            val templateId = block.template.id
            val template = block.template
            val templateSchedules = activeSchedules.filter { it.templateId == templateId }

            val resolvedNodes = resolveNodesRecursive(
                parentId = rootNode.id,
                allNodes = allNodes,
                nodeSchedulesMap = nodeSchedulesMap,
                overridesMap = overridesMap,
                fieldValues = fieldValues,
                schemas = schemas,
                planningMap = planningMap,
                depth = 1,
                todayWeekday = weekday,
                template = template,
                allTemplateSchedules = templateSchedules
            )

            val label = when {
                block.effectiveStart != null && block.effectiveEnd != null && block.effectiveStart != block.effectiveEnd ->
                    "${block.effectiveStart} - ${block.effectiveEnd}"

                block.effectiveStart != null -> block.effectiveStart!!
                else -> "Flexible"
            }

            val isCurrent = if (block.effectiveStart != null && block.effectiveEnd != null) {
                val startMins = ScheduleResolver.timeToMinutes(block.effectiveStart!!)
                val endMins = ScheduleResolver.timeToMinutes(block.effectiveEnd!!)
                currentMins in startMins until endMins
            } else false

            // Subfix 12.3: Conflict Detection
            val hasConflict = block.appliedShiftMinutes != 0 && !block.node.isSequential

            val suggestions = if (hasConflict) {
                listOf(
                    ConflictResolutionUi(
                        "Aceptar nuevo horario",
                        ConflictResolutionType.RESCHEDULE,
                        block.effectiveStart
                    ),
                    ConflictResolutionUi("Omitir esta vez", ConflictResolutionType.SKIP),
                    ConflictResolutionUi("Reducir duración", ConflictResolutionType.REDUCE, "30")
                )
            } else emptyList()

            val color = try {
                Color(template.colorHex.toColorInt())
            } catch (e: Exception) {
                Color(0xFF42A5F5)
            }
            val rootValues = fieldValues.filter { it.nodeId == rootNode.id }

            TimelineEntryUi(
                id = rootNode.id,
                time = label,
                sortTime = block.effectiveStart ?: "99:99",
                endTime = block.effectiveEnd,
                title = rootNode.name,
                statusLabel = if (block.isSkipped) "skipped" else rootNode.status.name.lowercase(),
                statusColor = color,
                barColor = color,
                isSkipped = block.isSkipped,
                isCurrent = isCurrent,
                hasConflict = hasConflict,
                conflictResolutionSuggestions = suggestions,
                fields = rootValues.map { v ->
                    val s = schemas.find { it.id == v.schemaId }
                    ResolvedFieldUi(
                        v.schemaId,
                        v.fieldName,
                        s?.fieldLabel ?: v.fieldName,
                        v.value,
                        s?.fieldType ?: FieldType.TEXT
                    )
                },
                resolvedNodes = resolvedNodes,
                wasShiftedByDomino = block.appliedShiftMinutes != 0,
                dominoReason = if (hasConflict) "Conflicto por retraso previo" else block.shiftReason,
                planningInfo = calculatePlanningIndicator(planningMap[rootNode.id], rootNode.id)
            )
        }.sortedBy { it.sortTime }
    }

    private fun resolveNodesRecursive(
        parentId: String,
        allNodes: List<Node>,
        nodeSchedulesMap: Map<String, List<NodeSchedule>>,
        overridesMap: Map<String, List<NodeOverride>>,
        fieldValues: List<NodeFieldValue>,
        schemas: List<NodeMetadataSchema>,
        planningMap: Map<String?, List<PlanningItemEntity>>,
        depth: Int,
        todayWeekday: Int,
        template: RoutineTemplate,
        allTemplateSchedules: List<Schedule>
    ): List<ResolvedNodeUi> {
        val children = allNodes.filter { it.parentId == parentId }.sortedBy { it.position }
        val result = mutableListOf<ResolvedNodeUi>()
        var lastEndTime: String? = null

        children.forEach { node ->
            val todayGlobal = allTemplateSchedules.find { it.weekday == todayWeekday }
            val effectiveSchedules = ScheduleResolver.resolveEffectiveSchedules(
                node.id, allNodes, nodeSchedulesMap, template.name,
                allTemplateSchedules.map { it.weekday }.toSet(),
                todayGlobal?.startTime ?: allTemplateSchedules.firstOrNull()?.startTime,
                todayGlobal?.endTime ?: allTemplateSchedules.firstOrNull()?.endTime
            )

            val todaySchedule = effectiveSchedules.find { it.dayOfWeek == todayWeekday }
            val nodeOverrides = overridesMap[node.id].orEmpty()

            var appliesToday = true
            if (effectiveSchedules.isNotEmpty() && todaySchedule == null) {
                appliesToday = false
            }

            if (appliesToday) {
                var status = node.status
                var startTime = todaySchedule?.startTime ?: node.scheduledTime
                var endTime = todaySchedule?.endTime
                val durationMins = node.durationMinutes ?: 30

                // Subfix 12.5: Temporal Dependencies (Siblings)
                if (node.isSequential && lastEndTime != null) {
                    startTime = lastEndTime
                    endTime = addMinutes(startTime, durationMins)
                }

                nodeOverrides.forEach { override ->
                    when (override.overrideType) {
                        OverrideType.SKIP -> status = NodeStatus.SKIPPED
                        OverrideType.POSTPONE -> {
                            val mins = override.postponeMinutes ?: 0
                            startTime = addMinutes(startTime, mins)
                            endTime = addMinutes(endTime, mins)
                        }

                        OverrideType.RESCHEDULE -> startTime = override.newTime ?: startTime
                        OverrideType.DURATION_CHANGE -> {
                            val newDuration = override.newDurationMinutes ?: 0
                            endTime = addMinutes(startTime, newDuration)
                        }

                        else -> {}
                    }
                }

                if (endTime == null && startTime != null) {
                    endTime = addMinutes(startTime, durationMins)
                }

                val timeLabel = if (startTime != null) {
                    if (endTime != null && endTime != startTime) "$startTime - $endTime" else startTime
                } else null

                val nodeValues = fieldValues.filter { it.nodeId == node.id }
                val resolvedNode = ResolvedNodeUi(
                    id = node.id,
                    name = node.name,
                    depth = depth,
                    timeLabel = timeLabel,
                    isCompleted = status == NodeStatus.COMPLETED,
                    isSkipped = status == NodeStatus.SKIPPED,
                    fields = nodeValues.map { v ->
                        val s = schemas.find { it.id == v.schemaId }
                        ResolvedFieldUi(
                            v.schemaId,
                            v.fieldName,
                            s?.fieldLabel ?: v.fieldName,
                            v.value,
                            s?.fieldType ?: FieldType.TEXT
                        )
                    },
                    planningInfo = calculatePlanningIndicator(planningMap[node.id], node.id)
                )

                result.add(resolvedNode)
                result.addAll(
                    resolveNodesRecursive(
                        node.id,
                        allNodes,
                        nodeSchedulesMap,
                        overridesMap,
                        fieldValues,
                        schemas,
                        planningMap,
                        depth + 1,
                        todayWeekday,
                        template,
                        allTemplateSchedules
                    )
                )

                lastEndTime = endTime
            }
        }
        return result
    }

    private fun calculatePlanningIndicator(
        items: List<PlanningItemEntity>?,
        nodeId: String
    ): PlanningIndicatorUi? {
        if (items.isNullOrEmpty()) {
            return null
        }

        val today = DateUtils.getStartOfDay()
        val linkedItems = items.map { entity ->
            val pType = PlanningItemType.valueOf(entity.type)
            val urgency: Int

            if (entity.dueDate != null) {
                val dueStart = DateUtils.getStartOfDay(entity.dueDate)
                when {
                    dueStart < today -> urgency = 0
                    dueStart == today -> urgency = 1
                    else -> urgency = 2
                }
            } else {
                urgency = 2
            }

            PlanningLinkedItemUi(
                id = entity.id,
                type = pType,
                title = entity.title,
                dueDate = entity.dueDate,
                dueTime = entity.dueTime,
                status = PlanningStatus.valueOf(entity.status),
                urgency = urgency
            )
        }.sortedWith(
            compareBy<PlanningLinkedItemUi> { it.urgency }
                .thenBy {
                    when (it.type) {
                        PlanningItemType.TASK -> 0
                        PlanningItemType.NOTE -> 1
                        PlanningItemType.REMINDER -> 2
                    }
                }
        )

        if (linkedItems.isEmpty()) {
            return null
        }

        val overdue = linkedItems.count { it.urgency == 0 }
        val dueToday = linkedItems.count { it.urgency == 1 }
        val pending = linkedItems.count { it.urgency == 2 }

        return PlanningIndicatorUi(
            pendingCount = pending,
            todayCount = dueToday,
            overdueCount = overdue,
            totalCount = linkedItems.size,
            items = linkedItems
        )
    }

    private fun addMinutes(time: String?, minutes: Int): String? {
        if (time == null) return null
        try {
            val parts = time.split(":")
            var total = parts[0].toInt() * 60 + parts[1].toInt() + minutes
            total %= 1440
            if (total < 0) total += 1440
            return String.format(Locale.US, "%02d:%02d", total / 60, total % 60)
        } catch (e: Exception) {
            return time
        }
    }

    // QUICK ACTIONS
    fun postponeNode(nodeId: String, minutes: Int) {
        viewModelScope.launch {
            val node = nodeRepo.getById(nodeId) ?: return@launch
            val instanceId = node.instanceId ?: return@launch
            applyOverride(nodeId, instanceId, OverrideType.POSTPONE, postponeMinutes = minutes)
            _events.emit("Actividad pospuesta $minutes min")
        }
    }

    fun skipNode(nodeId: String) {
        viewModelScope.launch {
            val node = nodeRepo.getById(nodeId) ?: return@launch
            val instanceId = node.instanceId ?: return@launch
            applyOverride(nodeId, instanceId, OverrideType.SKIP)
            nodeRepo.update(node.copy(status = NodeStatus.SKIPPED))
            _events.emit("Actividad saltada")
        }
    }

    fun rescheduleNode(nodeId: String, newTime: String) {
        viewModelScope.launch {
            val node = nodeRepo.getById(nodeId) ?: return@launch
            val instanceId = node.instanceId ?: return@launch
            applyOverride(nodeId, instanceId, OverrideType.RESCHEDULE, newTime = newTime)
            _events.emit("Actividad reprogramada")
        }
    }

    fun changeDuration(nodeId: String, newDurationMinutes: Int) {
        viewModelScope.launch {
            val node = nodeRepo.getById(nodeId) ?: return@launch
            val instanceId = node.instanceId ?: return@launch
            applyOverride(
                nodeId,
                instanceId,
                OverrideType.DURATION_CHANGE,
                newDurationMinutes = newDurationMinutes
            )
            _events.emit("Duración actualizada")
        }
    }

    fun resolveConflict(nodeId: String, resolution: ConflictResolutionUi) {
        when (resolution.type) {
            ConflictResolutionType.RESCHEDULE -> resolution.newValue?.let {
                rescheduleNode(
                    nodeId,
                    it
                )
            }

            ConflictResolutionType.SKIP -> skipNode(nodeId)
            ConflictResolutionType.REDUCE -> resolution.newValue?.toIntOrNull()
                ?.let { changeDuration(nodeId, it) }
        }
    }

    private suspend fun applyOverride(
        nodeId: String,
        instanceId: String,
        type: OverrideType,
        newTime: String? = null,
        newDurationMinutes: Int? = null,
        postponeMinutes: Int? = null
    ) {
        val existing = nodeOverrideRepo.getAll().first().find {
            it.nodeId == nodeId && it.instanceId == instanceId && it.overrideType == type
        }

        if (existing != null) {
            nodeOverrideRepo.upsert(
                existing.copy(
                    newTime = newTime ?: existing.newTime,
                    newDurationMinutes = newDurationMinutes ?: existing.newDurationMinutes,
                    postponeMinutes = postponeMinutes ?: existing.postponeMinutes,
                    updatedAt = System.currentTimeMillis()
                )
            )
        } else {
            nodeOverrideRepo.upsert(
                NodeOverride(
                    nodeId = nodeId,
                    instanceId = instanceId,
                    overrideType = type,
                    newTime = newTime,
                    newDurationMinutes = newDurationMinutes,
                    postponeMinutes = postponeMinutes
                )
            )
        }
    }

    fun toggleNodeCompletion(nodeId: String) {
        viewModelScope.launch {
            val targetNode = nodeRepo.getById(nodeId) ?: return@launch
            val instanceId = targetNode.instanceId ?: return@launch
            val allNodes = nodeRepo.getByInstance(instanceId).first()
            val newStatus =
                if (targetNode.status == NodeStatus.COMPLETED) NodeStatus.PENDING else NodeStatus.COMPLETED

            val nodesToUpdate = mutableMapOf<String, Node>()
            val descendants = getDescendants(targetNode.id, allNodes)
            val now = System.currentTimeMillis()
            nodesToUpdate[targetNode.id] = targetNode.copy(status = newStatus, updatedAt = now)
            descendants.forEach { desc ->
                nodesToUpdate[desc.id] = desc.copy(status = newStatus, updatedAt = now)
            }
            updateAncestors(targetNode.parentId, allNodes, nodesToUpdate, newStatus)
            if (nodesToUpdate.isNotEmpty()) {
                nodeRepo.insertAll(nodesToUpdate.values.toList())
                if (newStatus == NodeStatus.COMPLETED) {
                    _events.emit("Actividad completada")
                }
            }
        }
    }

    fun togglePlanningTask(taskId: String) {
        viewModelScope.launch {
            val allItems = planningRepo.getAllPlanningItems().first()
            val item = allItems.find { it.id == taskId } ?: return@launch

            if (PlanningItemType.valueOf(item.type) != PlanningItemType.TASK) return@launch

            val isCompleting = item.status == PlanningStatus.PENDING.name
            val newStatus = if (isCompleting) PlanningStatus.COMPLETED else PlanningStatus.PENDING
            val updatedItem = item.copy(
                status = newStatus.name,
                updatedAt = System.currentTimeMillis(),
                version = item.version + 1
            )

            planningRepo.upsertPlanningItem(updatedItem)
            if (isCompleting) {
                _events.emit("Tarea completada")
            }
        }
    }

    private fun getDescendants(nodeId: String, allNodes: List<Node>): List<Node> {
        val children = allNodes.filter { it.parentId == nodeId }
        return children + children.flatMap { getDescendants(it.id, allNodes) }
    }

    private fun updateAncestors(
        parentId: String?,
        allNodes: List<Node>,
        updates: MutableMap<String, Node>,
        triggerNewStatus: NodeStatus
    ) {
        if (parentId == null) return
        val parentNode = allNodes.find { it.id == parentId } ?: return
        val children = allNodes.filter { it.parentId == parentId }
        val now = System.currentTimeMillis()
        if (triggerNewStatus == NodeStatus.PENDING) {
            updates[parentNode.id] = parentNode.copy(status = NodeStatus.PENDING, updatedAt = now)
            updateAncestors(parentNode.parentId, allNodes, updates, NodeStatus.PENDING)
        } else {
            val allCompleted = children.all { child ->
                (updates[child.id]?.status ?: child.status) == NodeStatus.COMPLETED
            }
            if (allCompleted) {
                updates[parentNode.id] =
                    parentNode.copy(status = NodeStatus.COMPLETED, updatedAt = now)
                updateAncestors(parentNode.parentId, allNodes, updates, NodeStatus.COMPLETED)
            }
        }
    }

    private fun generateInstanceIfNeeded(today: Long, weekday: Int) {
        viewModelScope.launch {
            val activeSchedules = scheduleRepo.getActiveForWeekday(weekday, today).first()
            activeSchedules.forEach { active ->
                instanceRepo.generateInstanceIfNeeded(active.templateId, today)
            }
        }
    }

    private fun updateTimeTicker() {
        viewModelScope.launch {
            while (true) {
                val cal = Calendar.getInstance()
                val newTime = String.format(
                    Locale.US,
                    "%02d:%02d",
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE)
                )
                _uiState.update { it.copy(currentTime = newTime) }
                delay(60_000)
            }
        }
    }
}
