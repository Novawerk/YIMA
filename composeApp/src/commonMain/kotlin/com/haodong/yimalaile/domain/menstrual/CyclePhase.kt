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
)
