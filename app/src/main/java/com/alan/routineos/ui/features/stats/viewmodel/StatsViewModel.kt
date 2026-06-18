package com.alan.routineos.ui.features.stats.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.core.util.DateUtils
import com.alan.routineos.data.local.entities.*
import com.alan.routineos.data.repository.*
import com.alan.routineos.domain.insights.InsightEngine
import com.alan.routineos.domain.insights.InsightSeverity
import com.alan.routineos.domain.insights.RoutineInsight
import com.alan.routineos.domain.observability.*
import com.alan.routineos.ui.features.stats.state.ActivityDetailStats
import com.alan.routineos.ui.features.stats.state.StatsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val nodeRepo: NodeRepository,
    private val schemaRepo: MetadataSchemaRepository,
    private val fieldValueRepo: FieldValueRepository,
    private val instanceRepo: InstanceRepository,
    private val observabilityRepo: ObservabilityRepository,
    private val overrideRepo: NodeOverrideRepository,
    private val executionRepo: ExecutionFieldValueRepository
) : ViewModel() {

    private val _selectedActivity = MutableStateFlow<StatsActivityOption?>(null)
    private val _selectedField = MutableStateFlow<NodeMetadataSchema?>(null)
    private val _nodeSearchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<StatsUiState> = combine(
        _selectedActivity,
        _selectedField,
        _nodeSearchQuery,
        flow {
            val rate = instanceRepo.getCompletionRate(System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000))
            val streak = instanceRepo.calculateCurrentStreak()
            emit(rate to streak)
        },
        observabilityRepo.observeDailySummary(DateUtils.getStartOfDay(System.currentTimeMillis())),
        observabilityRepo.observeWeeklySummary(DateUtils.getStartOfDay(System.currentTimeMillis())),
        nodeRepo.getAllTemplateNodesIncludingDeleted(),
        observabilityRepo.observeMostAdjustedActivities(),
        observabilityRepo.observeExecutionConsistency(),
        observabilityRepo.observeActivityHeatmap(90),
        nodeRepo.getAllInstanceNodes(),
        overrideRepo.getAll(),
        executionRepo.getAll()
    ) { args ->
        val selectedActivity = args[0] as StatsActivityOption?
        val field = args[1] as NodeMetadataSchema?
        val query = args[2] as String
        @Suppress("UNCHECKED_CAST")
        val metrics = args[3] as Pair<Float, Int>
        val daily = args[4] as DailySummary
        val weekly = args[5] as WeeklySummary
        @Suppress("UNCHECKED_CAST")
        val allTemplateNodes = args[6] as List<Node>
        @Suppress("UNCHECKED_CAST")
        val mostAdjusted = args[7] as List<MostAdjustedActivity>
        val consistency = args[8] as ExecutionConsistency
        @Suppress("UNCHECKED_CAST")
        val heatmap = args[9] as List<DailyActivityCell>
        @Suppress("UNCHECKED_CAST")
        val allInstanceNodes = args[10] as List<Node>
        @Suppress("UNCHECKED_CAST")
        val allOverrides = args[11] as List<NodeOverride>
        @Suppress("UNCHECKED_CAST")
        val allExecutionValues = args[12] as List<ExecutionFieldValue>

        val groupedActivities = allTemplateNodes
            .filter { 
                it.temporalMode != TemporalMode.NONE && 
                it.typeId != "activity_root" &&
                !it.name.lowercase().contains(Regex("lunes|martes|miércoles|miercoles|jueves|viernes|sábado|sabado|domingo"))
            }
            .groupBy { it.name.trim().lowercase() to it.typeId }
            .mapNotNull { (key, nodes) ->
                val (_, typeId) = key
                val activeNodes = nodes.filter { it.deletedAt == null }
                if (activeNodes.isEmpty()) return@mapNotNull null

                val representativeNode = activeNodes.first()

                StatsActivityOption(
                    id = representativeNode.id,
                    displayName = representativeNode.name,
                    typeId = typeId,
                    nodeIds = nodes.map { it.id },
                    sourceTemplateNodeIds = nodes.map { it.id }
                )
            }
            .sortedBy { it.displayName }

        val filteredActivities = if (query.isBlank()) groupedActivities
        else groupedActivities.filter { it.displayName.contains(query, ignoreCase = true) }

        var fields = emptyList<NodeMetadataSchema>()
        var history = emptyList<NodeFieldValue>()
        var activityDetail: ActivityDetailStats? = null

        if (selectedActivity != null) {
            val activityInstances = allInstanceNodes.filter { 
                selectedActivity.sourceTemplateNodeIds.contains(it.sourceTemplateNodeId) 
            }.sortedByDescending { it.createdAt }

            if (activityInstances.isNotEmpty()) {
                val completed = activityInstances.count { it.status == NodeStatus.COMPLETED }
                val total = activityInstances.size
                val rate = completed.toFloat() / total
                
                val recent = activityInstances.take(7)
                val recentRate = recent.count { it.status == NodeStatus.COMPLETED }.toFloat() / recent.size
                val trend = when {
                    recentRate > rate + 0.1f -> "Mejora"
                    recentRate < rate - 0.1f -> "Empeora"
                    else -> "Estable"
                }

                val activityNodeIds = activityInstances.map { it.id }.toSet()
                val activityOverrides = allOverrides.filter { it.nodeId in activityNodeIds }
                
                val postpones = activityOverrides.count { it.overrideType == OverrideType.POSTPONE }
                val skips = activityOverrides.count { it.overrideType == OverrideType.SKIP }
                val reschedules = activityOverrides.count { it.overrideType == OverrideType.RESCHEDULE }

                val avgPlanned = activityInstances.mapNotNull { it.durationMinutes }.takeIf { it.isNotEmpty() }?.average()?.toInt()
                
                val schemasForType = schemaRepo.getByTypeId(selectedActivity.typeId).first()
                val durationSchemaId = schemasForType.find { it.fieldType == FieldType.DURATION }?.id
                val avgActual = if (durationSchemaId != null) {
                    allExecutionValues.filter { it.nodeId in activityNodeIds && it.schemaId == durationSchemaId }
                        .mapNotNull { it.actualValue.toIntOrNull() }
                        .takeIf { it.isNotEmpty() }?.average()?.toInt()
                } else null

                val insight = when {
                    rate >= 0.9f -> "Esta actividad suele completarse."
                    postpones > total / 3 -> "Esta actividad suele posponerse frecuentemente."
                    avgActual != null && avgPlanned != null && avgActual > avgPlanned -> "La duración real supera frecuentemente lo planeado."
                    rate < 0.5f -> "Esta actividad presenta baja adherencia."
                    else -> "Comportamiento estable en el tiempo."
                }

                activityDetail = ActivityDetailStats(
                    totalDays = total,
                    completedCount = completed,
                    completionRate = rate,
                    trend = trend,
                    avgPlannedDuration = avgPlanned,
                    avgActualDuration = avgActual,
                    totalPostpones = postpones,
                    totalSkips = skips,
                    totalReschedules = reschedules,
                    specificInsight = insight
                )
            }

            fields = schemaRepo.getByTypeId(selectedActivity.typeId).first().filter {
                it.fieldType == FieldType.NUMBER || it.fieldType == FieldType.DURATION
            }

            if (field != null) {
                history = fieldValueRepo.getHistoryByTemplateNodes(selectedActivity.nodeIds, field.fieldName).first()
            }
        }

        val dailyInsights = InsightEngine.generateDailyInsights(daily)
        val weeklyInsights = InsightEngine.generateWeeklyInsights(weekly)

        val sortedInsights = (dailyInsights + weeklyInsights)
            .sortedWith(compareBy<RoutineInsight> { 
                when (it.severity) {
                    InsightSeverity.WARNING -> 0
                    InsightSeverity.POSITIVE -> 1
                    InsightSeverity.INFO -> 2
                }
            }.thenByDescending { it.id.startsWith("weekly") })
            .distinctBy { it.category }
            .take(3)

        val hasAnyActivity = daily.totalItems > 0 || weekly.dailySummaries.any { it.totalItems > 0 }
        val enoughWeekly = weekly.dailySummaries.count { it.totalItems > 0 } >= 2
        val enoughActivityHistory = (activityDetail?.totalDays ?: 0) >= 3
        val hasDuration = daily.actualDurationMinutes > 0 || (activityDetail?.avgActualDuration ?: 0) > 0

        StatsUiState(
            availableActivities = filteredActivities,
            selectedActivity = selectedActivity,
            activityDetail = activityDetail,
            availableFields = fields,
            selectedField = field,
            dataPoints = history,
            completionRate = metrics.first,
            currentStreak = metrics.second,
            isLoading = false,
            nodeSearchQuery = query,
            dailySummary = daily,
            weeklySummary = weekly,
            isObservabilityLoading = false,
            dailyInsights = if (hasAnyActivity) sortedInsights else emptyList(),
            weeklyInsights = emptyList(),
            mostAdjustedActivities = mostAdjusted.filter { it.nodeId !in (selectedActivity?.nodeIds ?: emptySet()) },
            consistency = consistency,
            heatmapData = heatmap,
            hasAnyActivityData = hasAnyActivity,
            hasEnoughWeeklyData = enoughWeekly,
            hasEnoughActivityHistory = enoughActivityHistory,
            hasDurationData = hasDuration
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUiState())

    fun selectActivity(activity: StatsActivityOption) {
        _selectedActivity.value = activity
        _selectedField.value = null
    }

    fun selectField(schema: NodeMetadataSchema) {
        _selectedField.value = schema
    }

    fun updateSearchQuery(query: String) {
        _nodeSearchQuery.value = query
    }
}
