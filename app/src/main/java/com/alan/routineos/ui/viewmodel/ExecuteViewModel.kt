package com.alan.routineos.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.data.local.entities.FieldType
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeFieldValue
import com.alan.routineos.data.local.entities.NodeMetadataSchema
import com.alan.routineos.data.local.entities.NodeStatus
import com.alan.routineos.data.local.entities.NodeType
import com.alan.routineos.data.repository.FieldValueRepository
import com.alan.routineos.data.repository.MetadataSchemaRepository
import com.alan.routineos.data.repository.NodeRepository
import com.alan.routineos.data.repository.NodeTypeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ExecuteUiState(
    val node: Node? = null,
    val parentNode: Node? = null,
    val nodeType: NodeType? = null,
    val schemas: List<NodeMetadataSchema> = emptyList(),
    val draftValues: Map<String, String> = emptyMap(),
    val history: List<HistorySession> = emptyList(),
    val isLoading: Boolean = true,
    val shouldStartTimer: Int? = null // Minutes to start timer
)

data class HistorySession(
    val date: Long,
    val values: List<NodeFieldValue>
)

@HiltViewModel
class ExecuteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val nodeRepo: NodeRepository,
    private val typeRepo: NodeTypeRepository,
    private val schemaRepo: MetadataSchemaRepository,
    private val valueRepo: FieldValueRepository
) : ViewModel() {

    private val nodeId: String = checkNotNull(savedStateHandle["nodeId"])

    private val _uiState = MutableStateFlow(ExecuteUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val node = nodeRepo.getById(nodeId) ?: return@launch
            val parentNode = node.parentId?.let { nodeRepo.getById(it) }
            val type = typeRepo.getById(node.typeId) ?: return@launch

            val schemas = schemaRepo.getByTypeId(type.id).first()
            val existingValues = valueRepo.getByNode(node.id).first()

            // Default values: either from DB or from Schema default
            val initialDraft = schemas.associate { schema ->
                val existing = existingValues.find { it.schemaId == schema.id }
                schema.fieldName to (existing?.value ?: schema.defaultValue ?: "")
            }

            valueRepo.getByNode(node.id).collect { values ->
                val history = values.groupBy { it.updatedAt / 1000 }
                    .map { (time, vals) -> HistorySession(time * 1000, vals) }
                    .sortedByDescending { it.date }

                _uiState.value = _uiState.value.copy(
                    node = node,
                    parentNode = parentNode,
                    nodeType = type,
                    schemas = schemas,
                    draftValues = initialDraft,
                    history = history,
                    isLoading = false
                )
            }
        }
    }

    fun updateDraftValue(fieldName: String, value: String) {
        _uiState.value = _uiState.value.copy(
            draftValues = _uiState.value.draftValues + (fieldName to value)
        )
    }

    fun saveIteration() {
        viewModelScope.launch {
            val state = _uiState.value
            val node = state.node ?: return@launch
            val timestamp = System.currentTimeMillis()

            // Reset timer signal
            _uiState.value = _uiState.value.copy(shouldStartTimer = null)

            // 1. Save values
            state.draftValues.forEach { (fieldName, value) ->
                val schema = state.schemas.find { it.fieldName == fieldName } ?: return@forEach
                valueRepo.upsert(
                    NodeFieldValue(
                        id = UUID.randomUUID().toString(),
                        nodeId = node.id,
                        schemaId = schema.id,
                        fieldName = fieldName,
                        value = value,
                        updatedAt = timestamp
                    )
                )
            }

            // 2. Logic for iterative fields (sets/series)
            val seriesField = state.schemas.find {
                val name = it.fieldName.lowercase()
                name.contains("ser") || name.contains("set")
            }

            // 3. Logic for timer (only if hasMetricFields is true and field is DURATION)
            var durationToStart: Int? = null
            if (state.nodeType?.hasMetricFields == true) {
                val durationField = state.schemas.find { it.fieldType == FieldType.DURATION }
                if (durationField != null) {
                    durationToStart = state.draftValues[durationField.fieldName]?.toIntOrNull()
                }
            }

            if (seriesField != null) {
                val currentSeries = state.draftValues[seriesField.fieldName]?.toIntOrNull() ?: 0
                if (currentSeries > 1) {
                    updateDraftValue(seriesField.fieldName, (currentSeries - 1).toString())
                    _uiState.value = _uiState.value.copy(shouldStartTimer = durationToStart)
                } else {
                    completeNode()
                }
            } else {
                completeNode()
            }
        }
    }

    fun completeNode() {
        viewModelScope.launch {
            val node = _uiState.value.node ?: return@launch
            nodeRepo.update(
                node.copy(
                    status = NodeStatus.COMPLETED,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
}
