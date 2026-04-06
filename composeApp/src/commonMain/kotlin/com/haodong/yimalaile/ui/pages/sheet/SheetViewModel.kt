package com.haodong.yimalaile.ui.pages.sheet

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import com.haodong.yimalaile.domain.menstrual.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.*
import kotlin.time.Clock

// ============================================================
// Result types
// ============================================================

data class LogDayResult(
    val intensity: Intensity?,
    val mood: Mood?,
    val symptoms: List<String>,
    val notes: String?,
)

sealed class DetailAction {
    object EditStart : DetailAction()
    object EditEnd : DetailAction()
    object LogDay : DetailAction()
    data class LogSpecificDay(val date: kotlinx.datetime.LocalDate) : DetailAction()
    object Delete : DetailAction()
}

// ============================================================
// Sheet requests — what the SheetHost renders
// ============================================================

sealed class SheetRequest {
    abstract val deferred: CompletableDeferred<*>

    data class StartPeriod(
        val records: List<MenstrualRecord>,
        val avgPeriodLength: Int,
        val result: CompletableDeferred<Pair<LocalDate, LocalDate?>?>,
    ) : SheetRequest() { override val deferred get() = result }

    data class EndPeriod(
        val records: List<MenstrualRecord>,
        val result: CompletableDeferred<LocalDate?>,
    ) : SheetRequest() { override val deferred get() = result }

    data class LogDay(
        val targetDate: LocalDate?,
        val result: CompletableDeferred<LogDayResult?>,
    ) : SheetRequest() { override val deferred get() = result }

    data class RecordDetail(
        val record: MenstrualRecord,
        val allRecords: List<MenstrualRecord>,
        val defaultCycleLength: Int,
        val result: CompletableDeferred<DetailAction?>,
    ) : SheetRequest() { override val deferred get() = result }

    data class PredictionDetail(
        val prediction: PredictedCycle,
        val avgPeriodLength: Int,
        val result: CompletableDeferred<Unit?>,
    ) : SheetRequest() { override val deferred get() = result }

    data class DatePicker(
        val title: String,
        val hint: String? = null,
        val minDate: LocalDate? = null,
        val maxDate: LocalDate? = null,
        val defaultDate: LocalDate? = null,
        val result: CompletableDeferred<LocalDate?>,
    ) : SheetRequest() { override val deferred get() = result }
}

// ============================================================
// CompositionLocal for global access
// ============================================================

val LocalSheetViewModel = staticCompositionLocalOf<SheetViewModel> {
    error("No SheetViewModel provided")
}

// ============================================================
// SheetViewModel — owns service, exposes suspend APIs
// ============================================================

class SheetViewModel(private val service: MenstrualService) : ViewModel() {
    fun getService() = service

    private val _activeSheet = MutableStateFlow<SheetRequest?>(null)
    val activeSheet: StateFlow<SheetRequest?> = _activeSheet

    private val _dataChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    /** Emitted after any data-modifying operation completes. */
    val dataChanged: SharedFlow<Unit> = _dataChanged

    // ---- Low-level: show sheet and suspend until result ----

    private suspend fun showStartPeriodSheet(): Pair<LocalDate, LocalDate?>? {
        val state = service.getCycleState()
        val avgPeriod = averagePeriodLength(state.records) ?: 5
        val deferred = CompletableDeferred<Pair<LocalDate, LocalDate?>?>()
        _activeSheet.value = SheetRequest.StartPeriod(state.records, avgPeriod, deferred)
        return deferred.await().also { _activeSheet.value = null }
    }

    private fun averagePeriodLength(records: List<MenstrualRecord>): Int? {
        val lengths = records
            .filter { !it.isDeleted && it.endDate != null }
            .map { it.startDate.until(it.endDate!!, DateTimeUnit.DAY).toInt() + 1 }
        return if (lengths.isEmpty()) null else lengths.sum() / lengths.size
    }

    private suspend fun showEndPeriodSheet(): LocalDate? {
        val state = service.getCycleState()
        val deferred = CompletableDeferred<LocalDate?>()
        _activeSheet.value = SheetRequest.EndPeriod(state.records, deferred)
        return deferred.await().also { _activeSheet.value = null }
    }

    private suspend fun showLogDaySheet(targetDate: LocalDate? = null): LogDayResult? {
        val deferred = CompletableDeferred<LogDayResult?>()
        _activeSheet.value = SheetRequest.LogDay(targetDate, deferred)
        return deferred.await().also { _activeSheet.value = null }
    }

    suspend fun showDatePicker(
        title: String,
        hint: String? = null,
        minDate: LocalDate? = null,
        maxDate: LocalDate? = null,
        defaultDate: LocalDate? = null,
    ): LocalDate? {
        val deferred = CompletableDeferred<LocalDate?>()
        _activeSheet.value = SheetRequest.DatePicker(title, hint, minDate, maxDate, defaultDate, deferred)
        return deferred.await().also { _activeSheet.value = null }
    }

