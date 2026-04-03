package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.ui.components.CompactStatRow
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.components.StatusPill
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.until
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

@Composable
internal fun ColumnScope.InPeriodContent(
    state: CycleState,
    today: LocalDate,
    onEndPeriod: () -> Unit,
    onLogDay: () -> Unit,
) {
    val activePeriod = state.activePeriod ?: return
    val dayCount = activePeriod.startDate.until(today, DateTimeUnit.DAY).toInt() + 1

    val completedPeriods = state.recentPeriods.filter { it.endDate != null }
    val avgPeriodLen = if (completedPeriods.isNotEmpty()) {
        completedPeriods.sumOf { it.startDate.until(it.endDate!!, DateTimeUnit.DAY).toInt() + 1 } / completedPeriods.size
    } else null
    val remainingDays = avgPeriodLen?.let { (it - dayCount).coerceAtLeast(0) }

    // ── Hero (fills remaining space, centered) ──
    Box(
        Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(Res.string.home_take_care),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(Res.string.home_in_period),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(20.dp))
            StatusPill(stringResource(Res.string.home_day_n, dayCount))
            if (remainingDays != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    if (remainingDays > 0) stringResource(Res.string.home_remaining_days, remainingDays)
                    else stringResource(Res.string.home_exceeded_avg),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (remainingDays > 0) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.primary,
                )
            }
        }
    }

    // ── Bottom: stats + actions ──
    val daysStr = stringResource(Res.string.unit_days)
    val avgCycleStr = stringResource(Res.string.stat_cycle_length)
    val avgPeriodStr = stringResource(Res.string.stat_period_length)

    if (avgPeriodLen != null) {
        val avgCycle = if (state.recentPeriods.size >= 2) {
            val sorted = state.recentPeriods.sortedBy { it.startDate }
            val gaps = sorted.zipWithNext().map { (a, b) -> a.startDate.until(b.startDate, DateTimeUnit.DAY).toInt() }
            gaps.sum() / gaps.size
        } else null

        CompactStatRow(
            items = listOfNotNull(
                avgCycle?.let { "$it $daysStr $avgCycleStr" },
                "$avgPeriodLen $daysStr $avgPeriodStr",
            ),
        )
        Spacer(Modifier.height(16.dp))
    }

    PrimaryCta(text = stringResource(Res.string.home_log_today), onClick = onLogDay)
    Spacer(Modifier.height(8.dp))
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
}
