package com.alan.routineos.ui.features.node_type_manager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.data.local.entities.ExecutionTrackingMode
import com.alan.routineos.data.local.entities.FieldType
import com.alan.routineos.data.local.entities.NodeMetadataSchema
import com.alan.routineos.data.local.entities.NodeType
import com.alan.routineos.data.local.entities.SyncStatus
import com.alan.routineos.data.repository.MetadataSchemaRepository
import com.alan.routineos.data.repository.NodeTypeRepository
import com.alan.routineos.ui.features.node_type_manager.state.NodeTypeManagerUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

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
            nodeTypeRepo.getAllActive().collect { types ->
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
                id = UUID.randomUUID().toString(),
                name = name,
                hasMetricFields = hasMetrics,
                position = _uiState.value.nodeTypes.size,
                syncStatus = SyncStatus.PENDING_SYNC
            )
            nodeTypeRepo.upsert(newType)
        }
    }

    fun deleteNodeType(type: NodeType) {
        viewModelScope.launch {
            // Soft delete: Archive the type instead of physical deletion
            nodeTypeRepo.archive(type)
            if (_uiState.value.selectedType?.id == type.id) {
                selectType(null)
            }
        }
    }

    fun addSchemaFull(
        typeId: String,
        name: String,
        label: String,
        fieldType: FieldType,
        defaultValue: String?,
        unit: String?,
        editableInTemplate: Boolean,
        editableInExecution: Boolean,
        trackingMode: ExecutionTrackingMode
    ) {
        viewModelScope.launch {
            val newSchema = NodeMetadataSchema(
                id = UUID.randomUUID().toString(),
                typeId = typeId,
                fieldName = name,
                fieldLabel = label,
                fieldType = fieldType,
                defaultValue = defaultValue,
                unit = unit,
                position = _uiState.value.schemasForSelectedType.size,
                editableInTemplate = editableInTemplate,
                editableInExecution = editableInExecution,
                executionTrackingMode = trackingMode,
                syncStatus = SyncStatus.PENDING_SYNC
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
