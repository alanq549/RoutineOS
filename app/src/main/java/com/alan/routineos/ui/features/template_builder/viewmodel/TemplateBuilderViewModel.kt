package com.alan.routineos.ui.features.template_builder.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.core.util.DateUtils
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeFieldValue
import com.alan.routineos.data.local.entities.NodeSchedule
import com.alan.routineos.data.local.entities.RoutineTemplate
import com.alan.routineos.data.local.entities.Schedule
import com.alan.routineos.data.local.entities.SyncStatus
import com.alan.routineos.data.repository.FieldValueRepository
import com.alan.routineos.data.repository.InstanceRepository
import com.alan.routineos.data.repository.MetadataSchemaRepository
import com.alan.routineos.data.repository.NodeRepository
import com.alan.routineos.data.repository.NodeTypeRepository
import com.alan.routineos.data.repository.ScheduleRepository
import com.alan.routineos.data.repository.TemplateRepository
import com.alan.routineos.ui.features.template_builder.navigation.INITIAL_COLOR_ARG
import com.alan.routineos.ui.features.template_builder.navigation.INITIAL_NAME_ARG
import com.alan.routineos.ui.features.template_builder.sections.ContextCategory
import com.alan.routineos.ui.features.template_builder.sections.TimeMode
import com.alan.routineos.ui.features.template_builder.state.TemplateBuilderUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
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
    private val fieldValueRepo: FieldValueRepository,
    private val instanceRepo: InstanceRepository
) : ViewModel() {

    private val templateIdArg: String = checkNotNull(savedStateHandle["templateId"])
    private val initialName: String? = savedStateHandle[INITIAL_NAME_ARG]
    private val initialColor: String? = savedStateHandle[INITIAL_COLOR_ARG]

    private val _uiState = MutableStateFlow(
        TemplateBuilderUiState(
            templateId = if (templateIdArg == "new") null else templateIdArg,
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
            val schemasMap = types.associate { it.id to schemaRepo.getByTypeId(it.id).first() }
            _uiState.update { it.copy(nodeTypes = types, metadataSchemas = schemasMap) }

            if (templateIdArg != "new") {
                val template = templateRepo.getById(templateIdArg)
                if (template != null) {
                    val allTemplateNodes = nodeRepo.getAllByTemplate(template.id)
                    val rootNodeId = template.rootNodeId

                    // Separar el nodo raíz de la actividad de los bloques del editor
                    // Los hijos directos del root de la actividad se ven como parentId = null en el editor
                    val nodesForEditor = allTemplateNodes.filter { it.id != rootNodeId }.map {
                        if (it.parentId == rootNodeId) it.copy(parentId = null) else it
                    }

                    val schedulesMap = allTemplateNodes.associate {
                        it.id to nodeRepo.getSchedulesForNode(it.id).first()
                    }
                    val fieldValuesMap = allTemplateNodes.associate {
                        it.id to fieldValueRepo.getByNode(it.id).first()
                    }

                    val globalSchedules = scheduleRepo.getByTemplate(template.id).first()

                    _uiState.update {
                        it.copy(
                            name = template.name,
                            colorHex = template.colorHex,
                            category = template.category,
                            timeMode = template.timeMode,
                            nodes = nodesForEditor.sortedBy { it.position },
                            nodeSchedules = schedulesMap,
                            fieldValues = fieldValuesMap,
                            selectedDays = globalSchedules.map { it.weekday }.toSet(),
                            startTime = globalSchedules.firstOrNull()?.startTime ?: "08:00",
                            endTime = globalSchedules.firstOrNull()?.endTime ?: "09:00",
                            isLoading = false
                        )
                    }
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
            val newDays =
                if (state.selectedDays.contains(day)) state.selectedDays - day else state.selectedDays + day
            state.copy(selectedDays = newDays)
        }
    }

    fun updateTimeMode(mode: TimeMode) = _uiState.update { it.copy(timeMode = mode) }
    fun updateStartTime(time: String) = _uiState.update { it.copy(startTime = time) }
    fun updateEndTime(time: String) = _uiState.update { it.copy(endTime = time) }

    fun addNode(name: String, typeId: String, parentId: String?) {
        val currentNodes = _uiState.value.nodes
        val position = currentNodes.count { it.parentId == parentId }
        val newNode = Node(
            id = UUID.randomUUID().toString(),
            name = name,
            typeId = typeId,
            parentId = parentId,
            templateId = _uiState.value.templateId ?: "TEMP_ID",
            position = position,
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
                currentValues + NodeFieldValue(
                    nodeId = nodeId,
                    schemaId = schemaId,
                    fieldName = fieldName,
                    value = value
                )
            }
            state.copy(fieldValues = state.fieldValues + (nodeId to newValues))
        }
    }

    fun deleteNode(nodeId: String) {
        val toDelete = mutableSetOf(nodeId)
        var added = true
        while (added) {
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
        // OBLIGATORY LOGS BEFORE SCHEDULE EDIT
        _uiState.value.nodes.forEach { node ->
            Log.d("TODAY_DEBUG", "TREE BEFORE SCHEDULE EDIT node=${node.name} id=${node.id} parentId=${node.parentId}")
        }

        val nodeName = _uiState.value.nodes.find { it.id == nodeId }?.name ?: "Unknown"
        Log.d("TODAY_DEBUG", "SCHEDULE DIRTY node=$nodeName id=$nodeId")
        
        _uiState.update { state ->
            // REGLA CRÍTICA: SOLO MODIFICA nodeSchedules y dirtyScheduleNodeIds. NO TOCAR state.nodes.
            state.copy(
                nodeSchedules = state.nodeSchedules + (nodeId to schedules),
                dirtyScheduleNodeIds = state.dirtyScheduleNodeIds + nodeId
            ) 
        }

        // OBLIGATORY LOGS AFTER SCHEDULE EDIT
        _uiState.value.nodes.forEach { node ->
            Log.d("TODAY_DEBUG", "TREE AFTER SCHEDULE EDIT node=${node.name} id=${node.id} parentId=${node.parentId}")
        }
    }

    fun toggleNodeSequential(nodeId: String, isSequential: Boolean) {
        _uiState.update { state ->
            state.copy(nodes = state.nodes.map { if (it.id == nodeId) it.copy(isSequential = isSequential) else it })
        }
    }

    fun saveTemplate(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true) }
                val state = _uiState.value
                val finalTemplateId = state.templateId ?: UUID.randomUUID().toString()

                // 1. Activity Root Node (Nivel 0) - e.g. "GYM"
                val currentTemplate = state.templateId?.let { templateRepo.getById(it) }
                val rootNodeId = currentTemplate?.rootNodeId ?: UUID.randomUUID().toString()

                val activityRootNode = Node(
                    id = rootNodeId,
                    name = state.name,
                    typeId = "activity_root",
                    templateId = finalTemplateId,
                    parentId = null,
                    instanceId = null,
                    position = 0
                )

                // 2. Map blocks (Nivel 1+) - Re-asignar parentId=rootNodeId si estaban en el nivel superior del editor
                val nodesToSave = state.nodes.map { node ->
                    val finalParentId = node.parentId.takeIf { !it.isNullOrBlank() } ?: rootNodeId
                    node.copy(
                        parentId = finalParentId,
                        templateId = finalTemplateId,
                        instanceId = null
                    )
                }

                val allNodesToSave = listOf(activityRootNode) + nodesToSave

                // OBLIGATORY LOGS BEFORE SAVE
                Log.d("TODAY_DEBUG", "EDITOR TREE COUNT = ${allNodesToSave.size}")
                allNodesToSave.forEach { node ->
                    Log.d(
                        "TODAY_DEBUG",
                        "EDITOR NODE name=${node.name} id=${node.id} parentId=${node.parentId}"
                    )
                }

                // 3. Routine Template
                val template = RoutineTemplate(
                    id = finalTemplateId,
                    rootNodeId = rootNodeId,
                    name = state.name,
                    colorHex = state.colorHex,
                    category = state.category,
                    timeMode = state.timeMode,
                    updatedAt = System.currentTimeMillis()
                )
                templateRepo.upsert(template)

                // 4. Save Nodes SMARTLY (Avoid delete all to preserve schedules)
                val existingNodes = nodeRepo.getAllByTemplate(finalTemplateId)
                val newNodeIds = allNodesToSave.map { it.id }.toSet()

                // Delete nodes that are no longer in the editor
                existingNodes.forEach { existingNode ->
                    if (!newNodeIds.contains(existingNode.id)) {
                        nodeRepo.deleteById(existingNode.id)
                    }
                }

                // Upsert current nodes
                nodeRepo.insertAll(allNodesToSave)

                // 5. Global Schedules
                scheduleRepo.deleteByTemplate(finalTemplateId)
                state.selectedDays.forEach { day ->
                    scheduleRepo.upsert(
                        Schedule(
                            templateId = finalTemplateId,
                            weekday = day,
                            startTime = if (state.timeMode != TimeMode.FLEXIBLE) state.startTime else null,
                            endTime = if (state.timeMode == TimeMode.RANGE) state.endTime else null,
                            syncStatus = SyncStatus.PENDING_SYNC
                        )
                    )
                }

                // 6. Metadata
                state.fieldValues.forEach { (nodeId, values) ->
                    values.forEach { fieldValueRepo.upsert(it.copy(nodeId = nodeId)) }
                }

                // 7. Node specific schedules - ONLY DIRTY ONES
                Log.d("TODAY_DEBUG", "DIRTY SCHEDULE IDS = ${state.dirtyScheduleNodeIds}")
                state.dirtyScheduleNodeIds.forEach { nodeId ->
                    val schedules = state.nodeSchedules[nodeId] ?: emptyList()
                    val node = allNodesToSave.find { it.id == nodeId } ?: return@forEach
                    
                    Log.d("TODAY_DEBUG", "SCHEDULE SAVE START node=${node.name} id=$nodeId")
                    
                    val oldSchedules = nodeRepo.getSchedulesForNode(nodeId).first()
                    Log.d("TODAY_DEBUG", "OLD SCHEDULE COUNT id=$nodeId count=${oldSchedules.size}")
                    
                    // nodeRepo.saveSchedules internally does the delete and insert logs
                    nodeRepo.saveSchedules(nodeId, schedules)
                }

                // OBLIGATORY VALIDATION LOGS AFTER SAVE
                val savedNodesFromDb = nodeRepo.getAllByTemplate(finalTemplateId)
                
                Log.d("TODAY_DEBUG", "TREE AFTER SAVE:")
                savedNodesFromDb.forEach { node ->
                    Log.d("TODAY_DEBUG", "node=${node.name} id=${node.id} parentId=${node.parentId}")
                }
                
                Log.d("TODAY_DEBUG", "SCHEDULES AFTER SAVE:")
                savedNodesFromDb.forEach { node ->
                    val schedules = nodeRepo.getSchedulesForNode(node.id).first()
                    val days = schedules.map { it.dayOfWeek }.joinToString(",")
                    Log.d("TODAY_DEBUG", "node=${node.name} id=${node.id} days=$days")
                }

                instanceRepo.dedupeInstancesForDate(DateUtils.getStartOfDay())

                // Regenerar instancia para hoy
                instanceRepo.regenerateTemplateInstanceForDate(
                    finalTemplateId,
                    DateUtils.getStartOfDay()
                )

                _uiState.update { it.copy(isSaving = false, templateId = finalTemplateId, dirtyScheduleNodeIds = emptySet()) }
                onSuccess()
            } catch (e: Exception) {
                Log.e("TODAY_DEBUG", "Error saving template", e)
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}
