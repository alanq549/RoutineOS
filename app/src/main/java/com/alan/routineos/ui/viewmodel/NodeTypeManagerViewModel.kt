package com.alan.routineos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.data.local.entities.FieldType
import com.alan.routineos.data.local.entities.NodeMetadataSchema
import com.alan.routineos.data.local.entities.NodeType
import com.alan.routineos.data.repository.MetadataSchemaRepository
import com.alan.routineos.data.repository.NodeTypeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class NodeTypeManagerUiState(
    val nodeTypes: List<NodeType> = emptyList(),
    val selectedType: NodeType? = null,
    val schemasForSelectedType: List<NodeMetadataSchema> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class NodeTypeManagerViewModel @Inject constructor(
    private val nodeTypeRepo: NodeTypeRepository,
    private val schemaRepo: MetadataSchemaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NodeTypeManagerUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadNodeTypes()
    }

    private fun loadNodeTypes() {
        viewModelScope.launch {
            nodeTypeRepo.getAll().collect { types ->
                _uiState.update { it.copy(nodeTypes = types, isLoading = false) }
            }
        }
    }

    fun selectType(type: NodeType?) {
        viewModelScope.launch {
            if (type == null) {
                _uiState.update { it.copy(selectedType = null, schemasForSelectedType = emptyList()) }
            } else {
                schemaRepo.getByTypeId(type.id).collect { schemas ->
                    _uiState.update { it.copy(selectedType = type, schemasForSelectedType = schemas) }
                }
            }
        }
    }

    fun createNodeType(name: String, hasMetrics: Boolean) {
        viewModelScope.launch {
            val newType = NodeType(
                name = name,
                hasMetricFields = hasMetrics,
                position = _uiState.value.nodeTypes.size
            )
            nodeTypeRepo.upsert(newType)
        }
    }

    fun deleteNodeType(type: NodeType) {
        viewModelScope.launch {
            nodeTypeRepo.delete(type)
        }
    }

    fun addSchemaFull(typeId: String, name: String, label: String, fieldType: FieldType, defaultValue: String?, unit: String?) {
        viewModelScope.launch {
            val newSchema = NodeMetadataSchema(
                typeId = typeId,
                fieldName = name,
                fieldLabel = label,
                fieldType = fieldType,
                defaultValue = defaultValue,
                unit = unit,
                position = _uiState.value.schemasForSelectedType.size
            )
            schemaRepo.upsert(newSchema)
        }
    }

    fun deleteSchema(schema: NodeMetadataSchema) {
        viewModelScope.launch {
            schemaRepo.delete(schema)
        }
    }
}
