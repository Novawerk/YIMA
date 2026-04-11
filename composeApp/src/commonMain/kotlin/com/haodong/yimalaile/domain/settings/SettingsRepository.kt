package com.haodong.yimalaile.domain.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.haodong.yimalaile.domain.notifications.NotificationPrefs
import kotlinx.coroutines.flow.first
import me.tatarka.inject.annotations.Inject

private val DISCLAIMER_ACCEPTED = booleanPreferencesKey("disclaimer_accepted")
private val DARK_MODE = stringPreferencesKey("dark_mode")
private val LANGUAGE = stringPreferencesKey("language")
private val HOME_MODE = stringPreferencesKey("home_mode")
private val CYCLE_LENGTH = stringPreferencesKey("cycle_length")
private val PERIOD_DURATION = stringPreferencesKey("period_duration")

// Notifications
private val NOTIF_PERIOD_ENABLED = booleanPreferencesKey("notif_period_enabled")
private val NOTIF_PERIOD_DAYS = intPreferencesKey("notif_period_days")
private val NOTIF_OVULATION_ENABLED = booleanPreferencesKey("notif_ovulation_enabled")
private val NOTIF_OVULATION_DAYS = intPreferencesKey("notif_ovulation_days")
private val NOTIF_DAILY_ENABLED = booleanPreferencesKey("notif_daily_enabled")
private val NOTIF_DAILY_HOUR = intPreferencesKey("notif_daily_hour")
private val NOTIF_DAILY_MINUTE = intPreferencesKey("notif_daily_minute")

@Inject
open class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    suspend fun isDisclaimerAccepted(): Boolean =
        dataStore.data.first()[DISCLAIMER_ACCEPTED] ?: false

    suspend fun setDisclaimerAccepted(value: Boolean) {
        dataStore.edit { prefs -> prefs[DISCLAIMER_ACCEPTED] = value }
    }

    suspend fun getDarkMode(): AppDarkMode =
        AppDarkMode.fromValue(dataStore.data.first()[DARK_MODE])

    suspend fun setDarkMode(value: AppDarkMode) {
        dataStore.edit { prefs -> prefs[DARK_MODE] = value.value }
    }

    suspend fun getLanguage(): String? =
        dataStore.data.first()[LANGUAGE]

    suspend fun setLanguage(value: String?) {
        dataStore.edit { prefs ->
            if (value == null) prefs.remove(LANGUAGE)
            else prefs[LANGUAGE] = value
        }
    }

    suspend fun getHomeMode(): String =
        dataStore.data.first()[HOME_MODE] ?: "calendar"

    suspend fun setHomeMode(value: String) {
        dataStore.edit { prefs -> prefs[HOME_MODE] = value }
    }

    suspend fun getCycleLength(): Int =
        dataStore.data.first()[CYCLE_LENGTH]?.toIntOrNull() ?: 28

    suspend fun setCycleLength(value: Int) {
        dataStore.edit { prefs -> prefs[CYCLE_LENGTH] = value.toString() }
    }

    suspend fun getPeriodDuration(): Int =
        dataStore.data.first()[PERIOD_DURATION]?.toIntOrNull() ?: 5

    suspend fun setPeriodDuration(value: Int) {
        dataStore.edit { prefs -> prefs[PERIOD_DURATION] = value.toString() }
    }

    // ---------- Notification preferences ----------

    suspend fun getNotificationPrefs(): NotificationPrefs {
        val data = dataStore.data.first()
        val defaults = NotificationPrefs()
        return NotificationPrefs(
            periodReminderEnabled = data[NOTIF_PERIOD_ENABLED] ?: defaults.periodReminderEnabled,
            periodReminderDaysBefore = data[NOTIF_PERIOD_DAYS] ?: defaults.periodReminderDaysBefore,
            ovulationReminderEnabled = data[NOTIF_OVULATION_ENABLED] ?: defaults.ovulationReminderEnabled,
            ovulationReminderDaysBefore = data[NOTIF_OVULATION_DAYS] ?: defaults.ovulationReminderDaysBefore,
            dailyReportEnabled = data[NOTIF_DAILY_ENABLED] ?: defaults.dailyReportEnabled,
            dailyReportHour = data[NOTIF_DAILY_HOUR] ?: defaults.dailyReportHour,
            dailyReportMinute = data[NOTIF_DAILY_MINUTE] ?: defaults.dailyReportMinute,
        )
    }

    suspend fun setNotificationPrefs(value: NotificationPrefs) {
        dataStore.edit { prefs ->
            prefs[NOTIF_PERIOD_ENABLED] = value.periodReminderEnabled
            prefs[NOTIF_PERIOD_DAYS] = value.periodReminderDaysBefore
            prefs[NOTIF_OVULATION_ENABLED] = value.ovulationReminderEnabled
            prefs[NOTIF_OVULATION_DAYS] = value.ovulationReminderDaysBefore
            prefs[NOTIF_DAILY_ENABLED] = value.dailyReportEnabled
            prefs[NOTIF_DAILY_HOUR] = value.dailyReportHour
            prefs[NOTIF_DAILY_MINUTE] = value.dailyReportMinute
        }
    }
}
