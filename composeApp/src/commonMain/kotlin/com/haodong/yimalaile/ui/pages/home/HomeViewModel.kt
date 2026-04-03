package com.haodong.yimalaile.ui.pages.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haodong.yimalaile.domain.menstrual.CyclePhaseInfo
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class HomeViewModel(private val service: MenstrualService) : ViewModel() {

    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val state: StateFlow<HomeUiState> = _state

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val cycleState = service.getCycleState()
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val phaseInfo = service.getCurrentPhase(cycleState, today)
            _state.value = HomeUiState.Ready(cycleState, phaseInfo)
        }
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Ready(
        val cycleState: CycleState,
        val phaseInfo: CyclePhaseInfo?,
    ) : HomeUiState()
}
