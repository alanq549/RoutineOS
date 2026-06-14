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

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

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
                    isLoading = false,
                    hasUnsavedChanges = false // Reset on load
                ) }
            }.collect()
        }
    }

    fun updateDraftValue(fieldName: String, value: String) {
        _uiState.update { it.copy(
            fieldValues = it.fieldValues + (fieldName to value),
            hasUnsavedChanges = true
        ) }
    }

    fun setShowExitConfirmation(show: Boolean) {
        _uiState.update { it.copy(showExitConfirmation = show) }
    }

    fun handleBackPress(onNavigateBack: () -> Unit) {
        if (uiState.value.hasUnsavedChanges) {
            setShowExitConfirmation(true)
        } else {
            onNavigateBack()
        }
    }

    /**
     * Persists all current draft values to the database.
     * Uses nodeId + schemaId to ensure upsert behavior.
     */
    fun saveChanges(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            val state = uiState.value
            val node = state.node ?: return@launch
            val timestamp = System.currentTimeMillis()

            state.fieldValues.forEach { (fieldName, value) ->
                val schema = state.schemas.find { it.fieldName == fieldName } ?: return@forEach
                val existingValue = valueRepo.getByNodeAndSchema(node.id, schema.id)

                if (existingValue != null) {
                    valueRepo.update(
                        existingValue.copy(
                            value = value,
                            updatedAt = timestamp,
                            syncStatus = SyncStatus.PENDING_SYNC
                        )
                    )
                } else {
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
            
            _uiState.update { it.copy(hasUnsavedChanges = false, showExitConfirmation = false) }
            _events.emit("Cambios guardados")
            onComplete?.invoke()
        }
    }

    /**
     * Saves all changes and marks the node as COMPLETED.
     */
    fun saveAndComplete() {
        viewModelScope.launch {
            saveChanges()
            val node = uiState.value.node ?: return@launch
            nodeRepo.update(node.copy(
                status = NodeStatus.COMPLETED,
                updatedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING_SYNC,
                version = node.version + 1
            ))
            _events.emit("Actividad completada")
        }
    }
}
