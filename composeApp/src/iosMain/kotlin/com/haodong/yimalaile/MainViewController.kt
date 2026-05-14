package com.haodong.yimalaile

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.ComposeUIViewController
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.haodong.yimalaile.di.AppComponent
import com.haodong.yimalaile.di.create
import com.haodong.yimalaile.domain.export.IosReportExportService
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.domain.notifications.NotificationPrefs
import com.haodong.yimalaile.domain.settings.SettingsRepository
import com.haodong.yimalaile.domain.health.HealthAuthStatus
import com.haodong.yimalaile.notifications.IosNotificationScheduler
import com.viktormykhailiv.kmp.health.HealthManagerFactory
import com.viktormykhailiv.kmp.health.HealthManagerFactoryOptions
import com.haodong.yimalaile.fakes.FakeRecordsRepository
import com.haodong.yimalaile.fakes.createScreenshotTestData
import com.haodong.yimalaile.ui.locale.LocalAppLocale
import com.haodong.yimalaile.ui.navigation.DisclaimerRoute
import com.haodong.yimalaile.ui.navigation.HomeRoute
import com.haodong.yimalaile.ui.navigation.OnboardingRoute
import com.haodong.yimalaile.ui.navigation.SettingsRoute
import com.haodong.yimalaile.ui.pages.disclaimer.DisclaimerScreen
import com.haodong.yimalaile.ui.pages.home.HomeScreen
import com.haodong.yimalaile.ui.pages.onboarding.OnboardingScreen
import com.haodong.yimalaile.ui.pages.settings.NotificationSettingsScreen
import com.haodong.yimalaile.ui.pages.settings.SettingsScreen
import com.haodong.yimalaile.ui.pages.sheet.LocalSheetViewModel
import com.haodong.yimalaile.ui.pages.sheet.SheetHost
import com.haodong.yimalaile.ui.pages.sheet.SheetViewModel
import com.haodong.yimalaile.ui.theme.AppTheme
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath

private const val DATA_STORE_FILE_NAME = "yimalaile.preferences_pb"

// DataStore must be a process-wide singleton — opening two instances against the
// same file throws IllegalStateException. Lazy single-init survives any
// re-instantiation of the view controller (e.g. scene restoration).
private var sharedDataStore: DataStore<Preferences>? = null

private fun appDataStore(): DataStore<Preferences> {
    sharedDataStore?.let { return it }
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { dataStorePath(DATA_STORE_FILE_NAME).toPath() }
    ).also { sharedDataStore = it }
}

fun MainViewController() = ComposeUIViewController {
    val dataStore = appDataStore()
    val scheduler = IosNotificationScheduler()
    val reportExportService = IosReportExportService()
    val healthManager = HealthManagerFactory()
        .createManager(options = HealthManagerFactoryOptions.default())
    val component = AppComponent.create(dataStore, scheduler, reportExportService, healthManager)
    App(component)
}

/**
 * Entry point for iOS screenshot UI tests.
 * Pre-loads fake data and skips disclaimer so tests land directly on HomeScreen.
 */
fun ScreenshotMainViewController() = ComposeUIViewController {
    val dataStore = PreferenceDataStoreFactory.createWithPath(
        produceFile = { dataStorePath("screenshot_test.preferences_pb").toPath() }
    )
    val settings = SettingsRepository(dataStore)
    val fakeRepo = FakeRecordsRepository(createScreenshotTestData().toMutableList())
    val service = MenstrualService(fakeRepo)

    val scope = rememberCoroutineScope()
    val viewModel = remember {
        AppViewModel(service, settings).also {
            scope.launch {
                settings.setDisclaimerAccepted(true)
            }
        }
    }
    val sheetViewModel = remember { SheetViewModel(service) }

    val darkMode = viewModel.darkMode
    val language = viewModel.language

    CompositionLocalProvider(
        LocalAppLocale provides language,
        LocalSheetViewModel provides sheetViewModel,
    ) {
        key(language) {
            AppTheme(darkMode = darkMode) {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = HomeRoute) {
                    composable<HomeRoute> {
                        HomeScreen(
                            service = service,
                            sheetViewModel = sheetViewModel,
                            settings = settings,
                            onNavigateSettings = { navController.navigate(SettingsRoute) },
                        )
                    }
                    composable<SettingsRoute> {
                        SettingsScreen(
                            currentDarkMode = darkMode,
                            currentLanguage = language,
                            currentCycleLength = viewModel.cycleLength,
                            currentPeriodDuration = viewModel.periodDuration,
                            onDarkModeChange = { viewModel.updateDarkMode(it) },
                            onLanguageChange = { viewModel.updateLanguage(it) },
                            onCycleLengthChange = { viewModel.updateCycleLength(it) },
                            onPeriodDurationChange = { viewModel.updatePeriodDuration(it) },
                            onBack = { navController.popBackStack() },
                            onClearData = {},
                            healthSyncEnabled = true,
                            healthAuthStatus = HealthAuthStatus.AUTHORIZED,
                            healthLastSync = 1_712_400_000_000L,
                        )
                    }
                    composable<DisclaimerRoute> {
                        DisclaimerScreen(onAccept = {})
                    }
                }
                SheetHost(sheetViewModel)
            }
        }
    }
}

