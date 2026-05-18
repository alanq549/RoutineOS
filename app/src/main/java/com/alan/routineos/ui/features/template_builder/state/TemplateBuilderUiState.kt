package com.alan.routineos.ui.features.template_builder.state

import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeFieldValue
import com.alan.routineos.data.local.entities.NodeMetadataSchema
import com.alan.routineos.data.local.entities.NodeSchedule
import com.alan.routineos.data.local.entities.NodeType
import com.alan.routineos.ui.features.template_builder.sections.ContextCategory
import com.alan.routineos.ui.features.template_builder.sections.TimeMode

data class TemplateBuilderUiState(
    val templateId: String? = null,
    val name: String = "",
    val category: ContextCategory = ContextCategory.FLEXIBLE,
    val colorHex: String = "#3FB950",
    
    // Temporal State
    val selectedDays: Set<Int> = emptySet(),
    val timeMode: TimeMode = TimeMode.RANGE,
    val startTime: String = "08:00",
    val endTime: String = "09:00",

    val nodes: List<Node> = emptyList(),
    val nodeSchedules: Map<String, List<NodeSchedule>> = emptyMap(), // nodeId -> list of schedules
    val fieldValues: Map<String, List<NodeFieldValue>> = emptyMap(), // nodeId -> list of values
    val nodeTypes: List<NodeType> = emptyList(),
    val metadataSchemas: Map<String, List<NodeMetadataSchema>> = emptyMap(), // typeId -> list of schemas

    val isLoading: Boolean = true,
    val isSaving: Boolean = false
)
