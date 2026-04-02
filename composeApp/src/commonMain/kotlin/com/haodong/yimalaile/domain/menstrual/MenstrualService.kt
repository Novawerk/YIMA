package com.haodong.yimalaile.domain.menstrual

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import me.tatarka.inject.annotations.Inject

@Inject
class MenstrualService(
    private val repository: RecordsRepository,
    private val calculator: CycleCalculator
) {

    // ---------- Scenario 1: Normal recording ----------

    suspend fun startPeriod(startDate: LocalDate): AddRecordResult {
        val all = repository.getAllRecords()

        if (all.any { it.endDate == null }) return AddRecordResult.ActivePeriodExists

        // Overlap: existing completed period's range contains or touches startDate
        if (all.any { startDate <= it.endDate!! }) return AddRecordResult.OverlappingPeriod

        val now = Clock.System.now().toEpochMilliseconds()
        return repository.insertRecord(
            MenstrualRecord(id = newId(), startDate = startDate,
                createdAtEpochMillis = now, updatedAtEpochMillis = now)
        )
    }

    suspend fun logDay(recordId: String, day: DailyRecord): Boolean {
        val record = repository.getAllRecords().find { it.id == recordId } ?: return false
        val updatedDaily = record.dailyRecords.filter { it.date != day.date } + day
        return repository.updateRecord(
            record.copy(
                dailyRecords = updatedDaily,
                updatedAtEpochMillis = Clock.System.now().toEpochMilliseconds()
            )
        )
    }

    suspend fun endPeriod(recordId: String, endDate: LocalDate): Boolean {
        val record = repository.getAllRecords().find { it.id == recordId } ?: return false
        if (endDate < record.startDate) return false
        return repository.updateRecord(
            record.copy(
                endDate = endDate,
                updatedAtEpochMillis = Clock.System.now().toEpochMilliseconds()
            )
        )
    }

    // ---------- Scenario 2: Backfill ----------

    suspend fun backfillPeriod(startDate: LocalDate, endDate: LocalDate): AddRecordResult {
        if (endDate < startDate) return AddRecordResult.InvalidDateRange

        val all = repository.getAllRecords()

        if (all.any { it.endDate == null }) return AddRecordResult.ActivePeriodExists

        // Overlap: [startDate, endDate] intersects any existing [existStart, existEnd]
        val overlaps = all.any { it.startDate <= endDate && startDate <= it.endDate!! }
        if (overlaps) return AddRecordResult.OverlappingPeriod

        val now = Clock.System.now().toEpochMilliseconds()
        return repository.insertRecord(
            MenstrualRecord(id = newId(), startDate = startDate, endDate = endDate,
                createdAtEpochMillis = now, updatedAtEpochMillis = now)
        )
    }

    // ---------- State ----------

    suspend fun getCycleState(): CycleState {
        val all = repository.getAllRecords()
        val sorted = all.sortedByDescending { it.startDate }

        // Active = the most recent record with no end date
        val active = sorted.firstOrNull { it.endDate == null }

        // Recent = last 10 completed periods (have an end date), newest first
        val recent = sorted.filter { it.endDate != null }.take(10)

        val predictions = predictNextCycles(records = all)

        return CycleState(
            activePeriod = active,
            recentPeriods = recent,
            predictions = predictions
        )
    }

    // ---------- Prediction ----------

    suspend fun predictNextCycles(count: Int = 3): List<PredictedCycle> =
        predictNextCycles(count, repository.getAllRecords())

    private fun predictNextCycles(count: Int = 3, records: List<MenstrualRecord>): List<PredictedCycle> {
        val avgCycleLength = calculator.calculateAverageCycleLength(records) ?: return emptyList()
        val avgPeriodLength = calculator.calculateAveragePeriodLength(records)
        val lastStart = records.maxByOrNull { it.startDate }?.startDate ?: return emptyList()

        return (1..count).map { i ->
            val predictedStart = lastStart.plus(avgCycleLength * i, DateTimeUnit.DAY)
            val predictedEnd = avgPeriodLength?.let {
                predictedStart.plus(it - 1, DateTimeUnit.DAY)
            }
            PredictedCycle(predictedStart, predictedEnd)
        }
    }

    private fun newId() =
        "record_${Clock.System.now().toEpochMilliseconds()}_${(0..9999).random()}"
}
