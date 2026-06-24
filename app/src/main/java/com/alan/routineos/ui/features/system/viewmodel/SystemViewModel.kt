package com.alan.routineos.ui.features.system.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.core.util.DateUtils
import com.alan.routineos.data.local.entities.*
import com.alan.routineos.data.repository.InstanceRepository
import com.alan.routineos.data.repository.NodeRepository
import com.alan.routineos.data.repository.PlanningRepository
import com.alan.routineos.data.repository.ScheduleExceptionRepository
import com.alan.routineos.ui.features.system.state.PlanningItemType
import com.alan.routineos.ui.features.system.state.PlanningItemUi
import com.alan.routineos.ui.features.system.state.PlanningSection
import com.alan.routineos.ui.features.system.state.PlanningStatus
import com.alan.routineos.ui.features.system.state.PlanningTargetUi
import com.alan.routineos.ui.features.system.state.SystemUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SystemViewModel @Inject constructor(
    private val exceptionRepo: ScheduleExceptionRepository,
    private val nodeRepo: NodeRepository,
    private val planningRepo: PlanningRepository,
    private val instanceRepo: InstanceRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(DateUtils.getStartOfDay())
    private val _currentWeekStart = MutableStateFlow(DateUtils.getStartOfWeek())
    private val _planningSubTab = MutableStateFlow(PlanningSection.ROUTINE_CHANGES)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SystemUiState> = _selectedDate.flatMapLatest { selectedDate ->
        combine(
            _currentWeekStart,
            _planningSubTab,
            planningRepo.getAllPlanningItems(),
            nodeRepo.getAllTemplateNodes(),
            exceptionRepo.getActiveForDate(selectedDate)
        ) { weekStart, subTab, itemsEntities, allNodes, adaptations ->
            
            val targets = buildPlanningTargets(allNodes)
            val items = itemsEntities.map { it.toUi() }

            val filteredItems = items.filter { item ->
                item.dueDate == null || DateUtils.getStartOfDay(item.dueDate) == selectedDate
            }.sortedWith(
                compareBy<PlanningItemUi> { it.status == PlanningStatus.COMPLETED }
                    .thenByDescending { it.dueDate != null }
                    .thenBy { it.dueTime ?: "zzzz" }
                    .thenBy { it.title }
            )
            
            SystemUiState(
                selectedDate = selectedDate,
                currentWeekStart = weekStart,
                planningSubTab = subTab,
                adaptations = adaptations,
                allNodes = allNodes,
                planningItems = filteredItems,
                planningTargets = targets,
                isLoading = false,
                activeTab = 1
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SystemUiState(isLoading = true)
    )

    fun setPlanningSubTab(section: PlanningSection) {
        _planningSubTab.value = section
    }

    fun selectDate(date: Long) {
        val startOfDay = DateUtils.getStartOfDay(date)
        _selectedDate.value = startOfDay
    }

    fun nextWeek() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = _currentWeekStart.value
        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        _currentWeekStart.value = calendar.timeInMillis
    }

    fun prevWeek() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = _currentWeekStart.value
        calendar.add(Calendar.WEEK_OF_YEAR, -1)
        _currentWeekStart.value = calendar.timeInMillis
    }

    fun createAdaptation(label: String, rangeType: Int, recurrence: RecurrenceType = RecurrenceType.NONE) {
        viewModelScope.launch {
            val from: Long
            val to: Long
            
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = _selectedDate.value
            val weekday = DateUtils.getDayOfWeek(calendar.time)

            when (rangeType) {
                0 -> { // Solo este día
                    from = _selectedDate.value
                    to = _selectedDate.value + (24 * 60 * 60 * 1000L) - 1
                }
                1 -> { // Toda esta semana
                    from = _currentWeekStart.value
                    to = _currentWeekStart.value + (7 * 24 * 60 * 60 * 1000L) - 1
                }
                else -> {
                    from = _selectedDate.value
                    to = _selectedDate.value
                }
            }

            val exception = ScheduleException(
                label = label,
                dateFrom = from,
                dateTo = if (recurrence != RecurrenceType.NONE) Long.MAX_VALUE else to,
                affectsGeneration = true,
                recurrenceType = recurrence,
                weekday = if (recurrence == RecurrenceType.WEEKLY) weekday else if (recurrence == RecurrenceType.MONTHLY) calendar.get(Calendar.DAY_OF_MONTH) else null
            )
            exceptionRepo.upsert(exception)
            Log.d("PLANNING_DEBUG", "ROUTINE_CHANGE CREATE label=$label from=$from recurrence=$recurrence")
            
            if (recurrence == RecurrenceType.NONE) {
                refreshInstancesInRange(from, to)
            } else {
                instanceRepo.refreshInstancesForDate(_selectedDate.value)
            }
        }
    }

    fun deleteAdaptation(adaptation: ScheduleException) {
        viewModelScope.launch {
            exceptionRepo.delete(adaptation)
            Log.d("PLANNING_DEBUG", "ROUTINE_CHANGE DELETE id=${adaptation.id}")
            if (adaptation.recurrenceType == RecurrenceType.NONE) {
                refreshInstancesInRange(adaptation.dateFrom, adaptation.dateTo)
            } else {
                instanceRepo.refreshInstancesForDate(_selectedDate.value)
            }
        }
    }

    private suspend fun refreshInstancesInRange(from: Long, to: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = from
        val endDay = DateUtils.getStartOfDay(to)
        
        var currentDay = DateUtils.getStartOfDay(calendar.timeInMillis)
        while (currentDay <= endDay) {
            instanceRepo.refreshInstancesForDate(currentDay)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            currentDay = DateUtils.getStartOfDay(calendar.timeInMillis)
            if (calendar.timeInMillis > from + 365L * 24 * 60 * 60 * 1000) break 
        }
    }

    // Planning Items
    fun createPlanningItem(title: String, desc: String?, type: PlanningItemType, nodeId: String? = null, nodePath: String? = null, dueDate: Long? = null, dueTime: String? = null) {
        Log.d("PLANNING_DEBUG", "ACTION: createPlanningItem - title: $title, type: $type, nodeId: $nodeId")
        viewModelScope.launch {
            val newItem = PlanningItemUi(
                id = UUID.randomUUID().toString(),
                type = type,
                title = title,
                description = desc,
                dueDate = dueDate,
                dueTime = dueTime,
                relatedNodeId = nodeId,
                relatedNodePath = nodePath,
                status = PlanningStatus.PENDING
            )
            planningRepo.upsertPlanningItem(newItem.toEntity())
            Log.d("PLANNING_DEBUG", "AFTER_CREATE_PLANNING: id=${newItem.id} | title=${newItem.title} | nodeId=$nodeId")
        }
    }

    fun updatePlanningItem(id: String, title: String, desc: String?, nodeId: String? = null, nodePath: String? = null, dueDate: Long? = null, dueTime: String? = null) {
        Log.d("PLANNING_DEBUG", "ACTION: updatePlanningItem - id: $id, title: $title")
        viewModelScope.launch {
            val items = planningRepo.getAllPlanningItems().first()
            val currentEntity = items.find { it.id == id } ?: run {
                Log.e("PLANNING_DEBUG", "UPDATE ERROR: item $id not found")
                return@launch
            }
            
            Log.d("PLANNING_DEBUG", "BEFORE_UPDATE_PLANNING: id=${currentEntity.id} | title=${currentEntity.title} | status=${currentEntity.status}")
            
            val updatedEntity = currentEntity.copy(
                title = title, 
                description = desc, 
                dueDate = dueDate, 
                dueTime = dueTime, 
                relatedNodeId = nodeId, 
                relatedNodePath = nodePath, 
                updatedAt = System.currentTimeMillis(), 
                version = currentEntity.version + 1
            )
            planningRepo.upsertPlanningItem(updatedEntity)
            Log.d("PLANNING_DEBUG", "AFTER_UPDATE_PLANNING: id=${updatedEntity.id} | title=${updatedEntity.title}")
        }
    }

    fun togglePlanningItem(id: String) {
        Log.d("PLANNING_DEBUG", "ACTION: togglePlanningItem - id: $id")
        viewModelScope.launch {
            val itemsEntities = planningRepo.getAllPlanningItems().first()
            val currentEntity = itemsEntities.find { it.id == id } ?: return@launch
            val newStatus = if (currentEntity.status == PlanningStatus.PENDING.name) PlanningStatus.COMPLETED.name else PlanningStatus.PENDING.name
            planningRepo.upsertPlanningItem(currentEntity.copy(status = newStatus, updatedAt = System.currentTimeMillis(), version = currentEntity.version + 1))
        }
    }

    fun deletePlanningItem(id: String) {
        Log.d("PLANNING_DEBUG", "ACTION: deletePlanningItem - id: $id")
        viewModelScope.launch {
            planningRepo.deletePlanningItem(id)
        }
    }

    private fun buildPlanningTargets(nodes: List<Node>): List<PlanningTargetUi> {
        val nodeMap = nodes.associateBy { it.id }
        return nodes.map { node ->
            val pathParts = mutableListOf<String>()
            var current: Node? = node
            var level = 0
            while (current != null) {
                pathParts.add(0, current.name)
                current = current.parentId?.let { nodeMap[it] }
                if (current != null) level++
            }
            PlanningTargetUi(nodeId = node.id, path = pathParts.joinToString(" > "), level = level)
        }.sortedBy { it.path }
    }

    private fun PlanningItemEntity.toUi() = PlanningItemUi(id = id, type = PlanningItemType.valueOf(type), title = title, description = description, dueDate = dueDate, dueTime = dueTime, relatedNodeId = relatedNodeId, relatedNodePath = relatedNodePath, status = PlanningStatus.valueOf(status), createdAt = createdAt, updatedAt = updatedAt, syncStatus = syncStatus, version = version)
    private fun PlanningItemUi.toEntity() = PlanningItemEntity(id = id, type = type.name, title = title, description = description, dueDate = dueDate, dueTime = dueTime, relatedNodeId = relatedNodeId, relatedNodePath = relatedNodePath, status = status.name, updatedAt = System.currentTimeMillis(), version = version, syncStatus = syncStatus)
}
