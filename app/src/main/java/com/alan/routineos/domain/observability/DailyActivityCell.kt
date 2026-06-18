package com.alan.routineos.domain.observability

/**
 * DailyActivityCell: Represents a single day in the contribution heatmap.
 */
data class DailyActivityCell(
    val date: Long,
    val completedCount: Int,
    val totalCount: Int,
    val intensity: Int // 0 to 4
)
