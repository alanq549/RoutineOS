package com.alan.routineos.ui.features.today.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.core.util.DateUtils
import com.alan.routineos.core.util.ScheduleResolver
import com.alan.routineos.data.local.entities.DayInstance
import com.alan.routineos.data.local.entities.ExecutionTrackingMode
import com.alan.routineos.data.local.entities.InstanceStatus
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeFieldValue
import com.alan.routineos.data.local.entities.NodeMetadataSchema
import com.alan.routineos.data.local.entities.NodeOverride
import com.alan.routineos.data.local.entities.NodeSchedule
import com.alan.routineos.data.local.entities.NodeStatus
import com.alan.routineos.data.local.entities.OverrideType
import com.alan.routineos.data.local.entities.RoutineTemplate
import com.alan.routineos.data.local.entities.Schedule
import com.alan.routineos.data.local.entities.TemporalMode
import com.alan.routineos.data.repository.ExecutionFieldValueRepository
import com.alan.routineos.data.repository.FieldValueRepository
import com.alan.routineos.data.repository.InstanceRepository
import com.alan.routineos.data.repository.MetadataSchemaRepository
import com.alan.routineos.data.repository.NodeOverrideRepository
import com.alan.routineos.data.repository.NodeRepository
import com.alan.routineos.data.repository.PlanningRepository
import com.alan.routineos.data.repository.ScheduleExceptionRepository
import com.alan.routineos.data.repository.ScheduleRepository
import com.alan.routineos.data.repository.TemplateRepository
import com.alan.routineos.notifications.AlarmScheduler
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
import timber.log.Timber
import java.util.Calendar
import java.util.Locale
import java.util.UUID
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
    private val planningRepo: PlanningRepository,
    private val executionValueRepo: ExecutionFieldValueRepository,
    private val alarmScheduler: AlarmScheduler
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

            Timber.d("INITIALIZING TODAY - Date: %d, Weekday: %d", today, weekday)
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
                    Timber.d("OBSERVE: No instances found for today.")
                    flowOf(
                        Triple(
                            emptyList<TimelineEntryUi>(),
                            exceptions,
                            emptyList<PlanningLinkedItemUi>()
                        )
                    )
                } else {
                    Timber.d("OBSERVE: Found %d instances. Resolving data...", instances.size)
                    val templatesFlow = templateRepo.getAll()
                    val schedulesFlow = scheduleRepo.getAll()
                    val nodeSchedulesFlow = nodeRepo.getAllNodeSchedules()
                    val fieldValuesFlow = fieldValueRepo.getAll()
                    val schemasFlow = schemaRepo.getAll()
                    val overridesFlow = nodeOverrideRepo.getAll()
                    val planningFlow = planningRepo.getAllPlanningItems()
                    val executionValuesFlow = executionValueRepo.getAll()

                    val nodeFlows = instances.map { instance ->
                        val tId = instance.templateId
                        nodeRepo.getByInstance(instance.id).map { nodes ->
                            nodes.map { node -> node to tId }
                        }
                    }

                    val allNodesFlow = if (nodeFlows.isEmpty()) {
                        flowOf(emptyList())
                    } else {
                        combine(nodeFlows) { arrays -> arrays.toList().flatten() }
                    }

                    combine(
                        allNodesFlow,
                        templatesFlow,
                        schedulesFlow,
                        nodeSchedulesFlow,
                        fieldValuesFlow,
                        schemasFlow,
                        overridesFlow,
                        planningFlow,
                        executionValuesFlow
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
                        val planningItems =
                            args[7] as List<com.alan.routineos.data.local.entities.PlanningItemEntity>

                        @Suppress("UNCHECKED_CAST")
                        val executionValues =
                            args[8] as List<com.alan.routineos.data.local.entities.ExecutionFieldValue>

                        val timeline = buildTimeline(
                            nodesWithId, templates, schedules, nodeSchedules,
                            fieldValues, schemas, overrides, planningItems,
                            executionValues, weekday, currentTime
                        )

                        val unlinkedPlanningItems = planningItems
                            .filter { it.relatedNodeId == null && it.status != PlanningStatus.COMPLETED.name }
                        val resolvedUnlinked =
                            calculatePlanningIndicator(unlinkedPlanningItems)?.items ?: emptyList()

                        Triple(timeline, exceptions, resolvedUnlinked)
                    }
                }
            }.collect { (timeline, exceptions, unlinkedItems) ->
                _uiState.update {
                    it.copy(
                        timelineEntries = timeline,
                        activeExceptions = exceptions,
                        unlinkedPlanningItems = unlinkedItems,
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
        planningItems: List<com.alan.routineos.data.local.entities.PlanningItemEntity>,
        executionValues: List<com.alan.routineos.data.local.entities.ExecutionFieldValue>,
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
            val template =
                templates.find { it.id == templateId } ?: if (templateId == "adhoc_events") {
                    RoutineTemplate(
                        id = "adhoc_events",
                        rootNodeId = "",
                        name = "Agenda",
                        colorHex = "#607D8B"
                    )
                } else {
                    return@mapNotNull null
                }

            val templateSchedules = activeSchedules.filter { it.templateId == templateId }
            val globalSchedule = templateSchedules.find { it.weekday == weekday }

            val baseStart = when (rootNode.temporalMode) {
                TemporalMode.NONE, TemporalMode.SEQUENTIAL -> null
                TemporalMode.START_ONLY -> rootNode.scheduledTime ?: globalSchedule?.startTime
                TemporalMode.START_END -> globalSchedule?.startTime ?: rootNode.scheduledTime
            }

            val baseEnd = when (rootNode.temporalMode) {
                TemporalMode.NONE, TemporalMode.START_ONLY, TemporalMode.SEQUENTIAL -> null
                TemporalMode.START_END -> {
                    globalSchedule?.endTime ?: baseStart?.let {
                        addMinutes(it, rootNode.durationMinutes ?: 60)
                    }
                }
            }

            val duration = when (rootNode.temporalMode) {
                TemporalMode.NONE, TemporalMode.START_ONLY -> 0
                TemporalMode.SEQUENTIAL -> rootNode.durationMinutes ?: 30
                TemporalMode.START_END -> {
                    if (baseStart != null && baseEnd != null) {
                        val diff = ScheduleResolver.timeToMinutes(baseEnd) -
                                ScheduleResolver.timeToMinutes(baseStart)
                        if (diff <= 0) rootNode.durationMinutes ?: 60 else diff
                    } else {
                        rootNode.durationMinutes ?: 60
                    }
                }
            }

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
            val overridesByType = nodeOverrides.associateBy { it.overrideType }

            Timber.tag("TIMELINE_DEBUG").d(
                "SEQUENTIAL_DECISION node=${block.node.name} mode=${block.node.temporalMode} isSequential=${block.node.isSequential} originalStart=${block.originalStart} lastBlockEndTime=$lastBlockEndTime willUseSequential=${block.node.temporalMode == TemporalMode.SEQUENTIAL && lastBlockEndTime != null}"
            )

            if (block.node.temporalMode == TemporalMode.SEQUENTIAL && lastBlockEndTime != null) {
                block.effectiveStart = lastBlockEndTime
            } else if (accumulatedShift != 0 && block.originalStart != null) {
                block.effectiveStart = addMinutes(block.originalStart, accumulatedShift)
                block.appliedShiftMinutes = accumulatedShift
                block.shiftReason =
                    "Movido por $shiftSource ${if (accumulatedShift > 0) "+" else ""}${accumulatedShift}m"
            }

            var nodeSpecificDelta = 0
            if (overridesByType.containsKey(OverrideType.SKIP)) {
                block.isSkipped = true
            }

            overridesByType[OverrideType.RESCHEDULE]?.let { ov ->
                val newTime = ov.newTime
                if (newTime != null && block.originalStart != null &&
                    (block.node.temporalMode == TemporalMode.START_ONLY || block.node.temporalMode == TemporalMode.START_END)
                ) {
                    block.effectiveStart = newTime
                    if (block.node.temporalMode == TemporalMode.START_END) {
                        val totalShiftFromOriginal = ScheduleResolver.timeToMinutes(newTime) -
                                ScheduleResolver.timeToMinutes(block.originalStart)
                        nodeSpecificDelta = totalShiftFromOriginal - block.appliedShiftMinutes
                    }
                }
            }

            overridesByType[OverrideType.POSTPONE]?.let { ov ->
                if (block.node.temporalMode == TemporalMode.START_ONLY || block.node.temporalMode == TemporalMode.START_END) {
                    val mins = ov.postponeMinutes ?: 0
                    block.effectiveStart = addMinutes(block.effectiveStart, mins)
                    if (block.node.temporalMode == TemporalMode.START_END) {
                        nodeSpecificDelta += mins
                    }
                }
            }

            overridesByType[OverrideType.DURATION_CHANGE]?.let { ov ->
                if (block.node.temporalMode == TemporalMode.START_END || block.node.temporalMode == TemporalMode.SEQUENTIAL) {
                    val oldDur = block.durationMinutes
                    val newDur = ov.newDurationMinutes ?: oldDur
                    val durationDelta = newDur - oldDur

                    Timber.tag("TIMELINE_DEBUG").d(
                        "DURATION_APPLY: node=${block.node.name} mode=${block.node.temporalMode} oldDur=$oldDur newDur=$newDur delta=$durationDelta"
                    )

                    nodeSpecificDelta += durationDelta
                    block.durationMinutes = newDur
                }
            }

            block.effectiveEnd = when (block.node.temporalMode) {
                TemporalMode.START_ONLY, TemporalMode.NONE -> null
                else -> addMinutes(block.effectiveStart, block.durationMinutes)
            }

            Timber.tag("TIMELINE_DEBUG").v(
                "RESOLVED_BLOCK: node=${block.node.name} start=${block.effectiveStart} end=${block.effectiveEnd} dur=${block.durationMinutes}"
            )

            if (nodeSpecificDelta != 0) {
                accumulatedShift += nodeSpecificDelta
                shiftSource = block.node.name
            }

            lastBlockEndTime = when (block.node.temporalMode) {
                TemporalMode.START_END, TemporalMode.SEQUENTIAL -> block.effectiveEnd ?: block.effectiveStart
                else -> lastBlockEndTime
            }
            block
        }

        val currentMins = ScheduleResolver.timeToMinutes(currentTime)

        // LOG RESOLVED TIMELINE TREE
        val treeLog = StringBuilder("\nTIMELINE_RESOLUTION_TREE\n")
        val finalEntries = resolvedBlocks.mapNotNull { block ->
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
                executionValues = executionValues,
                schemas = schemas,
                planningMap = planningMap,
                depth = 1,
                todayWeekday = weekday,
                template = template,
                allTemplateSchedules = templateSchedules
            )

            val isRootVisible = when {
                rootNode.temporalMode != TemporalMode.NONE -> true
                else -> resolvedNodes.isNotEmpty() || rootNode.typeId != "activity_root"
            }

            if (!isRootVisible) return@mapNotNull null

            val label = when (rootNode.temporalMode) {
                TemporalMode.START_ONLY -> block.effectiveStart ?: "Flexible"
                TemporalMode.SEQUENTIAL -> {
                    if (block.effectiveStart != null && block.effectiveEnd != null) {
                        "${block.effectiveStart} - ${block.effectiveEnd}"
                    } else {
                        "Secuencial"
                    }
                }

                TemporalMode.START_END -> {
                    if (block.effectiveStart != null && block.effectiveEnd != null && block.effectiveStart != block.effectiveEnd) {
                        "${block.effectiveStart} - ${block.effectiveEnd}"
                    } else {
                        block.effectiveStart ?: "Flexible"
                    }
                }

                TemporalMode.NONE -> "Flexible"
            }

            val isCurrent = if (
                rootNode.temporalMode == TemporalMode.START_END ||
                rootNode.temporalMode == TemporalMode.SEQUENTIAL
            ) {
                if (block.effectiveStart != null && block.effectiveEnd != null) {
                    val startMins = ScheduleResolver.timeToMinutes(block.effectiveStart!!)
                    val endMins = ScheduleResolver.timeToMinutes(block.effectiveEnd!!)
                    currentMins in startMins until endMins
                } else {
                    false
                }
            } else {
                false
            }

            val hasConflict =
                block.appliedShiftMinutes != 0 && block.node.temporalMode == TemporalMode.START_END

            val suggestions = if (hasConflict) {
                when (rootNode.temporalMode) {
                    TemporalMode.START_END -> listOf(
                        ConflictResolutionUi(
                            "Aceptar nuevo horario",
                            ConflictResolutionType.RESCHEDULE,
                            block.effectiveStart
                        ),
                        ConflictResolutionUi("Omitir esta vez", ConflictResolutionType.SKIP),
                        ConflictResolutionUi(
                            "Reducir duración",
                            ConflictResolutionType.REDUCE,
                            "30"
                        )
                    )

                    TemporalMode.START_ONLY -> listOf(
                        ConflictResolutionUi(
                            "Aceptar nueva hora",
                            ConflictResolutionType.RESCHEDULE,
                            block.effectiveStart
                        ),
                        ConflictResolutionUi("Omitir esta vez", ConflictResolutionType.SKIP)
                    )

                    TemporalMode.SEQUENTIAL -> listOf(
                        ConflictResolutionUi("Omitir esta vez", ConflictResolutionType.SKIP),
                        ConflictResolutionUi(
                            "Reducir duración",
                            ConflictResolutionType.REDUCE,
                            "30"
                        )
                    )

                    TemporalMode.NONE -> emptyList()
                }
            } else {
                emptyList()
            }

            val color = try {
                Color(template.colorHex.toColorInt())
            } catch (e: Exception) {
                Color(0xFF42A5F5)
            }
            val rootValues = fieldValues.filter { it.nodeId == rootNode.id }
            val rootExecValues = executionValues.filter { it.nodeId == rootNode.id }

            val entrySortTime = if (rootNode.temporalMode == TemporalMode.NONE) {
                resolvedNodes.mapNotNull { it.timeLabel?.split(" ")?.firstOrNull() }.minOrNull()
                    ?: "99:99"
            } else {
                block.effectiveStart ?: "99:99"
            }

            val entry = TimelineEntryUi(
                id = rootNode.id,
                time = label,
                sortTime = entrySortTime,
                endTime = block.effectiveEnd,
                title = rootNode.name,
                statusLabel = if (block.isSkipped) "skipped" else rootNode.status.name.lowercase(),
                statusColor = color,
                barColor = color,
                isSkipped = block.isSkipped,
                isCurrent = isCurrent,
                hasConflict = hasConflict,
                conflictResolutionSuggestions = suggestions,
                fields = schemas.filter { s -> rootNode.typeId == s.typeId }.map { s ->
                    val execValue = rootExecValues.find { it.schemaId == s.id }
                    val baseValue = rootValues.find { it.schemaId == s.id }
                    val value = when {
                        execValue != null -> execValue.actualValue
                        s.executionTrackingMode == ExecutionTrackingMode.RECORD_ACTUAL -> ""
                        else -> baseValue?.value ?: s.defaultValue ?: ""
                    }
                    ResolvedFieldUi(s.id, s.fieldName, s.fieldLabel, value, s.fieldType)
                },
                resolvedNodes = resolvedNodes,
                wasShiftedByDomino = block.appliedShiftMinutes != 0,
                dominoReason = if (hasConflict) "Conflicto por retraso previo" else block.shiftReason,
                planningInfo = calculatePlanningIndicator(planningMap[rootNode.id]),
                isSpontaneousEvent = rootNode.templateId == "adhoc_events",
                durationMinutes = block.durationMinutes
            )

            treeLog.append("${entry.title} [${rootNode.temporalMode}] original=${block.originalStart}-${block.originalEnd} effective=${block.effectiveStart}-${block.effectiveEnd} dur=${block.durationMinutes} status=${entry.statusLabel} shift=${block.appliedShiftMinutes} reason=${block.shiftReason}\n")
            entry.resolvedNodes.forEach { node ->
                val indent = "  ".repeat(node.depth)
                treeLog.append("${indent}${node.name} depth=${node.depth} time=${node.timeLabel} status=${if (node.isCompleted) "DONE" else if (node.isSkipped) "SKIP" else "PEND"}\n")
            }
            entry
        }.sortedBy { it.sortTime }

        Timber.tag("TIMELINE_DEBUG").d(treeLog.toString())
        return finalEntries
    }

    private fun resolveNodesRecursive(
        parentId: String,
        allNodes: List<Node>,
        nodeSchedulesMap: Map<String, List<NodeSchedule>>,
        overridesMap: Map<String, List<NodeOverride>>,
        fieldValues: List<NodeFieldValue>,
        executionValues: List<com.alan.routineos.data.local.entities.ExecutionFieldValue>,
        schemas: List<NodeMetadataSchema>,
        planningMap: Map<String?, List<com.alan.routineos.data.local.entities.PlanningItemEntity>>,
        depth: Int,
        todayWeekday: Int,
        template: RoutineTemplate,
        allTemplateSchedules: List<Schedule>
    ): List<ResolvedNodeUi> {
        val children = allNodes.filter { it.parentId == parentId && it.deletedAt == null }
            .sortedBy { it.position }
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
            val nodeOverrides = overridesMap[node.id].orEmpty().associateBy { it.overrideType }

            var appliesToday = true
            if (effectiveSchedules.isNotEmpty() && todaySchedule == null) {
                appliesToday = false
            }

            if (appliesToday) {
                var status = node.status
                var startTime = when (node.temporalMode) {
                    TemporalMode.NONE, TemporalMode.SEQUENTIAL -> null
                    TemporalMode.START_ONLY -> node.scheduledTime ?: todaySchedule?.startTime
                    TemporalMode.START_END -> todaySchedule?.startTime ?: node.scheduledTime
                }
                var endTime = when (node.temporalMode) {
                    TemporalMode.NONE, TemporalMode.START_ONLY, TemporalMode.SEQUENTIAL -> null
                    TemporalMode.START_END -> todaySchedule?.endTime
                }
                val durationMins = when (node.temporalMode) {
                    TemporalMode.NONE, TemporalMode.START_ONLY -> 0
                    TemporalMode.SEQUENTIAL -> node.durationMinutes ?: 30
                    TemporalMode.START_END -> {
                        val scheduleStart = todaySchedule?.startTime ?: node.scheduledTime
                        val scheduleEnd = todaySchedule?.endTime
                        if (scheduleStart != null && scheduleEnd != null) {
                            val diff = ScheduleResolver.timeToMinutes(scheduleEnd) -
                                    ScheduleResolver.timeToMinutes(scheduleStart)
                            if (diff <= 0) node.durationMinutes ?: 60 else diff
                        } else {
                            node.durationMinutes ?: 60
                        }
                    }
                }

                if ((node.temporalMode == TemporalMode.SEQUENTIAL || node.isSequential) && lastEndTime != null) {
                    startTime = lastEndTime
                    endTime = addMinutes(startTime, durationMins)
                }

                if (nodeOverrides.containsKey(OverrideType.SKIP)) {
                    status = NodeStatus.SKIPPED
                }

                nodeOverrides[OverrideType.RESCHEDULE]?.let { override ->
                    if (node.temporalMode == TemporalMode.START_ONLY || node.temporalMode == TemporalMode.START_END) {
                        startTime = override.newTime ?: startTime
                    }
                }

                nodeOverrides[OverrideType.POSTPONE]?.let { override ->
                    if (node.temporalMode == TemporalMode.START_ONLY || node.temporalMode == TemporalMode.START_END) {
                        val mins = override.postponeMinutes ?: 0
                        startTime = addMinutes(startTime, mins)
                        if (node.temporalMode == TemporalMode.START_END) {
                            endTime = addMinutes(endTime, mins)
                        }
                    }
                }

                nodeOverrides[OverrideType.DURATION_CHANGE]?.let { override ->
                    if (node.temporalMode == TemporalMode.START_END || node.temporalMode == TemporalMode.SEQUENTIAL) {
                        val newDuration = override.newDurationMinutes ?: durationMins
                        endTime = addMinutes(startTime, newDuration)
                    }
                }

                if (node.temporalMode == TemporalMode.START_END && endTime == null && startTime != null) {
                    endTime = addMinutes(startTime, durationMins)
                }

                val timeLabel = when (node.temporalMode) {
                    TemporalMode.NONE -> null
                    TemporalMode.START_ONLY -> startTime
                    TemporalMode.SEQUENTIAL -> if (startTime != null && endTime != null) "$startTime - $endTime" else null
                    TemporalMode.START_END -> if (startTime != null) {
                        if (endTime != null && endTime != startTime) "$startTime - $endTime" else startTime
                    } else null
                }

                val childSubNodes = resolveNodesRecursive(
                    node.id, allNodes, nodeSchedulesMap, overridesMap, fieldValues,
                    executionValues, schemas, planningMap, depth + 1, todayWeekday,
                    template, allTemplateSchedules
                )

                val hasOwnTime = timeLabel != null
                val hasDescendantsWithTime = childSubNodes.any { it.timeLabel != null }

                val isVisible = when {
                    hasOwnTime -> true
                    hasDescendantsWithTime -> true
                    node.temporalMode == TemporalMode.SEQUENTIAL -> true
                    node.temporalMode == TemporalMode.NONE -> childSubNodes.isNotEmpty() || node.typeId != "activity_root"
                    else -> childSubNodes.isNotEmpty()
                }

                if (isVisible) {
                    val nodeValues = fieldValues.filter { it.nodeId == node.id }
                    val nodeExecValues = executionValues.filter { it.nodeId == node.id }
                    val resolvedNode = ResolvedNodeUi(
                        id = node.id,
                        name = node.name,
                        depth = depth,
                        timeLabel = timeLabel,
                        isCompleted = status == NodeStatus.COMPLETED,
                        isSkipped = status == NodeStatus.SKIPPED,
                        fields = schemas.filter { s -> node.typeId == s.typeId }.map { s ->
                            val execValue = nodeExecValues.find { it.schemaId == s.id }
                            val baseValue = nodeValues.find { it.schemaId == s.id }
                            val value = when {
                                execValue != null -> execValue.actualValue
                                s.executionTrackingMode == ExecutionTrackingMode.RECORD_ACTUAL -> ""
                                else -> baseValue?.value ?: s.defaultValue ?: ""
                            }
                            ResolvedFieldUi(s.id, s.fieldName, s.fieldLabel, value, s.fieldType)
                        },
                        planningInfo = calculatePlanningIndicator(planningMap[node.id]),
                        durationMinutes = durationMins
                    )
                    result.add(resolvedNode)
                    result.addAll(childSubNodes)
                }
                lastEndTime = endTime ?: startTime
            }
        }
        return result
    }

    private fun calculatePlanningIndicator(
        items: List<com.alan.routineos.data.local.entities.PlanningItemEntity>?
    ): PlanningIndicatorUi? {
        if (items.isNullOrEmpty()) return null
        val today = DateUtils.getStartOfDay()
        val linkedItems = items.map { entity ->
            val pType = PlanningItemType.valueOf(entity.type)
            val urgency = entity.dueDate?.let { dueDate ->
                val dueStart = DateUtils.getStartOfDay(dueDate)
                when {
                    dueStart < today -> 0
                    dueStart == today -> 1
                    else -> 2
                }
            } ?: 2
            PlanningLinkedItemUi(
                id = entity.id, type = pType, title = entity.title,
                dueDate = entity.dueDate, dueTime = entity.dueTime,
                status = PlanningStatus.valueOf(entity.status), urgency = urgency
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
        if (linkedItems.isEmpty()) return null
        return PlanningIndicatorUi(
            pendingCount = linkedItems.count { it.urgency == 2 },
            todayCount = linkedItems.count { it.urgency == 1 },
            overdueCount = linkedItems.count { it.urgency == 0 },
            totalCount = linkedItems.size,
            items = linkedItems
        )
    }

    private fun addMinutes(time: String?, minutes: Int): String? {
        if (time == null) return null
        return try {
            val parts = time.split(":")
            var total = parts[0].toInt() * 60 + parts[1].toInt() + minutes
            total %= 1440
            if (total < 0) total += 1440
            String.format(Locale.US, "%02d:%02d", total / 60, total % 60)
        } catch (e: Exception) {
            time
        }
    }

    fun createSpontaneousEvent(title: String, startTime: String, durationMinutes: Int) {
        Timber.tag("TODAY_DEBUG").d("ACTION: createSpontaneousEvent - title: $title, start: $startTime, duration: $durationMinutes")
        viewModelScope.launch {
            val today = DateUtils.getStartOfDay()
            val currentInstances = instanceRepo.getByDate(today).first()
            val existingAdhoc = currentInstances.find { it.templateId == "adhoc_events" }
            val adhocId = if (existingAdhoc == null) {
                val newAdhocInstance = DayInstance(
                    id = UUID.randomUUID().toString(), templateId = "adhoc_events",
                    date = today, status = InstanceStatus.GENERATED
                )
                instanceRepo.upsert(newAdhocInstance)
                newAdhocInstance.id
            } else existingAdhoc.id

            val typeId = nodeRepo.getAllTemplateNodes().first().firstOrNull()?.typeId ?: "event"
            val newNode = Node(
                id = UUID.randomUUID().toString(),
                instanceId = adhocId,
                templateId = "adhoc_events",
                name = title,
                scheduledTime = startTime,
                durationMinutes = durationMinutes,
                temporalMode = TemporalMode.START_END,
                typeId = typeId,
                status = NodeStatus.PENDING
            )
            nodeRepo.insertAll(listOf(newNode))
            logNodeDetail("TODAY_DEBUG", "NEW_SPONTANEOUS_EVENT_CREATED", newNode.id)
            _events.emit("Evento espontáneo creado")
        }
    }

    fun updateSpontaneousEvent(
        nodeId: String,
        title: String,
        startTime: String,
        durationMinutes: Int
    ) {
        Timber.tag("TODAY_DEBUG").d(
            "ACTION updateSpontaneousEvent nodeId=%s title=%s startTime=%s duration=%d",
            nodeId, title, startTime, durationMinutes
        )
        viewModelScope.launch {
            val node = nodeRepo.getById(nodeId) ?: run {
                Timber.tag("TODAY_DEBUG").e("UPDATE ERROR: node %s not found", nodeId)
                return@launch
            }
            if (node.templateId != "adhoc_events") {
                Timber.tag("TODAY_DEBUG").e("UPDATE ERROR: node %s is not a spontaneous event", nodeId)
                return@launch
            }
            val updatedNode = node.copy(
                name = title,
                scheduledTime = startTime,
                durationMinutes = durationMinutes,
                temporalMode = TemporalMode.START_END,
                updatedAt = System.currentTimeMillis(),
                version = node.version + 1
            )
            nodeRepo.update(updatedNode)
            _events.emit("Evento actualizado")
        }
    }

    fun deleteSpontaneousEvent(nodeId: String) {
        viewModelScope.launch {
            val node = nodeRepo.getById(nodeId) ?: run {
                Timber.tag("TODAY_DEBUG").e("DELETE ERROR: node %s not found", nodeId)
                return@launch
            }
            if (node.templateId != "adhoc_events") {
                Timber.tag("TODAY_DEBUG").e("DELETE ERROR: node %s is not a spontaneous event", nodeId)
                return@launch
            }
            Timber.tag("TODAY_DEBUG").d("ACTION deleteSpontaneousEvent nodeId=%s name=%s", node.id, node.name)
            val updatedNode = node.copy(
                deletedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                version = node.version + 1
            )
            nodeRepo.update(updatedNode)
            _events.emit("Evento eliminado")
        }
    }

    fun createQuickPlanningItem(
        title: String,
        type: PlanningItemType,
        date: Long? = DateUtils.getStartOfDay(),
        time: String? = null,
        nodeId: String? = null
    ) {
        viewModelScope.launch {
            val item = com.alan.routineos.data.local.entities.PlanningItemEntity(
                id = UUID.randomUUID().toString(), type = type.name, title = title,
                description = null, dueDate = date, dueTime = time,
                relatedNodeId = nodeId, relatedNodePath = null, status = PlanningStatus.PENDING.name
            )
            planningRepo.upsertPlanningItem(item)
            _events.emit("${if (type == PlanningItemType.TASK) "Tarea" else "Nota"} rápida creada")
        }
    }

    fun adjustNodeDuration(nodeId: String, deltaMinutes: Int) {
        viewModelScope.launch {
            val entry = findEntryInState(nodeId)
            val resolved = if (entry == null) findResolvedNodeInState(nodeId) else null

            val startBefore = entry?.time?.split("-")?.firstOrNull()?.trim()
                ?: resolved?.timeLabel?.split("-")?.firstOrNull()?.trim()
            val endBefore = entry?.time?.split("-")?.getOrNull(1)?.trim()
                ?: resolved?.timeLabel?.split("-")?.getOrNull(1)?.trim()
            val durBefore = entry?.durationMinutes ?: resolved?.durationMinutes ?: 0

            val newDuration = durBefore + deltaMinutes

            Timber.tag("TODAY_DEBUG").d(
                """
                ACTION: adjustNodeDuration (INCREMENTAL)
                nodeId=$nodeId
                actionType=${if (deltaMinutes > 0) "EXTEND" else "REDUCE"}
                effectiveBefore=$startBefore - $endBefore
                currentEffectiveDuration=$durBefore
                requestedDelta=$deltaMinutes
                finalDuration=$newDuration
                """.trimIndent()
            )

            changeDuration(nodeId, newDuration)
        }
    }

    private fun findEntryInState(nodeId: String): TimelineEntryUi? {
        return _uiState.value.timelineEntries.find { it.id == nodeId }
    }

    private fun findResolvedNodeInState(nodeId: String): ResolvedNodeUi? {
        _uiState.value.timelineEntries.forEach { entry ->
            entry.resolvedNodes.find { it.id == nodeId }?.let { return it }
        }
        return null
    }

    private fun findNodeDurationInState(nodeId: String): Int? {
        val state = _uiState.value
        state.timelineEntries.forEach { entry ->
            if (entry.id == nodeId) return entry.durationMinutes
            entry.resolvedNodes.forEach { sub ->
                if (sub.id == nodeId) return sub.durationMinutes
            }
        }
        return null
    }

    fun postponeNode(nodeId: String, minutes: Int) {
        viewModelScope.launch {
            val node = nodeRepo.getById(nodeId) ?: return@launch
            if (node.temporalMode == TemporalMode.NONE || node.temporalMode == TemporalMode.SEQUENTIAL) {
                _events.emit("Esta actividad no tiene hora fija para posponer")
                return@launch
            }
            val instanceId = node.instanceId ?: return@launch

            val entry = findEntryInState(nodeId)
            val resolved = if (entry == null) findResolvedNodeInState(nodeId) else null
            val startBefore = entry?.time?.split("-")?.firstOrNull()?.trim() ?: resolved?.timeLabel?.split("-")?.firstOrNull()?.trim()
            val endBefore = entry?.time?.split("-")?.getOrNull(1)?.trim() ?: resolved?.timeLabel?.split("-")?.getOrNull(1)?.trim()

            Timber.tag("TODAY_DEBUG").d(
                """
                ACTION: postponeNode
                nodeId=$nodeId
                baseStart=${node.scheduledTime}
                effectiveBefore=$startBefore - $endBefore
                postponeMinutes=$minutes
                """.trimIndent()
            )

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
        Timber.tag("TODAY_DEBUG").d("ACTION: rescheduleNode - nodeId: $nodeId, newTime: $newTime")
        viewModelScope.launch {
            val node = nodeRepo.getById(nodeId) ?: run {
                Timber.tag("TODAY_DEBUG").e("ACTION ERROR: rescheduleNode - Node $nodeId not found")
                return@launch
            }

            if (node.temporalMode == TemporalMode.NONE || node.temporalMode == TemporalMode.SEQUENTIAL) {
                Timber.tag("TODAY_DEBUG").w("ACTION WARNING: rescheduleNode - Node $nodeId is not fixed-time")
                _events.emit("Esta actividad no tiene hora fija para reprogramar")
                return@launch
            }

            val instanceId = node.instanceId ?: run {
                Timber.tag("TODAY_DEBUG").e("ACTION ERROR: rescheduleNode - InstanceId is null for node $nodeId")
                return@launch
            }

            val entry = findEntryInState(nodeId)
            val resolved = if (entry == null) findResolvedNodeInState(nodeId) else null
            val durBefore = entry?.durationMinutes ?: resolved?.durationMinutes ?: 0

            Timber.tag("TODAY_DEBUG").d(
                """
                ACTION: rescheduleNode (MOVER HORA)
                nodeId=$nodeId
                baseStart=${node.scheduledTime}
                newRequestedStart=$newTime
                preservedDuration=$durBefore
                """.trimIndent()
            )

            // FIX TIME-0.4: Clear previous POSTPONE when rescheduling manually
            nodeOverrideRepo.deleteSpecificOverrideSync(nodeId, instanceId, OverrideType.POSTPONE)

            applyOverride(nodeId, instanceId, OverrideType.RESCHEDULE, newTime = newTime)
            _events.emit("Actividad reprogramada")
        }
    }

    fun customizeSchedule(nodeId: String, startTime: String, endTime: String) {
        Timber.tag("TODAY_DEBUG").d("ACTION: customizeSchedule - nodeId: $nodeId, start: $startTime, end: $endTime")
        viewModelScope.launch {
            val node = nodeRepo.getById(nodeId) ?: run {
                Timber.tag("TODAY_DEBUG").e("ACTION ERROR: customizeSchedule - Node $nodeId not found")
                return@launch
            }

            if (node.temporalMode == TemporalMode.NONE) {
                _events.emit("Esta actividad no tiene horario")
                return@launch
            }

            val instanceId = node.instanceId ?: run {
                Timber.tag("TODAY_DEBUG").e("ACTION ERROR: customizeSchedule - InstanceId is null for node $nodeId")
                return@launch
            }

            val startMins = ScheduleResolver.timeToMinutes(startTime)
            val endMins = ScheduleResolver.timeToMinutes(endTime)

            if (endMins <= startMins) {
                _events.emit("La hora final debe ser posterior al inicio")
                return@launch
            }

            val durationMinutes = endMins - startMins

            Timber.tag("TODAY_DEBUG").d(
                """
                ACTION: customizeSchedule (MANUAL OVERRIDE)
                nodeId=$nodeId
                newStartTime=$startTime
                newEndTime=$endTime
                calculatedDuration=$durationMinutes
                """.trimIndent()
            )

            // Clear previous POSTPONE to avoid double offsets when setting explicit times
            nodeOverrideRepo.deleteSpecificOverrideSync(nodeId, instanceId, OverrideType.POSTPONE)

            // Apply RESCHEDULE for Start Time
            applyOverride(nodeId, instanceId, OverrideType.RESCHEDULE, newTime = startTime)

            // Apply DURATION_CHANGE for End Time (calculated as duration)
            if (node.temporalMode == TemporalMode.START_END || node.temporalMode == TemporalMode.SEQUENTIAL) {
                applyOverride(nodeId, instanceId, OverrideType.DURATION_CHANGE, newDurationMinutes = durationMinutes)
            }

            _events.emit("Horario personalizado")
        }
    }

    fun changeDuration(nodeId: String, newDurationMinutes: Int) {
        viewModelScope.launch {
            val node = nodeRepo.getById(nodeId) ?: return@launch
            if (node.temporalMode == TemporalMode.NONE || node.temporalMode == TemporalMode.START_ONLY) {
                _events.emit("Esta actividad no usa duración")
                return@launch
            }
            val instanceId = node.instanceId ?: return@launch

            Timber.tag("TODAY_DEBUG").d(
                """
                ACTION: changeDuration (SET ABSOLUTE)
                nodeId=$nodeId
                requestedAbsoluteDuration=$newDurationMinutes
                """.trimIndent()
            )

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
        logNodeDetail("TODAY_DEBUG", "BEFORE_OVERRIDE_APPLY", nodeId)
        Timber.tag("TODAY_DEBUG").d(
            "APPLYING_OVERRIDE: type=$type, newTime=$newTime, dur=$newDurationMinutes, post=$postponeMinutes"
        )
        val existing = nodeOverrideRepo.getSpecificOverrideSync(nodeId, instanceId, type)

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

        logNodeDetail("TODAY_DEBUG", "AFTER_OVERRIDE_APPLY", nodeId)
    }

    private suspend fun logNodeDetail(tag: String, prefix: String, nodeId: String) {
        val node = nodeRepo.getById(nodeId)
        if (node == null) {
            Timber.tag(tag).d("$prefix: Node $nodeId not found")
            return
        }
        val children = nodeRepo.getChildren(node.id).first()
        val overrides = nodeOverrideRepo.getAll().first().filter { it.nodeId == nodeId }

        val details = """
            $prefix
            ID: ${node.id} | NAME: ${node.name} | PARENT: ${node.parentId}
            INSTANCE: ${node.instanceId} | TEMPLATE: ${node.templateId} | TYPE: ${node.typeId}
            MODE: ${node.temporalMode} | SEQ: ${node.isSequential} | POS: ${node.position}
            TIME: ${node.scheduledTime} | DUR: ${node.durationMinutes} | STATUS: ${node.status}
            CHILDREN: ${children.map { it.name }}
            OVERRIDES: ${overrides.map { "${it.overrideType}(time=${it.newTime}, dur=${it.newDurationMinutes}, post=${it.postponeMinutes})" }}
        """.trimIndent()
        Timber.tag(tag).d(details)
    }

    fun toggleNodeCompletion(nodeId: String) {
        viewModelScope.launch {
            val targetNode = nodeRepo.getById(nodeId) ?: return@launch
            val instanceId = targetNode.instanceId ?: return@launch

            val visibleNodeIdsToday = _uiState.value.timelineEntries
                .flatMap { entry ->
                    listOf(entry.id) + entry.resolvedNodes.map { it.id }
                }
                .toSet()

            if (targetNode.id !in visibleNodeIdsToday) {
                Timber.tag("TODAY_DEBUG").w(
                    "BLOCKED toggleNodeCompletion: node is not visible today nodeId=%s name=%s",
                    targetNode.id,
                    targetNode.name
                )
                return@launch
            }

            val allNodes = nodeRepo.getByInstance(instanceId).first()
            val newStatus =
                if (targetNode.status == NodeStatus.COMPLETED) NodeStatus.PENDING else NodeStatus.COMPLETED

            val nodesToUpdate = mutableMapOf<String, Node>()
            val allDescendants = getDescendants(targetNode.id, allNodes)
            val descendants = allDescendants.filter { it.id in visibleNodeIdsToday }

            Timber.tag("TODAY_DEBUG").d(
                """
                COMPLETION_SCOPE
                target=${targetNode.name}
                targetId=${targetNode.id}
                newStatus=$newStatus
                visibleTodayIds=$visibleNodeIdsToday
                descendantsToUpdate=${descendants.map { it.name }}
                excludedDescendants=${allDescendants.filter { it.id !in visibleNodeIdsToday }.map { it.name }}
                """.trimIndent()
            )

            val now = System.currentTimeMillis()
            nodesToUpdate[targetNode.id] = targetNode.copy(status = newStatus, updatedAt = now)
            descendants.forEach { desc ->
                nodesToUpdate[desc.id] = desc.copy(status = newStatus, updatedAt = now)
            }
            updateAncestors(targetNode.parentId, allNodes, nodesToUpdate, newStatus, visibleNodeIdsToday)
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

    fun updateNotificationPermissionStatus(granted: Boolean) {
        _uiState.update { it.copy(isNotificationPermissionGranted = granted) }
    }

    private fun getDescendants(nodeId: String, allNodes: List<Node>): List<Node> {
        val children = allNodes.filter { it.parentId == nodeId }
        return children + children.flatMap { getDescendants(it.id, allNodes) }
    }

    private fun updateAncestors(
        parentId: String?,
        allNodes: List<Node>,
        updates: MutableMap<String, Node>,
        triggerNewStatus: NodeStatus,
        visibleNodeIdsToday: Set<String>
    ) {
        if (parentId == null) return
        val parentNode = allNodes.find { it.id == parentId } ?: return
        val children = allNodes.filter { it.parentId == parentId && it.id in visibleNodeIdsToday }
        val now = System.currentTimeMillis()
        if (triggerNewStatus == NodeStatus.PENDING) {
            updates[parentNode.id] = parentNode.copy(status = NodeStatus.PENDING, updatedAt = now)
            updateAncestors(parentNode.parentId, allNodes, updates, NodeStatus.PENDING, visibleNodeIdsToday)
        } else {
            val allCompleted = children.all { child ->
                (updates[child.id]?.status ?: child.status) == NodeStatus.COMPLETED
            }
            if (allCompleted) {
                updates[parentNode.id] =
                    parentNode.copy(status = NodeStatus.COMPLETED, updatedAt = now)
                updateAncestors(parentNode.parentId, allNodes, updates, NodeStatus.COMPLETED, visibleNodeIdsToday)
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
