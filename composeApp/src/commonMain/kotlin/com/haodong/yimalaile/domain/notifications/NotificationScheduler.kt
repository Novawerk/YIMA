package com.haodong.yimalaile.domain.notifications

/**
 * Platform abstraction for delivering scheduled local notifications.
 *
 * Implementations:
 *  - Android: AlarmManager + BroadcastReceiver + NotificationManagerCompat
 *  - iOS:     UNUserNotificationCenter with UNCalendarNotificationTrigger
 *
 * Scheduling is idempotent per [ReminderKind] — calling [schedule] for a kind
 * that already has a pending reminder replaces the previous one.
 */
interface NotificationScheduler {

    /** Whether the user has granted notification permission at the OS level. */
    suspend fun hasPermission(): Boolean

    /**
     * Ask the OS for notification permission. On Android 13+ this shows the
     * system dialog; on iOS it shows the UNUserNotificationCenter prompt.
     * Returns true if permission is granted after the call.
     */
    suspend fun requestPermission(): Boolean

    /** Schedule (or replace) a reminder. Safe to call even if permission is denied — no-op in that case. */
    fun schedule(reminder: ScheduledReminder)

    /** Cancel any pending reminder for the given kind. */
    fun cancel(kind: ReminderKind)

    /** Cancel every reminder this app has scheduled. */
    fun cancelAll()
}
