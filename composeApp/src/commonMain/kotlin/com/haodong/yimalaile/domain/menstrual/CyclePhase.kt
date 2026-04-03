package com.haodong.yimalaile.domain.menstrual

import kotlinx.datetime.LocalDate

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
    val cycleLength: Int,           // average cycle length in days
    val periodLength: Int,          // average period length in days
    val progress: Float,            // 0f..1f progress through cycle
    val daysUntilNextPeriod: Int,   // days remaining until next predicted period
    val nextPeriodStart: LocalDate?,
) {
    /** Start day (1-based) of each phase within this cycle. */
    fun phaseStartDay(p: CyclePhase): Int = when (p) {
        CyclePhase.MENSTRUAL -> 1
        CyclePhase.FOLLICULAR -> periodLength + 1
        CyclePhase.OVULATION -> (cycleLength * 0.46).toInt() + 1
        CyclePhase.LUTEAL -> (cycleLength * 0.57).toInt() + 1
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
