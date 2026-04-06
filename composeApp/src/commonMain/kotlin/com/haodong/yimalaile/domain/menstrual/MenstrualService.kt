package com.haodong.yimalaile.domain.menstrual

import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.until
import me.tatarka.inject.annotations.Inject

data class MenstrualCycle(
    val record: MenstrualRecord,
    val cycleLength: Int?,
    val cycleEndDate: LocalDate,
    val ovulationDates: Set<LocalDate>,
    val ovulationPeakDate: LocalDate,
    val isAnomaly: Boolean = false // 是否为异常短周期（< 14天）
)

@Inject
class MenstrualService(private val repository: RecordsRepository) {

    // ---------- Record period arrival / departure ----------

    /**
     * User confirms "姨妈来了" — period has arrived on [startDate].
     * If [endDate] is provided (user selected it), use it directly.
     * Otherwise, creates a record with an estimated end date based on average period length.
     */
    suspend fun recordPeriodStart(startDate: LocalDate, endDate: LocalDate? = null): AddRecordResult {
        val all = repository.getAllRecords()

        val avgPeriod = averagePeriodLength(all) ?: 5
        val estimatedEnd = endDate ?: startDate.plus(avgPeriod - 1, DateTimeUnit.DAY)

        val overlaps = all.any { record ->
            val rEnd = record.endDate ?: startDate // treat legacy null-endDate as today
            record.startDate <= estimatedEnd && startDate <= rEnd
        }
        if (overlaps) return AddRecordResult.OverlappingPeriod

        val now = Clock.System.now().toEpochMilliseconds()
        return repository.insertRecord(
            MenstrualRecord(
                id = newId(), startDate = startDate, endDate = estimatedEnd,
                endConfirmed = endDate != null,
                createdAtEpochMillis = now, updatedAtEpochMillis = now,
                source = RecordSource.MANUAL,
            )
        )
    }

    /**
     * User confirms "姨妈走了" — period ended on [endDate].
     * Finds the most recent record covering today (or the latest record) and sets its endDate.
     */
    suspend fun recordPeriodEnd(endDate: LocalDate): Boolean {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val all = repository.getAllRecords()

        // Find the record that covers today, or the most recent record
        val record = all.find { r ->
            val rEnd = r.endDate ?: today
            today in r.startDate..rEnd
        } ?: all.maxByOrNull { it.startDate } ?: return false

        if (endDate < record.startDate) return false
        val trimmedDailyRecords = record.dailyRecords.filter { it.date <= endDate }
        return repository.updateRecord(
            record.copy(
                endDate = endDate,
                endConfirmed = true,
                dailyRecords = trimmedDailyRecords,
                updatedAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
            )
        )
    }

    // ---------- Daily logging ----------

    suspend fun logDay(recordId: String, day: DailyRecord): Boolean {
        val record = repository.getAllRecords().find { it.id == recordId } ?: return false
        val updatedDaily = record.dailyRecords.filter { it.date != day.date } + day
        return repository.updateRecord(
            record.copy(dailyRecords = updatedDaily,
                updatedAtEpochMillis = Clock.System.now().toEpochMilliseconds())
        )
    }

    // ---------- Backfill ----------

    suspend fun backfillPeriod(startDate: LocalDate, endDate: LocalDate): AddRecordResult {
        if (endDate < startDate) return AddRecordResult.InvalidDateRange

        val all = repository.getAllRecords()
        val overlaps = all.any { record ->
            val rEnd = record.endDate ?: endDate
            record.startDate <= endDate && startDate <= rEnd
        }
        if (overlaps) return AddRecordResult.OverlappingPeriod

        val now = Clock.System.now().toEpochMilliseconds()
        return repository.insertRecord(
            MenstrualRecord(id = newId(), startDate = startDate, endDate = endDate,
                endConfirmed = true, createdAtEpochMillis = now, updatedAtEpochMillis = now)
        )
    }

    // ---------- State ----------

