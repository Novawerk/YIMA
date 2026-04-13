package com.haodong.yimalaile

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.haodong.yimalaile.di.AppComponent
import com.haodong.yimalaile.ui.locale.LocalAppLocale
import com.haodong.yimalaile.ui.navigation.DisclaimerRoute
import com.haodong.yimalaile.ui.navigation.HomeRoute
import com.haodong.yimalaile.ui.navigation.NotificationSettingsRoute
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

@Composable
fun App(component: AppComponent) {
    val service = component.menstrualService
    val settings = component.settingsRepository
    val notificationService = component.notificationService
    val reportExportService = component.reportExportService
    val healthSyncManager = component.healthSyncManager

    val viewModel = remember {
        AppViewModel(service, settings, notificationService, reportExportService, healthSyncManager)
    }
    val sheetViewModel = remember { SheetViewModel(service) }

    val darkMode = viewModel.darkMode
    val language = viewModel.language
    val cycleLength = viewModel.cycleLength
    val periodDuration = viewModel.periodDuration
    val startRoute = viewModel.startRoute ?: return

    CompositionLocalProvider(
        LocalAppLocale provides language,
        LocalSheetViewModel provides sheetViewModel,
    ) {
        key(language) {
            AppTheme(darkMode = darkMode) {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = startRoute) {
                    composable<DisclaimerRoute> {
                        DisclaimerScreen(onAccept = {
                            viewModel.setDisclaimerAccepted(true)
                            navController.navigate(OnboardingRoute) {
                                popUpTo(DisclaimerRoute) { inclusive = true }
                            }
                        })
                    }

                    composable<OnboardingRoute> {
                        OnboardingScreen(
                            service = service,
                            settings = settings,
                            onComplete = {
                                viewModel.rescheduleNotifications()
                                navController.navigate(HomeRoute) {
                                    popUpTo(OnboardingRoute) { inclusive = true }
                                }
                            },
                        )
                    }

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
                            currentCycleLength = cycleLength,
                            currentPeriodDuration = periodDuration,
                            onDarkModeChange = { newMode -> viewModel.updateDarkMode(newMode) },
                            onLanguageChange = { newLang -> viewModel.updateLanguage(newLang) },
                            onCycleLengthChange = { newLen -> viewModel.updateCycleLength(newLen) },
                            onPeriodDurationChange = { newDur -> viewModel.updatePeriodDuration(newDur) },
                            onBack = { navController.popBackStack() },
                            onClearData = {
                                viewModel.clearAllData()
                                navController.navigate(DisclaimerRoute) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onNavigateNotifications = { navController.navigate(NotificationSettingsRoute) },
                            exportStatus = viewModel.exportStatus,
                            onExport = { lang -> viewModel.exportReport(lang) },
                            onResetExportStatus = { viewModel.resetExportStatus() },
                            healthSyncEnabled = viewModel.healthSyncEnabled,
                            healthAuthStatus = viewModel.healthAuthStatus,
                            healthLastSync = viewModel.healthLastSync,
                            healthSyncInProgress = viewModel.healthSyncInProgress,
                            onToggleHealthSync = { viewModel.toggleHealthSync(it) },
                            onSyncHealthNow = { viewModel.syncHealth() },
                        )
                    }

                    composable<NotificationSettingsRoute> {
                        NotificationSettingsScreen(
                            prefs = viewModel.notificationPrefs,
                            hasPermission = viewModel.notificationPermissionGranted,
                            onRequestPermission = { viewModel.requestNotificationPermission() },
                            onPrefsChange = { viewModel.updateNotificationPrefs(it) },
                            onBack = { navController.popBackStack() },
                        )
                    }
                }

                // Global sheet host — renders active sheet from SheetViewModel
                SheetHost(sheetViewModel)
            }
        }
    }
}
