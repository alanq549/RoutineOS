package com.alan.routineos.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationWrapper: NotificationManagerWrapper

    override fun onReceive(context: Context, intent: Intent) {
        val nodeId = intent.getStringExtra("NODE_ID") ?: return
        val title = intent.getStringExtra("TITLE") ?: "Recordatorio"
        
        // Trigger the visual notification
        notificationWrapper.showReminderNotification(
            title = title,
            message = "Es hora de comenzar tu actividad",
            nodeId = nodeId
        )
    }
}
