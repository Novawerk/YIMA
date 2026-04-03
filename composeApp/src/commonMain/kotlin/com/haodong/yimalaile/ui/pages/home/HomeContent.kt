package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.CyclePhase
import com.haodong.yimalaile.domain.menstrual.CyclePhaseInfo
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.ui.components.GrowSpacer
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.components.SmallSpacer
import com.haodong.yimalaile.ui.theme.expressiveShapes
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.until
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

/**
 * Unified home content for both in-period and not-in-period states.
 * Layout: Hero (fills space) → Info cards → History cards → CTA buttons.
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
    val inPeriod = state.activePeriod != null
    val daysUntil = phaseInfo?.daysUntilNextPeriod
    val dayCount = if (inPeriod) {
        state.activePeriod!!.startDate.until(today, DateTimeUnit.DAY).toInt() + 1
    } else null

    // ── Hero Section (fills remaining space) ──
    Column(
        Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        // Title
        Text(
            if (inPeriod) stringResource(Res.string.home_take_care)
            else stringResource(Res.string.home_next_visit),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SmallSpacer(12)

        GrowSpacer()

        // Big countdown number
        val heroNumber = if (inPeriod) dayCount ?: 0 else daysUntil ?: 0
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                "$heroNumber",
                style = MaterialTheme.typography.displayLargeEmphasized,
                fontSize = 128.sp,
                fontWeight = FontWeight.Black,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (phaseInfo != null) {
                    CircularWavyProgressIndicator(
                        progress = { phaseInfo.progress },
                        modifier = Modifier.size(48.dp),
                        wavelength = 12.dp,
                        waveSpeed = 4.dp,
                    )
                }
                SmallSpacer(48)
                Text(
                    text = if (inPeriod) stringResource(Res.string.home_day_n, dayCount ?: 0)
                           else stringResource(Res.string.unit_days),
                    style = MaterialTheme.typography.labelSmall,
                )
                SmallSpacer(24)
            }
        }
        SmallSpacer(24)

        // ── Info Cards ──
        if (phaseInfo != null) {
            // Current phase card
            Surface(
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(Res.string.home_current_phase),
                        style = MaterialTheme.typography.labelSmallEmphasized,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Black,
                    )
                    GrowSpacer()
                    Text(
                        phaseDisplayName(phaseInfo.phase),
                        style = MaterialTheme.typography.bodyLargeEmphasized,
                    )
                    SmallSpacer(8)
                    Surface(
                        modifier = Modifier.size(24.dp),
                        color = phaseColor(phaseInfo.phase),
                        shape = phaseShape(phaseInfo.phase),
                    ) { }
                }
            }
            SmallSpacer(16)

            // Next period / remaining days card
            Surface(
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (inPeriod) stringResource(Res.string.home_estimated_remaining, phaseInfo.periodLength - (dayCount ?: 0))
                        else stringResource(Res.string.home_next_period_starts),
                        style = MaterialTheme.typography.labelSmallEmphasized,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Black,
                    )
                    GrowSpacer()
                    if (!inPeriod && phaseInfo.nextPeriodStart != null) {
                        val d = phaseInfo.nextPeriodStart
                        Text(
                            "${d.monthNumber}/${d.dayOfMonth}",
                            style = MaterialTheme.typography.bodyLargeEmphasized,
                        )
                        SmallSpacer(8)
                        Surface(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.tertiary,
                            shape = MaterialTheme.expressiveShapes.sunny,
                        ) { }
                    }
                }
            }
            SmallSpacer(24)
        }
    }

    // ── Bottom Section: History + CTA ──
    Surface(tonalElevation = 1.dp, shape = MaterialTheme.shapes.extraLarge) {
        Column(
            Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                stringResource(Res.string.home_past_records),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Light,
            )

            // History cards (real data)
            val completedRecords = state.recentPeriods.filter { it.endDate != null }
                .sortedByDescending { it.startDate }
                .take(4)
            val predictions = state.predictions.take(3)

            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Actual period records
                items(completedRecords, key = { it.id }) { record ->
                    val days = record.startDate.until(record.endDate!!, DateTimeUnit.DAY).toInt() + 1
                    HistoryCard(
                        label = "${record.startDate.monthNumber}/${record.startDate.dayOfMonth}",
                        days = days,
                        maxDays = 10,
                        isPrediction = false,
                    )
                }
                // Prediction cards (if sparse data)
                if (completedRecords.size < 3) {
                    items(predictions.size, key = { "pred_$it" }) { i ->
                        val pred = predictions[i]
                        HistoryCard(
                            label = "${pred.predictedStart.monthNumber}/${pred.predictedStart.dayOfMonth}",
                            days = phaseInfo?.periodLength ?: 5,
                            maxDays = 10,
                            isPrediction = true,
                        )
                    }
                }
            }

            // CTA buttons
            if (inPeriod) {
                PrimaryCta(text = stringResource(Res.string.home_log_today), onClick = onLogDay)
                OutlinedButton(
                    onClick = onEndPeriod,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Text(
                        stringResource(Res.string.home_end_period),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                if (completedRecords.size < 2) {
                    OutlinedButton(
                        onClick = onBackfill,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                    ) {
                        Text(
                            stringResource(Res.string.home_backfill_to_predict),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                PrimaryCta(
                    text = "✦ ${stringResource(Res.string.btn_record_period)}",
                    onClick = onStartPeriod,
                )
            }
        }
    }
}

// ============================================================
// History card
// ============================================================

@Composable
private fun HistoryCard(
    label: String,
    days: Int,
    maxDays: Int,
    isPrediction: Boolean,
) {
    val barHeight = (days.toFloat() / maxDays * 60).coerceIn(12f, 60f).dp
    Surface(
        tonalElevation = if (isPrediction) 0.dp else 3.dp,
        shape = MaterialTheme.shapes.small,
        border = if (isPrediction) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
    ) {
        Box(modifier = Modifier.width(78.dp).height(110.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.Center),
                color = if (isPrediction) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            )
            Surface(
                color = if (isPrediction) MaterialTheme.colorScheme.outlineVariant
                        else MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).height(barHeight),
            ) { }
        }
    }
}

// ============================================================
// Phase display helpers
// ============================================================

@Composable
private fun phaseDisplayName(phase: CyclePhase): String = when (phase) {
    CyclePhase.MENSTRUAL -> stringResource(Res.string.phase_menstrual)
    CyclePhase.FOLLICULAR -> stringResource(Res.string.phase_follicular)
    CyclePhase.OVULATION -> stringResource(Res.string.phase_ovulation)
    CyclePhase.LUTEAL -> stringResource(Res.string.phase_luteal)
}

@Composable
private fun phaseShape(phase: CyclePhase) = when (phase) {
    CyclePhase.MENSTRUAL -> MaterialTheme.expressiveShapes.heart
    CyclePhase.FOLLICULAR -> MaterialTheme.expressiveShapes.flower
    CyclePhase.OVULATION -> MaterialTheme.expressiveShapes.sunny
    CyclePhase.LUTEAL -> MaterialTheme.expressiveShapes.bun
}

@Composable
private fun phaseColor(phase: CyclePhase) = when (phase) {
    CyclePhase.MENSTRUAL -> MaterialTheme.colorScheme.error
    CyclePhase.FOLLICULAR -> MaterialTheme.colorScheme.primary
    CyclePhase.OVULATION -> MaterialTheme.colorScheme.tertiary
    CyclePhase.LUTEAL -> MaterialTheme.colorScheme.secondary
}