    // ---- High-level: show sheet + execute service call ----

    /** "姨妈来了" — record period arrival */
    suspend fun recordPeriodStart(): AddRecordResult? {
        val (start, end) = showStartPeriodSheet() ?: return null
        return service.recordPeriodStart(start, end).also { _dataChanged.tryEmit(Unit) }
    }

    /** "姨妈走了" — record period departure */
    suspend fun recordPeriodEnd(): Boolean? {
        val date = showEndPeriodSheet() ?: return null
        return service.recordPeriodEnd(date).also { _dataChanged.tryEmit(Unit) }
    }

    suspend fun logDay(targetDate: LocalDate? = null): Boolean? {
        val result = showLogDaySheet(targetDate) ?: return null
        val state = service.getCycleState()
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val record = state.currentPeriod
            ?: state.records.maxByOrNull { it.startDate }
            ?: return false
        val day = DailyRecord(
            date = targetDate ?: today,
            intensity = result.intensity,
            mood = result.mood,
            symptoms = result.symptoms,
            notes = result.notes,
        )
        return service.logDay(record.id, day)
    }

    /** Log a day for a specific record. */
    suspend fun logDayForRecord(recordId: String, targetDate: LocalDate): Boolean? {
        val result = showLogDaySheet(targetDate) ?: return null
        val day = DailyRecord(
            date = targetDate,
            intensity = result.intensity,
            mood = result.mood,
            symptoms = result.symptoms,
            notes = result.notes,
        )
        return service.logDay(recordId, day)
    }

    /** Show record detail. Returns the action the user chose, or null if dismissed. */
    suspend fun showRecordDetail(record: MenstrualRecord, defaultCycleLength: Int): DetailAction? {
        val state = service.getCycleState()
        val deferred = CompletableDeferred<DetailAction?>()
        _activeSheet.value = SheetRequest.RecordDetail(record, state.records, defaultCycleLength, deferred)
        return deferred.await().also { _activeSheet.value = null }
    }

    /**
     * Show record detail and handle all actions internally.
     * Callers don't need to handle DetailAction — just call and forget.
     */
    suspend fun showAndHandleRecordDetail(record: MenstrualRecord, defaultCycleLength: Int) {
        val allRecords = service.getCycleState().records.filter { !it.isDeleted }.sortedBy { it.startDate }
        val action = showRecordDetail(record, defaultCycleLength) ?: return
        when (action) {
            is DetailAction.EditStart -> {
                val recordIndex = allRecords.indexOfFirst { it.id == record.id }
                val prevRecord = if (recordIndex > 0) allRecords[recordIndex - 1] else null
                val minStart = prevRecord?.endDate?.plus(1, DateTimeUnit.DAY)
                val maxStart = record.endDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault())

                val newStart = showDatePicker(
                    title = "修改开始日期",
                    minDate = minStart,
                    maxDate = maxStart,
                    defaultDate = record.startDate
                )
                if (newStart != null) {
                    service.editRecordDates(record.id, newStart = newStart, newEnd = null)
                }
            }
            is DetailAction.EditEnd -> {
                val recordIndex = allRecords.indexOfFirst { it.id == record.id }
                val nextRecord = if (recordIndex != -1 && recordIndex < allRecords.size - 1) allRecords[recordIndex + 1] else null
                val minEnd = record.startDate
                val maxEnd = nextRecord?.startDate?.minus(1, DateTimeUnit.DAY) ?: Clock.System.todayIn(TimeZone.currentSystemDefault())

                val newEnd = showDatePicker(
                    title = "修改结束日期",
                    minDate = minEnd,
                    maxDate = maxEnd,
                    defaultDate = record.endDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault())
                )
                if (newEnd != null) {
                    service.editRecordDates(record.id, newStart = null, newEnd = newEnd)
                }
            }
            is DetailAction.LogDay -> logDay()
            is DetailAction.LogSpecificDay -> logDayForRecord(record.id, action.date)
            is DetailAction.Delete -> service.deleteRecord(record.id)
        }
        _dataChanged.tryEmit(Unit)
    }

    /** Show prediction detail (read-only). Suspends until dismissed. */
    suspend fun showPredictionDetail(prediction: PredictedCycle, avgPeriodLength: Int) {
        val deferred = CompletableDeferred<Unit?>()
        _activeSheet.value = SheetRequest.PredictionDetail(prediction, avgPeriodLength, deferred)
        deferred.await()
        _activeSheet.value = null
    }

    // ---- Dismiss (from swipe/back) ----

    fun dismiss() {
        val current = _activeSheet.value ?: return
        current.deferred.let {
            @Suppress("UNCHECKED_CAST")
            (it as CompletableDeferred<Any?>).complete(null)
        }
    }
}
