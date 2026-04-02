package com.haodong.yimalaile.domain.menstrual

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.until
import me.tatarka.inject.annotations.Inject

@Inject
class MenstrualService(private val repository: RecordsRepository) {

    // ---------- Scenario 1: Normal recording ----------

    suspend fun startPeriod(startDate: LocalDate): AddRecordResult {
        val all = repository.getAllRecords()

        if (all.any { it.endDate == null }) return AddRecordResult.ActivePeriodExists

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
            record.copy(dailyRecords = updatedDaily,
                updatedAtEpochMillis = Clock.System.now().toEpochMilliseconds())
        )
    }

    suspend fun endPeriod(recordId: String, endDate: LocalDate): Boolean {
        val record = repository.getAllRecords().find { it.id == recordId } ?: return false
        if (endDate < record.startDate) return false
        val trimmedDailyRecords = record.dailyRecords.filter { it.date <= endDate }
        return repository.updateRecord(
            record.copy(
                endDate = endDate,
                dailyRecords = trimmedDailyRecords,
                updatedAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
            )
        )
    }

    // ---------- Scenario 2: Backfill ----------

    suspend fun backfillPeriod(startDate: LocalDate, endDate: LocalDate): AddRecordResult {
        if (endDate < startDate) return AddRecordResult.InvalidDateRange

        val all = repository.getAllRecords()
        val overlaps = all.filter { it.endDate != null }
            .any { it.startDate <= endDate && startDate <= it.endDate!! }
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
        return CycleState(
            activePeriod = sorted.firstOrNull { it.endDate == null },
            recentPeriods = sorted.filter { it.endDate != null }.take(10),
            predictions = predictNextCycles(all)
        )
    }

    // ---------- Data management ----------

    suspend fun clearAllData() {
        repository.clearAll()
    }

    // ---------- Prediction ----------

    suspend fun predictNextCycles(count: Int = 3): List<PredictedCycle> =
        predictNextCycles(repository.getAllRecords(), count)

    // ---------- Private ----------

    private fun predictNextCycles(records: List<MenstrualRecord>, count: Int = 3): List<PredictedCycle> {
        val avgCycleLength = averageCycleLength(records) ?: return emptyList()
        val avgPeriodLength = averagePeriodLength(records)
        val lastStart = records.maxByOrNull { it.startDate }?.startDate ?: return emptyList()
        return (1..count).map { i ->
            val start = lastStart.plus(avgCycleLength * i, DateTimeUnit.DAY)
            PredictedCycle(start, avgPeriodLength?.let { start.plus(it - 1, DateTimeUnit.DAY) })
        }
    }

    private fun averageCycleLength(records: List<MenstrualRecord>): Int? {
        val sorted = records.filter { !it.isDeleted }.sortedBy { it.startDate }
        if (sorted.size < 2) return null
        val gaps = sorted.zipWithNext().map { (a, b) -> a.startDate.until(b.startDate, DateTimeUnit.DAY) }
        return gaps.sum() / gaps.size
    }

    private fun averagePeriodLength(records: List<MenstrualRecord>): Int? {
        val lengths = records
            .filter { !it.isDeleted && it.endDate != null }
            .map { it.startDate.until(it.endDate!!, DateTimeUnit.DAY) + 1 }
        return if (lengths.isEmpty()) null else lengths.sum() / lengths.size
    }

    private fun newId() = "record_${Clock.System.now().toEpochMilliseconds()}_${(0..9999).random()}"
}
