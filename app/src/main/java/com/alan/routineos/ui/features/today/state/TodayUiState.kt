package com.alan.routineos.ui.features.today.state

import com.alan.routineos.data.local.entities.DayInstance
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeType

data class TodayUiState(
    val instance: DayInstance? = null,
    val nodes: List<Node> = emptyList(),
    val nodeTypes: List<NodeType> = emptyList(),
    val isLoading: Boolean = true,
    val currentTime: String = ""
)
