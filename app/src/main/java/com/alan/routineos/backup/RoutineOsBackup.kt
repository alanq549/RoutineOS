package com.alan.routineos.backup

import com.alan.routineos.data.local.entities.*

/**
 * RoutineOsBackup: Data model representing a full local backup of the application state.
 */
data class RoutineOsBackup(
    val version: Int,
    val exportedAt: Long,
    val templates: List<RoutineTemplate>,
    val nodes: List<Node>,
    val metadataSchemas: List<NodeMetadataSchema>,
    val fieldValues: List<NodeFieldValue>,
    val dayInstances: List<DayInstance>,
    val nodeOverrides: List<NodeOverride>,
    val executionFieldValues: List<ExecutionFieldValue>,
    val planningItems: List<PlanningItemEntity>,
    val scheduleExceptions: List<ScheduleException>
)
