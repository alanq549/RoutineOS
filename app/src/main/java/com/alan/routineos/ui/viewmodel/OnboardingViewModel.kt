package com.alan.routineos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.core.datastore.SettingsDataStore
import com.alan.routineos.data.local.entities.*
import com.alan.routineos.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class NodeTypeDraft(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: String? = null,
    val hasMetrics: Boolean = false,
    val schemas: List<NodeMetadataSchemaDraft> = emptyList()
)

data class NodeMetadataSchemaDraft(
    val fieldName: String,
    val fieldLabel: String,
    val fieldType: FieldType,
    val unit: String? = null,
    val defaultValue: String? = null
)

data class OnboardingUiState(
    val currentStep: Int = 1,
    val routineName: String = "",
    val nodeTypes: List<NodeTypeDraft> = emptyList(),
    val selectedDays: List<Int> = emptyList(), // 1=Mon, 7=Sun
    val startTime: String = "08:00"
)

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
        _uiState.value = _uiState.value.copy(routineName = name)
    }

    fun addNodeType(name: String, hasMetrics: Boolean) {
        val current = _uiState.value.nodeTypes.toMutableList()
        current.add(NodeTypeDraft(name = name, hasMetrics = hasMetrics))
        _uiState.value = _uiState.value.copy(nodeTypes = current)
    }

    fun removeNodeType(id: String) {
        val current = _uiState.value.nodeTypes.filter { it.id != id }
        _uiState.value = _uiState.value.copy(nodeTypes = current)
    }

    fun updateNodeTypeSchemas(id: String, schemas: List<NodeMetadataSchemaDraft>) {
        val current = _uiState.value.nodeTypes.map {
            if (it.id == id) it.copy(schemas = schemas) else it
        }
        _uiState.value = _uiState.value.copy(nodeTypes = current)
    }

    fun toggleDay(day: Int) {
        val current = _uiState.value.selectedDays.toMutableList()
        if (current.contains(day)) current.remove(day) else current.add(day)
        _uiState.value = _uiState.value.copy(selectedDays = current)
    }

    fun updateStartTime(time: String) {
        _uiState.value = _uiState.value.copy(startTime = time)
    }

    fun nextStep() {
        _uiState.value = _uiState.value.copy(currentStep = _uiState.value.currentStep + 1)
    }

    fun finishOnboarding() {
        viewModelScope.launch {
            val state = _uiState.value
            
            // 1. Create NodeTypes and Schemas
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

            val firstTypeId = state.nodeTypes.firstOrNull()?.id ?: ""
            
            val rootNode = Node(
                typeId = firstTypeId, 
                name = state.routineName,
                parentId = null,
                syncStatus = SyncStatus.PENDING_SYNC
            )
            nodeRepo.upsert(rootNode)

            val template = RoutineTemplate(
                rootNodeId = rootNode.id,
                name = state.routineName,
                syncStatus = SyncStatus.PENDING_SYNC
            )
            templateRepo.upsert(template)

            // 3. Create Schedules
            state.selectedDays.forEach { day ->
                scheduleRepo.upsert(Schedule(
                    templateId = template.id,
                    weekday = day,
                    startTime = state.startTime,
                    syncStatus = SyncStatus.PENDING_SYNC
                ))
            }

            // 4. Mark onboarding as completed
            settingsDataStore.setOnboardingCompleted(true)
        }
    }
}
