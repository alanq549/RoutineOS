package com.alan.routineos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.data.local.entities.FieldType
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeFieldValue
import com.alan.routineos.data.local.entities.NodeMetadataSchema
import com.alan.routineos.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class StatsUiState(
    val availableNodes: List<Node> = emptyList(),
    val selectedNode: Node? = null,
    val availableFields: List<NodeMetadataSchema> = emptyList(),
    val selectedField: NodeMetadataSchema? = null,
    val dataPoints: List<NodeFieldValue> = emptyList(),
    val completionRate: Float = 0f,
    val currentStreak: Int = 0,
    val isLoading: Boolean = true,
    val nodeSearchQuery: String = ""
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val nodeRepo: NodeRepository,
    private val schemaRepo: MetadataSchemaRepository,
    private val fieldValueRepo: FieldValueRepository,
    private val instanceRepo: InstanceRepository
) : ViewModel() {

    private val _selectedNode = MutableStateFlow<Node?>(null)
    private val _selectedField = MutableStateFlow<NodeMetadataSchema?>(null)
    private val _nodeSearchQuery = MutableStateFlow("")

    val uiState: StateFlow<StatsUiState> = combine(
        _selectedNode,
        _selectedField,
        _nodeSearchQuery,
        // Trigger metrics update every time date or nodes might change (simplified)
        flow { 
            val rate = instanceRepo.getCompletionRate(System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000))
            val streak = instanceRepo.calculateCurrentStreak()
            emit(rate to streak)
        }
    ) { node, field, query, metrics ->
        val templateNodes = nodeRepo.getAllTemplateNodes().first()
        
        val filteredNodes = if (query.isBlank()) templateNodes 
                           else templateNodes.filter { it.name.contains(query, ignoreCase = true) }

        var fields = emptyList<NodeMetadataSchema>()
        var history = emptyList<NodeFieldValue>()
        
        if (node != null) {
            fields = schemaRepo.getByTypeId(node.typeId).first().filter { 
                it.fieldType == FieldType.NUMBER || it.fieldType == FieldType.DURATION
            }
            if (field != null) {
                history = fieldValueRepo.getHistoryByTemplateNode(node.id, field.fieldName).first()
            }
        }

        StatsUiState(
            availableNodes = filteredNodes,
            selectedNode = node,
            availableFields = fields,
            selectedField = field,
            dataPoints = history,
            completionRate = metrics.first,
            currentStreak = metrics.second,
            isLoading = false,
            nodeSearchQuery = query
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUiState())

    fun selectNode(node: Node) {
        _selectedNode.value = node
        _selectedField.value = null
    }

    fun selectField(schema: NodeMetadataSchema) {
        _selectedField.value = schema
    }

    fun updateSearchQuery(query: String) {
        _nodeSearchQuery.value = query
    }
}
