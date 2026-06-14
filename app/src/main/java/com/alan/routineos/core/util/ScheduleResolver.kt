package com.alan.routineos.core.util

import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeSchedule

object ScheduleResolver {
    fun resolveEffectiveSchedules(
        nodeId: String,
        allNodes: List<Node>,
        nodeSchedules: Map<String, List<NodeSchedule>>,
        activityName: String? = null,
        activityDays: Set<Int> = emptySet(),
        activityStart: String? = null,
        activityEnd: String? = null
    ): List<NodeSchedule> {
        val ownSchedules = nodeSchedules[nodeId].orEmpty()
        if (ownSchedules.isNotEmpty()) {
            return ownSchedules
        }

        val info = resolveInheritanceInfo(nodeId, allNodes, nodeSchedules, activityName, activityDays, activityStart, activityEnd)
        if (info != null) {
            return info.second
        }

        return emptyList()
    }

    fun resolveInheritanceInfo(
        nodeId: String,
        allNodes: List<Node>,
        nodeSchedules: Map<String, List<NodeSchedule>>,
        activityName: String? = null,
        activityDays: Set<Int> = emptySet(),
        activityStart: String? = null,
        activityEnd: String? = null
    ): Pair<String, List<NodeSchedule>>? {
        val node = allNodes.find { it.id == nodeId } ?: return null
        var currentParentId = node.parentId

        while (currentParentId != null) {
            val parentNode = allNodes.find { it.id == currentParentId }
            if (parentNode != null) {
                val parentSchedules = nodeSchedules[parentNode.id].orEmpty()
                if (parentSchedules.isNotEmpty()) {
                    return parentNode.name to parentSchedules
                }
                currentParentId = parentNode.parentId
            } else {
                break
            }
        }

        if (activityName != null && activityDays.isNotEmpty() && activityStart != null) {
            val virtualSchedules = activityDays.map { day ->
                NodeSchedule(
                    nodeId = nodeId,
                    dayOfWeek = day,
                    startTime = activityStart,
                    endTime = activityEnd ?: activityStart
                )
            }
            return activityName to virtualSchedules
        }

        return null
    }

    fun resolveInheritedSchedules(
        node: Node,
        allNodes: List<Node>,
        nodeSchedules: Map<String, List<NodeSchedule>>,
        activityName: String? = null,
        activityDays: Set<Int> = emptySet(),
        activityStart: String? = null,
        activityEnd: String? = null
    ): List<NodeSchedule> {
        return resolveInheritanceInfo(node.id, allNodes, nodeSchedules, activityName, activityDays, activityStart, activityEnd)?.second ?: emptyList()
    }

    fun isOutsideRange(ownSchedules: List<NodeSchedule>, parentSchedules: List<NodeSchedule>): Boolean {
        if (ownSchedules.isEmpty() || parentSchedules.isEmpty()) return false
        
        for (own in ownSchedules) {
            val parent = parentSchedules.find { it.dayOfWeek == own.dayOfWeek } ?: continue
            if (!isTimeRangeInside(own.startTime, own.endTime, parent.startTime, parent.endTime)) {
                return true
            }
        }
        return false
    }

    fun isTimeRangeInside(
        childStart: String,
        childEnd: String,
        parentStart: String,
        parentEnd: String
    ): Boolean {
        val cs = timeToMinutes(childStart)
        val ce = timeToMinutes(childEnd)
        val ps = timeToMinutes(parentStart)
        val pe = timeToMinutes(parentEnd)
        
        return cs >= ps && ce <= pe && cs < ce
    }

    fun timeToMinutes(time: String): Int {
        return try {
            val parts = time.split(":")
            parts[0].toInt() * 60 + parts[1].toInt()
        } catch (e: Exception) {
            0
        }
    }
}
