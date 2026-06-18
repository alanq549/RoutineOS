package com.alan.routineos.domain.observability

/**
 * StatsActivityOption: Grouped activities for stats exploration,
 * consolidating technical nodes into human-identifiable activities.
 */
data class StatsActivityOption(
    val id: String,
    val displayName: String,
    val typeId: String,
    val nodeIds: List<String>,
    val sourceTemplateNodeIds: List<String>
)
