package com.haodong.yimalaile.notifications

import com.haodong.yimalaile.domain.notifications.NotificationScheduler
import com.haodong.yimalaile.domain.notifications.ReminderKind
import com.haodong.yimalaile.domain.notifications.ScheduledReminder
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSettings
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

/**
 * iOS scheduler backed by [UNUserNotificationCenter] with
 * [UNCalendarNotificationTrigger]. Scheduling is stateless — identifiers are
 * derived from [ReminderKind] so rescheduling replaces any existing request.
 */
@OptIn(ExperimentalForeignApi::class)
class IosNotificationScheduler : NotificationScheduler {

    private val center get() = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun hasPermission(): Boolean =
        suspendCancellableCoroutine { cont ->
            center.getNotificationSettingsWithCompletionHandler { settings: UNNotificationSettings? ->
                val status = settings?.authorizationStatus
                val granted = status == UNAuthorizationStatusAuthorized ||
                    status == UNAuthorizationStatusProvisional
                cont.resume(granted)
            }
        }

    override suspend fun requestPermission(): Boolean =
        suspendCancellableCoroutine { cont ->
            val options = UNAuthorizationOptionAlert or
                UNAuthorizationOptionSound or
                UNAuthorizationOptionBadge
            center.requestAuthorizationWithOptions(options) { granted, _ ->
                cont.resume(granted)
            }
        }

    override fun schedule(reminder: ScheduledReminder) {
        // Cancel any previous request with the same identifier before adding.
        val identifier = identifierFor(reminder.kind)
        center.removePendingNotificationRequestsWithIdentifiers(listOf(identifier))

        val content = UNMutableNotificationContent().apply {
            setTitle(reminder.title)
            setBody(reminder.body)
        }

        val components = NSDateComponents().apply {
            setHour(reminder.fireAt.hour.toLong())
            setMinute(reminder.fireAt.minute.toLong())
            if (!reminder.repeatDaily) {
                setYear(reminder.fireAt.year.toLong())
                @Suppress("DEPRECATION")
                setMonth(reminder.fireAt.monthNumber.toLong())
                @Suppress("DEPRECATION")
                setDay(reminder.fireAt.dayOfMonth.toLong())
            }
        }

        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            dateComponents = components,
            repeats = reminder.repeatDaily,
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = identifier,
            content = content,
            trigger = trigger,
        )

        center.addNotificationRequest(request) { _ -> }
    }

    override fun cancel(kind: ReminderKind) {
        val id = identifierFor(kind)
        center.removePendingNotificationRequestsWithIdentifiers(listOf(id))
        center.removeDeliveredNotificationsWithIdentifiers(listOf(id))
    }

    override fun cancelAll() {
        center.removeAllPendingNotificationRequests()
        center.removeAllDeliveredNotifications()
    }

    private fun identifierFor(kind: ReminderKind): String = "yimalaile.${kind.name}"
}