    /**
     * Get the current cycle state.
     * [cycleLength] is the user-configured cycle length from settings (default 28).
     */
    suspend fun getCycleState(cycleLength: Int = 28): CycleState {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val all = repository.getAllRecords()
        val predictions = predictNextCycles(all, cycleLength = cycleLength)

        // Auto-confirm predictions that are 3+ days past their end
        autoConfirmPastPredictions(all, predictions, today)

        // Re-read after potential auto-confirmation inserts
        val records = repository.getAllRecords()
            .sortedByDescending { it.startDate }
            .take(10)

        val updatedPredictions = predictNextCycles(repository.getAllRecords(), cycleLength = cycleLength)

        // Current period: a record covering today whose end hasn't been confirmed yet
        val currentPeriod = records.find { r ->
            !r.endConfirmed && r.endDate != null &&
            today in r.startDate..r.endDate
        }

        // In predicted period: today falls within a prediction (and no real record covers it)
        val inPredictedPeriod = currentPeriod == null && updatedPredictions.any { pred ->
            val avgPeriod = averagePeriodLength(records) ?: 5
            val pEnd = pred.predictedEnd ?: pred.predictedStart.plus(avgPeriod - 1, DateTimeUnit.DAY)
            today in pred.predictedStart..pEnd
        }

        return CycleState(
            records = records,
            predictions = updatedPredictions,
            currentPeriod = currentPeriod,
            inPredictedPeriod = inPredictedPeriod,
        )
    }

    private suspend fun autoConfirmPastPredictions(
        existingRecords: List<MenstrualRecord>,
        predictions: List<PredictedCycle>,
        today: LocalDate,
    ) {
        val avgPeriod = averagePeriodLength(existingRecords) ?: 5

        for (pred in predictions) {
            val pEnd = pred.predictedEnd ?: pred.predictedStart.plus(avgPeriod - 1, DateTimeUnit.DAY)
            val confirmDate = pEnd.plus(3, DateTimeUnit.DAY)

            if (today < confirmDate) continue

            // Check no real record already covers this range
            val alreadyCovered = existingRecords.any { r ->
                val rEnd = r.endDate ?: today
                r.startDate <= pEnd && pred.predictedStart <= rEnd
            }
            if (alreadyCovered) continue

            val now = Clock.System.now().toEpochMilliseconds()
            repository.insertRecord(
                MenstrualRecord(
                    id = newId(),
                    startDate = pred.predictedStart,
                    endDate = pEnd,
                    endConfirmed = true,
                    createdAtEpochMillis = now,
                    updatedAtEpochMillis = now,
                    source = RecordSource.AUTO_CONFIRMED,
                )
            )
        }
    }

    // ---------- Edit / Delete ----------

    suspend fun editRecordDates(recordId: String, newStart: LocalDate?, newEnd: LocalDate?): Boolean {
        val record = repository.getAllRecords().find { it.id == recordId } ?: return false
        val start = newStart ?: record.startDate
        val end = newEnd ?: record.endDate
        if (end != null && end < start) return false
        val trimmedDaily = if (end != null) record.dailyRecords.filter { it.date in start..end } else record.dailyRecords
        return repository.updateRecord(
            record.copy(
                startDate = start,
                endDate = end,
                dailyRecords = trimmedDaily,
                updatedAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
            )
        )
    }

    suspend fun deleteRecord(recordId: String): Boolean =
        repository.deleteRecord(recordId)

    // ---------- Data management ----------

    suspend fun clearAllData() {
        repository.clearAll()
    }

    // ---------- Prediction ----------

    suspend fun predictNextCycles(count: Int = 3, cycleLength: Int = 28): List<PredictedCycle> =
        predictNextCycles(repository.getAllRecords(), count, cycleLength)

    // ---------- Cycle Analysis ----------

