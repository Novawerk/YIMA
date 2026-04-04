package com.haodong.yimalaile.ui.pages.sheet

import com.haodong.yimalaile.domain.menstrual.AddRecordResult
import com.haodong.yimalaile.domain.menstrual.DailyRecord
import com.haodong.yimalaile.domain.menstrual.Intensity
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.domain.menstrual.Mood
import com.haodong.yimalaile.domain.menstrual.PredictedCycle
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

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
        val result: CompletableDeferred<LocalDate?>,
    ) : SheetRequest() { override val deferred get() = result }

    data class EndPeriod(
        val startDate: LocalDate,
        val dailyRecords: List<DailyRecord>,
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
        val isActive: Boolean,
        val result: CompletableDeferred<DetailAction?>,
    ) : SheetRequest() { override val deferred get() = result }

    data class PredictionDetail(
        val prediction: PredictedCycle,
        val avgPeriodLength: Int,
        val result: CompletableDeferred<Unit?>,
    ) : SheetRequest() { override val deferred get() = result }
}

// ============================================================
// SheetManager — owns service, exposes suspend APIs
// ============================================================

class SheetManager(private val service: MenstrualService) {

    private val _activeSheet = MutableStateFlow<SheetRequest?>(null)
    val activeSheet: StateFlow<SheetRequest?> = _activeSheet

    // ---- Low-level: show sheet and suspend until result ----

    private suspend fun showStartPeriodSheet(): LocalDate? {
        val state = service.getCycleState()
        val allRecords = state.recentPeriods + listOfNotNull(state.activePeriod)
        val deferred = CompletableDeferred<LocalDate?>()
        _activeSheet.value = SheetRequest.StartPeriod(allRecords, deferred)
        return deferred.await().also { _activeSheet.value = null }
    }

    private suspend fun showEndPeriodSheet(): LocalDate? {
        val state = service.getCycleState()
        val active = state.activePeriod ?: return null
        val others = state.recentPeriods.filter { it.id != active.id }
        val deferred = CompletableDeferred<LocalDate?>()
        _activeSheet.value = SheetRequest.EndPeriod(active.startDate, active.dailyRecords, others, deferred)
        return deferred.await().also { _activeSheet.value = null }
    }

    private suspend fun showLogDaySheet(targetDate: LocalDate? = null): LogDayResult? {
        val deferred = CompletableDeferred<LogDayResult?>()
        _activeSheet.value = SheetRequest.LogDay(targetDate, deferred)
        return deferred.await().also { _activeSheet.value = null }
    }

    private suspend fun showBackfillSheet(): Pair<LocalDate, LocalDate>? {
        val state = service.getCycleState()
        val allRecords = state.recentPeriods + listOfNotNull(state.activePeriod)
        val deferred = CompletableDeferred<Pair<LocalDate, LocalDate>?>()
        _activeSheet.value = SheetRequest.Backfill(allRecords, deferred)
        return deferred.await().also { _activeSheet.value = null }
    }

    // ---- High-level: show sheet + execute service call ----

    suspend fun startPeriod(): AddRecordResult? {
        val date = showStartPeriodSheet() ?: return null
        return service.startPeriod(date)
    }

    suspend fun endPeriod(): Boolean? {
        val date = showEndPeriodSheet() ?: return null
        val state = service.getCycleState()
        val active = state.activePeriod ?: return false
        return service.endPeriod(active.id, date)
    }

    suspend fun logDay(targetDate: LocalDate? = null): Boolean? {
        val result = showLogDaySheet(targetDate) ?: return null
        val state = service.getCycleState()
        val active = state.activePeriod ?: return false
        val day = DailyRecord(
            date = targetDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault()),
            intensity = result.intensity,
            mood = result.mood,
            symptoms = result.symptoms,
            notes = result.notes,
        )
        return service.logDay(active.id, day)
    }

    /** Log a day for a specific record (not just the active period). */
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
    suspend fun showRecordDetail(record: MenstrualRecord, isActive: Boolean = false): DetailAction? {
        val deferred = CompletableDeferred<DetailAction?>()
        _activeSheet.value = SheetRequest.RecordDetail(record, isActive, deferred)
        return deferred.await().also { _activeSheet.value = null }
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
