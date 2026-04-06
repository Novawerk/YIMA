package com.haodong.yimalaile.domain.menstrual

import kotlinx.datetime.*
import kotlin.time.Clock

/**
 * The four phases of a menstrual cycle.
 * Approximate proportions based on a standard 28-day cycle:
 * - Menstrual:   ~days 1–5   (0%–18%)
 * - Follicular:  ~days 6–13  (18%–46%)
 * - Ovulation:   ~days 14–16 (46%–57%)
 * - Luteal:      ~days 17–28 (57%–100%)
 */
enum class CyclePhase {
    MENSTRUAL,
    FOLLICULAR,
    OVULATION,
    LUTEAL,
}

/**
 * Current cycle phase information derived from historical data.
 */
data class CyclePhaseInfo(
    val phase: CyclePhase,
    val dayInCycle: Int,            // 1-based day within current cycle
    val cycleLength: Int,           // specific cycle length in days
    val periodLength: Int,          // average period length in days
    val progress: Float,            // 0f..1f progress through cycle
    val daysUntilNextPeriod: Int,   // days remaining until next predicted period
    val nextPeriodStart: LocalDate?,
) {
    companion object {
        /**
         * 统一的周期计算逻辑。根据日期、当前的周期状态和平均周期长度，
         * 计算该日期所属周期的开始日期、天数以及对应的阶段信息。
         */
        fun getPhaseInfo(
            date: LocalDate,
            state: CycleState,
            avgCycleLength: Int,
        ): CyclePhaseInfo? {
            val allRecords = state.records.filter { !it.isDeleted }.sortedBy { it.startDate }
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            
            // 1. 寻找当前日期所属的周期起点
            val refRecord = allRecords.lastOrNull { it.startDate <= date }
            val refPrediction = state.predictions.lastOrNull { it.predictedStart <= date }
            
            val cycleStart = if (refPrediction != null && (refRecord == null || refPrediction.predictedStart > refRecord.startDate)) {
                refPrediction.predictedStart
            } else {
                refRecord?.startDate
            } ?: return null
            
            // 2. 确定该周期的实际长度
            val currentCycleLen = if (cycleStart == refRecord?.startDate) {
                val index = allRecords.indexOf(refRecord)
                if (index < allRecords.size - 1) {
                    refRecord.startDate.until(allRecords[index + 1].startDate, DateTimeUnit.DAY).toInt()
                } else {
                    val nextPred = state.predictions.firstOrNull { it.predictedStart > refRecord.startDate }
                    nextPred?.let { refRecord.startDate.until(it.predictedStart, DateTimeUnit.DAY).toInt() } ?: avgCycleLength
                }
            } else {
                val pred = state.predictions.find { it.predictedStart == cycleStart }
                val index = state.predictions.indexOf(pred)
                if (index != -1 && index < state.predictions.size - 1) {
                    cycleStart.until(state.predictions[index + 1].predictedStart, DateTimeUnit.DAY).toInt()
                } else {
                    avgCycleLength
                }
            }
            
            val dayInCycle = cycleStart.until(date, DateTimeUnit.DAY).toInt() + 1
            if (dayInCycle < 1 || dayInCycle > avgCycleLength * 3) return null
            
            // 3. 确定阶段 (Phase)
            val avgPeriod = averagePeriodLength(allRecords) ?: 5
            
            val inActualPeriod = allRecords.any { r ->
                val rEnd = r.endDate ?: today
                date in r.startDate..rEnd
            }
            val inPredictedPeriod = state.predictions.any { p ->
                val pEnd = p.predictedEnd ?: p.predictedStart.plus(avgPeriod - 1, DateTimeUnit.DAY)
                date in p.predictedStart..pEnd
            }
            
            val currentPeriodLen = refRecord?.let { r ->
                val end = r.endDate ?: if (r.startDate == allRecords.last().startDate && state.currentPeriod != null) today else null
                end?.let { r.startDate.until(it, DateTimeUnit.DAY).toInt() + 1 }
            } ?: avgPeriod
            
            val peakDayInCycle = currentCycleLen - 14
            
            val phase = when {
                inActualPeriod || inPredictedPeriod -> CyclePhase.MENSTRUAL
                dayInCycle <= currentPeriodLen -> CyclePhase.MENSTRUAL
                dayInCycle < peakDayInCycle - 4 -> CyclePhase.FOLLICULAR
                dayInCycle <= peakDayInCycle + 1 -> CyclePhase.OVULATION
                else -> CyclePhase.LUTEAL
            }
            
            val progress = (dayInCycle.toFloat() / currentCycleLen).coerceIn(0f, 1f)
            val nextStart = state.predictions.firstOrNull { it.predictedStart > date }?.predictedStart
            val daysUntilNext = if (nextStart != null) date.until(nextStart, DateTimeUnit.DAY).toInt() else (currentCycleLen - dayInCycle).coerceAtLeast(0)
            
            return CyclePhaseInfo(
                phase = phase,
                dayInCycle = dayInCycle,
                cycleLength = currentCycleLen,
                periodLength = avgPeriod,
                progress = progress,
                daysUntilNextPeriod = daysUntilNext,
                nextPeriodStart = nextStart
            )
        }

    }

    /** Start day (1-based) of each phase within this cycle. */
    fun phaseStartDay(p: CyclePhase): Int = when (p) {
        CyclePhase.MENSTRUAL -> 1
        CyclePhase.FOLLICULAR -> periodLength + 1
        CyclePhase.OVULATION -> (cycleLength - 14) - 4
        CyclePhase.LUTEAL -> (cycleLength - 14) + 2
    }

    /** Days until a given phase starts. Negative = already past, 0 = current phase. */
    fun daysUntilPhase(p: CyclePhase): Int {
        if (p == phase) return 0
        val startDay = phaseStartDay(p)
        return if (startDay > dayInCycle) {
            startDay - dayInCycle
        } else {
            // Phase already passed this cycle — show days until next cycle's occurrence
            (cycleLength - dayInCycle) + startDay
        }
    }
}
