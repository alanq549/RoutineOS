package com.alan.routineos.ui.features.execute.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.data.local.entities.*
import com.alan.routineos.data.repository.*
import com.alan.routineos.ui.features.execute.state.ExecuteUiState
import com.alan.routineos.ui.features.execute.state.HistorySession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

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
            
            val schemasFlow = schemaRepo.getByTypeId(type.id)
            val valuesFlow = valueRepo.getByNode(node.id)

            combine(schemasFlow, valuesFlow) { schemas, values ->
                val history = values.groupBy { it.updatedAt / 1000 } 
                    .map { (time, vals) -> HistorySession(time * 1000, vals) }
                    .sortedByDescending { it.date }

                val draft = schemas.associate { schema ->
                    val existing = values.find { it.schemaId == schema.id }
                    schema.fieldName to (existing?.value ?: schema.defaultValue ?: "")
                }

                _uiState.update { it.copy(
                    node = node,
                    parentNode = parentNode,
                    nodeType = type,
                    schemas = schemas,
                    fieldValues = draft,
                    history = history,
                    isLoading = false
                ) }
            }.collect()
        }
    }

    fun updateDraftValue(fieldName: String, value: String) {
        _uiState.update { it.copy(
            fieldValues = it.fieldValues + (fieldName to value)
        ) }
    }

    fun saveIteration() {
        viewModelScope.launch {
            val state = _uiState.value
            val node = state.node
            
            if (node == null) {
                Log.d("EXECUTE_DEBUG", "EXECUTE NODE NOT FOUND id=$nodeId")
                return@launch
            }

            Log.d("EXECUTE_DEBUG", "EXECUTE SAVE CALLED")
            val timestamp = System.currentTimeMillis()
            
            _uiState.update { it.copy(shouldStartTimer = null) }

            state.fieldValues.forEach { (fieldName, value) ->
                val schema = state.schemas.find { it.fieldName == fieldName } ?: return@forEach
                
                // REGLA FASE 3: Evitar duplicados por nodeId + schemaId
                val existingValue = valueRepo.getByNodeAndSchema(node.id, schema.id)
                
                if (existingValue != null) {
                    Log.d(
                        "EXECUTE_DEBUG", 
                        "FIELD UPSERT MODE=UPDATE nodeId=${node.id} schemaId=${schema.id} field=$fieldName old=${existingValue.value} new=$value"
                    )
                    valueRepo.update(
                        existingValue.copy(
                            value = value,
                            updatedAt = timestamp,
                            syncStatus = SyncStatus.PENDING_SYNC
                        )
                    )
                } else {
                    Log.d(
                        "EXECUTE_DEBUG", 
                        "FIELD UPSERT MODE=INSERT nodeId=${node.id} schemaId=${schema.id} field=$fieldName value=$value"
                    )
                    valueRepo.upsert(
                        NodeFieldValue(
                            id = UUID.randomUUID().toString(),
                            nodeId = node.id,
                            schemaId = schema.id,
                            fieldName = fieldName,
                            value = value,
                            updatedAt = timestamp,
                            syncStatus = SyncStatus.PENDING_SYNC
                        )
                    )
                }
            }

            val seriesField = state.schemas.find { 
                val name = it.fieldName.lowercase()
                name.contains("ser") || name.contains("set") 
            }
            
            var durationToStart: Int? = null
            if (state.nodeType?.hasMetricFields == true) {
                val durationField = state.schemas.find { it.fieldType == FieldType.DURATION }
                if (durationField != null) {
                    durationToStart = state.fieldValues[durationField.fieldName]?.toIntOrNull()
                }
            }

            if (seriesField != null) {
                val currentSeries = state.fieldValues[seriesField.fieldName]?.toIntOrNull() ?: 0
                if (currentSeries > 1) {
                    updateDraftValue(seriesField.fieldName, (currentSeries - 1).toString())
                    _uiState.update { it.copy(shouldStartTimer = durationToStart) }
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
            nodeRepo.update(node.copy(
                status = NodeStatus.COMPLETED, 
                updatedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING_SYNC,
                version = node.version + 1
            ))
        }
    }
}
