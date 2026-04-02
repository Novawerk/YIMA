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
        val now = Clock.System.now().toEpochMilliseconds()
        val record = MenstrualRecord(
            id = newId(),
            startDate = startDate,
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now,
        )
        return repository.insertRecord(record)
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
        val now = Clock.System.now().toEpochMilliseconds()
        val record = MenstrualRecord(
            id = newId(),
            startDate = startDate,
            endDate = endDate,
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now,
        )
        return repository.insertRecord(record)
    }

    // ---------- Prediction ----------

    suspend fun predictNextCycles(count: Int = 3): List<PredictedCycle> {
        val records = repository.getAllRecords()
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
