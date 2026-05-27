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
import com.alan.routineos.ui.features.template_builder.sections.ContextCategory
import com.alan.routineos.ui.features.template_builder.sections.TimeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        _uiState.value = _uiState.value.copy(routineName = name)
    }

    fun updateCategory(category: ContextCategory) {
        _uiState.value = _uiState.value.copy(category = category)
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

    fun updateEndTime(time: String) {
        _uiState.value = _uiState.value.copy(endTime = time)
    }

    fun nextStep() {
        _uiState.value = _uiState.value.copy(currentStep = _uiState.value.currentStep + 1)
    }

    private fun nodeDayFromName(name: String): Int? {
        return when (name.trim().lowercase()) {
            "lunes" -> 1
            "martes" -> 2
            "miércoles", "miercoles" -> 3
            "jueves" -> 4
            "viernes" -> 5
            "sábado", "sabado" -> 6
            "domingo" -> 7
            else -> null
        }
    }

    fun finishOnboarding() {
        viewModelScope.launch {
            val state = _uiState.value
            val templateId = UUID.randomUUID().toString()
            val rootNodeId = UUID.randomUUID().toString()
            
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

            // 2. Create Root Activity Node (Flexible and no fixed time to allow block-based scheduling)
            val rootNode = Node(
                id = rootNodeId,
                name = state.routineName,
                typeId = "activity_root",
                parentId = null,
                templateId = templateId,
                scheduledTime = null, 
                isSequential = true, 
                syncStatus = SyncStatus.PENDING_SYNC
            )
            nodeRepo.upsert(rootNode)
            Log.d("TODAY_DEBUG", "CREATE NODE name=${rootNode.name} id=${rootNode.id} parentId=null")

            // 3. Create Children Nodes for the blocks defined
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
                Log.d("TODAY_DEBUG", "CREATE NODE name=${childNode.name} id=${childNode.id} parentId=${childNode.parentId}")

                // 4. Assign specific schedule if name is a weekday
                val dayOfWeek = nodeDayFromName(draft.name)
                if (dayOfWeek != null) {
                    val nodeSchedules = listOf(
                        NodeSchedule(
                            nodeId = childNodeId,
                            dayOfWeek = dayOfWeek,
                            startTime = state.startTime,
                            endTime = state.endTime
                        )
                    )
                    nodeRepo.saveSchedules(childNodeId, nodeSchedules)
                    Log.d("TODAY_DEBUG", "CREATE SCHEDULE node=${childNode.name} day=$dayOfWeek start=${state.startTime} end=${state.endTime}")
                }
            }

            // 5. Create Routine Template
            val template = RoutineTemplate(
                id = templateId,
                rootNodeId = rootNodeId,
                name = state.routineName,
                category = state.category,
                timeMode = TimeMode.FLEXIBLE,
                syncStatus = SyncStatus.PENDING_SYNC
            )
            templateRepo.upsert(template)

            // 6. Create Global Schedules (Optional, but helps Instance generation if no day-blocks exist)
            state.selectedDays.forEach { day ->
                scheduleRepo.upsert(Schedule(
                    templateId = templateId,
                    weekday = day,
                    startTime = state.startTime,
                    endTime = state.endTime,
                    syncStatus = SyncStatus.PENDING_SYNC
                ))
            }

            // 7. Mark onboarding as completed
            settingsDataStore.setOnboardingCompleted(true)
        }
    }
}
