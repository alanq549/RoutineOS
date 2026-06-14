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
    val id: String = UUID.randomUUID().toString(),
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
    val startTime: String = "08:00",
    val endTime: String = "09:00",
    val isCustomizingTypes: Boolean = false,
    val hasMadeTypeChoice: Boolean = false
) {
    val totalSteps: Int
        get() {
            var count = 3 // 1: Nombre, 2: Elección, 3: Horario (Mínimo)
            if (isCustomizingTypes) {
                count += 1 // 4: Tipos
                if (nodeTypes.any { it.hasMetrics }) {
                    count += 1 // 5: Métricas
                }
            }
            return count
        }

    val isTimeValid: Boolean
        get() = try {
            val s = startTime.split(":").map { it.toInt() }
            val e = endTime.split(":").map { it.toInt() }
            val startMins = s[0] * 60 + s[1]
            val endMins = e[0] * 60 + e[1]
            endMins > startMins
        } catch (ex: Exception) {
            false
        }
}
