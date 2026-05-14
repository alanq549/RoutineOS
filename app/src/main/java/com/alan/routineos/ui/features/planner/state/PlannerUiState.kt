package com.alan.routineos.ui.features.planner.state

import com.alan.routineos.data.local.entities.DayInstance
import com.alan.routineos.data.local.entities.RoutineTemplate
import com.alan.routineos.data.local.entities.Schedule
import com.alan.routineos.data.local.entities.ScheduleException
import com.alan.routineos.core.util.DateUtils

data class PlannerUiState(
    val selectedDate: Long = DateUtils.getStartOfDay(),
    val weekDays: List<Long> = emptyList(),
    val activeSchedules: List<ScheduleWithTemplate> = emptyList(),
    val templates: List<RoutineTemplate> = emptyList(),
    val exceptions: List<ScheduleException> = emptyList(),
    val instanceForSelectedDate: DayInstance? = null,
    val isLoading: Boolean = true
)

data class ScheduleWithTemplate(
    val schedule: Schedule,
    val template: RoutineTemplate?
)
