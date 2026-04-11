package com.haodong.yimalaile.domain.notifications

import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.domain.menstrual.PredictedCycle
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * Computes upcoming reminders from cycle data + preferences and pushes them
 * to the platform scheduler. Call [reschedule] whenever:
 *   - the app starts
 *   - notification preferences change
 *   - menstrual records or predictions change
 */
class NotificationService(
    private val menstrualService: MenstrualService,
    private val scheduler: NotificationScheduler,
) {

    /**
     * Pre-resolved localized copy for every reminder. The caller (AppViewModel)
     * knows the `days before` values at the moment it schedules, so it can
     * format the strings and pass them here — this keeps the domain layer
     * free of Compose Resources.
     */
    data class Copy(
        val periodTitle: String,
        val periodBody: String,
        val ovulationTitle: String,
        val ovulationBody: String,
        val dailyTitle: String,
        val dailyBody: String,
    )

    /** Recompute and apply the schedule for all enabled reminders. */
    suspend fun reschedule(
        prefs: NotificationPrefs,
        cycleLength: Int,
        copy: Copy,
    ) {
        // If the whole feature is off, clear and bail.
        if (!prefs.periodReminderEnabled &&
            !prefs.ovulationReminderEnabled &&
            !prefs.dailyReportEnabled
        ) {
            scheduler.cancel(ReminderKind.PERIOD)
            scheduler.cancel(ReminderKind.OVULATION)
            scheduler.cancel(ReminderKind.DAILY_REPORT)
            return
        }

        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.todayIn(tz)

        // ---------- Daily report (cycle-independent) ----------
        if (prefs.dailyReportEnabled) {
            val fireAt = nextDailyFireAt(today, prefs.dailyReportHour, prefs.dailyReportMinute)
            scheduler.schedule(
                ScheduledReminder(
                    id = ReminderKind.DAILY_REPORT.stableId,
                    kind = ReminderKind.DAILY_REPORT,
                    title = copy.dailyTitle,
                    body = copy.dailyBody,
                    fireAt = fireAt,
                    repeatDaily = true,
                )
            )
        } else {
            scheduler.cancel(ReminderKind.DAILY_REPORT)
        }

        // ---------- Cycle-dependent reminders ----------
        if (prefs.periodReminderEnabled || prefs.ovulationReminderEnabled) {
            val predictions = menstrualService.predictNextCycles(
                count = 6,
                cycleLength = cycleLength,
            )

            if (prefs.periodReminderEnabled) {
                val fire = nextReminderDate(
                    predictions = predictions,
                    daysBefore = prefs.periodReminderDaysBefore,
                    today = today,
                    anchor = ReminderAnchor.PERIOD_START,
                )
                if (fire != null) {
                    scheduler.schedule(
                        ScheduledReminder(
                            id = ReminderKind.PERIOD.stableId,
                            kind = ReminderKind.PERIOD,
                            title = copy.periodTitle,
                            body = copy.periodBody,
                            fireAt = fire.atTime(LocalTime(9, 0)),
                        )
                    )
                } else {
                    scheduler.cancel(ReminderKind.PERIOD)
                }
            } else {
                scheduler.cancel(ReminderKind.PERIOD)
            }

            if (prefs.ovulationReminderEnabled) {
                val fire = nextReminderDate(
                    predictions = predictions,
                    daysBefore = prefs.ovulationReminderDaysBefore,
                    today = today,
                    anchor = ReminderAnchor.OVULATION,
                )
                if (fire != null) {
                    scheduler.schedule(
                        ScheduledReminder(
                            id = ReminderKind.OVULATION.stableId,
                            kind = ReminderKind.OVULATION,
                            title = copy.ovulationTitle,
                            body = copy.ovulationBody,
                            fireAt = fire.atTime(LocalTime(9, 0)),
                        )
                    )
                } else {
                    scheduler.cancel(ReminderKind.OVULATION)
                }
            } else {
                scheduler.cancel(ReminderKind.OVULATION)
            }
        } else {
            scheduler.cancel(ReminderKind.PERIOD)
            scheduler.cancel(ReminderKind.OVULATION)
        }
    }

    suspend fun hasPermission(): Boolean = scheduler.hasPermission()

    suspend fun requestPermission(): Boolean = scheduler.requestPermission()

    fun cancelAll() {
        scheduler.cancelAll()
    }

    // ---------- helpers ----------

    private enum class ReminderAnchor { PERIOD_START, OVULATION }

    private fun nextDailyFireAt(today: LocalDate, hour: Int, minute: Int): LocalDateTime {
        val tz = TimeZone.currentSystemDefault()
        val now = Clock.System.now().toLocalDateTime(tz)
        val candidateToday = today.atTime(LocalTime(hour, minute))
        return if (candidateToday > now) candidateToday
        else today.plus(1, DateTimeUnit.DAY).atTime(LocalTime(hour, minute))
    }

    private fun nextReminderDate(
        predictions: List<PredictedCycle>,
        daysBefore: Int,
        today: LocalDate,
        anchor: ReminderAnchor,
    ): LocalDate? {
        for (p in predictions) {
            val anchorDate = when (anchor) {
                ReminderAnchor.PERIOD_START -> p.predictedStart
                // Ovulation ≈ 14 days before the next period start (luteal phase).
                ReminderAnchor.OVULATION -> p.predictedStart.plus(-14, DateTimeUnit.DAY)
            }
            val fire = anchorDate.plus(-daysBefore, DateTimeUnit.DAY)
            if (fire >= today) return fire
        }
        return null
    }
}
