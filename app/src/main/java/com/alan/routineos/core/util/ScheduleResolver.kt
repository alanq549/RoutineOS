package com.alan.routineos.core.util

import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeSchedule

object ScheduleResolver {
    fun resolveEffectiveSchedules(
        nodeId: String,
        allNodes: List<Node>,
        nodeSchedules: Map<String, List<NodeSchedule>>
    ): List<NodeSchedule> {
        val ownSchedules = nodeSchedules[nodeId].orEmpty()
        if (ownSchedules.isNotEmpty()) return ownSchedules

        val node = allNodes.find { it.id == nodeId } ?: return emptyList()
        var parentId = node.parentId

        while (parentId != null) {
            val parentSchedules = nodeSchedules[parentId].orEmpty()
            if (parentSchedules.isNotEmpty()) return parentSchedules

            parentId = allNodes.find { it.id == parentId }?.parentId
        }

        return emptyList()
    }

    /**
     * Busca horarios únicamente en los ancestros del nodo, ignorando los propios.
     */
    fun resolveInheritedSchedules(
        node: Node,
        allNodes: List<Node>,
        nodeSchedules: Map<String, List<NodeSchedule>>
    ): List<NodeSchedule> {
        var parentId = node.parentId

        while (parentId != null) {
            val parentSchedules = nodeSchedules[parentId].orEmpty()
            if (parentSchedules.isNotEmpty()) return parentSchedules

            parentId = allNodes.find { it.id == parentId }?.parentId
        }

        return emptyList()
    }

    /**
     * Compara los horarios propios de un nodo con los de su padre para detectar si están fuera de rango.
     */
    fun isOutsideRange(ownSchedules: List<NodeSchedule>, parentSchedules: List<NodeSchedule>): Boolean {
        if (ownSchedules.isEmpty() || parentSchedules.isEmpty()) return false
        
        for (own in ownSchedules) {
            val parent = parentSchedules.find { it.dayOfWeek == own.dayOfWeek } ?: continue
            // Comparación simple de strings HH:mm
            if (own.startTime < parent.startTime || own.endTime > parent.endTime) {
                return true
            }
        }
        return false
    }
}
