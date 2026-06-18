package com.alan.routineos.ui.features.stats.state

import com.alan.routineos.data.local.entities.NodeFieldValue
import com.alan.routineos.data.local.entities.NodeMetadataSchema
import com.alan.routineos.domain.insights.RoutineInsight
import com.alan.routineos.domain.observability.DailyActivityCell
import com.alan.routineos.domain.observability.DailySummary
import com.alan.routineos.domain.observability.ExecutionConsistency
import com.alan.routineos.domain.observability.MostAdjustedActivity
import com.alan.routineos.domain.observability.StatsActivityOption
import com.alan.routineos.domain.observability.WeeklySummary

data class ActivityDetailStats(
    val totalDays: Int,
    val completedCount: Int,
    val completionRate: Float,
    val trend: String, // "Mejora", "Empeora", "Estable"
    val avgPlannedDuration: Int?,
    val avgActualDuration: Int?,
    val totalPostpones: Int,
    val totalSkips: Int,
    val totalReschedules: Int,
    val specificInsight: String
)

data class StatsUiState(
    val availableActivities: List<StatsActivityOption> = emptyList(),
    val selectedActivity: StatsActivityOption? = null,
    val activityDetail: ActivityDetailStats? = null,
    val availableFields: List<NodeMetadataSchema> = emptyList(),
    val selectedField: NodeMetadataSchema? = null,
    val dataPoints: List<NodeFieldValue> = emptyList(),
    val completionRate: Float = 0f,
    val currentStreak: Int = 0,
    val isLoading: Boolean = true,
    val nodeSearchQuery: String = "",
    val dailySummary: DailySummary? = null,
    val weeklySummary: WeeklySummary? = null,
    val isObservabilityLoading: Boolean = true,
    val dailyInsights: List<RoutineInsight> = emptyList(),
    val weeklyInsights: List<RoutineInsight> = emptyList(),
    val mostAdjustedActivities: List<MostAdjustedActivity> = emptyList(),
    val consistency: ExecutionConsistency? = null,
    val heatmapData: List<DailyActivityCell> = emptyList(),
    
    // Sufficiency flags
    val hasAnyActivityData: Boolean = false,
    val hasEnoughWeeklyData: Boolean = false,
    val hasEnoughActivityHistory: Boolean = false,
    val hasDurationData: Boolean = false
)
