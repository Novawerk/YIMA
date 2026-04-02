package com.haodong.yimalaile.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haodong.yimalaile.domain.menstrual.AddRecordResult
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class HomeViewModel(private val service: MenstrualService) : ViewModel() {

    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val state: StateFlow<HomeUiState> = _state

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = HomeUiState.Ready(service.getCycleState())
        }
    }

    fun startPeriod(date: LocalDate, onResult: (AddRecordResult) -> Unit) {
        viewModelScope.launch {
            val result = service.startPeriod(date)
            onResult(result)
            if (result is AddRecordResult.Success) refresh()
        }
    }

    fun endPeriod(endDate: LocalDate, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val current = (state.value as? HomeUiState.Ready)?.cycleState?.activePeriod ?: return@launch
            val ok = service.endPeriod(current.id, endDate)
            onResult(ok)
            if (ok) refresh()
        }
    }

    fun logDay(
        date: LocalDate,
        intensity: com.haodong.yimalaile.domain.menstrual.Intensity?,
        mood: com.haodong.yimalaile.domain.menstrual.Mood?,
        symptoms: List<String>,
        notes: String?,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val current = (state.value as? HomeUiState.Ready)?.cycleState?.activePeriod ?: return@launch
            val day = com.haodong.yimalaile.domain.menstrual.DailyRecord(
                date = date, intensity = intensity, mood = mood,
                symptoms = symptoms, notes = notes
            )
            val ok = service.logDay(current.id, day)
            onResult(ok)
            if (ok) refresh()
        }
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Ready(val cycleState: CycleState) : HomeUiState()
}
