package com.alan.routineos.data.repository

import com.alan.routineos.core.util.DateUtils
import com.alan.routineos.data.local.dao.*
import com.alan.routineos.data.local.entities.FieldType
import com.alan.routineos.data.local.entities.NodeStatus
import com.alan.routineos.data.local.entities.OverrideType
import com.alan.routineos.data.local.entities.TemporalMode
import com.alan.routineos.domain.observability.DailyActivityCell
import com.alan.routineos.domain.observability.DailySummary
import com.alan.routineos.domain.observability.ExecutionConsistency
import com.alan.routineos.domain.observability.MostAdjustedActivity
import com.alan.routineos.domain.observability.WeeklySummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObservabilityRepository @Inject constructor(
    private val dayInstanceDao: DayInstanceDao,
    private val nodeDao: NodeDao,
    private val overrideDao: NodeOverrideDao,
    private val executionDao: ExecutionFieldValueDao,
    private val schemaDao: MetadataSchemaDao
) {

    /**
     * Observa el historial de actividad para el heatmap (últimos 90 días).
     */
    fun observeActivityHeatmap(days: Int = 90): Flow<List<DailyActivityCell>> {
        val today = DateUtils.getStartOfDay()
        val startDate = today - (days.toLong() * 24 * 60 * 60 * 1000)

        // Combinamos flujos diarios
        val dayFlows = (0 until days).map { i ->
            observeDailySummary(startDate + (i * 24L * 60 * 60 * 1000))
        }

        return combine(dayFlows) { summaries ->
            summaries.map { summary ->
                val intensity = when {
                    summary.totalItems == 0 -> 0
                    summary.completionRate >= 1.0f -> 4
                    summary.completionRate >= 0.7f -> 3
                    summary.completionRate >= 0.4f -> 2
                    summary.completionRate > 0f -> 1
                    else -> 0
                }
                DailyActivityCell(
                    date = summary.date,
                    completedCount = summary.completedItems,
                    totalCount = summary.totalItems,
                    intensity = intensity
                )
            }
        }
    }

    /**
     * Observa la consistencia de ejecución de los últimos 30 días.
     */
    fun observeExecutionConsistency(): Flow<ExecutionConsistency> {
        val today = DateUtils.getStartOfDay()
        val thirtyDaysAgo = today - (30L * 24 * 60 * 60 * 1000)
        
        val dayFlows = (0 until 30).map { i ->
            observeDailySummary(thirtyDaysAgo + (i * 24L * 60 * 60 * 1000))
        }

        return combine(dayFlows) { summaries ->
            val summaryList = summaries.toList()
            val activeDaysList = summaryList.filter { it.totalItems > 0 }
            val consistentDaysList = activeDaysList.filter { it.completionRate >= 0.7f }

            val activeCount = activeDaysList.size
            val consistentCount = consistentDaysList.size
            val rate = if (activeCount > 0) consistentCount.toFloat() / activeCount else 0f

            ExecutionConsistency(
                activeDays = activeCount,
                consistentDays = consistentCount,
                consistencyRate = rate
            )
        }
    }

    /**
     * Observa las actividades con mayor número de ajustes.
     */
    fun observeMostAdjustedActivities(limit: Int = 5): Flow<List<MostAdjustedActivity>> {
        return combine(
            overrideDao.getAll(),
            nodeDao.getAllTemplateNodes()
        ) { overrides, templateNodes ->
            val nodeNameMap = templateNodes.associate { it.id to it.name }
            
            overrides
                .groupBy { it.nodeId }
                .mapNotNull { (nodeId, nodeOverrides) ->
                    val name = nodeNameMap[nodeId] ?: return@mapNotNull null
                    MostAdjustedActivity(
                        nodeId = nodeId,
                        title = name,
                        adjustedCount = nodeOverrides.size
                    )
                }
                .sortedByDescending { it.adjustedCount }
                .take(limit)
        }
    }

    /**
     * Observa el resumen diario calculado a partir de la actividad real.
     */
    fun observeDailySummary(date: Long): Flow<DailySummary> {
        return combine(
            dayInstanceDao.getAllByDate(date),
            nodeDao.getInstanceNodesForDate(date),
            overrideDao.getAll(),
            executionDao.getAll(),
            schemaDao.getAll()
        ) { instances, dayNodes, allOverrides, allExecValues, allSchemas ->
            val instanceIds = instances.map { it.id }.toSet()

            // 1. Filtrar nodos accionables (Excluir TemporalMode.NONE)
            // Nota: getInstanceNodesForDate ya filtra deletedAt IS NULL
            val actionableNodes = dayNodes.filter { it.temporalMode != TemporalMode.NONE }

            // 2. Conteo de ítems
            val totalItems = actionableNodes.size
            val completedItems = actionableNodes.count { it.status == NodeStatus.COMPLETED }

            // 3. Overrides y ajustes
            val dayOverrides = allOverrides.filter { it.instanceId in instanceIds }

            val skippedNodeIds = dayNodes.filter { it.status == NodeStatus.SKIPPED }.map { it.id }.toSet() +
                    dayOverrides.filter { it.overrideType == OverrideType.SKIP || it.overrideType == OverrideType.CANCEL }
                        .map { it.nodeId }.toSet()
            val skippedCount = skippedNodeIds.size

            val delayedCount = dayOverrides.count { it.overrideType == OverrideType.POSTPONE }
            val modifiedCount = dayOverrides.count {
                it.overrideType == OverrideType.POSTPONE ||
                        it.overrideType == OverrideType.RESCHEDULE ||
                        it.overrideType == OverrideType.DURATION_CHANGE
            }

            // 4. Duración planeada
            val plannedDuration = actionableNodes.sumOf { node ->
                when (node.temporalMode) {
                    TemporalMode.START_END, TemporalMode.SEQUENTIAL -> node.durationMinutes ?: 0
                    else -> 0
                }
            }

            // 5. Duración real
            val durationSchemas = allSchemas.filter { it.fieldType == FieldType.DURATION }.map { it.id }.toSet()
            val dayExecValues = allExecValues.filter { it.dayInstanceId in instanceIds }
            val actualDuration = dayExecValues
                .filter { it.schemaId in durationSchemas }
                .sumOf { it.actualValue.toIntOrNull() ?: 0 }

            DailySummary(
                date = date,
                totalItems = totalItems,
                completedItems = completedItems,
                skippedCount = skippedCount,
                delayedCount = delayedCount,
                modifiedCount = modifiedCount,
                plannedDurationMinutes = plannedDuration,
                actualDurationMinutes = actualDuration
            )
        }
    }

    /**
     * Observa el resumen semanal a partir de una fecha de referencia.
     */
    fun observeWeeklySummary(referenceDate: Long): Flow<WeeklySummary> {
        val startOfWeek = DateUtils.getStartOfWeek(Date(referenceDate))
        val days = DateUtils.getDaysOfWeek(startOfWeek)

        val dailyFlows = days.map { observeDailySummary(it) }

        return combine(dailyFlows) { summaries ->
            val summaryList = summaries.toList()
            val activeDays = summaryList.filter { it.totalItems > 0 }
            
            val averageRate = if (activeDays.isNotEmpty()) {
                activeDays.map { it.completionRate }.average().toFloat()
            } else 0f

            WeeklySummary(
                weekStartDate = startOfWeek,
                weekEndDate = days.last() + (24 * 3600 * 1000L) - 1,
                dailySummaries = summaryList,
                averageCompletionRate = averageRate,
                totalSkippedCount = summaryList.sumOf { it.skippedCount },
                totalDelayedCount = summaryList.sumOf { it.delayedCount },
                totalModifiedCount = summaryList.sumOf { it.modifiedCount },
                totalPlannedDurationMinutes = summaryList.sumOf { it.plannedDurationMinutes },
                totalActualDurationMinutes = summaryList.sumOf { it.actualDurationMinutes }
            )
        }
    }
}

