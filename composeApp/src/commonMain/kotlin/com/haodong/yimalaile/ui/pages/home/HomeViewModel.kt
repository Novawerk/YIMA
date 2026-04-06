package com.haodong.yimalaile.ui.pages.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haodong.yimalaile.domain.menstrual.CyclePhaseInfo
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.domain.settings.SettingsRepository
import kotlin.time.Clock
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class HomeViewModel(
    private val service: MenstrualService,
    private val settings: SettingsRepository,
) : ViewModel() {

    var cycleState by mutableStateOf<CycleState?>(null)
        private set
    
    var phaseInfo by mutableStateOf<CyclePhaseInfo?>(null)
        private set

    var isLoading by mutableStateOf(true)
        private set
    
    var homeMode by mutableStateOf(HomeMode.CALENDAR)
        private set

    init {
        viewModelScope.launch {
            homeMode = HomeMode.fromKey(settings.getHomeMode())
            refresh()
        }
    }

    fun updateHomeMode(mode: HomeMode) {
        viewModelScope.launch {
            homeMode = mode
            settings.setHomeMode(mode.key)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            isLoading = true
            val cycleLen = settings.getCycleLength()
            val state = service.getCycleState(cycleLen)
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            
            phaseInfo = CyclePhaseInfo.getPhaseInfo(today, state, cycleLen)
            cycleState = state
            isLoading = false
        }
    }
}
