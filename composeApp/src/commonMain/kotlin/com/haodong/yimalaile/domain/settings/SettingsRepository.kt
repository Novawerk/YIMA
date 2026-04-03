package com.haodong.yimalaile.domain.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import me.tatarka.inject.annotations.Inject

private val DISCLAIMER_ACCEPTED = booleanPreferencesKey("disclaimer_accepted")
private val DARK_MODE = stringPreferencesKey("dark_mode")
private val LANGUAGE = stringPreferencesKey("language")

@Inject
open class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    suspend fun isDisclaimerAccepted(): Boolean =
        dataStore.data.first()[DISCLAIMER_ACCEPTED] ?: false

    suspend fun setDisclaimerAccepted(value: Boolean) {
        dataStore.edit { prefs -> prefs[DISCLAIMER_ACCEPTED] = value }
    }

    suspend fun getDarkMode(): String =
        dataStore.data.first()[DARK_MODE] ?: "system"

    suspend fun setDarkMode(value: String) {
        dataStore.edit { prefs -> prefs[DARK_MODE] = value }
    }

    suspend fun getLanguage(): String? =
        dataStore.data.first()[LANGUAGE]

    suspend fun setLanguage(value: String?) {
        dataStore.edit { prefs ->
            if (value == null) prefs.remove(LANGUAGE)
            else prefs[LANGUAGE] = value
        }
    }
}
