package com.haodong.yimalaile.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.domain.menstrual.RecordsRepository
import com.haodong.yimalaile.infrastructure.persistence.DataStoreRecordsRepository
import com.haodong.yimalaile.domain.settings.SettingsRepository
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.KmpComponentCreate
import me.tatarka.inject.annotations.Provides

@Component
abstract class AppComponent(
    @get:Provides protected val dataStore: DataStore<Preferences>
) {
    abstract val menstrualService: MenstrualService
    abstract val settingsRepository: SettingsRepository

    // Bind the interface to its @Inject-annotated implementation
    @Provides
    fun DataStoreRecordsRepository.bind(): RecordsRepository = this

    companion object
}

@KmpComponentCreate
expect fun AppComponent.Companion.create(
    dataStore: DataStore<Preferences>
): AppComponent
