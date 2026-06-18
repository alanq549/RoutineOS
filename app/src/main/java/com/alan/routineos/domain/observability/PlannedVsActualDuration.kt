package com.alan.routineos.domain.observability

data class PlannedVsActualDuration(
    val plannedMinutes: Int,
    val actualMinutes: Int,
    val deltaMinutes: Int = actualMinutes - plannedMinutes,
    val ratio: Float = if (plannedMinutes > 0) actualMinutes.toFloat() / plannedMinutes else 0f
)
