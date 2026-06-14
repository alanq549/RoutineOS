package com.alan.routineos.ui.features.onboarding.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.core.datastore.SettingsDataStore
import com.alan.routineos.data.local.entities.*
import com.alan.routineos.data.repository.*
import com.alan.routineos.ui.features.onboarding.state.OnboardingUiState
import com.alan.routineos.ui.features.onboarding.state.NodeTypeDraft
import com.alan.routineos.ui.features.onboarding.state.NodeMetadataSchemaDraft
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val nodeTypeRepo: NodeTypeRepository,
    private val schemaRepo: MetadataSchemaRepository,
    private val templateRepo: TemplateRepository,
    private val nodeRepo: NodeRepository,
    private val scheduleRepo: ScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    fun updateRoutineName(name: String) {
        _uiState.update { it.copy(routineName = name) }
    }

    fun setCustomizationChoice(customize: Boolean) {
        _uiState.update { it.copy(isCustomizingTypes = customize, hasMadeTypeChoice = true) }
        nextStep()
    }

    fun addNodeType(name: String, hasMetrics: Boolean) {
        val current = _uiState.value.nodeTypes.toMutableList()
        current.add(NodeTypeDraft(name = name, hasMetrics = hasMetrics))
        _uiState.update { it.copy(nodeTypes = current) }
    }

    fun removeNodeType(id: String) {
        val current = _uiState.value.nodeTypes.filter { it.id != id }
        _uiState.update { it.copy(nodeTypes = current) }
    }

    fun updateNodeTypeSchemas(id: String, schemas: List<NodeMetadataSchemaDraft>) {
        val current = _uiState.value.nodeTypes.map {
            if (it.id == id) it.copy(schemas = schemas) else it
        }
        _uiState.update { it.copy(nodeTypes = current) }
    }

    fun toggleDay(day: Int) {
        val current = _uiState.value.selectedDays.toMutableList()
        if (current.contains(day)) current.remove(day) else current.add(day)
        _uiState.update { it.copy(selectedDays = current) }
    }

    fun updateStartTime(time: String) {
        _uiState.update { it.copy(startTime = time) }
    }

    fun updateEndTime(time: String) {
        _uiState.update { it.copy(endTime = time) }
    }

    fun nextStep() {
        _uiState.update { it.copy(currentStep = it.currentStep + 1) }
    }

    fun finishOnboarding() {
        viewModelScope.launch {
            val state = _uiState.value
            val templateId = UUID.randomUUID().toString()
            val rootNodeId = UUID.randomUUID().toString()
            
            // Ensure generic root node type exists
            nodeTypeRepo.upsert(NodeType(
                id = "activity_root",
                name = "Actividad",
                allowsChildren = true,
                hasMetricFields = false,
                syncStatus = SyncStatus.PENDING_SYNC
            ))

            // 1. Create NodeTypes and Schemas defined by user (if any)
            if (state.isCustomizingTypes) {
                state.nodeTypes.forEach { draft ->
                    val nodeType = NodeType(
                        id = draft.id,
                        name = draft.name,
                        hasMetricFields = draft.hasMetrics,
                        allowsChildren = true,
                        syncStatus = SyncStatus.PENDING_SYNC
                    )
                    nodeTypeRepo.upsert(nodeType)
                    
                    draft.schemas.forEachIndexed { index, s ->
                        schemaRepo.upsert(NodeMetadataSchema(
                            typeId = nodeType.id,
                            fieldName = s.fieldName,
                            fieldLabel = s.fieldLabel,
                            fieldType = s.fieldType,
                            unit = s.unit,
                            defaultValue = s.defaultValue,
                            position = index,
                            syncStatus = SyncStatus.PENDING_SYNC
                        ))
                    }
                }
            }

            // 2. Create Root Node
            val rootNode = Node(
                id = rootNodeId,
                name = state.routineName,
                typeId = "activity_root",
                parentId = null,
                templateId = templateId,
                scheduledTime = state.startTime, 
                isSequential = false,
                syncStatus = SyncStatus.PENDING_SYNC
            )
            nodeRepo.upsert(rootNode)

            // 3. Create Children Nodes (only if customized)
            if (state.isCustomizingTypes) {
                state.nodeTypes.forEachIndexed { index, draft ->
                    val childNodeId = UUID.randomUUID().toString()
                    val childNode = Node(
                        id = childNodeId,
                        name = draft.name,
                        typeId = draft.id,
                        parentId = rootNodeId,
                        templateId = templateId,
                        position = index,
                        syncStatus = SyncStatus.PENDING_SYNC
                    )
                    nodeRepo.upsert(childNode)
                }
            } else {
                // RÁPIDO: Crear un nodo hijo por defecto para que Today no esté vacío
                val childNodeId = UUID.randomUUID().toString()
                val childNode = Node(
                    id = childNodeId,
                    name = "Mi primera actividad",
                    typeId = "activity_root",
                    parentId = rootNodeId,
                    templateId = templateId,
                    position = 0,
                    syncStatus = SyncStatus.PENDING_SYNC
                )
                nodeRepo.upsert(childNode)
            }

            // 4. Create Routine Template
            val template = RoutineTemplate(
                id = templateId,
                rootNodeId = rootNodeId,
                name = state.routineName,
                syncStatus = SyncStatus.PENDING_SYNC
            )
            templateRepo.upsert(template)

            // 5. Create Schedules
            val daysToSchedule = if (state.selectedDays.isEmpty()) listOf(1, 2, 3, 4, 5, 6, 7) else state.selectedDays
            daysToSchedule.forEach { day ->
                scheduleRepo.upsert(Schedule(
                    templateId = templateId,
                    weekday = day,
                    startTime = state.startTime,
                    endTime = state.endTime,
                    syncStatus = SyncStatus.PENDING_SYNC
                ))
            }

            // 6. Mark onboarding as completed
            settingsDataStore.setOnboardingCompleted(true)
        }
    }
}
