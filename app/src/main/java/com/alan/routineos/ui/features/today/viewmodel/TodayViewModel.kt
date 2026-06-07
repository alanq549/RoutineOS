package com.alan.routineos.ui.features.today.viewmodel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.core.util.DateUtils
import com.alan.routineos.core.util.ScheduleResolver
import com.alan.routineos.data.local.entities.*
import com.alan.routineos.data.repository.*
import com.alan.routineos.ui.features.template_builder.sections.TimeMode
import com.alan.routineos.ui.features.today.state.ResolvedFieldUi
import com.alan.routineos.ui.features.today.state.ResolvedNodeUi
import com.alan.routineos.ui.features.today.state.TimelineEntryUi
import com.alan.routineos.ui.features.today.state.TodayUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
    private val nodeOverrideRepo: NodeOverrideRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TodayUiState(
            dateLabel = DateUtils.formatHeaderDate(),
            monthLabel = DateUtils.formatHeaderMonth(),
            isLoading = true
        )
    )

    val uiState: StateFlow<TodayUiState> = _uiState

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
            instanceRepo.getByDate(today)
                .flatMapLatest { instances ->
                    if (instances.isEmpty()) {
                        Log.d("TODAY_DEBUG", "OBSERVE: No instances found for today")
                        flowOf(emptyList<TimelineEntryUi>())
                    } else {
                        Log.d("TODAY_DEBUG", "OBSERVE: Found ${instances.size} instances. Resolving data...")
                        val templatesFlow = templateRepo.getAll()
                        val schedulesFlow = scheduleRepo.getAll()
                        val nodeSchedulesFlow = nodeRepo.getAllNodeSchedules()
                        val fieldValuesFlow = fieldValueRepo.getAll()
                        val schemasFlow = schemaRepo.getAll()
                        val overridesFlow = nodeOverrideRepo.getAll()

                        val nodeFlows = instances.map { instance ->
                            val tId = instance.templateId ?: ""
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
                            overridesFlow
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

                            buildTimeline(
                                nodesWithId,
                                templates,
                                schedules,
                                nodeSchedules,
                                fieldValues,
                                schemas,
                                overrides,
                                weekday
                            )
                        }
                    }
                }
                .collect { entries ->
                    _uiState.update { state ->
                        state.copy(
                            timelineEntries = entries,
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
        weekday: Int
    ): List<TimelineEntryUi> {

        val allNodes = nodesWithId.map { it.first }
        val rootNodesWithTemplate = nodesWithId.filter { it.first.parentId == null }
        val nodeSchedulesMap = nodeSchedules.groupBy { it.nodeId }
        val overridesMap = overrides.groupBy { it.nodeId }

        Log.d("TODAY_DOMINO_DEBUG", "DOMINO START entries=${rootNodesWithTemplate.size}")

        // 1. Prepare initial blocks for root nodes
        val blocks = rootNodesWithTemplate.mapNotNull { (rootNode, templateId) ->
            val template = templates.find { it.id == templateId } ?: return@mapNotNull null
            val templateSchedules = schedules.filter { it.templateId == templateId }
            val globalSchedule = templateSchedules.find { it.weekday == weekday }
            
            val baseStart = globalSchedule?.startTime ?: rootNode.scheduledTime
            val baseEnd = globalSchedule?.endTime ?: baseStart?.let { addMinutes(it, rootNode.durationMinutes ?: 60) }
            
            val duration = if (baseStart != null && baseEnd != null) {
                val diff = ScheduleResolver.timeToMinutes(baseEnd) - ScheduleResolver.timeToMinutes(baseStart)
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

        // 2. Apply Overrides & Domino Propagations
        var accumulatedShift = 0
        var shiftSource: String? = null

        val resolvedBlocks = blocks.map { block ->
            Log.d("TODAY_DOMINO_DEBUG", "DOMINO BLOCK base node=${block.node.name} start=${block.originalStart} end=${block.originalEnd}")

            val nodeOverrides = overridesMap[block.node.id].orEmpty()
            
            // Apply accumulated shift if block has a defined start
            if (accumulatedShift != 0 && block.originalStart != null) {
                block.effectiveStart = addMinutes(block.originalStart, accumulatedShift)
                block.appliedShiftMinutes = accumulatedShift
                block.shiftReason = "Movido por $shiftSource ${if(accumulatedShift > 0) "+" else ""}${accumulatedShift}m"
                Log.d("TODAY_DOMINO_DEBUG", "DOMINO SHIFT APPLIED node=${block.node.name} shift=$accumulatedShift")
            }

            var nodeSpecificDelta = 0
            nodeOverrides.forEach { ov ->
                Log.d("TODAY_DOMINO_DEBUG", "DOMINO OVERRIDE node=${block.node.name} type=${ov.overrideType}")
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
                            // Calculate how much this anchor changes the timeline relative to current shift
                            val totalShiftFromOriginal = ScheduleResolver.timeToMinutes(newTime) - ScheduleResolver.timeToMinutes(block.originalStart)
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
                Log.d("TODAY_DOMINO_DEBUG", "DOMINO DELTA GENERATED node=${block.node.name} delta=$nodeSpecificDelta")
            }

            Log.d("TODAY_DOMINO_DEBUG", "DOMINO RESULT node=${block.node.name} effectiveStart=${block.effectiveStart} effectiveEnd=${block.effectiveEnd}")
            block
        }

        Log.d("TODAY_DOMINO_DEBUG", "DOMINO END")

        // 3. Convert to UI State
        return resolvedBlocks.map { block ->
            val rootNode = block.node
            val templateId = block.template.id
            val template = block.template
            val templateSchedules = schedules.filter { it.templateId == templateId }

            val resolvedNodes = resolveNodesRecursive(
                parentId = rootNode.id,
                allNodes = allNodes,
                nodeSchedulesMap = nodeSchedulesMap,
                overridesMap = overridesMap,
                fieldValues = fieldValues,
                schemas = schemas,
                depth = 1,
                todayWeekday = weekday,
                template = template,
                allTemplateSchedules = templateSchedules
            )

            val label = when {
                template.timeMode == TimeMode.FLEXIBLE -> "Flexible"
                block.effectiveStart != null && block.effectiveEnd != null && block.effectiveStart != block.effectiveEnd ->
                    "${block.effectiveStart} - ${block.effectiveEnd}"
                block.effectiveStart != null -> block.effectiveStart!!
                else -> "--:--"
            }

            val color = try { Color(template.colorHex.toColorInt()) } catch (e: Exception) { Color(0xFF42A5F5) }
            val rootValues = fieldValues.filter { it.nodeId == rootNode.id }

            TimelineEntryUi(
                id = rootNode.id,
                time = label,
                sortTime = block.effectiveStart ?: "99:99",
                title = rootNode.name,
                statusLabel = if (block.isSkipped) "skipped" else rootNode.status.name.lowercase(),
                statusColor = color,
                barColor = color,
                isSkipped = block.isSkipped,
                fields = rootValues.map { v ->
                    val s = schemas.find { it.id == v.schemaId }
                    ResolvedFieldUi(v.schemaId, v.fieldName, s?.fieldLabel ?: v.fieldName, v.value, s?.fieldType ?: FieldType.TEXT)
                },
                resolvedNodes = resolvedNodes,
                wasShiftedByDomino = block.appliedShiftMinutes != 0,
                dominoReason = block.shiftReason
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
        depth: Int,
        todayWeekday: Int,
        template: RoutineTemplate,
        allTemplateSchedules: List<Schedule>
    ): List<ResolvedNodeUi> {
        return allNodes
            .filter { it.parentId == parentId }
            .sortedBy { it.position }
            .flatMap { node ->
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
                } else if (effectiveSchedules.isEmpty()) {
                    if (!ScheduleResolver.isWeekdayNameFallback(node.name, todayWeekday) && 
                        ScheduleResolver.getWeekdayFromName(node.name) != null) {
                        appliesToday = false
                    }
                }

                if (!appliesToday) return@flatMap emptyList<ResolvedNodeUi>()

                // APPLY OVERRIDES LOGIC
                var status = node.status
                var startTime = todaySchedule?.startTime ?: node.scheduledTime
                var endTime = todaySchedule?.endTime
                
                nodeOverrides.forEach { override ->
                    when (override.overrideType) {
                        OverrideType.SKIP -> {
                            status = NodeStatus.SKIPPED
                        }
                        OverrideType.POSTPONE -> {
                            val mins = override.postponeMinutes ?: 0
                            startTime = addMinutes(startTime, mins)
                            endTime = addMinutes(endTime, mins)
                        }
                        OverrideType.RESCHEDULE -> {
                            startTime = override.newTime ?: startTime
                        }
                        OverrideType.DURATION_CHANGE -> {
                            val newDuration = override.newDurationMinutes ?: 0
                            endTime = addMinutes(startTime, newDuration)
                        }
                        else -> {}
                    }
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
                        ResolvedFieldUi(v.schemaId, v.fieldName, s?.fieldLabel ?: v.fieldName, v.value, s?.fieldType ?: FieldType.TEXT)
                    }
                )

                listOf(resolvedNode) + resolveNodesRecursive(
                    node.id, allNodes, nodeSchedulesMap, overridesMap, fieldValues, schemas,
                    depth + 1, todayWeekday, template, allTemplateSchedules
                )
            }
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
        }
    }

    fun skipNode(nodeId: String) {
        viewModelScope.launch {
            val node = nodeRepo.getById(nodeId) ?: return@launch
            val instanceId = node.instanceId ?: return@launch
            applyOverride(nodeId, instanceId, OverrideType.SKIP)
            nodeRepo.update(node.copy(status = NodeStatus.SKIPPED))
        }
    }

    fun rescheduleNode(nodeId: String, newTime: String) {
        viewModelScope.launch {
            val node = nodeRepo.getById(nodeId) ?: return@launch
            val instanceId = node.instanceId ?: return@launch
            applyOverride(nodeId, instanceId, OverrideType.RESCHEDULE, newTime = newTime)
        }
    }

    fun changeDuration(nodeId: String, newDurationMinutes: Int) {
        viewModelScope.launch {
            val node = nodeRepo.getById(nodeId) ?: return@launch
            val instanceId = node.instanceId ?: return@launch
            applyOverride(nodeId, instanceId, OverrideType.DURATION_CHANGE, newDurationMinutes = newDurationMinutes)
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
            nodeOverrideRepo.upsert(existing.copy(
                newTime = newTime ?: existing.newTime,
                newDurationMinutes = newDurationMinutes ?: existing.newDurationMinutes,
                postponeMinutes = postponeMinutes ?: existing.postponeMinutes,
                updatedAt = System.currentTimeMillis()
            ))
            Log.d("TODAY_OVERRIDE_DEBUG", "UPDATE EXISTING OVERRIDE nodeId=$nodeId type=$type")
        } else {
            nodeOverrideRepo.upsert(NodeOverride(
                nodeId = nodeId,
                instanceId = instanceId,
                overrideType = type,
                newTime = newTime,
                newDurationMinutes = newDurationMinutes,
                postponeMinutes = postponeMinutes
            ))
            Log.d("TODAY_OVERRIDE_DEBUG", "CREATE OVERRIDE nodeId=$nodeId type=$type")
        }
    }

    fun toggleNodeCompletion(nodeId: String) {
        viewModelScope.launch {
            val targetNode = nodeRepo.getById(nodeId) ?: return@launch
            val instanceId = targetNode.instanceId ?: return@launch
            val allNodes = nodeRepo.getByInstance(instanceId).first()
            val newStatus = if (targetNode.status == NodeStatus.COMPLETED) NodeStatus.PENDING else NodeStatus.COMPLETED
            
            val nodesToUpdate = mutableMapOf<String, Node>()
            val descendants = getDescendants(targetNode.id, allNodes)
            val now = System.currentTimeMillis()
            nodesToUpdate[targetNode.id] = targetNode.copy(status = newStatus, updatedAt = now)
            descendants.forEach { desc -> nodesToUpdate[desc.id] = desc.copy(status = newStatus, updatedAt = now) }
            updateAncestors(targetNode.parentId, allNodes, nodesToUpdate, newStatus)
            if (nodesToUpdate.isNotEmpty()) nodeRepo.insertAll(nodesToUpdate.values.toList())
        }
    }

    private fun getDescendants(nodeId: String, allNodes: List<Node>): List<Node> {
        val children = allNodes.filter { it.parentId == nodeId }
        return children + children.flatMap { getDescendants(it.id, allNodes) }
    }

    private fun updateAncestors(parentId: String?, allNodes: List<Node>, updates: MutableMap<String, Node>, triggerNewStatus: NodeStatus) {
        if (parentId == null) return
        val parentNode = allNodes.find { it.id == parentId } ?: return
        val children = allNodes.filter { it.parentId == parentId }
        val now = System.currentTimeMillis()
        if (triggerNewStatus == NodeStatus.PENDING) {
            updates[parentNode.id] = parentNode.copy(status = NodeStatus.PENDING, updatedAt = now)
            updateAncestors(parentNode.parentId, allNodes, updates, NodeStatus.PENDING)
        } else {
            val allCompleted = children.all { child -> (updates[child.id]?.status ?: child.status) == NodeStatus.COMPLETED }
            if (allCompleted) {
                updates[parentNode.id] = parentNode.copy(status = NodeStatus.COMPLETED, updatedAt = now)
                updateAncestors(parentNode.parentId, allNodes, updates, NodeStatus.COMPLETED)
            }
        }
    }

    private fun generateInstanceIfNeeded(today: Long, weekday: Int) {
        viewModelScope.launch {
            val exceptions = exceptionRepo.getActiveForDate(today).first()
            if (exceptions.any { it.affectsGeneration }) return@launch
            val activeSchedules = scheduleRepo.getActiveForWeekday(weekday, today).first()
            activeSchedules.forEach { active -> instanceRepo.generateInstanceIfNeeded(active.templateId, today) }
        }
    }

    private fun updateTimeTicker() {
        viewModelScope.launch {
            while (true) {
                val cal = Calendar.getInstance()
                _uiState.update { it.copy(currentTime = String.format(Locale.US, "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))) }
                delay(60_000)
            }
        }
    }
}
