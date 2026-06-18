package com.alan.routineos.notifications

import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.PlanningItemEntity
import com.alan.routineos.data.local.entities.TemporalMode
import com.alan.routineos.ui.features.system.state.PlanningStatus

/**
 * ReminderPolicy: Defines when an activity is eligible for a reminder.
 */
object ReminderPolicy {

    fun canScheduleReminder(node: Node): Boolean {
        // Only schedule for fixed time modes
        return when (node.temporalMode) {
            TemporalMode.START_ONLY,
            TemporalMode.START_END -> {
                // Must have a valid scheduled time "HH:mm"
                !node.scheduledTime.isNullOrBlank()
            }
            TemporalMode.NONE,
            TemporalMode.SEQUENTIAL -> false
        }
    }

    fun canSchedulePlanningReminder(item: PlanningItemEntity): Boolean {
        return item.status != PlanningStatus.COMPLETED.name && !item.dueTime.isNullOrBlank()
    }
}
