package com.haodong.yimalaile.domain.notifications

import kotlinx.datetime.LocalDateTime

/**
 * A single scheduled reminder to be delivered by the platform notifier.
 *
 * Identity is stable across reschedules: the [id] is derived from [kind] so that
 * rescheduling replaces the previous pending alarm for the same slot.
 */
data class ScheduledReminder(
    val id: Int,
    val kind: ReminderKind,
    val title: String,
    val body: String,
    val fireAt: LocalDateTime,
    val repeatDaily: Boolean = false,
)

enum class ReminderKind(val stableId: Int) {
    PERIOD(1001),
    OVULATION(1002),
    DAILY_REPORT(1003),
}
