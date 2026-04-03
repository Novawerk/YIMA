package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.haodong.yimalaile.domain.menstrual.CyclePhaseInfo
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.domain.menstrual.PredictedCycle
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.until

/**
 * Thin orchestrator — dispatches to HeroSection, HistorySection, and PhaseSheet.
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
    onRecordClick: (MenstrualRecord, Boolean) -> Unit,
    onPredictionClick: (PredictedCycle) -> Unit,
) {
    var showPhaseSheet by remember { mutableStateOf(false) }
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
        onRecordClick = onRecordClick,
        onPredictionClick = onPredictionClick,
    )

    if (showPhaseSheet && phaseInfo != null) {
        PhaseExplanationSheet(
            phaseInfo = phaseInfo,
            onDismiss = { showPhaseSheet = false },
        )
    }
}
