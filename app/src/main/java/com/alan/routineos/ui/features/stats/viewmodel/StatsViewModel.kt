package com.alan.routineos.ui.features.stats.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.data.local.entities.FieldType
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeFieldValue
import com.alan.routineos.data.local.entities.NodeMetadataSchema
import com.alan.routineos.data.repository.FieldValueRepository
import com.alan.routineos.data.repository.InstanceRepository
import com.alan.routineos.data.repository.MetadataSchemaRepository
import com.alan.routineos.data.repository.NodeRepository
import com.alan.routineos.ui.features.stats.state.StatsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

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
            // Log de nodo seleccionado (el template que define la métrica)
            Log.d("STATS_DEBUG", "STATS NODE id=${node.id} name=${node.name} instanceId=${node.instanceId}")
            
            if (node.instanceId == null) {
                // Confirmamos que el nodo base es un template
                Log.d("STATS_DEBUG", "STATS INFO: Selected node is a template definition.")
            }

            fields = schemaRepo.getByTypeId(node.typeId).first().filter {
                it.fieldType == FieldType.NUMBER || it.fieldType == FieldType.DURATION
            }
            
            if (field != null) {
                // La query getHistoryByTemplateNode ya filtra instanceId IS NOT NULL internamente
                history = fieldValueRepo.getHistoryByTemplateNode(node.id, field.fieldName).first()
                
                history.forEach { valEntry ->
                    Log.d("STATS_DEBUG", "STATS FIELD node=${node.name} field=${field.fieldName} value=${valEntry.value}")
                }

                // Log diagnóstico adicional para confirmar que ignoramos el valor del template si existiera
                val templateValue = fieldValueRepo.getByNodeAndField(node.id, field.fieldName)
                if (templateValue != null) {
                    Log.d("STATS_DEBUG", "STATS IGNORE TEMPLATE NODE id=${node.id} name=${node.name}")
                }
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
