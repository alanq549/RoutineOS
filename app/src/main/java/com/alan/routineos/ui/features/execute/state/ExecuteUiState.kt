package com.alan.routineos.ui.features.execute.state

import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeFieldValue
import com.alan.routineos.data.local.entities.NodeMetadataSchema
import com.alan.routineos.data.local.entities.NodeType

data class ExecuteUiState(
    val node: Node? = null,
    val parentNode: Node? = null,
    val nodeType: NodeType? = null,
    val schemas: List<NodeMetadataSchema> = emptyList(),
    val fieldValues: Map<String, String> = emptyMap(),
    val history: List<HistorySession> = emptyList(),
    val isLoading: Boolean = true,
    val shouldStartTimer: Int? = null,
    val hasUnsavedChanges: Boolean = false,
    val showExitConfirmation: Boolean = false
)

data class HistorySession(
    val date: Long,
    val values: List<NodeFieldValue>
)
