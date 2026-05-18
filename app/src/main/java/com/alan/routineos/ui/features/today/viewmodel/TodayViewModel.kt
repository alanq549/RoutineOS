package com.alan.routineos.ui.features.today.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.core.util.DateUtils
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

    private val _uiState = MutableStateFlow(TodayUiState(
        dateLabel = DateUtils.formatHeaderDate(),
        monthLabel = DateUtils.formatHeaderMonth(),
        isLoading = true
    ))
    val uiState = _uiState.asStateFlow()

    init {
        observeTodayData()
        updateTimeTicker()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeTodayData() {
        val today = DateUtils.getStartOfDay()
        val weekday = DateUtils.getDayOfWeek()

        viewModelScope.launch {
            instanceRepo.getByDate(today)
                .onEach { instances ->
                    if (instances.isEmpty()) {
                        generateInstanceIfNeeded(today, weekday)
                    }
                }
                .flatMapLatest { instances ->
                    if (instances.isEmpty()) flowOf(emptyList())
                    else {
                        // 1. Combine base data flows
                        val templatesFlow = templateRepo.getAll()
                        val schedulesFlow = scheduleRepo.getAll()
                        val fieldValuesFlow = fieldValueRepo.getAll()
                        val schemasFlow = schemaRepo.getAll()
                        
                        // Combine instance nodes
                        val allNodesFlow = combine(instances.map { instance ->
                            nodeRepo.getByInstance(instance.id).map { nodes -> 
                                nodes.map { node -> node to instance.templateId } 
                            }
                        }) { flowArray -> flowArray.toList().flatten() }

                        combine(
                            allNodesFlow,
                            templatesFlow,
                            schedulesFlow,
                            fieldValuesFlow,
                            schemasFlow
                        ) { nodesWithId, templates, schedules, fieldValues, schemas ->
                            buildTimeline(nodesWithId, templates, schedules, fieldValues, schemas, weekday)
                        }
                    }
                }
                .collect { entries ->
                    _uiState.update { state ->
                        state.copy(
                            timelineEntries = entries,
                            totalCount = entries.sumOf { entry -> entry.resolvedNodes.size + 1 },
                            completedCount = entries.sumOf { entry -> 
                                val rootDone = if (entry.statusLabel == NodeStatus.COMPLETED.name.lowercase()) 1 else 0
                                val childrenDone = entry.resolvedNodes.count { node -> node.isCompleted }
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
        fieldValues: List<NodeFieldValue>,
        schemas: List<NodeMetadataSchema>,
        weekday: Int
    ): List<TimelineEntryUi> {
        val rootNodes = nodesWithId.filter { it.first.parentId == null }
        
        return rootNodes.mapNotNull { (rootNode, templateId) ->
            // Filter out deleted activities
            val template = templates.find { it.id == templateId } ?: return@mapNotNull null
            
            val children = nodesWithId.filter { it.first.parentId == rootNode.id }.map { it.first }
            
            // Find global schedule to build the time label
            val schedule = schedules.find { it.templateId == templateId && it.weekday == weekday }

            val timeDisplay = when (template.timeMode) {
                TimeMode.FIXED_START -> schedule?.startTime ?: rootNode.scheduledTime ?: "--:--"
                TimeMode.RANGE -> if (schedule?.startTime != null && schedule.endTime != null) 
                    "${schedule.startTime} - ${schedule.endTime}" else rootNode.scheduledTime ?: "--:--"
                TimeMode.DURATION -> schedule?.startTime ?: rootNode.scheduledTime ?: "--:--"
                TimeMode.FLEXIBLE -> "Flexible"
            }

            val color = try {
                Color(template.colorHex.toColorInt())
            } catch (e: Exception) {
                Color(0xFF42A5F5)
            }

            TimelineEntryUi(
                id = rootNode.id,
                time = timeDisplay,
                title = rootNode.name,
                statusLabel = rootNode.status.name.lowercase(),
                statusColor = color,
                barColor = color,
                resolvedNodes = children.map { child ->
                    val nodeValues = fieldValues.filter { it.nodeId == child.id }
                    ResolvedNodeUi(
                        id = child.id, 
                        name = child.name, 
                        depth = 1, 
                        time = child.scheduledTime,
                        isCompleted = child.status == NodeStatus.COMPLETED,
                        fields = nodeValues.map { v ->
                            val s = schemas.find { it.id == v.schemaId }
                            ResolvedFieldUi(v.schemaId, v.fieldName, s?.fieldLabel ?: v.fieldName, v.value, s?.fieldType ?: FieldType.TEXT)
                        }
                    )
                }
            )
        }.sortedBy { it.time }
    }

    private suspend fun generateInstanceIfNeeded(today: Long, weekday: Int) {
        val exceptions = exceptionRepo.getActiveForDate(today).first()
        if (exceptions.any { it.affectsGeneration }) return
        val activeSchedules = scheduleRepo.getActiveForWeekday(weekday, today).first()
        activeSchedules.forEach { active ->
            instanceRepo.generateInstance(active.templateId, today)
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
                val newStatus = if (node.status == NodeStatus.COMPLETED) NodeStatus.PENDING else NodeStatus.COMPLETED
                nodeRepo.update(node.copy(status = newStatus, updatedAt = System.currentTimeMillis()))
            }
        }
    }
}
