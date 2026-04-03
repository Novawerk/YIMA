package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.ui.components.BigStatCard
import com.haodong.yimalaile.ui.components.HeartDecoration
import com.haodong.yimalaile.ui.components.PeriodDurationChart
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.components.StatusPill
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.until
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

/** Home screen content when not currently in a period — shows predictions and stats. */
@Composable
internal fun ColumnScope.NotInPeriodContent(
    state: CycleState,
    today: LocalDate,
    onStartPeriod: () -> Unit,
    onBackfill: () -> Unit,
    onNavigateStatistics: () -> Unit,
) {
    val prediction = state.predictions.firstOrNull()
    val daysUntil = prediction?.let { today.until(it.predictedStart, DateTimeUnit.DAY).toInt() }

    val (question, heroText) = when {
        daysUntil == null -> "" to stringResource(Res.string.status_no_prediction)
        daysUntil < 0 -> stringResource(Res.string.home_hero_overdue_sub) to stringResource(Res.string.home_hero_overdue)
        daysUntil <= 3 -> stringResource(Res.string.home_hero_soon_sub) to stringResource(Res.string.home_hero_soon)
        else -> stringResource(Res.string.home_hero_early_sub) to stringResource(Res.string.home_hero_early)
    }

    Text(
        question,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(8.dp))

    Row(verticalAlignment = Alignment.Top) {
        Text(
            heroText,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        HeartDecoration()
    }

    Spacer(Modifier.height(16.dp))

    if (prediction != null) {
        val startDate = prediction.predictedStart
        val label = if (daysUntil != null && daysUntil < 0) {
            stringResource(Res.string.home_predicted_delayed, startDate.monthNumber, startDate.dayOfMonth, -daysUntil)
        } else if (daysUntil != null) {
            stringResource(Res.string.home_predicted_in_days, startDate.monthNumber, startDate.dayOfMonth, daysUntil)
        } else ""
        StatusPill(label)
    }

    Spacer(Modifier.height(24.dp))

    // Stat cards
    val daysStr = stringResource(Res.string.unit_days)
    val records = state.recentPeriods
    val avgCycle = if (records.size >= 2) {
        val sorted = records.sortedBy { it.startDate }
        val gaps = sorted.zipWithNext().map { (a, b) -> a.startDate.until(b.startDate, DateTimeUnit.DAY).toInt() }
        gaps.sum() / gaps.size
    } else null
    val avgPeriod = records.filter { it.endDate != null }.let { list ->
        if (list.isEmpty()) null
        else list.sumOf { it.startDate.until(it.endDate!!, DateTimeUnit.DAY).toInt() + 1 } / list.size
    }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        BigStatCard(
            stringResource(Res.string.stat_cycle_length),
            avgCycle?.toString() ?: "--", daysStr,
            Modifier.weight(1f),
        )
        BigStatCard(
            stringResource(Res.string.stat_period_length),
            avgPeriod?.toString() ?: "--", daysStr,
            Modifier.weight(1f),
        )
    }

    // Period duration dots
    val withEnd = records.filter { it.endDate != null }.sortedBy { it.startDate }
    if (withEnd.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        PeriodDurationChart(withEnd, daysStr, onViewAll = onNavigateStatistics)
    }

    // Confidence
    if (avgCycle != null && records.size >= 2) {
        Spacer(Modifier.height(12.dp))
        val sorted = records.sortedBy { it.startDate }
        val gaps = sorted.zipWithNext().map { (a, b) -> a.startDate.until(b.startDate, DateTimeUnit.DAY).toInt() }
        val variance = gaps.map { (it - avgCycle) * (it - avgCycle) }.average()
        val confidence = when {
            sorted.size >= 6 && variance < 9 -> stringResource(Res.string.stats_confidence_high)
            sorted.size >= 3 -> stringResource(Res.string.stats_confidence_medium)
            else -> stringResource(Res.string.stats_confidence_low)
        }
        Row(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(Res.string.home_prediction_confidence), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            StatusPill(confidence)
        }
    }

    Spacer(Modifier.weight(1f))

    if (avgCycle == null) {
        OutlinedButton(
            onClick = onBackfill,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
        ) {
            Text(stringResource(Res.string.home_backfill_to_predict), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(12.dp))
    }

    PrimaryCta(
        text = "✦ ${stringResource(Res.string.btn_record_period)}",
        onClick = onStartPeriod,
    )
}
