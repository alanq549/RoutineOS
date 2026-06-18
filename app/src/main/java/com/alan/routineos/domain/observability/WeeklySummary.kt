package com.alan.routineos.domain.observability

data class WeeklySummary(
    val weekStartDate: Long,
    val weekEndDate: Long,
    val dailySummaries: List<DailySummary>,
    val averageCompletionRate: Float,
    val totalSkippedCount: Int,
    val totalDelayedCount: Int,
    val totalModifiedCount: Int,
    val totalPlannedDurationMinutes: Int,
    val totalActualDurationMinutes: Int
)
