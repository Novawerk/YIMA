package com.haodong.yimalaile.domain.notifications

/**
 * No-op implementation for previews, tests, and contexts where real
 * scheduling is undesirable (e.g. screenshot harnesses).
 */
class NoOpNotificationScheduler : NotificationScheduler {
    override suspend fun hasPermission(): Boolean = false
    override suspend fun requestPermission(): Boolean = false
    override fun schedule(reminder: ScheduledReminder) = Unit
    override fun cancel(kind: ReminderKind) = Unit
    override fun cancelAll() = Unit
}
