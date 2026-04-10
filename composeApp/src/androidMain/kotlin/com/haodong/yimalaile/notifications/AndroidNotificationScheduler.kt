package com.haodong.yimalaile.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.haodong.yimalaile.domain.notifications.NotificationScheduler
import com.haodong.yimalaile.domain.notifications.ReminderKind
import com.haodong.yimalaile.domain.notifications.ScheduledReminder
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.coroutines.CompletableDeferred

/**
 * Android scheduler backed by [AlarmManager].
 *
 * Reminders are stored as PendingIntents routed to [ReminderReceiver], which
 * surfaces them through [NotificationManager]. Exact alarms are avoided to
 * keep the app off the SCHEDULE_EXACT_ALARM permission list — the reminder
 * hour is approximate anyway ("around 9 am, two days before your period").
 */
class AndroidNotificationScheduler(
    private val context: Context,
) : NotificationScheduler {

    init {
        ensureChannel()
    }

    override suspend fun hasPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun requestPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        // If already granted, return true. If not, the caller (Activity) is
        // responsible for showing the system dialog — we expose a static bridge.
        if (hasPermission()) return true
        val activity = currentActivityHolder.get() ?: return false
        val deferred = CompletableDeferred<Boolean>()
        pendingPermissionResult = deferred
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            PERMISSION_REQUEST_CODE,
        )
        return deferred.await()
    }

    override fun schedule(reminder: ScheduledReminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = buildPendingIntent(reminder, create = true) ?: return

        val triggerMillis = reminder.fireAt
            .toInstant(TimeZone.currentSystemDefault())
            .toEpochMilliseconds()

        if (reminder.repeatDaily) {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                AlarmManager.INTERVAL_DAY,
                pending,
            )
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pending)
        }
    }

    override fun cancel(kind: ReminderKind) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = buildPendingIntent(
            ScheduledReminder(
                id = kind.stableId,
                kind = kind,
                title = "",
                body = "",
                fireAt = kotlinx.datetime.LocalDateTime(1970, 1, 1, 0, 0),
            ),
            create = false,
        ) ?: return
        alarmManager.cancel(pending)
        pending.cancel()
    }

    override fun cancelAll() {
        ReminderKind.entries.forEach { cancel(it) }
    }

    private fun buildPendingIntent(reminder: ScheduledReminder, create: Boolean): PendingIntent? {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMINDER
            putExtra(EXTRA_ID, reminder.id)
            putExtra(EXTRA_KIND, reminder.kind.name)
            putExtra(EXTRA_TITLE, reminder.title)
            putExtra(EXTRA_BODY, reminder.body)
        }
        val flags = if (create) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        }
        return PendingIntent.getBroadcast(context, reminder.id, intent, flags)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = CHANNEL_DESCRIPTION
                }
                nm.createNotificationChannel(channel)
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "yimalaile_reminders"
        const val CHANNEL_NAME = "Reminders"
        const val CHANNEL_DESCRIPTION = "Period, ovulation and daily reminders"
        const val ACTION_REMINDER = "com.haodong.yimalaile.ACTION_REMINDER"
        const val EXTRA_ID = "extra_id"
        const val EXTRA_KIND = "extra_kind"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_BODY = "extra_body"
        const val PERMISSION_REQUEST_CODE = 9901

        internal val currentActivityHolder = java.util.concurrent.atomic.AtomicReference<android.app.Activity?>()
        internal var pendingPermissionResult: CompletableDeferred<Boolean>? = null

        fun onPermissionResult(requestCode: Int, grantResults: IntArray) {
            if (requestCode != PERMISSION_REQUEST_CODE) return
            val granted = grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            pendingPermissionResult?.complete(granted)
            pendingPermissionResult = null
        }
    }
}
