package com.haodong.yimalaile.ui.pages.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.haodong.yimalaile.domain.menstrual.CyclePhase
import com.haodong.yimalaile.domain.menstrual.CyclePhaseInfo
import com.haodong.yimalaile.ui.theme.expressiveShapes
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

@Composable
internal fun CyclePhase.displayName(): String = when (this) {
    CyclePhase.MENSTRUAL -> stringResource(Res.string.phase_menstrual)
    CyclePhase.FOLLICULAR -> stringResource(Res.string.phase_follicular)
    CyclePhase.OVULATION -> stringResource(Res.string.phase_ovulation)
    CyclePhase.LUTEAL -> stringResource(Res.string.phase_luteal)
}

/**
 * Day-aware label: shows "排卵日" (Ovulation day) on the peak day,
 * otherwise falls back to the broader phase name (e.g. "排卵期").
 */
@Composable
internal fun CyclePhaseInfo.dayLabel(): String =
    if (isOvulationPeakDay) stringResource(Res.string.detail_ovulation_day)
    else phase.displayName()

@Composable
internal fun CyclePhase.description(): String = when (this) {
    CyclePhase.MENSTRUAL -> stringResource(Res.string.phase_menstrual_desc)
    CyclePhase.FOLLICULAR -> stringResource(Res.string.phase_follicular_desc)
    CyclePhase.OVULATION -> stringResource(Res.string.phase_ovulation_desc)
    CyclePhase.LUTEAL -> stringResource(Res.string.phase_luteal_desc)
}

@Composable
internal fun CyclePhase.description(info: CyclePhaseInfo): String {
    if (this == CyclePhase.MENSTRUAL) {
        return when (info.dayInCycle) {
            1 -> stringResource(Res.string.phase_menstrual_desc_day1)
            2 -> stringResource(Res.string.phase_menstrual_desc_day2)
            3 -> stringResource(Res.string.phase_menstrual_desc_day3)
            4 -> stringResource(Res.string.phase_menstrual_desc_day4)
            else -> stringResource(Res.string.phase_menstrual_desc_day5_plus)
        }
    }

    val phaseStart = info.phaseStartDay(this)
    val nextPhase = when (this) {
        CyclePhase.FOLLICULAR -> CyclePhase.OVULATION
        CyclePhase.OVULATION -> CyclePhase.LUTEAL
        else -> null
    }
    val phaseEnd = nextPhase?.let { info.phaseStartDay(it) - 1 } ?: info.cycleLength
    val phaseLen = (phaseEnd - phaseStart + 1).coerceAtLeast(1)
    val dayInPhase = (info.dayInCycle - phaseStart + 1).coerceIn(1, phaseLen)
    val progress = (dayInPhase - 1).toFloat() / phaseLen.toFloat()

    return when (this) {
        CyclePhase.FOLLICULAR -> when {
            progress < 0.34f -> stringResource(Res.string.phase_follicular_desc_early)
            progress < 0.67f -> stringResource(Res.string.phase_follicular_desc_mid)
            else -> stringResource(Res.string.phase_follicular_desc_late)
        }
        CyclePhase.OVULATION -> when {
            info.isOvulationPeakDay -> stringResource(Res.string.phase_ovulation_desc_mid)
            info.dayInCycle < info.peakDayInCycle -> stringResource(Res.string.phase_ovulation_desc_early)
            else -> stringResource(Res.string.phase_ovulation_desc_late)
        }
        CyclePhase.LUTEAL -> when {
            progress < 0.34f -> stringResource(Res.string.phase_luteal_desc_early)
            progress < 0.67f -> stringResource(Res.string.phase_luteal_desc_mid)
            else -> stringResource(Res.string.phase_luteal_desc_late)
        }
        else -> description()
    }
}

@Composable
internal fun CyclePhase.shape(): Shape = when (this) {
    CyclePhase.MENSTRUAL -> MaterialTheme.expressiveShapes.arrow
    CyclePhase.FOLLICULAR -> MaterialTheme.expressiveShapes.flower
    CyclePhase.OVULATION -> MaterialTheme.expressiveShapes.sunny
    CyclePhase.LUTEAL -> MaterialTheme.expressiveShapes.bun
}

@Composable
internal fun CyclePhase.color(): Color = when (this) {
    CyclePhase.MENSTRUAL -> MaterialTheme.colorScheme.error
    CyclePhase.FOLLICULAR -> MaterialTheme.colorScheme.primary
    CyclePhase.OVULATION -> MaterialTheme.colorScheme.tertiary
    CyclePhase.LUTEAL -> MaterialTheme.colorScheme.secondary
}
