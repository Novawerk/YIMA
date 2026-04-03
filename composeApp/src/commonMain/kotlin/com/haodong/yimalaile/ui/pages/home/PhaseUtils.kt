package com.haodong.yimalaile.ui.pages.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import com.haodong.yimalaile.domain.menstrual.CyclePhase
import com.haodong.yimalaile.ui.theme.expressiveShapes
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

@Composable
internal fun phaseDisplayName(phase: CyclePhase): String = when (phase) {
    CyclePhase.MENSTRUAL -> stringResource(Res.string.phase_menstrual)
    CyclePhase.FOLLICULAR -> stringResource(Res.string.phase_follicular)
    CyclePhase.OVULATION -> stringResource(Res.string.phase_ovulation)
    CyclePhase.LUTEAL -> stringResource(Res.string.phase_luteal)
}

@Composable
internal fun phaseDescription(phase: CyclePhase): String = when (phase) {
    CyclePhase.MENSTRUAL -> stringResource(Res.string.phase_menstrual_desc)
    CyclePhase.FOLLICULAR -> stringResource(Res.string.phase_follicular_desc)
    CyclePhase.OVULATION -> stringResource(Res.string.phase_ovulation_desc)
    CyclePhase.LUTEAL -> stringResource(Res.string.phase_luteal_desc)
}

@Composable
internal fun phaseShape(phase: CyclePhase): Shape = when (phase) {
    CyclePhase.MENSTRUAL -> MaterialTheme.expressiveShapes.heart
    CyclePhase.FOLLICULAR -> MaterialTheme.expressiveShapes.flower
    CyclePhase.OVULATION -> MaterialTheme.expressiveShapes.sunny
    CyclePhase.LUTEAL -> MaterialTheme.expressiveShapes.bun
}

@Composable
internal fun phaseColor(phase: CyclePhase) = when (phase) {
    CyclePhase.MENSTRUAL -> MaterialTheme.colorScheme.error
    CyclePhase.FOLLICULAR -> MaterialTheme.colorScheme.primary
    CyclePhase.OVULATION -> MaterialTheme.colorScheme.tertiary
    CyclePhase.LUTEAL -> MaterialTheme.colorScheme.secondary
}
