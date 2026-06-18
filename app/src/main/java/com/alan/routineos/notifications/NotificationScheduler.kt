package com.alan.routineos.notifications

import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeOverride
import com.alan.routineos.data.local.entities.NodeStatus
import com.alan.routineos.data.local.entities.OverrideType
import com.alan.routineos.data.local.entities.PlanningItemEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * NotificationScheduler: Determines which activities from the daily agenda should have notifications scheduled.
 */
object NotificationScheduler {

    data class SchedulableReminder(
        val nodeId: String,
        val title: String,
        val targetTime: String // Final scheduled time format "HH:mm" considering overrides
    )

    fun getSchedulableReminders(
        nodes: List<Node>,
        overrides: List<NodeOverride>
    ): List<SchedulableReminder> {
        val overrideMap = overrides.associateBy { it.nodeId }

        return nodes.mapNotNull { node ->
            // Skip if already done or skipped
            if (node.status == NodeStatus.COMPLETED || node.status == NodeStatus.SKIPPED) return@mapNotNull null

            if (!ReminderPolicy.canScheduleReminder(node)) return@mapNotNull null

            val override = overrideMap[node.id]
            if (override != null) {
                when (override.overrideType) {
                    OverrideType.SKIP,
                    OverrideType.CANCEL -> return@mapNotNull null // Ignored entirely
                    OverrideType.RESCHEDULE -> {
                        val newTime = override.newTime
                        if (newTime.isNullOrBlank()) return@mapNotNull null
                        SchedulableReminder(node.id, node.name, newTime)
                    }
                    OverrideType.POSTPONE -> {
                        val baseTime = node.scheduledTime ?: return@mapNotNull null
                        val postponeMin = override.postponeMinutes ?: 0
                        val finalTime = calculatePostponedTime(baseTime, postponeMin)
                        SchedulableReminder(node.id, node.name, finalTime)
                    }
                    OverrideType.DURATION_CHANGE -> {
                        // Duration change doesn't change start time
                        val time = node.scheduledTime ?: return@mapNotNull null
                        SchedulableReminder(node.id, node.name, time)
                    }
                }
            } else {
                val time = node.scheduledTime ?: return@mapNotNull null
                SchedulableReminder(node.id, node.name, time)
            }
        }
    }

    fun getPlanningReminders(
        items: List<PlanningItemEntity>
    ): List<SchedulableReminder> {
        return items.mapNotNull { item ->
            if (!ReminderPolicy.canSchedulePlanningReminder(item)) return@mapNotNull null
            
            SchedulableReminder(
                nodeId = item.id,
                title = item.title,
                targetTime = item.dueTime!!
            )
        }
    }

    private fun calculatePostponedTime(baseTime: String, minutes: Int): String {
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = sdf.parse(baseTime) ?: return baseTime
            val calendar = Calendar.getInstance().apply {
                time = date
                add(Calendar.MINUTE, minutes)
            }
            sdf.format(calendar.time)
        } catch (e: Exception) {
            baseTime
        }
    }
}
