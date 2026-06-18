package com.alan.routineos.domain.observability

data class MostAdjustedActivity(
    val nodeId: String, // String to align with Room Node primary key type
    val title: String,
    val adjustedCount: Int
)
