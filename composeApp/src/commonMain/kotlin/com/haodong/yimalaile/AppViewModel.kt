package com.haodong.yimalaile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.domain.settings.AppDarkMode
import com.haodong.yimalaile.domain.settings.SettingsRepository
import com.haodong.yimalaile.ui.navigation.DisclaimerRoute
import com.haodong.yimalaile.ui.navigation.HomeRoute
import com.haodong.yimalaile.ui.navigation.OnboardingRoute
import kotlinx.coroutines.launch

class AppViewModel(
    private val service: MenstrualService,
    private val settings: SettingsRepository,
) : ViewModel() {

    var darkMode by mutableStateOf(AppDarkMode.SYSTEM)
        private set

    var language by mutableStateOf<String?>(null)
        private set

    var cycleLength by mutableIntStateOf(28)
        private set

    var periodDuration by mutableIntStateOf(5)
        private set

    var startRoute by mutableStateOf<Any?>(null)
        private set

    init {
        viewModelScope.launch {
            darkMode = settings.getDarkMode()
            language = settings.getLanguage()
            cycleLength = settings.getCycleLength()
            periodDuration = settings.getPeriodDuration()

            val disclaimerAccepted = settings.isDisclaimerAccepted()
            val state = service.getCycleState(cycleLength)
            val hasData = state.records.isNotEmpty()
            startRoute = when {
                !disclaimerAccepted -> DisclaimerRoute
                !hasData -> OnboardingRoute
                else -> HomeRoute
            }
        }
    }

    fun updateDarkMode(mode: AppDarkMode) {
        viewModelScope.launch {
            darkMode = mode
            settings.setDarkMode(mode)
        }
    }

    fun updateLanguage(lang: String?) {
        viewModelScope.launch {
            language = lang
            settings.setLanguage(lang)
        }
    }

    fun updateCycleLength(length: Int) {
        viewModelScope.launch {
            cycleLength = length
            settings.setCycleLength(length)
        }
    }

    fun updatePeriodDuration(duration: Int) {
        viewModelScope.launch {
            periodDuration = duration
            settings.setPeriodDuration(duration)
        }
    }

    fun setDisclaimerAccepted(accepted: Boolean) {
        viewModelScope.launch {
            settings.setDisclaimerAccepted(accepted)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            service.clearAllData()
            settings.setDisclaimerAccepted(false)
        }
    }
}
