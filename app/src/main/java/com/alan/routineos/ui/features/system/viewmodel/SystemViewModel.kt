package com.alan.routineos.ui.features.system.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.core.util.DateUtils
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.ScheduleException
import com.alan.routineos.data.repository.NodeRepository
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
    private val nodeRepo: NodeRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(DateUtils.getStartOfDay())
    private val _currentWeekStart = MutableStateFlow(DateUtils.getStartOfWeek())
    private val _planningSubTab = MutableStateFlow(PlanningSection.ROUTINE_CHANGES)
    
    // In-memory storage for Planning Items as requested for FIX 9 (not persisted in Room yet)
    private val _planningItems = MutableStateFlow<List<PlanningItemUi>>(emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SystemUiState> = _selectedDate.flatMapLatest { selectedDate ->
        combine(
            _currentWeekStart,
            _planningSubTab,
            _planningItems,
            nodeRepo.getAllTemplateNodes(),
            exceptionRepo.getActiveForDate(selectedDate)
        ) { weekStart, subTab, items, allNodes, adaptations ->
            
            val targets = buildPlanningTargets(allNodes)
            Log.d("PLANNING_DEBUG", "PLANNING TARGETS LOADED count=${targets.size}")

            // Filter: show items due on selectedDate OR items with no due date
            val filteredItems = items.filter { item ->
                item.dueDate == null || DateUtils.getStartOfDay(item.dueDate) == selectedDate
            }.sortedWith(
                compareByDescending<PlanningItemUi> { it.dueDate != null }
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
        Log.d("PLANNING_DEBUG", "PLANNING SECTION SELECTED section=$section")
    }

    fun selectDate(date: Long) {
        val startOfDay = DateUtils.getStartOfDay(date)
        _selectedDate.value = startOfDay
        Log.d("PLANNING_DEBUG", "CALENDAR DAY SELECTED date=$startOfDay")
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

    fun createAdaptation(label: String, type: String, rangeType: Int) {
        viewModelScope.launch {
            val from: Long
            val to: Long
            
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
                dateTo = to,
                affectsGeneration = true
            )
            exceptionRepo.upsert(exception)
            Log.d("PLANNING_DEBUG", "ROUTINE_CHANGE CREATE label=$label from=$from to=$to")
        }
    }

    fun deleteAdaptation(adaptation: ScheduleException) {
        viewModelScope.launch {
            exceptionRepo.delete(adaptation)
            Log.d("PLANNING_DEBUG", "ROUTINE_CHANGE DELETE id=${adaptation.id}")
        }
    }

    // Planning Items (Tasks, Notes, Reminders) - In memory for now
    fun createPlanningItem(
        title: String,
        description: String?,
        type: PlanningItemType,
        nodeId: String? = null,
        nodePath: String? = null,
        dueDate: Long? = null,
        dueTime: String? = null
    ) {
        viewModelScope.launch {
            val newItem = PlanningItemUi(
                id = UUID.randomUUID().toString(),
                type = type,
                title = title,
                description = description,
                dueDate = dueDate,
                dueTime = dueTime,
                relatedNodeId = nodeId,
                relatedNodePath = nodePath,
                status = PlanningStatus.PENDING
            )
            _planningItems.value = _planningItems.value + newItem
            Log.d("PLANNING_DEBUG", "PLANNING ITEM CREATED relatedNodeId=$nodeId relatedPath=$nodePath")
        }
    }

    fun togglePlanningItem(id: String) {
        _planningItems.value = _planningItems.value.map {
            if (it.id == id) {
                val newStatus = if (it.status == PlanningStatus.PENDING) PlanningStatus.DONE else PlanningStatus.PENDING
                it.copy(status = newStatus)
            } else it
        }
    }

    fun deletePlanningItem(id: String) {
        _planningItems.value = _planningItems.value.filter { it.id != id }
        Log.d("PLANNING_DEBUG", "PLANNING ITEM DELETE id=$id")
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
            PlanningTargetUi(
                nodeId = node.id,
                path = pathParts.joinToString(" > "),
                level = level
            )
        }.sortedBy { it.path }
    }
}
