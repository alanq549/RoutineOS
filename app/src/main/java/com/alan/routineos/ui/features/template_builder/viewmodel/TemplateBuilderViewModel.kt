package com.alan.routineos.ui.features.template_builder.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeSchedule
import com.alan.routineos.data.local.entities.RoutineTemplate
import com.alan.routineos.data.local.entities.SyncStatus
import com.alan.routineos.data.repository.NodeRepository
import com.alan.routineos.data.repository.NodeTypeRepository
import com.alan.routineos.data.repository.TemplateRepository
import com.alan.routineos.ui.features.template_builder.state.TemplateBuilderUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TemplateBuilderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val templateRepo: TemplateRepository,
    private val nodeRepo: NodeRepository,
    private val typeRepo: NodeTypeRepository
) : ViewModel() {

    private val templateId: String = checkNotNull(savedStateHandle["templateId"])
    
    private val _uiState = MutableStateFlow(TemplateBuilderUiState(templateId = if(templateId == "new") null else templateId))
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val types = typeRepo.getAll().first()
            _uiState.update { it.copy(nodeTypes = types) }

            if (templateId != "new") {
                val template = templateRepo.getById(templateId)
                if (template != null) {
                    val nodes = nodeRepo.getAllByTemplate(template.id)
                    
                    // Load schedules for each node
                    val schedulesMap = mutableMapOf<String, List<NodeSchedule>>()
                    nodes.forEach { node ->
                        val schedules = nodeRepo.getSchedulesForNode(node.id).first()
                        schedulesMap[node.id] = schedules
                    }

                    _uiState.update { it.copy(
                        name = template.name,
                        colorHex = template.colorHex,
                        nodes = nodes,
                        nodeSchedules = schedulesMap,
                        isLoading = false
                    ) }
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateColor(colorHex: String) {
        _uiState.update { it.copy(colorHex = colorHex) }
    }

    fun addNode(name: String, typeId: String, parentId: String?) {
        val newNode = Node(
            id = UUID.randomUUID().toString(),
            name = name,
            typeId = typeId,
            parentId = parentId,
            templateId = _uiState.value.templateId ?: "TEMP_ID",
            position = _uiState.value.nodes.filter { it.parentId == parentId }.size,
            syncStatus = SyncStatus.PENDING_SYNC,
            isSequential = true
        )
        _uiState.update { it.copy(nodes = it.nodes + newNode) }
    }

    fun toggleNodeSequential(nodeId: String, isSequential: Boolean) {
        _uiState.update { state ->
            state.copy(nodes = state.nodes.map {
                if (it.id == nodeId) it.copy(isSequential = isSequential) else it
            })
        }
        if (isSequential) {
            // If switched back to sequential, we might want to clear schedules or just ignore them
            // For now, let's keep them in state but they won't be used by the engine if node schedules are empty
        }
    }

    fun updateNodeSchedules(nodeId: String, schedules: List<NodeSchedule>) {
        _uiState.update { state ->
            val newMap = state.nodeSchedules.toMutableMap()
            newMap[nodeId] = schedules
            state.copy(nodeSchedules = newMap)
        }
    }

    fun updateNodeName(nodeId: String, newName: String) {
        _uiState.update { state ->
            state.copy(nodes = state.nodes.map { 
                if (it.id == nodeId) it.copy(
                    name = newName, 
                    syncStatus = SyncStatus.PENDING_SYNC,
                    version = it.version + 1,
                    updatedAt = System.currentTimeMillis()
                ) else it 
            })
        }
    }

    fun deleteNode(nodeId: String) {
        val toDelete = mutableSetOf(nodeId)
        var added = true
        while(added) {
            added = false
            _uiState.value.nodes.forEach { 
                if (it.parentId != null && toDelete.contains(it.parentId) && !toDelete.contains(it.id)) {
                    toDelete.add(it.id)
                    added = true
                }
            }
        }
        _uiState.update { state ->
            state.copy(
                nodes = state.nodes.filterNot { toDelete.contains(it.id) },
                nodeSchedules = state.nodeSchedules.filterKeys { !toDelete.contains(it) }
            )
        }
    }

    fun saveTemplate() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val state = _uiState.value
            
            val finalTemplateId = state.templateId ?: UUID.randomUUID().toString()
            
            var rootNode = state.nodes.find { it.parentId == null }
            if (rootNode == null && state.nodes.isNotEmpty()) {
                rootNode = state.nodes.first()
            }
            
            if (rootNode == null) {
                _uiState.update { it.copy(isSaving = false) }
                return@launch
            }

            val template = RoutineTemplate(
                id = finalTemplateId,
                rootNodeId = rootNode.id,
                name = state.name,
                colorHex = state.colorHex,
                updatedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING_SYNC
            )
            
            templateRepo.upsert(template)
            
            val finalNodes = state.nodes.map { it.copy(
                templateId = finalTemplateId,
                syncStatus = SyncStatus.PENDING_SYNC
            ) }
            nodeRepo.insertAll(finalNodes)

            // Save schedules for each node
            state.nodes.forEach { node ->
                val schedules = state.nodeSchedules[node.id] ?: emptyList()
                nodeRepo.saveSchedules(node.id, schedules)
            }
            
            _uiState.update { it.copy(isSaving = false, templateId = finalTemplateId) }
        }
    }
}
