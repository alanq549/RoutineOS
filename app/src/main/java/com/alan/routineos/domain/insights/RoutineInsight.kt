package com.alan.routineos.domain.insights

data class RoutineInsight(
    val id: String,
    val title: String,
    val message: String,
    val category: InsightCategory,
    val severity: InsightSeverity
)
