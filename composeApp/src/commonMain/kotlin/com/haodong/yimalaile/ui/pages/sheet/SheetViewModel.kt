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
import org.jetbrains.compose.resources.StringResource
import yimalaile.composeapp.generated.resources.*
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
        val titleRes: StringResource,
        val hintRes: StringResource? = null,
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

    private suspend fun showLogDaySheet(targetDate: LocalDate? = null): LogDayResult? {
        val deferred = CompletableDeferred<LogDayResult?>()
        _activeSheet.value = SheetRequest.LogDay(targetDate, deferred)
        return deferred.await().also { _activeSheet.value = null }
    }

    suspend fun showDatePicker(
        titleRes: StringResource,
        hintRes: StringResource? = null,
        hint: String? = null,
        minDate: LocalDate? = null,
        maxDate: LocalDate? = null,
        defaultDate: LocalDate? = null,
    ): LocalDate? {
        val deferred = CompletableDeferred<LocalDate?>()
        _activeSheet.value = SheetRequest.DatePicker(titleRes, hintRes, hint, minDate, maxDate, defaultDate, deferred)
        return deferred.await().also { _activeSheet.value = null }
    }

    // ---- High-level: show sheet + execute service call ----

    private fun averagePeriodLength(records: List<MenstrualRecord>): Int? {
        val lengths = records
            .filter { !it.isDeleted && it.endDate != null }
            .map { it.startDate.until(it.endDate!!, DateTimeUnit.DAY).toInt() + 1 }
        return if (lengths.isEmpty()) null else lengths.sum() / lengths.size
    }

    /** "姨妈来了" — record period arrival */
    suspend fun recordPeriodStart(): AddRecordResult? {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val state = service.getCycleState()
        val records = state.records

        // Min date: day after the last record's end date
        val minDate = records
            .filter { it.endDate != null }
            .maxByOrNull { it.endDate!! }
            ?.endDate?.plus(1, DateTimeUnit.DAY)

        // Step 1: Pick start date
        val start = showDatePicker(
            titleRes = Res.string.start_period_question,
            hintRes = Res.string.start_period_hint,
            minDate = minDate,
            maxDate = today,
            defaultDate = today,
        ) ?: return null

        // Step 2: If start > 3 days ago, also ask for end date (backfill scenario)
        var end: LocalDate? = null
        if (start.until(today, DateTimeUnit.DAY).toInt() > 3) {
            val avgPeriod = averagePeriodLength(records) ?: 5
            val predictedEnd = start.plus(avgPeriod - 1, DateTimeUnit.DAY)
                .let { if (it > today) today else it }
            end = showDatePicker(
                titleRes = Res.string.end_period_question,
                minDate = start,
                maxDate = today,
                defaultDate = predictedEnd,
            )
            if (end == null) return null  // User dismissed the end date picker
        }

        return service.recordPeriodStart(start, end).also { _dataChanged.tryEmit(Unit) }
    }

    /** "姨妈走了" — record period departure */
    suspend fun recordPeriodEnd(): Boolean? {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val state = service.getCycleState()
        val records = state.records

        // Find the current active period's start date
        val currentPeriodStart = records
            .filter { !it.isDeleted && !it.endConfirmed && it.endDate != null }
            .maxByOrNull { it.startDate }
            ?.startDate

        // Compute smart default: predicted end date based on average period length
        val defaultDate = run {
            val completed = records.filter { !it.isDeleted && it.endDate != null && it.endConfirmed }
            val avgPeriod = if (completed.isNotEmpty()) {
                completed.map { it.startDate.until(it.endDate!!, DateTimeUnit.DAY).toInt() + 1 }.average().toInt()
            } else 5
            val start = currentPeriodStart ?: return@run today
            val predictedEnd = start.plus(avgPeriod - 1, DateTimeUnit.DAY)
            if (predictedEnd <= today) predictedEnd else today
        }

        val date = showDatePicker(
            titleRes = Res.string.end_period_question,
            minDate = currentPeriodStart ?: today.minus(1, DateTimeUnit.MONTH),
            maxDate = today,
            defaultDate = defaultDate,
        ) ?: return null

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
                    titleRes = Res.string.edit_start_date,
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
                    titleRes = Res.string.edit_end_date,
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
