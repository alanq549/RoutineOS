package com.alan.routineos.domain.observability

data class DailySummary(
    val date: Long,
    val totalItems: Int,
    val completedItems: Int,
    val skippedCount: Int,
    val delayedCount: Int,
    val modifiedCount: Int,
    val plannedDurationMinutes: Int,
    val actualDurationMinutes: Int,
    val completionRate: Float = if (totalItems > 0) completedItems.toFloat() / totalItems else 0f
)
