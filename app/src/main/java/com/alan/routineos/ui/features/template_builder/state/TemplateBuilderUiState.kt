package com.alan.routineos.ui.features.template_builder.state

import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeType

data class TemplateBuilderUiState(
    val templateId: String? = null,
    val name: String = "",
    val colorHex: String = "#3FB950",
    val nodes: List<Node> = emptyList(),
    val nodeTypes: List<NodeType> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false
)
