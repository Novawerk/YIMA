package com.haodong.yimalaile.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.haodong.yimalaile.domain.export.ReportExportService
import com.haodong.yimalaile.domain.health.HealthSyncManager
import com.haodong.yimalaile.domain.menstrual.DailyNoteRepository
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.domain.menstrual.RecordsRepository
import com.haodong.yimalaile.domain.notifications.NotificationScheduler
import com.haodong.yimalaile.domain.notifications.NotificationService
import com.haodong.yimalaile.domain.settings.SettingsRepository
import com.haodong.yimalaile.infrastructure.persistence.DataStoreDailyNoteRepository
import com.haodong.yimalaile.infrastructure.persistence.DataStoreRecordsRepository
import com.viktormykhailiv.kmp.health.HealthManager
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.KmpComponentCreate
import me.tatarka.inject.annotations.Provides

@Component
abstract class AppComponent(
    @get:Provides protected val dataStore: DataStore<Preferences>,
    @get:Provides protected val notificationScheduler: NotificationScheduler,
    @get:Provides val reportExportService: ReportExportService,
    @get:Provides protected val healthManager: HealthManager,
) {
    abstract val menstrualService: MenstrualService
    abstract val settingsRepository: SettingsRepository
    abstract val dailyNoteRepository: DailyNoteRepository
    abstract val healthSyncManager: HealthSyncManager

    val notificationService: NotificationService by lazy {
        NotificationService(menstrualService, notificationScheduler)
    }

    @Provides
    fun DataStoreRecordsRepository.bind(): RecordsRepository = this

    @Provides
    fun DataStoreDailyNoteRepository.bind(): DailyNoteRepository = this

    companion object
}

@KmpComponentCreate
expect fun AppComponent.Companion.create(
    dataStore: DataStore<Preferences>,
    notificationScheduler: NotificationScheduler,
    reportExportService: ReportExportService,
    healthManager: HealthManager,
): AppComponent