    /**
     * Get detailed cycle information for a specific record.
     * Logic:
     * 1. Cycle Length: If next record exists, use (next.start - this.start). Otherwise use [defaultCycleLength].
     * 2. Cycle End: The day before next record starts.
     * 3. Ovulation: Scientific calculation based on the *actual or estimated cycle length*.
     *    Standard fertile window is usually in the middle of the cycle.
     *    According to user request, we use the "count-back method" (CycleLength - 14).
     *    Luteal phase is relatively constant (around 14 days).
     */
    fun getMenstrualCycle(
        record: MenstrualRecord,
        allRecords: List<MenstrualRecord>,
        predictions: List<PredictedCycle> = emptyList(),
        defaultCycleLength: Int = 28
    ): MenstrualCycle {
        val sorted = allRecords.filter { !it.isDeleted }.sortedBy { it.startDate }
        val index = sorted.indexOfFirst { it.id == record.id }
        
        val cycleLength = if (index != -1 && index < sorted.size - 1) {
            sorted[index].startDate.until(sorted[index + 1].startDate, DateTimeUnit.DAY).toInt()
        } else null
        
        // 标记是否为异常短周期
        val isAnomaly = cycleLength != null && cycleLength < 14

        val effectiveLength = cycleLength ?: defaultCycleLength
        val cycleEndDate = if (index != -1 && index < sorted.size - 1) {
            sorted[index + 1].startDate.plus(-1, DateTimeUnit.DAY)
        } else {
            val nextStart = predictions.firstOrNull()?.predictedStart 
                ?: record.startDate.plus(effectiveLength, DateTimeUnit.DAY)
            nextStart.plus(-1, DateTimeUnit.DAY)
        }

        // 医学标准计算法：排卵日通常在下次月经开始前的第14天左右。
        // 黄体期（排卵到下次月经）相对固定，约为14天。
        // 而卵泡期（月经开始到排卵）变动较大，周期的长短主要取决于卵泡期。
        val peakDayOffset = (effectiveLength - 14).coerceAtLeast(0)
        val peakDate = record.startDate.plus(peakDayOffset, DateTimeUnit.DAY)
        
        val ovulationDates = buildSet {
            // 如果是异常短周期，排卵计算可能不适用，但这里仍按公式给出一个参考，UI层会提示异常
            // 排卵期窗口通常为6天（高峰日前4天到后1天）
            for (offset in -4..1) {
                add(peakDate.plus(offset, DateTimeUnit.DAY))
            }
        }

        return MenstrualCycle(
            record = record,
            cycleLength = cycleLength ?: defaultCycleLength,
            cycleEndDate = cycleEndDate,
            ovulationDates = ovulationDates,
            ovulationPeakDate = peakDate,
            isAnomaly = isAnomaly
        )
    }

    /**
     * Determine the current cycle phase based on historical data.
     * Uses user-configured [cycleLength] from settings.
     * Returns null if not enough data (< 1 completed record).
     */
    fun getCurrentPhase(state: CycleState, today: LocalDate, cycleLength: Int = 28): CyclePhaseInfo? {
        return CyclePhaseInfo.getPhaseInfo(today, state, cycleLength)
    }

    // ---------- Private ----------

    /**
     * Predict next cycles using the user-configured [cycleLength].
     */
    private fun predictNextCycles(records: List<MenstrualRecord>, count: Int = 3, cycleLength: Int = 28): List<PredictedCycle> {
        // 计算历史平均周期长度，排除掉周期小于 14 天的记录
        val sorted = records.filter { !it.isDeleted }.sortedBy { it.startDate }
        val cycleLengths = mutableListOf<Int>()
        for (i in 0 until sorted.size - 1) {
            val length = sorted[i].startDate.until(sorted[i + 1].startDate, DateTimeUnit.DAY).toInt()
            if (length >= 14) {
                cycleLengths.add(length)
            }
        }
        
        val effectiveCycleLength = if (cycleLengths.isNotEmpty()) {
            cycleLengths.sum() / cycleLengths.size
        } else {
            cycleLength
        }

        val avgPeriodLength = averagePeriodLength(records)
        val lastStart = records.maxByOrNull { it.startDate }?.startDate ?: return emptyList()
        return (1..count).map { i ->
            val start = lastStart.plus(effectiveCycleLength * i, DateTimeUnit.DAY)
            PredictedCycle(start, avgPeriodLength?.let { start.plus(it - 1, DateTimeUnit.DAY) })
        }
    }

    private fun averagePeriodLength(records: List<MenstrualRecord>): Int? {
        // 过滤掉异常记录（周期过短的记录可能伴随异常的经期长度）
        // 这里主要通过 getAllCycleLengths 来间接判断可能更复杂，
        // 简单起见，先过滤掉经期长度异常的情况（比如 > 10天 或 < 2天，如果需要的话）
        // 但目前用户主要关注的是周期长度。
        val lengths = records
            .filter { !it.isDeleted && it.endDate != null }
            .map { it.startDate.until(it.endDate!!, DateTimeUnit.DAY).toInt() + 1 }
        return if (lengths.isEmpty()) null else lengths.sum() / lengths.size
    }

    private fun newId() = "record_${Clock.System.now().toEpochMilliseconds()}_${(0..9999).random()}"
}
