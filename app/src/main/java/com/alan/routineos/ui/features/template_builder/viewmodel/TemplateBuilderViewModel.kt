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
import com.alan.routineos.data.local.entities.TemporalMode
import com.alan.routineos.data.repository.FieldValueRepository
import com.alan.routineos.data.repository.InstanceRepository
import com.alan.routineos.data.repository.MetadataSchemaRepository
import com.alan.routineos.data.repository.NodeRepository
import com.alan.routineos.data.repository.NodeTypeRepository
import com.alan.routineos.data.repository.ScheduleRepository
import com.alan.routineos.data.repository.TemplateRepository
import com.alan.routineos.ui.features.template_builder.navigation.INITIAL_COLOR_ARG
import com.alan.routineos.ui.features.template_builder.navigation.INITIAL_NAME_ARG
import com.alan.routineos.ui.features.template_builder.state.TemplateBuilderUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
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

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val types = typeRepo.getAllActive().first()
            val schemasMap = types.associate { it.id to schemaRepo.getByTypeId(it.id).first() }
            _uiState.update { it.copy(nodeTypes = types, metadataSchemas = schemasMap) }

            if (templateIdArg != "new") {
                val template = templateRepo.getById(templateIdArg)
                if (template != null) {
                    val allTemplateNodes = nodeRepo.getAllByTemplate(template.id)
                    val rootNodeId = template.rootNodeId
                    val rootNode = allTemplateNodes.find { it.id == rootNodeId }

                    val globalSchedules = scheduleRepo.getByTemplate(template.id).first()
                    val firstSchedule = globalSchedules.firstOrNull()

                    val dbStartTime = firstSchedule?.startTime ?: rootNode?.scheduledTime ?: "08:00"
                    val dbEndTime = firstSchedule?.endTime ?: rootNode?.durationMinutes?.let {
                        calculateEndTime(
                            dbStartTime,
                            it
                        )
                    } ?: "09:00"

                    val nodesForEditor =
                        allTemplateNodes.filter { it.id != rootNodeId }.map { node ->
                            if (node.parentId == rootNodeId) node.copy(parentId = null) else node
                        }

                    val schedulesMap = allTemplateNodes.associate {
                        it.id to nodeRepo.getSchedulesForNode(it.id).first()
                    }
                    val fieldValuesMap = allTemplateNodes.associate {
                        it.id to fieldValueRepo.getByNode(it.id).first()
                    }

                    var duration = 60
                    try {
                        val startParts = dbStartTime.split(":")
                        val endParts = dbEndTime.split(":")
                        val startMins = startParts[0].toInt() * 60 + startParts[1].toInt()
                        val endMins = endParts[0].toInt() * 60 + endParts[1].toInt()
                        duration = endMins - startMins
                        if (duration < 0) duration += 1440
                    } catch (e: Exception) {
                        Log.e("TODAY_DEBUG", "Error calculating duration", e)
                    }

                    _uiState.update {
                        it.copy(
                            name = template.name,
                            colorHex = template.colorHex,
                            nodes = nodesForEditor.sortedBy { it.position },
                            nodeSchedules = schedulesMap,
                            fieldValues = fieldValuesMap,
                            selectedDays = globalSchedules.map { it.weekday }.toSet(),
                            startTime = dbStartTime,
                            endTime = dbEndTime,
                            durationMinutes = duration,
                            temporalMode = rootNode?.temporalMode ?: TemporalMode.START_END,
                            isLoading = false,
                            hasUnsavedChanges = false // Reset after load
                        )
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, hasUnsavedChanges = false) }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, hasUnsavedChanges = true) }
    }

    fun updateColor(colorHex: String) {
        _uiState.update { it.copy(colorHex = colorHex, hasUnsavedChanges = true) }
    }

    fun toggleDay(day: Int) {
        _uiState.update { state ->
            val newDays =
                if (state.selectedDays.contains(day)) state.selectedDays - day else state.selectedDays + day
            state.copy(selectedDays = newDays, hasUnsavedChanges = true)
        }
    }

    fun updateStartTime(time: String) =
        _uiState.update { it.copy(startTime = time, hasUnsavedChanges = true) }

    fun updateEndTime(time: String) =
        _uiState.update { it.copy(endTime = time, hasUnsavedChanges = true) }

    fun updateTemporalMode(mode: TemporalMode) {
        _uiState.update { state ->
            state.copy(
                temporalMode = mode,
                // Al cambiar a NONE, a efectos de validación, desactivamos el bloqueo por rango
                // usando un rango universal que no activará isOutsideRange.
                startTime = if (mode == TemporalMode.NONE) "00:00" else state.startTime,
                endTime = if (mode == TemporalMode.NONE) "23:59" else state.endTime,
                hasUnsavedChanges = true
            )
        }
    }

    fun updateDurationMinutes(minutes: Int) =
        _uiState.update { it.copy(durationMinutes = minutes, hasUnsavedChanges = true) }

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

    fun addNode(name: String, typeId: String, parentId: String?) {
        Log.d("BUILDER_DEBUG", "ACTION: addNode - name: $name, type: $typeId, parent: $parentId")
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
        _uiState.update { it.copy(nodes = it.nodes + newNode, hasUnsavedChanges = true) }
        logNodeDetailSimple("BUILDER_DEBUG", "AFTER_ADD_NODE", newNode)
    }

    fun updateNodeName(nodeId: String, newName: String) {
        Log.d("BUILDER_DEBUG", "ACTION: updateNodeName - nodeId: $nodeId, newName: $newName")
        val nodeBefore = _uiState.value.nodes.find { it.id == nodeId }
        nodeBefore?.let { logNodeDetailSimple("BUILDER_DEBUG", "BEFORE_NAME_UPDATE", it) }
        
        _uiState.update { state ->
            state.copy(
                nodes = state.nodes.map { if (it.id == nodeId) it.copy(name = newName) else it },
                hasUnsavedChanges = true
            )
        }

        val nodeAfter = _uiState.value.nodes.find { it.id == nodeId }
        nodeAfter?.let { logNodeDetailSimple("BUILDER_DEBUG", "AFTER_NAME_UPDATE", it) }
    }

    fun updateNodeType(nodeId: String, newTypeId: String) {
        Log.d("BUILDER_DEBUG", "ACTION: updateNodeType - nodeId: $nodeId, newType: $newTypeId")
        val nodeBefore = _uiState.value.nodes.find { it.id == nodeId }
        nodeBefore?.let { logNodeDetailSimple("BUILDER_DEBUG", "BEFORE_TYPE_UPDATE", it) }

        _uiState.update { state ->
            state.copy(
                nodes = state.nodes.map { if (it.id == nodeId) it.copy(typeId = newTypeId) else it },
                hasUnsavedChanges = true
            )
        }

        val nodeAfter = _uiState.value.nodes.find { it.id == nodeId }
        nodeAfter?.let { logNodeDetailSimple("BUILDER_DEBUG", "AFTER_TYPE_UPDATE", it) }
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
            state.copy(
                fieldValues = state.fieldValues + (nodeId to newValues),
                hasUnsavedChanges = true
            )
        }
    }

    fun deleteNode(nodeId: String) {
        Log.d("BUILDER_DEBUG", "ACTION: deleteNode - nodeId: $nodeId")
        val nodeBefore = _uiState.value.nodes.find { it.id == nodeId }
        nodeBefore?.let { logNodeDetailSimple("BUILDER_DEBUG", "BEFORE_DELETE", it) }

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
                fieldValues = state.fieldValues.filterKeys { !toDelete.contains(it) },
                hasUnsavedChanges = true
            )
        }
        Log.d("BUILDER_DEBUG", "AFTER_DELETE: Removed ${toDelete.size} nodes")
    }

    fun updateNodeSchedules(nodeId: String, schedules: List<NodeSchedule>) {
        _uiState.update { state ->
            state.copy(
                nodeSchedules = state.nodeSchedules + (nodeId to schedules),
                dirtyScheduleNodeIds = state.dirtyScheduleNodeIds + nodeId,
                hasUnsavedChanges = true
            )
        }
    }

    fun toggleNodeSequential(nodeId: String, isSequential: Boolean) {
        _uiState.update { state ->
            state.copy(
                nodes = state.nodes.map { if (it.id == nodeId) it.copy(isSequential = isSequential) else it },
                hasUnsavedChanges = true
            )
        }
    }

    private fun calculateEndTime(startTime: String, durationMinutes: Int): String {
        try {
            val parts = startTime.split(":")
            val totalMins = parts[0].toInt() * 60 + parts[1].toInt() + durationMinutes
            val finalMins = totalMins % 1440
            val h = finalMins / 60
            val m = finalMins % 60
            return String.format(Locale.US, "%02d:%02d", h, m)
        } catch (e: Exception) {
            return "09:00"
        }
    }

    private fun logNodeDetailSimple(tag: String, prefix: String, node: Node) {
        val children = _uiState.value.nodes.filter { it.parentId == node.id }
        val details = """
            $prefix
            ID: ${node.id} | NAME: ${node.name} | PARENT: ${node.parentId}
            TEMPLATE: ${node.templateId} | TYPE: ${node.typeId}
            MODE: ${node.temporalMode} | SEQ: ${node.isSequential} | POS: ${node.position}
            TIME: ${node.scheduledTime} | DUR: ${node.durationMinutes} | STATUS: ${node.status}
            CHILDREN: ${children.map { it.name }}
        """.trimIndent()
        Log.d(tag, details)
    }

    fun saveTemplate(onSuccess: (() -> Unit)? = null) {
        Log.d("BUILDER_DEBUG", "ACTION: saveTemplate - templateId: ${_uiState.value.templateId}")
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true) }
                val state = _uiState.value
                val finalTemplateId = state.templateId ?: UUID.randomUUID().toString()
                Log.d("BUILDER_DEBUG", "ACTION: saving template $finalTemplateId with ${state.nodes.size} nodes")

                val currentTemplate = state.templateId?.let { templateRepo.getById(it) }
                val rootNodeId = currentTemplate?.rootNodeId ?: UUID.randomUUID().toString()

                val activityRootNode = Node(
                    id = rootNodeId,
                    name = state.name,
                    typeId = "activity_root",
                    templateId = finalTemplateId,
                    parentId = null,
                    instanceId = null,
                    position = 0,
                    scheduledTime = when (state.temporalMode) {
                        TemporalMode.NONE -> null
                        TemporalMode.SEQUENTIAL -> null
                        TemporalMode.START_ONLY -> state.startTime
                        TemporalMode.START_END -> state.startTime
                    },
                    durationMinutes = when (state.temporalMode) {
                        TemporalMode.NONE -> null
                        TemporalMode.START_ONLY -> null
                        TemporalMode.START_END -> state.durationMinutes
                        TemporalMode.SEQUENTIAL -> state.durationMinutes
                    },
                    isSequential = state.temporalMode == TemporalMode.SEQUENTIAL,
                    temporalMode = state.temporalMode
                )

                val nodesToSave = state.nodes.map { node ->
                    val finalParentId = node.parentId.takeIf { !it.isNullOrBlank() } ?: rootNodeId
                    node.copy(
                        parentId = finalParentId,
                        templateId = finalTemplateId,
                        instanceId = null
                    )
                }

                val allNodesToSave = listOf(activityRootNode) + nodesToSave

                val template = RoutineTemplate(
                    id = finalTemplateId,
                    rootNodeId = rootNodeId,
                    name = state.name,
                    colorHex = state.colorHex,
                    updatedAt = System.currentTimeMillis()
                )
                templateRepo.upsert(template)

                val existingNodesFromDb = nodeRepo.getAllByTemplate(finalTemplateId)
                val existingNodeMap = existingNodesFromDb.associateBy { it.id }
                val newNodeIds = allNodesToSave.map { it.id }.toSet()

                existingNodesFromDb.forEach { existingNode ->
                    if (!newNodeIds.contains(existingNode.id)) {
                        nodeRepo.deleteById(existingNode.id)
                    }
                }

                allNodesToSave.forEach { node ->
                    val existing = existingNodeMap[node.id]
                    if (existing != null) {
                        nodeRepo.update(
                            node.copy(
                                createdAt = existing.createdAt,
                                status = existing.status,
                                version = existing.version,
                                syncStatus = existing.syncStatus
                            )
                        )
                    } else {
                        nodeRepo.upsert(node)
                    }
                }

                scheduleRepo.deleteByTemplate(finalTemplateId)

                state.selectedDays.forEach { day ->
                    scheduleRepo.upsert(
                        Schedule(
                            templateId = finalTemplateId,
                            weekday = day,
                            startTime = state.startTime,
                            endTime = state.endTime,
                            syncStatus = SyncStatus.PENDING_SYNC
                        )
                    )
                }

                state.fieldValues.forEach { (nodeId, values) ->
                    values.forEach { fieldValueRepo.upsert(it.copy(nodeId = nodeId)) }
                }

                allNodesToSave.forEach { node ->
                    val nodeId = node.id
                    if (state.dirtyScheduleNodeIds.contains(nodeId)) {
                        val schedules = state.nodeSchedules[nodeId] ?: return@forEach
                        nodeRepo.saveSchedules(nodeId, schedules)
                    }
                }

                instanceRepo.dedupeInstancesForDate(DateUtils.getStartOfDay())
                instanceRepo.regenerateTemplateInstanceForDate(
                    finalTemplateId,
                    DateUtils.getStartOfDay()
                )

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        templateId = finalTemplateId,
                        dirtyScheduleNodeIds = emptySet(),
                        hasUnsavedChanges = false,
                        showExitConfirmation = false
                    )
                }
                _events.emit("Rutina guardada")
                onSuccess?.invoke()
            } catch (e: Exception) {
                Log.e("TODAY_DEBUG", "Error saving template", e)
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}
