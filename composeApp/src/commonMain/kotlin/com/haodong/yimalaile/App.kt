package com.haodong.yimalaile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.haodong.yimalaile.di.AppComponent
import com.haodong.yimalaile.ui.disclaimer.DisclaimerScreen
import com.haodong.yimalaile.ui.home.HomeScreen
import com.haodong.yimalaile.ui.navigation.DisclaimerRoute
import com.haodong.yimalaile.ui.navigation.HomeRoute
import com.haodong.yimalaile.ui.navigation.OnboardingRoute
import com.haodong.yimalaile.ui.navigation.SettingsRoute
import com.haodong.yimalaile.ui.navigation.StatisticsRoute
import com.haodong.yimalaile.ui.onboarding.OnboardingScreen
import com.haodong.yimalaile.ui.settings.SettingsScreen
import com.haodong.yimalaile.ui.statistics.StatisticsScreen
import com.haodong.yimalaile.ui.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun App(component: AppComponent) {
    val service = component.menstrualService
    val settings = component.settingsRepository
    val scope = rememberCoroutineScope()

    // Theme state
    var palette by remember { mutableStateOf("warm") }
    var darkMode by remember { mutableStateOf("system") }

    // Startup: load settings + determine start route
    var startRoute by remember { mutableStateOf<Any?>(null) }

    LaunchedEffect(Unit) {
        palette = settings.getColorPalette()
        darkMode = settings.getDarkMode()

        val disclaimerAccepted = settings.isDisclaimerAccepted()
        val state = service.getCycleState()
        val hasData = state.recentPeriods.isNotEmpty() || state.activePeriod != null
        startRoute = when {
            !disclaimerAccepted -> DisclaimerRoute
            !hasData -> OnboardingRoute
            else -> HomeRoute
        }
    }

    val route = startRoute ?: return

    AppTheme(palette = palette, darkMode = darkMode) {
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
                    onNavigateStatistics = { navController.navigate(StatisticsRoute) },
                    onNavigateSettings = { navController.navigate(SettingsRoute) },
                )
            }

            composable<StatisticsRoute> {
                StatisticsScreen(
                    service = service,
                    onBack = { navController.popBackStack() },
                )
            }

            composable<SettingsRoute> {
                SettingsScreen(
                    currentPalette = palette,
                    currentDarkMode = darkMode,
                    onPaletteChange = { newPalette ->
                        palette = newPalette
                        scope.launch { settings.setColorPalette(newPalette) }
                    },
                    onDarkModeChange = { newMode ->
                        darkMode = newMode
                        scope.launch { settings.setDarkMode(newMode) }
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
    }
}