/**
 * Entry point for iOS screenshot tests — starts on the Disclaimer screen.
 */
fun ScreenshotDisclaimerViewController() = ComposeUIViewController {
    val dataStore = PreferenceDataStoreFactory.createWithPath(
        produceFile = { dataStorePath("screenshot_disclaimer.preferences_pb").toPath() }
    )
    val settings = SettingsRepository(dataStore)
    val fakeRepo = FakeRecordsRepository()
    val service = MenstrualService(fakeRepo)

    val viewModel = remember { AppViewModel(service, settings) }
    val darkMode = viewModel.darkMode
    val language = viewModel.language

    CompositionLocalProvider(LocalAppLocale provides language) {
        key(language) {
            AppTheme(darkMode = darkMode) {
                DisclaimerScreen(onAccept = {})
            }
        }
    }
}

/**
 * Entry point for iOS screenshot tests — renders the NotificationSettings screen
 * directly with sample preferences and permission granted.
 */
fun ScreenshotNotificationsViewController() = ComposeUIViewController {
    val dataStore = PreferenceDataStoreFactory.createWithPath(
        produceFile = { dataStorePath("screenshot_notifications.preferences_pb").toPath() }
    )
    val settings = SettingsRepository(dataStore)
    val fakeRepo = FakeRecordsRepository()
    val service = MenstrualService(fakeRepo)

    val viewModel = remember { AppViewModel(service, settings) }
    val darkMode = viewModel.darkMode
    val language = viewModel.language

    val samplePrefs = NotificationPrefs(
        periodReminderEnabled = true,
        periodReminderDaysBefore = 2,
        ovulationReminderEnabled = true,
        ovulationReminderDaysBefore = 1,
        dailyReportEnabled = true,
        dailyReportHour = 9,
        dailyReportMinute = 0,
    )

    CompositionLocalProvider(LocalAppLocale provides language) {
        key(language) {
            AppTheme(darkMode = darkMode) {
                NotificationSettingsScreen(
                    prefs = samplePrefs,
                    hasPermission = true,
                    onRequestPermission = {},
                    onPrefsChange = {},
                    onBack = {},
                )
            }
        }
    }
}

/**
 * Entry point for iOS screenshot tests — starts on the Onboarding screen.
 */
fun ScreenshotOnboardingViewController() = ComposeUIViewController {
    val dataStore = PreferenceDataStoreFactory.createWithPath(
        produceFile = { dataStorePath("screenshot_onboarding.preferences_pb").toPath() }
    )
    val settings = SettingsRepository(dataStore)
    val fakeRepo = FakeRecordsRepository()
    val service = MenstrualService(fakeRepo)

    val viewModel = remember { AppViewModel(service, settings) }
    val darkMode = viewModel.darkMode
    val language = viewModel.language

    CompositionLocalProvider(LocalAppLocale provides language) {
        key(language) {
            AppTheme(darkMode = darkMode) {
                OnboardingScreen(
                    service = service,
                    settings = settings,
                    healthSyncManager = null,
                    notificationPrefs = NotificationPrefs(),
                    notificationPermissionGranted = false,
                    onUpdateNotificationPrefs = {},
                    onRequestNotificationPermission = {},
                    onComplete = {},
                )
            }
        }
    }
}
