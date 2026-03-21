package com.haodong.yimalaile.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeState(
    val records: List<MenstrualRecord> = emptyList(),
    val lastPeriodDate: LocalDateKey? = null,
    val averageCycleLength: Int? = null,
    val averagePeriodLength: Int? = null,
    val predictedNextPeriod: LocalDateKey? = null
)

class HomeViewModel(private val repository: SuspendRecordsRepository) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val calculator = CycleCalculator()

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadRecords()
    }

    fun loadRecords() {
        scope.launch { refreshState() }
    }

    private suspend fun refreshState() {
        val records = repository.getAllRecords().filter { !it.isDeleted }
        _state.value = HomeState(
            records = records,
            lastPeriodDate = records.maxByOrNull { it.startDate }?.startDate,
            averageCycleLength = calculator.calculateAverageCycleLength(records),
            averagePeriodLength = calculator.calculateAveragePeriodLength(records),
            predictedNextPeriod = calculator.predictNextPeriod(records)
        )
    }

    suspend fun addRecord(record: MenstrualRecord): AddRecordResult {
        val result = repository.insertRecord(record)
        if (result is AddRecordResult.Success) refreshState()
        return result
    }

    fun clear() {
        scope.cancel()
    }
}
