package com.alan.routineos.ui.features.today.viewmodel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.core.util.DateUtils
import com.alan.routineos.data.local.entities.FieldType
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeFieldValue
import com.alan.routineos.data.local.entities.NodeMetadataSchema
import com.alan.routineos.data.local.entities.NodeSchedule
import com.alan.routineos.data.local.entities.NodeStatus
import com.alan.routineos.data.local.entities.RoutineTemplate
import com.alan.routineos.data.local.entities.Schedule
import com.alan.routineos.data.repository.FieldValueRepository
import com.alan.routineos.data.repository.InstanceRepository
import com.alan.routineos.data.repository.MetadataSchemaRepository
import com.alan.routineos.data.repository.NodeRepository
import com.alan.routineos.data.repository.ScheduleExceptionRepository
import com.alan.routineos.data.repository.ScheduleRepository
import com.alan.routineos.data.repository.TemplateRepository
import com.alan.routineos.ui.features.template_builder.sections.TimeMode
import com.alan.routineos.ui.features.today.state.ResolvedFieldUi
import com.alan.routineos.ui.features.today.state.ResolvedNodeUi
import com.alan.routineos.ui.features.today.state.TimelineEntryUi
import com.alan.routineos.ui.features.today.state.TodayUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val instanceRepo: InstanceRepository,
    private val nodeRepo: NodeRepository,
    private val fieldValueRepo: FieldValueRepository,
    private val templateRepo: TemplateRepository,
    private val scheduleRepo: ScheduleRepository,
    private val exceptionRepo: ScheduleExceptionRepository,
    private val schemaRepo: MetadataSchemaRepository
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
                            schemasFlow
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

                            buildTimeline(
                                nodesWithId,
                                templates,
                                schedules,
                                nodeSchedules,
                                fieldValues,
                                schemas,
                                weekday
                            )
                        }
                    }
                }
                .collect { entries ->
                    Log.d("TODAY_DEBUG", "--- TODAY TIMELINE RENDER UPDATE ---")
                    Log.d("TODAY_DEBUG", "Activities to display: ${entries.size}")
                    entries.forEachIndexed { index, entry ->
                        val fieldsLog = entry.fields.joinToString { "${it.label}: ${it.value}" }
                        Log.d("TODAY_DEBUG", "  [$index] ACTIVITY: [${entry.time}] ${entry.title} | Status: ${entry.statusLabel} | Sort: ${entry.sortTime} | Fields: $fieldsLog")
                        entry.resolvedNodes.forEach { node ->
                            val nodeFieldsLog = node.fields.joinToString { "${it.label}: ${it.value}" }
                            Log.d("TODAY_DEBUG", "    node: ${"  ".repeat(node.depth)}[${node.timeLabel ?: "Seq"}] ${node.name} | Done: ${node.isCompleted} | Fields: $nodeFieldsLog")
                        }
                    }
                    Log.d("TODAY_DEBUG", "-------------------------------------")

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
        weekday: Int
    ): List<TimelineEntryUi> {

        val allNodes = nodesWithId.map { it.first }
        val rootNodesWithTemplate = nodesWithId.filter { it.first.parentId == null }

        return rootNodesWithTemplate.mapNotNull { (rootNode, templateId) ->
            val template = templates.find { it.id == templateId } ?: return@mapNotNull null

            // LIMPIEZA AUTOMÁTICA (Fase 3.1)
            dedupeFieldValuesForNode(rootNode.id)

            val resolvedNodes = resolveNodesRecursive(
                parentId = rootNode.id,
                allNodes = allNodes,
                nodeSchedules = nodeSchedules,
                fieldValues = fieldValues,
                schemas = schemas,
                depth = 1,
                todayWeekday = weekday
            )

            val globalSchedule =
                schedules.find { it.templateId == templateId && it.weekday == weekday }
            
            val earliestChild = resolvedNodes
                .mapNotNull { node ->
                    val time = node.timeLabel
                    if (time != null && time.matches(Regex("\\d{2}:\\d{2}.*"))) {
                        time.take(5)
                    } else {
                        null
                    }
                }
                .minOrNull()

            val label = when {
                template.timeMode == TimeMode.FLEXIBLE -> "Flexible"
                globalSchedule?.startTime != null && globalSchedule.endTime != null -> 
                    "${globalSchedule.startTime} - ${globalSchedule.endTime}"
                globalSchedule?.startTime != null -> globalSchedule.startTime
                rootNode.scheduledTime != null -> rootNode.scheduledTime
                else -> "--:--"
            }

            val sortTime = globalSchedule?.startTime ?: earliestChild ?: "99:99"

            Log.d("TODAY_DEBUG", "TIME RESOLVE activity=${template.name} mode=${template.timeMode} start=${globalSchedule?.startTime} end=${globalSchedule?.endTime} earliestChild=$earliestChild label=$label sort=$sortTime")

            val color = try {
                Color(template.colorHex.toColorInt())
            } catch (e: Exception) {
                Color(0xFF42A5F5)
            }

            val rootValues = fieldValues.filter { it.nodeId == rootNode.id }

            TimelineEntryUi(
                id = rootNode.id,
                time = label,
                sortTime = sortTime,
                title = rootNode.name,
                statusLabel = rootNode.status.name.lowercase(),
                statusColor = color,
                barColor = color,
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
                resolvedNodes = resolvedNodes
            )
        }.sortedBy { it.sortTime }
    }

    private fun resolveNodesRecursive(
        parentId: String,
        allNodes: List<Node>,
        nodeSchedules: List<NodeSchedule>,
        fieldValues: List<NodeFieldValue>,
        schemas: List<NodeMetadataSchema>,
        depth: Int,
        todayWeekday: Int
    ): List<ResolvedNodeUi> {
        return allNodes
            .filter { it.parentId == parentId }
            .sortedBy { it.position }
            .flatMap { node ->
                // LIMPIEZA AUTOMÁTICA (Fase 3.1)
                dedupeFieldValuesForNode(node.id)

                val schedules = nodeSchedules.filter {
                    it.nodeId == node.id || it.nodeId == node.templateId
                }
                val todaySchedule = schedules.find { it.dayOfWeek == todayWeekday }

                val dayFromName = nodeDayFromName(node.name)
                if (dayFromName != null && dayFromName != todayWeekday) {
                    return@flatMap emptyList<ResolvedNodeUi>()
                }

                if (schedules.isNotEmpty() && todaySchedule == null) {
                    return@flatMap emptyList<ResolvedNodeUi>()
                }

                val timeLabel = todaySchedule?.let {
                    if (!it.endTime.isNullOrBlank() && it.endTime != it.startTime) {
                        "${it.startTime} - ${it.endTime}"
                    } else {
                        it.startTime
                    }
                } ?: node.scheduledTime

                val nodeValues = fieldValues.filter { it.nodeId == node.id }
                val resolvedNode = ResolvedNodeUi(
                    id = node.id,
                    name = node.name,
                    depth = depth,
                    timeLabel = timeLabel,
                    isCompleted = node.status == NodeStatus.COMPLETED,
                    fields = nodeValues.map { v ->
                        val s = schemas.find { it.id == v.schemaId }
                        ResolvedFieldUi(
                            v.schemaId,
                            v.fieldName,
                            s?.fieldLabel ?: v.fieldName,
                            v.value,
                            s?.fieldType ?: FieldType.TEXT
                        )
                    }
                )

                listOf(resolvedNode) + resolveNodesRecursive(
                    node.id,
                    allNodes,
                    nodeSchedules,
                    fieldValues,
                    schemas,
                    depth + 1,
                    todayWeekday
                )
            }
    }

    private fun dedupeFieldValuesForNode(nodeId: String) {
        viewModelScope.launch {
            val values = fieldValueRepo.getByNodeSync(nodeId)
            if (values.isEmpty()) return@launch

            // Limpieza por schemaId
            values.groupBy { it.schemaId }.forEach { (schemaId, group) ->
                if (group.size > 1) {
                    val sorted = group.sortedByDescending { it.updatedAt }
                    val toDelete = sorted.drop(1)
                    fieldValueRepo.deleteByIds(toDelete.map { it.id })
                }
            }
        }
    }

    private fun nodeDayFromName(name: String): Int? {
        return when (name.trim().lowercase()) {
            "lunes" -> 1
            "martes" -> 2
            "miércoles", "miercoles" -> 3
            "jueves" -> 4
            "viernes" -> 5
            "sábado", "sabado" -> 6
            "domingo" -> 7
            else -> null
        }
    }

    private suspend fun generateInstanceIfNeeded(today: Long, weekday: Int) {
        val exceptions = exceptionRepo.getActiveForDate(today).first()
        if (exceptions.any { it.affectsGeneration }) {
            Log.d("TODAY_DEBUG", "GENERATE: Generation skipped due to active exception")
            return
        }

        val activeSchedules = scheduleRepo.getActiveForWeekday(weekday, today).first()
        Log.d("TODAY_DEBUG", "GENERATE: Found ${activeSchedules.size} active schedules for weekday $weekday")

        activeSchedules.forEach { active ->
            Log.d("TODAY_DEBUG", "GENERATE: Triggering generation for templateId=${active.templateId}")
            instanceRepo.generateInstanceIfNeeded(active.templateId, today)
        }
    }

    private fun updateTimeTicker() {
        viewModelScope.launch {
            while (true) {
                val cal = Calendar.getInstance()
                val hour = cal.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
                val min = cal.get(Calendar.MINUTE).toString().padStart(2, '0')
                _uiState.update { state ->
                    state.copy(currentTime = "$hour:$min")
                }
                delay(60_000)
            }
        }
    }

    fun toggleNodeCompletion(nodeId: String) {
        viewModelScope.launch {
            nodeRepo.getById(nodeId)?.let { node ->
                val newStatus =
                    if (node.status == NodeStatus.COMPLETED) NodeStatus.PENDING else NodeStatus.COMPLETED
                nodeRepo.update(
                    node.copy(
                        status = newStatus,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}
