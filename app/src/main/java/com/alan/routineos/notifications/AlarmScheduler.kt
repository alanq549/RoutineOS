package com.alan.routineos.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import com.alan.routineos.core.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminders(reminders: List<NotificationScheduler.SchedulableReminder>) {
        // In a real implementation, we might want to track scheduled IDs to cancel specifically,
        // but using unique PendingIntents by nodeId handles cancellation/replacement.
        
        reminders.forEach { reminder ->
            val targetMillis = parseTimeToMillis(reminder.targetTime)
            
            // Skip if time already passed today
            if (targetMillis <= System.currentTimeMillis()) return@forEach

            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("NODE_ID", reminder.nodeId)
                putExtra("TITLE", reminder.title)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.nodeId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Exact alarm for time-sensitive routine reminders
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                targetMillis,
                pendingIntent
            )
        }
    }

    fun cancelReminder(nodeId: String) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            nodeId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun parseTimeToMillis(time: String): Long {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = sdf.parse(time) ?: return 0L
        
        val target = Calendar.getInstance().apply {
            val d = Calendar.getInstance().apply { this.time = date }
            set(Calendar.HOUR_OF_DAY, d.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, d.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        return target.timeInMillis
    }
}
