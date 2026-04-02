package com.haodong.yimalaile.domain.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import me.tatarka.inject.annotations.Inject

private val DISCLAIMER_ACCEPTED = booleanPreferencesKey("disclaimer_accepted")

@Inject
open class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    suspend fun isDisclaimerAccepted(): Boolean =
        dataStore.data.first()[DISCLAIMER_ACCEPTED] ?: false

    suspend fun setDisclaimerAccepted(value: Boolean) {
        dataStore.edit { prefs -> prefs[DISCLAIMER_ACCEPTED] = value }
    }
}
