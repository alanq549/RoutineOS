package com.alan.routineos.core.util

import android.util.Log
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
            Log.d("SCHEDULE_DEBUG", "NODE OWN SCHEDULE FOUND nodeId=$nodeId")
            return ownSchedules
        }

        val info = resolveInheritanceInfo(nodeId, allNodes, nodeSchedules, activityName, activityDays, activityStart, activityEnd)
        if (info != null) {
            Log.d("SCHEDULE_DEBUG", "NODE INHERITED SCHEDULE FOUND nodeId=$nodeId inheritedFrom=${info.first}")
            return info.second
        }

        Log.d("SCHEDULE_DEBUG", "NODE NO EFFECTIVE SCHEDULE nodeId=$nodeId")
        return emptyList()
    }

    /**
     * Busca el horario heredado y el nombre de la fuente (nodo o actividad).
     */
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

        // Fallback a la actividad si tiene horario definido
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

    /**
     * Busca horarios únicamente en los ancestros del nodo, ignorando los propios.
     */
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

    /**
     * Compara los horarios propios de un nodo con los de su padre para detectar si están fuera de rango.
     */
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

    /**
     * FIX 4 - Fallback por nombre para detectar días de la semana de forma flexible.
     */
    fun getWeekdayFromName(name: String): Int? {
        val normalized = name.trim().lowercase()
        return when {
            normalized.contains("lunes") -> 1
            normalized.contains("martes") -> 2
            normalized.contains("miércoles") || normalized.contains("miercoles") -> 3
            normalized.contains("jueves") -> 4
            normalized.contains("viernes") -> 5
            normalized.contains("sábado") || normalized.contains("sabado") -> 6
            normalized.contains("domingo") -> 7
            else -> null
        }
    }

    fun isWeekdayNameFallback(name: String, weekday: Int): Boolean {
        val dayValue = getWeekdayFromName(name)
        return dayValue != null && dayValue == weekday
    }

    /**
     * FIX 5 - Helper para validar si un rango está contenido en otro.
     */
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
