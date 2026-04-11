package com.haodong.yimalaile.domain.notifications

data class NotificationPrefs(
    val periodReminderEnabled: Boolean = false,
    val periodReminderDaysBefore: Int = 2,
    val ovulationReminderEnabled: Boolean = false,
    val ovulationReminderDaysBefore: Int = 1,
    val dailyReportEnabled: Boolean = false,
    val dailyReportHour: Int = 9,
    val dailyReportMinute: Int = 0,
) {
    companion object {
        const val MIN_DAYS_BEFORE: Int = 1
        const val MAX_DAYS_BEFORE: Int = 7
    }
}
