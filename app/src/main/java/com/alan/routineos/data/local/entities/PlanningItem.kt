package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "planning_items")
data class PlanningItemEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val description: String?,
    val dueDate: Long?,
    val dueTime: String?,
    val relatedNodeId: String?,
    val relatedNodePath: String?,
    val status: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "LOCAL",
    val version: Int = 1
)
