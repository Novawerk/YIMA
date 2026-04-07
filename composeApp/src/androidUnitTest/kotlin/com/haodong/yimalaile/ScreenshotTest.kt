package com.haodong.yimalaile

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.captureRoboImage
import com.haodong.yimalaile.domain.menstrual.*
import com.haodong.yimalaile.domain.settings.AppDarkMode
import com.haodong.yimalaile.domain.settings.SettingsRepository
import com.haodong.yimalaile.fakes.FakeRecordsRepository
import com.haodong.yimalaile.fakes.createBeautifulTestData
import com.haodong.yimalaile.ui.locale.LocalAppLocale
import com.haodong.yimalaile.ui.pages.disclaimer.DisclaimerScreen
import com.haodong.yimalaile.ui.pages.home.HomeScreen
import com.haodong.yimalaile.ui.pages.settings.SettingsScreen
import com.haodong.yimalaile.ui.pages.sheet.LocalSheetViewModel
import com.haodong.yimalaile.ui.pages.sheet.SheetHost
import com.haodong.yimalaile.ui.pages.sheet.SheetViewModel
import com.haodong.yimalaile.ui.theme.AppTheme
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "w393dp-h851dp-xxhdpi")
class ScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ═══════════════════════════════════════════════════
    // Disclaimer Screen
    // ═══════════════════════════════════════════════════

    @Test
    fun disclaimer_en_light() = screenshotTest("en", AppDarkMode.LIGHT) {
        DisclaimerScreen(onAccept = {})
    }

    @Test
    fun disclaimer_en_dark() = screenshotTest("en", AppDarkMode.DARK) {
        DisclaimerScreen(onAccept = {})
    }

    @Test
    fun disclaimer_zh_light() = screenshotTest("zh", AppDarkMode.LIGHT) {
        DisclaimerScreen(onAccept = {})
    }

    @Test
    fun disclaimer_zh_dark() = screenshotTest("zh", AppDarkMode.DARK) {
        DisclaimerScreen(onAccept = {})
    }

    // ═══════════════════════════════════════════════════
    // Settings Screen
    // ═══════════════════════════════════════════════════

    @Test
    fun settings_en_light() = screenshotTest("en", AppDarkMode.LIGHT) {
        SettingsScreen(
            currentDarkMode = AppDarkMode.LIGHT,
            currentLanguage = "en",
            currentCycleLength = 28,
            currentPeriodDuration = 5,
            onDarkModeChange = {},
            onLanguageChange = {},
            onCycleLengthChange = {},
            onPeriodDurationChange = {},
            onBack = {},
            onClearData = {},
        )
    }

    @Test
    fun settings_en_dark() = screenshotTest("en", AppDarkMode.DARK) {
        SettingsScreen(
            currentDarkMode = AppDarkMode.DARK,
            currentLanguage = "en",
            currentCycleLength = 30,
            currentPeriodDuration = 6,
            onDarkModeChange = {},
            onLanguageChange = {},
            onCycleLengthChange = {},
            onPeriodDurationChange = {},
            onBack = {},
            onClearData = {},
        )
    }

    @Test
    fun settings_zh_light() = screenshotTest("zh", AppDarkMode.LIGHT) {
        SettingsScreen(
            currentDarkMode = AppDarkMode.LIGHT,
            currentLanguage = "zh",
            currentCycleLength = 28,
            currentPeriodDuration = 5,
            onDarkModeChange = {},
            onLanguageChange = {},
            onCycleLengthChange = {},
            onPeriodDurationChange = {},
            onBack = {},
            onClearData = {},
        )
    }

    @Test
    fun settings_zh_dark() = screenshotTest("zh", AppDarkMode.DARK) {
        SettingsScreen(
            currentDarkMode = AppDarkMode.DARK,
            currentLanguage = "zh",
            currentCycleLength = 30,
            currentPeriodDuration = 6,
            onDarkModeChange = {},
            onLanguageChange = {},
            onCycleLengthChange = {},
            onPeriodDurationChange = {},
            onBack = {},
            onClearData = {},
        )
    }

    // ═══════════════════════════════════════════════════
    // Home Screen (Calendar mode)
    // ═══════════════════════════════════════════════════

    @Test
    fun home_calendar_en_light() = homeScreenTest("en", AppDarkMode.LIGHT, "calendar")

    @Test
    fun home_calendar_en_dark() = homeScreenTest("en", AppDarkMode.DARK, "calendar")

    @Test
    fun home_calendar_zh_light() = homeScreenTest("zh", AppDarkMode.LIGHT, "calendar")

    @Test
    fun home_calendar_zh_dark() = homeScreenTest("zh", AppDarkMode.DARK, "calendar")

    // ═══════════════════════════════════════════════════
    // Home Screen (Statistics mode)
    // ═══════════════════════════════════════════════════

    @Test
    fun home_stats_en_light() = homeScreenTest("en", AppDarkMode.LIGHT, "stats")

    @Test
    fun home_stats_en_dark() = homeScreenTest("en", AppDarkMode.DARK, "stats")

    @Test
    fun home_stats_zh_light() = homeScreenTest("zh", AppDarkMode.LIGHT, "stats")

    @Test
    fun home_stats_zh_dark() = homeScreenTest("zh", AppDarkMode.DARK, "stats")

    // ═══════════════════════════════════════════════════
    // Home Screen (Detail mode)
    // ═══════════════════════════════════════════════════

    @Test
    fun home_detail_en_light() = homeScreenTest("en", AppDarkMode.LIGHT, "detail")

    @Test
    fun home_detail_en_dark() = homeScreenTest("en", AppDarkMode.DARK, "detail")

    @Test
    fun home_detail_zh_light() = homeScreenTest("zh", AppDarkMode.LIGHT, "detail")

    @Test
    fun home_detail_zh_dark() = homeScreenTest("zh", AppDarkMode.DARK, "detail")

    // ═══════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════

    private fun screenshotTest(
        locale: String,
        darkMode: AppDarkMode,
        content: @Composable () -> Unit,
    ) {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppLocale provides locale) {
                AppTheme(darkMode = darkMode) {
                    content()
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    private fun homeScreenTest(
        locale: String,
        darkMode: AppDarkMode,
        homeMode: String,
    ) {
        val testData = createBeautifulTestData()
        val fakeRepo = FakeRecordsRepository(testData.toMutableList())
        val service = MenstrualService(fakeRepo)
        val sheetViewModel = SheetViewModel(service)

        val context = ApplicationProvider.getApplicationContext<Application>()
        val dataStore = PreferenceDataStoreFactory.create {
            File(context.filesDir, "test_${System.nanoTime()}.preferences_pb")
        }
        val settings = SettingsRepository(dataStore)

        // Pre-set the home mode before HomeViewModel reads it
        runBlocking { settings.setHomeMode(homeMode) }

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalAppLocale provides locale,
                LocalSheetViewModel provides sheetViewModel,
            ) {
                AppTheme(darkMode = darkMode) {
                    HomeScreen(
                        service = service,
                        sheetViewModel = sheetViewModel,
                        settings = settings,
                        onNavigateSettings = {},
                    )
                    SheetHost(sheetViewModel)
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage()
    }
}
