package com.haodong.yimalaile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.domain.notifications.NotificationPrefs
import com.haodong.yimalaile.domain.notifications.NotificationService
import com.haodong.yimalaile.domain.settings.AppDarkMode
import com.haodong.yimalaile.domain.settings.SettingsRepository
import com.haodong.yimalaile.ui.navigation.DisclaimerRoute
import com.haodong.yimalaile.ui.navigation.HomeRoute
import com.haodong.yimalaile.ui.navigation.OnboardingRoute
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.notif_daily_body
import yimalaile.composeapp.generated.resources.notif_daily_title
import yimalaile.composeapp.generated.resources.notif_ovulation_body
import yimalaile.composeapp.generated.resources.notif_ovulation_title
import yimalaile.composeapp.generated.resources.notif_period_body
import yimalaile.composeapp.generated.resources.notif_period_title

class AppViewModel(
    private val service: MenstrualService,
    private val settings: SettingsRepository,
    private val notificationService: NotificationService? = null,
) : ViewModel() {

    var darkMode by mutableStateOf(AppDarkMode.SYSTEM)
        private set

    var language by mutableStateOf<String?>(null)
        private set

    var cycleLength by mutableIntStateOf(28)
        private set

    var periodDuration by mutableIntStateOf(5)
        private set

    var startRoute by mutableStateOf<Any?>(null)
        private set

    var notificationPrefs by mutableStateOf(NotificationPrefs())
        private set

    var notificationPermissionGranted by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            darkMode = settings.getDarkMode()
            language = settings.getLanguage()
            cycleLength = settings.getCycleLength()
            periodDuration = settings.getPeriodDuration()
            notificationPrefs = settings.getNotificationPrefs()
            notificationPermissionGranted = notificationService?.hasPermission() ?: false

            val disclaimerAccepted = settings.isDisclaimerAccepted()
            val state = service.getCycleState(cycleLength)
            val hasData = state.records.isNotEmpty()
            startRoute = when {
                !disclaimerAccepted -> DisclaimerRoute
                !hasData -> OnboardingRoute
                else -> HomeRoute
            }

            // Re-apply the stored schedule on app start so alarms survive
            // app relaunch and preference bundle migrations.
            applySchedule()
        }
    }

    fun updateDarkMode(mode: AppDarkMode) {
        viewModelScope.launch {
            darkMode = mode
            settings.setDarkMode(mode)
        }
    }

    fun updateLanguage(lang: String?) {
        viewModelScope.launch {
            language = lang
            settings.setLanguage(lang)
            // Reschedule so reminder copy follows the new language.
            applySchedule()
        }
    }

    fun updateCycleLength(length: Int) {
        viewModelScope.launch {
            cycleLength = length
            settings.setCycleLength(length)
            applySchedule()
        }
    }

    fun updatePeriodDuration(duration: Int) {
        viewModelScope.launch {
            periodDuration = duration
            settings.setPeriodDuration(duration)
        }
    }

    fun setDisclaimerAccepted(accepted: Boolean) {
        viewModelScope.launch {
            settings.setDisclaimerAccepted(accepted)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            service.clearAllData()
            settings.setDisclaimerAccepted(false)
            notificationService?.cancelAll()
        }
    }

    fun updateNotificationPrefs(prefs: NotificationPrefs) {
        viewModelScope.launch {
            val wasEnabled = notificationPrefs.periodReminderEnabled ||
                notificationPrefs.ovulationReminderEnabled ||
                notificationPrefs.dailyReportEnabled
            val isEnabled = prefs.periodReminderEnabled ||
                prefs.ovulationReminderEnabled ||
                prefs.dailyReportEnabled

            notificationPrefs = prefs
            settings.setNotificationPrefs(prefs)

            // If the user is turning a reminder ON for the first time and we
            // don't yet have OS-level permission, prompt them now. Without
            // this the alarm is scheduled silently and nothing ever shows up.
            if (!wasEnabled && isEnabled && !notificationPermissionGranted) {
                val granted = notificationService?.requestPermission() ?: false
                notificationPermissionGranted = granted
            }

            applySchedule()
        }
    }

    fun requestNotificationPermission() {
        viewModelScope.launch {
            val granted = notificationService?.requestPermission() ?: false
            notificationPermissionGranted = granted
            if (granted) applySchedule()
        }
    }

    /** Public trigger for rescheduling after cycle data changes. */
    fun rescheduleNotifications() {
        viewModelScope.launch { applySchedule() }
    }

    private suspend fun applySchedule() {
        val svc = notificationService ?: return
        val prefs = notificationPrefs
        val copy = NotificationService.Copy(
            periodTitle = getString(Res.string.notif_period_title),
            periodBody = getString(Res.string.notif_period_body, prefs.periodReminderDaysBefore),
            ovulationTitle = getString(Res.string.notif_ovulation_title),
            ovulationBody = getString(Res.string.notif_ovulation_body, prefs.ovulationReminderDaysBefore),
            dailyTitle = getString(Res.string.notif_daily_title),
            dailyBody = getString(Res.string.notif_daily_body),
        )
        svc.reschedule(prefs = prefs, cycleLength = cycleLength, copy = copy)
    }
}
