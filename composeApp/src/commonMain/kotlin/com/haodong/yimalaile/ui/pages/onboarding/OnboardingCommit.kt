package com.haodong.yimalaile.ui.pages.onboarding

import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.domain.settings.SettingsRepository
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/** A meaningful threshold for "we have enough cycles to predict without padding". */
internal const val ENOUGH_RECORDS_FOR_PREDICTION = 3

/**
 * Persist the onboarding result. Saves any manually-entered period and pads
 * with synthetic past cycles only when we don't already have enough data
 * for predictions to feel useful.
 */
internal suspend fun commitOnboarding(
    service: MenstrualService,
    settings: SettingsRepository,
    cycleLength: Int,
    periodDuration: Int,
    manualStart: LocalDate?,
    manualEnd: LocalDate?,
) {
    settings.setCycleLength(cycleLength)
    settings.setPeriodDuration(periodDuration)

    if (manualStart != null && manualEnd != null) {
        service.backfillPeriod(manualStart, manualEnd)
    }

    val state = service.getCycleState(cycleLength)
    val recordsCount = state.records.size
    if (recordsCount >= ENOUGH_RECORDS_FOR_PREDICTION) return

    val anchor = state.records.minByOrNull { it.startDate }?.startDate ?: return
    val needed = ENOUGH_RECORDS_FOR_PREDICTION + 2 - recordsCount
    for (i in 1..needed) {
        val pastStart = anchor.minus(cycleLength * i, DateTimeUnit.DAY)
        val pastEnd = pastStart.plus(periodDuration - 1, DateTimeUnit.DAY)
        service.backfillPeriod(pastStart, pastEnd)
    }
}
