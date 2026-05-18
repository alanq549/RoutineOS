package com.alan.routineos.ui.features.template_builder.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.data.local.entities.*
import com.alan.routineos.data.repository.*
import com.alan.routineos.ui.features.template_builder.navigation.INITIAL_COLOR_ARG
import com.alan.routineos.ui.features.template_builder.navigation.INITIAL_NAME_ARG
import com.alan.routineos.ui.features.template_builder.sections.ContextCategory
import com.alan.routineos.ui.features.template_builder.sections.TimeMode
import com.alan.routineos.ui.features.template_builder.state.TemplateBuilderUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TemplateBuilderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val templateRepo: TemplateRepository,
    private val nodeRepo: NodeRepository,
    private val scheduleRepo: ScheduleRepository,
    private val typeRepo: NodeTypeRepository,
    private val schemaRepo: MetadataSchemaRepository,
    private val fieldValueRepo: FieldValueRepository
) : ViewModel() {

    private val templateIdArg: String = checkNotNull(savedStateHandle["templateId"])
    private val initialName: String? = savedStateHandle[INITIAL_NAME_ARG]
    private val initialColor: String? = savedStateHandle[INITIAL_COLOR_ARG]
    
    private val _uiState = MutableStateFlow(
        TemplateBuilderUiState(
            templateId = if(templateIdArg == "new") null else templateIdArg,
            name = initialName ?: "",
            colorHex = initialColor ?: "#3FB950"
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val types = typeRepo.getAll().first()
            val schemasMap = mutableMapOf<String, List<NodeMetadataSchema>>()
            types.forEach { type ->
                schemasMap[type.id] = schemaRepo.getByTypeId(type.id).first()
            }
            _uiState.update { it.copy(nodeTypes = types, metadataSchemas = schemasMap) }

            if (templateIdArg != "new") {
                val template = templateRepo.getById(templateIdArg)
                if (template != null) {
                    val nodes = nodeRepo.getAllByTemplate(template.id)
                    val schedulesMap = mutableMapOf<String, List<NodeSchedule>>()
                    val fieldValuesMap = mutableMapOf<String, List<NodeFieldValue>>()
                    
                    nodes.forEach { node ->
                        schedulesMap[node.id] = nodeRepo.getSchedulesForNode(node.id).first()
                        fieldValuesMap[node.id] = fieldValueRepo.getByNode(node.id).first()
                    }

                    val globalSchedules = scheduleRepo.getByTemplate(template.id).first()
                    val selectedDays = globalSchedules.map { it.weekday }.toSet()
                    val firstSched = globalSchedules.firstOrNull()

                    _uiState.update { it.copy(
                        name = template.name,
                        colorHex = template.colorHex,
                        category = template.category,
                        timeMode = template.timeMode,
                        nodes = nodes,
                        nodeSchedules = schedulesMap,
                        fieldValues = fieldValuesMap,
                        selectedDays = selectedDays,
                        startTime = firstSched?.startTime ?: "08:00",
                        endTime = firstSched?.endTime ?: "09:00",
                        isLoading = false
                    ) }
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateName(name: String) = _uiState.update { it.copy(name = name) }
    fun updateCategory(category: ContextCategory) = _uiState.update { it.copy(category = category) }
    fun updateColor(colorHex: String) = _uiState.update { it.copy(colorHex = colorHex) }

    fun toggleDay(day: Int) {
        _uiState.update { state ->
            val newDays = if (state.selectedDays.contains(day)) state.selectedDays - day else state.selectedDays + day
            state.copy(selectedDays = newDays)
        }
    }

    fun updateTimeMode(mode: TimeMode) = _uiState.update { it.copy(timeMode = mode) }
    fun updateStartTime(time: String) = _uiState.update { it.copy(startTime = time) }
    fun updateEndTime(time: String) = _uiState.update { it.copy(endTime = time) }

    fun addNode(name: String, typeId: String, parentId: String?) {
        val newNode = Node(
            id = UUID.randomUUID().toString(),
            name = name,
            typeId = typeId,
            parentId = parentId,
            templateId = _uiState.value.templateId ?: "TEMP_ID",
            syncStatus = SyncStatus.PENDING_SYNC
        )
        _uiState.update { it.copy(nodes = it.nodes + newNode) }
    }

    fun updateNodeName(nodeId: String, newName: String) {
        _uiState.update { state ->
            state.copy(nodes = state.nodes.map { if (it.id == nodeId) it.copy(name = newName) else it })
        }
    }

    fun updateNodeType(nodeId: String, newTypeId: String) {
        _uiState.update { state ->
            state.copy(nodes = state.nodes.map { if (it.id == nodeId) it.copy(typeId = newTypeId) else it })
        }
    }

    fun updateFieldValue(nodeId: String, schemaId: String, fieldName: String, value: String) {
        _uiState.update { state ->
            val currentValues = state.fieldValues[nodeId] ?: emptyList()
            val newValues = if (currentValues.any { it.schemaId == schemaId }) {
                currentValues.map { if (it.schemaId == schemaId) it.copy(value = value) else it }
            } else {
                currentValues + NodeFieldValue(nodeId = nodeId, schemaId = schemaId, fieldName = fieldName, value = value)
            }
            state.copy(fieldValues = state.fieldValues + (nodeId to newValues))
        }
    }

    fun deleteNode(nodeId: String) {
        // Recursive deletion in state
        val toDelete = mutableSetOf(nodeId)
        var added = true
        while(added) {
            added = false
            _uiState.value.nodes.forEach { 
                if (it.parentId != null && toDelete.contains(it.parentId) && !toDelete.contains(it.id)) {
                    toDelete.add(it.id)
                    added = true
                }
            }
        }
        _uiState.update { state ->
            state.copy(
                nodes = state.nodes.filterNot { toDelete.contains(it.id) },
                nodeSchedules = state.nodeSchedules.filterKeys { !toDelete.contains(it) },
                fieldValues = state.fieldValues.filterKeys { !toDelete.contains(it) }
            )
        }
    }

    fun updateNodeSchedules(nodeId: String, schedules: List<NodeSchedule>) {
        _uiState.update { it.copy(nodeSchedules = it.nodeSchedules + (nodeId to schedules)) }
    }

    fun toggleNodeSequential(nodeId: String, isSequential: Boolean) {
        _uiState.update { state ->
            state.copy(nodes = state.nodes.map { if (it.id == nodeId) it.copy(isSequential = isSequential) else it })
        }
    }

    fun saveTemplate() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val state = _uiState.value
            val finalTemplateId = state.templateId ?: UUID.randomUUID().toString()
            
            // 1. Root Node
            var rootNode = state.nodes.find { it.parentId == null }
            if (rootNode == null) {
                rootNode = Node(id = UUID.randomUUID().toString(), name = state.name, typeId = "default", templateId = finalTemplateId)
            }

            // 2. Routine Template
            val template = RoutineTemplate(
                id = finalTemplateId,
                rootNodeId = rootNode.id,
                name = state.name,
                colorHex = state.colorHex,
                category = state.category,
                timeMode = state.timeMode,
                updatedAt = System.currentTimeMillis()
            )
            templateRepo.upsert(template)
            
            // 3. Clean and Save Nodes
            nodeRepo.deleteByTemplate(finalTemplateId)
            val nodesToSave = (state.nodes + rootNode).distinctBy { it.id }.map { 
                it.copy(templateId = finalTemplateId, syncStatus = SyncStatus.PENDING_SYNC) 
            }
            nodeRepo.insertAll(nodesToSave)

            // 4. Clean and Save Schedules
            scheduleRepo.deleteByTemplate(finalTemplateId)
            state.selectedDays.forEach { day ->
                scheduleRepo.upsert(Schedule(
                    templateId = finalTemplateId,
                    weekday = day,
                    startTime = if (state.timeMode != TimeMode.FLEXIBLE) state.startTime else null,
                    endTime = if (state.timeMode == TimeMode.RANGE) state.endTime else null,
                    syncStatus = SyncStatus.PENDING_SYNC
                ))
            }

            // 5. Metadata for each node
            state.fieldValues.forEach { (nodeId, values) ->
                values.forEach { fieldValueRepo.upsert(it.copy(nodeId = nodeId)) }
            }

            // 6. Node-specific schedules
            state.nodeSchedules.forEach { (nodeId, schedules) ->
                nodeRepo.saveSchedules(nodeId, schedules)
            }
            
            _uiState.update { it.copy(isSaving = false, templateId = finalTemplateId) }
        }
    }
}
