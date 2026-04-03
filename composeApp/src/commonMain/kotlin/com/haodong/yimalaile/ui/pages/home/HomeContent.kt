package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.haodong.yimalaile.domain.menstrual.CyclePhaseInfo
import com.haodong.yimalaile.domain.menstrual.CycleState
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.until

/**
 * Thin orchestrator — dispatches to HeroSection, HistorySection, PhaseSheet, and CycleCalendarSheet.
 */
@Composable
internal fun ColumnScope.HomeContent(
    state: CycleState,
    phaseInfo: CyclePhaseInfo?,
    today: LocalDate,
    onStartPeriod: () -> Unit,
    onEndPeriod: () -> Unit,
    onLogDay: () -> Unit,
    onBackfill: () -> Unit,
) {
    var showPhaseSheet by remember { mutableStateOf(false) }
    var showCalendarSheet by remember { mutableStateOf(false) }
    val inPeriod = state.activePeriod != null
    val dayCount = if (inPeriod) {
        state.activePeriod!!.startDate.until(today, DateTimeUnit.DAY).toInt() + 1
    } else null
    val heroNumber = if (inPeriod) dayCount ?: 0 else phaseInfo?.daysUntilNextPeriod ?: 0

    HeroSection(
        inPeriod = inPeriod,
        heroNumber = heroNumber,
        dayCount = dayCount,
        phaseInfo = phaseInfo,
        onPhaseClick = { showPhaseSheet = true },
        onCalendarClick = { showCalendarSheet = true },
    )

    HistorySection(
        state = state,
        phaseInfo = phaseInfo,
        today = today,
        inPeriod = inPeriod,
        onStartPeriod = onStartPeriod,
        onEndPeriod = onEndPeriod,
        onLogDay = onLogDay,
        onBackfill = onBackfill,
        onCalendarClick = { showCalendarSheet = true },
    )

    if (showPhaseSheet && phaseInfo != null) {
        PhaseExplanationSheet(
            phaseInfo = phaseInfo,
            onDismiss = { showPhaseSheet = false },
        )
    }

    if (showCalendarSheet) {
        CycleCalendarSheet(
            state = state,
            phaseInfo = phaseInfo,
            onDismiss = { showCalendarSheet = false },
        )
    }
}
