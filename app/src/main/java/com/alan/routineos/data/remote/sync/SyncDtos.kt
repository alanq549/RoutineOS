package com.alan.routineos.data.remote.sync

import com.alan.routineos.data.local.entities.*

data class SyncPushRequest(
    val nodes: List<Node> = emptyList(),
    val nodeTypes: List<NodeType> = emptyList(),
    val schemas: List<NodeMetadataSchema> = emptyList(),
    val values: List<NodeFieldValue> = emptyList(),
    val templates: List<RoutineTemplate> = emptyList(),
    val schedules: List<Schedule> = emptyList(),
    val instances: List<DayInstance> = emptyList(),
    val exceptions: List<ScheduleException> = emptyList(),
    val overrides: List<NodeOverride> = emptyList()
)

data class SyncResponse(
    val success: Boolean,
    val lastSyncTimestamp: Long,
    val nodes: List<Node> = emptyList(),
    val nodeTypes: List<NodeType> = emptyList(),
    val schemas: List<NodeMetadataSchema> = emptyList(),
    val values: List<NodeFieldValue> = emptyList(),
    val templates: List<RoutineTemplate> = emptyList(),
    val schedules: List<Schedule> = emptyList(),
    val instances: List<DayInstance> = emptyList(),
    val exceptions: List<ScheduleException> = emptyList(),
    val overrides: List<NodeOverride> = emptyList()
)
