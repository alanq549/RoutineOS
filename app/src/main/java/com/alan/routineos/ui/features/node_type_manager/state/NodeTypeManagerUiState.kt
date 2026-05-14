package com.alan.routineos.ui.features.node_type_manager.state

import com.alan.routineos.data.local.entities.NodeMetadataSchema
import com.alan.routineos.data.local.entities.NodeType

data class NodeTypeManagerUiState(
    val nodeTypes: List<NodeType> = emptyList(),
    val selectedType: NodeType? = null,
    val schemasForSelectedType: List<NodeMetadataSchema> = emptyList(),
    val isLoading: Boolean = true
)
