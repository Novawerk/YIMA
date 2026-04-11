package com.haodong.yimalaile.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.haodong.yimalaile.MainActivity
import com.haodong.yimalaile.R

/**
 * Delivered when an [android.app.AlarmManager] reminder fires. Builds a simple
 * notification that taps through to [MainActivity].
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AndroidNotificationScheduler.ACTION_REMINDER) return

        val id = intent.getIntExtra(AndroidNotificationScheduler.EXTRA_ID, 0)
        val title = intent.getStringExtra(AndroidNotificationScheduler.EXTRA_TITLE).orEmpty()
        val body = intent.getStringExtra(AndroidNotificationScheduler.EXTRA_BODY).orEmpty()

        val contentIntent = PendingIntent.getActivity(
            context,
            id,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, AndroidNotificationScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        val manager = NotificationManagerCompat.from(context)
        if (manager.areNotificationsEnabled()) {
            try {
                manager.notify(id, notification)
            } catch (_: SecurityException) {
                // POST_NOTIFICATIONS not granted — silently drop.
            }
        }
    }
}
