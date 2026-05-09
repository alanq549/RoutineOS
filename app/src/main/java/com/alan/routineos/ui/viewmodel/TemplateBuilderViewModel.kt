package com.alan.routineos.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeType
import com.alan.routineos.data.local.entities.RoutineTemplate
import com.alan.routineos.data.repository.NodeRepository
import com.alan.routineos.data.repository.NodeTypeRepository
import com.alan.routineos.data.repository.TemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class TemplateBuilderUiState(
    val templateId: String? = null,
    val name: String = "",
    val colorHex: String = "#3FB950",
    val nodes: List<Node> = emptyList(),
    val nodeTypes: List<NodeType> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false
)

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
                    _uiState.update { it.copy(
                        name = template.name,
                        colorHex = template.colorHex,
                        nodes = nodes,
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
            templateId = _uiState.value.templateId ?: "TEMP_ID", // Will update on save
            position = _uiState.value.nodes.filter { it.parentId == parentId }.size
        )
        _uiState.update { it.copy(nodes = it.nodes + newNode) }
    }

    fun updateNodeName(nodeId: String, newName: String) {
        _uiState.update { state ->
            state.copy(nodes = state.nodes.map { 
                if (it.id == nodeId) it.copy(name = newName) else it 
            })
        }
    }

    fun deleteNode(nodeId: String) {
        // Recursive delete logic
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
            state.copy(nodes = state.nodes.filterNot { toDelete.contains(it.id) })
        }
    }

    fun saveTemplate() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val state = _uiState.value
            
            val finalTemplateId = state.templateId ?: UUID.randomUUID().toString()
            
            // 1. Find or create root node
            // If it's a new template, we might need to designate one node as root or create a dummy root
            // Usually, a template has one root node.
            var rootNode = state.nodes.find { it.parentId == null }
            if (rootNode == null && state.nodes.isNotEmpty()) {
                rootNode = state.nodes.first() // Fallback
            }
            
            if (rootNode == null) {
                // Cannot save template without nodes
                _uiState.update { it.copy(isSaving = false) }
                return@launch
            }

            val template = RoutineTemplate(
                id = finalTemplateId,
                rootNodeId = rootNode.id,
                name = state.name,
                colorHex = state.colorHex,
                updatedAt = System.currentTimeMillis()
            )
            
            templateRepo.upsert(template)
            
            // 2. Save all nodes with the correct templateId
            val finalNodes = state.nodes.map { it.copy(templateId = finalTemplateId) }
            nodeRepo.insertAll(finalNodes)
            
            _uiState.update { it.copy(isSaving = false, templateId = finalTemplateId) }
        }
    }
}
