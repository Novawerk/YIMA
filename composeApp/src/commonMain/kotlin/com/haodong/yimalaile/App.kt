package com.haodong.yimalaile

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.haodong.yimalaile.di.AppComponent
import com.haodong.yimalaile.ui.locale.LocalAppLocale
import com.haodong.yimalaile.ui.navigation.*
import com.haodong.yimalaile.ui.pages.disclaimer.DisclaimerScreen
import com.haodong.yimalaile.ui.pages.home.HomeScreen
import com.haodong.yimalaile.ui.pages.onboarding.OnboardingScreen
import com.haodong.yimalaile.ui.pages.settings.SettingsScreen
import com.haodong.yimalaile.ui.pages.sheet.SheetHost
import com.haodong.yimalaile.ui.pages.sheet.SheetManager
import com.haodong.yimalaile.ui.pages.statistics.StatisticsScreen
import com.haodong.yimalaile.ui.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun App(component: AppComponent) {
    val service = component.menstrualService
    val settings = component.settingsRepository
    val scope = rememberCoroutineScope()
    val sheetManager = remember { SheetManager(service) }

    // Theme & language state
    var darkMode by remember { mutableStateOf("system") }
    var language by remember { mutableStateOf<String?>(null) }

    // Startup: load settings + determine start route
    var startRoute by remember { mutableStateOf<Any?>(null) }

    LaunchedEffect(Unit) {
        darkMode = settings.getDarkMode()
        language = settings.getLanguage()

        val disclaimerAccepted = settings.isDisclaimerAccepted()
        val state = service.getCycleState()
        val hasData = state.records.isNotEmpty()
        startRoute = when {
            !disclaimerAccepted -> DisclaimerRoute
            !hasData -> OnboardingRoute
            else -> HomeRoute
        }
    }

    val route = startRoute ?: return

    CompositionLocalProvider(LocalAppLocale provides language) {
        key(language) {
            AppTheme(darkMode = darkMode) {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = route) {
                    composable<DisclaimerRoute> {
                        DisclaimerScreen(onAccept = {
                            scope.launch { settings.setDisclaimerAccepted(true) }
                            navController.navigate(OnboardingRoute) {
                                popUpTo(DisclaimerRoute) { inclusive = true }
                            }
                        })
                    }

                    composable<OnboardingRoute> {
                        OnboardingScreen(
                            service = service,
                            onComplete = {
                                navController.navigate(HomeRoute) {
                                    popUpTo(OnboardingRoute) { inclusive = true }
                                }
                            },
                        )
                    }

                    composable<HomeRoute> {
                        HomeScreen(
                            service = service,
                            sheetManager = sheetManager,
                            settings = settings,
                            onNavigateStatistics = { navController.navigate(StatisticsRoute) },
                            onNavigateSettings = { navController.navigate(SettingsRoute) },
                        )
                    }

                    composable<StatisticsRoute> {
                        StatisticsScreen(
                            service = service,
                            sheetManager = sheetManager,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable<SettingsRoute> {
                        SettingsScreen(
                            currentDarkMode = darkMode,
                            currentLanguage = language,
                            onDarkModeChange = { newMode ->
                                darkMode = newMode
                                scope.launch { settings.setDarkMode(newMode) }
                            },
                            onLanguageChange = { newLang ->
                                language = newLang
                                scope.launch { settings.setLanguage(newLang) }
                            },
                            onBack = { navController.popBackStack() },
                            onClearData = {
                                scope.launch {
                                    service.clearAllData()
                                    settings.setDisclaimerAccepted(false)
                                }
                                navController.navigate(DisclaimerRoute) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                        )
                    }
                }

                // Global sheet host — renders active sheet from SheetManager
                SheetHost(sheetManager)
            }
        }
    }
}
