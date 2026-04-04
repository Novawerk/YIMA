package com.haodong.yimalaile.ui.pages.sheet

import com.haodong.yimalaile.domain.menstrual.AddRecordResult
import com.haodong.yimalaile.domain.menstrual.DailyRecord
import com.haodong.yimalaile.domain.menstrual.Intensity
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.domain.menstrual.Mood
import com.haodong.yimalaile.domain.menstrual.PredictedCycle
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.until

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

    data class Backfill(
        val records: List<MenstrualRecord>,
        val result: CompletableDeferred<Pair<LocalDate, LocalDate>?>,
    ) : SheetRequest() { override val deferred get() = result }

    data class RecordDetail(
        val record: MenstrualRecord,
        val result: CompletableDeferred<DetailAction?>,
    ) : SheetRequest() { override val deferred get() = result }

    data class PredictionDetail(
        val prediction: PredictedCycle,
        val avgPeriodLength: Int,
        val result: CompletableDeferred<Unit?>,
    ) : SheetRequest() { override val deferred get() = result }
}

// ============================================================
// CompositionLocal for global access
// ============================================================

val LocalSheetManager = androidx.compose.runtime.staticCompositionLocalOf<SheetManager> {
    error("No SheetManager provided")
}

// ============================================================
// SheetManager — owns service, exposes suspend APIs
// ============================================================

class SheetManager(private val service: MenstrualService) {

    private val _activeSheet = MutableStateFlow<SheetRequest?>(null)
    val activeSheet: StateFlow<SheetRequest?> = _activeSheet

    private val _dataChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    /** Emitted after any data-modifying operation completes. */
    val dataChanged: SharedFlow<Unit> = _dataChanged

    // ---- Low-level: show sheet and suspend until result ----

    private suspend fun showStartPeriodSheet(): Pair<LocalDate, LocalDate?>? {
        val state = service.getCycleState()
        val avgPeriod = state.records
            .filter { it.endDate != null }
            .map { it.startDate.until(it.endDate!!, DateTimeUnit.DAY).toInt() + 1 }
            .takeIf { it.isNotEmpty() }
            ?.let { it.sum() / it.size } ?: 5
        val deferred = CompletableDeferred<Pair<LocalDate, LocalDate?>?>()
        _activeSheet.value = SheetRequest.StartPeriod(state.records, avgPeriod, deferred)
        return deferred.await().also { _activeSheet.value = null }
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

    private suspend fun showBackfillSheet(): Pair<LocalDate, LocalDate>? {
        val state = service.getCycleState()
        val deferred = CompletableDeferred<Pair<LocalDate, LocalDate>?>()
        _activeSheet.value = SheetRequest.Backfill(state.records, deferred)
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
        // Find the record covering today (or the most recent one)
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

    suspend fun backfillPeriod(): AddRecordResult? {
        val (start, end) = showBackfillSheet() ?: return null
        return service.backfillPeriod(start, end)
    }

    /** Show record detail. Returns the action the user chose, or null if dismissed. */
    suspend fun showRecordDetail(record: MenstrualRecord): DetailAction? {
        val deferred = CompletableDeferred<DetailAction?>()
        _activeSheet.value = SheetRequest.RecordDetail(record, deferred)
        return deferred.await().also { _activeSheet.value = null }
    }

    /**
     * Show record detail and handle all actions internally.
     * Callers don't need to handle DetailAction — just call and forget.
     */
    suspend fun showAndHandleRecordDetail(record: MenstrualRecord) {
        val action = showRecordDetail(record) ?: return
        when (action) {
            is DetailAction.EditStart -> recordPeriodStart()
            is DetailAction.EditEnd -> recordPeriodEnd()
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
