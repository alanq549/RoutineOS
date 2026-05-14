package com.alan.routineos.ui.features.stats.state

import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeFieldValue
import com.alan.routineos.data.local.entities.NodeMetadataSchema

data class StatsUiState(
    val availableNodes: List<Node> = emptyList(),
    val selectedNode: Node? = null,
    val availableFields: List<NodeMetadataSchema> = emptyList(),
    val selectedField: NodeMetadataSchema? = null,
    val dataPoints: List<NodeFieldValue> = emptyList(),
    val completionRate: Float = 0f,
    val currentStreak: Int = 0,
    val isLoading: Boolean = true,
    val nodeSearchQuery: String = ""
)
