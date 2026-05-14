package com.alan.routineos.ui.features.onboarding.state

import com.alan.routineos.data.local.entities.FieldType
import java.util.UUID

data class NodeTypeDraft(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: String? = null,
    val hasMetrics: Boolean = false,
    val schemas: List<NodeMetadataSchemaDraft> = emptyList()
)

data class NodeMetadataSchemaDraft(
    val fieldName: String,
    val fieldLabel: String,
    val fieldType: FieldType,
    val unit: String? = null,
    val defaultValue: String? = null
)

data class OnboardingUiState(
    val currentStep: Int = 1,
    val routineName: String = "",
    val nodeTypes: List<NodeTypeDraft> = emptyList(),
    val selectedDays: List<Int> = emptyList(), // 1=Mon, 7=Sun
    val startTime: String = "08:00"
)
