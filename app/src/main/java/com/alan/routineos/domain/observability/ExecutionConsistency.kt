package com.alan.routineos.domain.observability

data class ExecutionConsistency(
    val activeDays: Int,
    val consistentDays: Int,
    val consistencyRate: Float
)
