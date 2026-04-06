package com.haodong.yimalaile.ui.pages.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.haodong.yimalaile.domain.menstrual.CyclePhase
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

@Composable
internal fun CyclePhase.description(): String = when (this) {
    CyclePhase.MENSTRUAL -> stringResource(Res.string.phase_menstrual_desc)
    CyclePhase.FOLLICULAR -> stringResource(Res.string.phase_follicular_desc)
    CyclePhase.OVULATION -> stringResource(Res.string.phase_ovulation_desc)
    CyclePhase.LUTEAL -> stringResource(Res.string.phase_luteal_desc)
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
