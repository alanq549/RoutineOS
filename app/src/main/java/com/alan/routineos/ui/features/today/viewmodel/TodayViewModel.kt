package com.alan.routineos.ui.features.today.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.core.util.DateUtils
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeStatus
import com.alan.routineos.data.local.entities.SyncStatus
import com.alan.routineos.data.repository.*
import com.alan.routineos.ui.features.today.state.TodayUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val instanceRepo: InstanceRepository,
    private val scheduleRepo: ScheduleRepository,
    private val exceptionRepo: ScheduleExceptionRepository,
    private val nodeRepo: NodeRepository,
    private val nodeTypeRepo: NodeTypeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeTodayData()
        loadNodeTypes()
        updateTimeTicker()
    }

    private fun loadNodeTypes() {
        viewModelScope.launch {
            nodeTypeRepo.getAll().collect { types ->
                _uiState.update { it.copy(nodeTypes = types) }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeTodayData() {
        val today = DateUtils.getStartOfDay()
        val weekday = DateUtils.getDayOfWeek()

        viewModelScope.launch {
            instanceRepo.getByDate(today)
                .onEach { instance ->
                    if (instance == null) {
                        generateInstanceIfNeeded(today, weekday)
                    }
                }
                .filterNotNull()
                .flatMapLatest { instance ->
                    nodeRepo.getByInstance(instance.id).map { nodes ->
                        instance to nodes
                    }
                }
                .collect { (instance, nodes) ->
                    _uiState.update { it.copy(
                        instance = instance,
                        nodes = nodes,
                        isLoading = false
                    ) }
                }
        }
    }

    private suspend fun generateInstanceIfNeeded(today: Long, weekday: Int) {
        val exceptions = exceptionRepo.getActiveForDate(today).first()
        if (exceptions.any { it.affectsGeneration }) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }

        val activeSchedules = scheduleRepo.getActiveForWeekday(weekday, today).first()
        activeSchedules.forEach { schedule ->
            instanceRepo.generateInstance(schedule.templateId, today)
        }
        
        if (activeSchedules.isEmpty()) {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun updateTimeTicker() {
        viewModelScope.launch {
            while (true) {
                val cal = Calendar.getInstance()
                val hour = cal.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
                val min = cal.get(Calendar.MINUTE).toString().padStart(2, '0')
                _uiState.update { it.copy(currentTime = "$hour:$min") }
                delay(60_000)
            }
        }
    }

    fun toggleNodeCompletion(node: Node) {
        viewModelScope.launch {
            val newStatus = if (node.status == NodeStatus.COMPLETED) NodeStatus.PENDING else NodeStatus.COMPLETED
            nodeRepo.update(node.copy(
                status = newStatus, 
                updatedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING_SYNC,
                version = node.version + 1
            ))
        }
    }

    fun updateNodeStatus(node: Node, status: NodeStatus) {
        viewModelScope.launch {
            nodeRepo.update(node.copy(
                status = status, 
                updatedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING_SYNC,
                version = node.version + 1
            ))
        }
    }

    fun addAdHocNode(name: String, typeId: String, parentId: String?) {
        val instanceId = _uiState.value.instance?.id ?: return
        viewModelScope.launch {
            val newNode = Node(
                id = UUID.randomUUID().toString(),
                name = name,
                typeId = typeId,
                parentId = parentId,
                instanceId = instanceId,
                status = NodeStatus.PENDING,
                syncStatus = SyncStatus.PENDING_SYNC
            )
            nodeRepo.upsert(newNode)
        }
    }
}
