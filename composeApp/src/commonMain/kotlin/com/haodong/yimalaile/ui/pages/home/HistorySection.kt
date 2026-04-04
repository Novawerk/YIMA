package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.CyclePhaseInfo
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.domain.menstrual.DailyRecord
import com.haodong.yimalaile.ui.components.SmallSpacer
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.until
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

/**
 * Bottom docked toolbar — M3 Expressive style.
 * Contains mode toggle icons + primary action FAB.
 */
@Composable
internal fun BottomSection(
    state: CycleState,
    phaseInfo: CyclePhaseInfo?,
    today: LocalDate,
    inPeriod: Boolean,
    calendarMode: Boolean,
    onToggleMode: (Boolean) -> Unit,
    onStartPeriod: () -> Unit,
    onEndPeriod: () -> Unit,
    onLogDay: () -> Unit,
    onBackfill: () -> Unit,
) {
    // Stats mode content above the toolbar
    if (!calendarMode) {
        Surface(
            tonalElevation = 1.dp,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth(),
        ) {
            val records = state.recentPeriods
            val avgCycle = if (records.size >= 2) {
                val sorted = records.sortedBy { it.startDate }
                val gaps = sorted.zipWithNext().map { (a, b) ->
                    a.startDate.until(b.startDate, DateTimeUnit.DAY).toInt()
                }
                gaps.sum() / gaps.size
            } else null
            val avgPeriod = records.filter { it.endDate != null }.let { list ->
                if (list.isEmpty()) null
                else list.sumOf { it.startDate.until(it.endDate!!, DateTimeUnit.DAY).toInt() + 1 } / list.size
            }
            val daysStr = stringResource(Res.string.unit_days)

            Row(
                Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatCard(stringResource(Res.string.stats_avg_cycle), avgCycle?.toString() ?: "--", daysStr, Modifier.weight(1f))
                StatCard(stringResource(Res.string.stats_avg_period), avgPeriod?.toString() ?: "--", daysStr, Modifier.weight(1f))
            }
        }
        SmallSpacer(8)
    }

    // Today summary (if in period and today logged)
    if (inPeriod) {
        val todayRecord = state.activePeriod?.dailyRecords?.find { it.date == today }
        if (todayRecord != null) {
            TodaySummaryCard(todayRecord, onClick = onLogDay)
            SmallSpacer(8)
        }
    }

    // Docked toolbar
    Surface(
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(50),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Mode toggle icons
            IconButton(
                onClick = { onToggleMode(true) },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = if (calendarMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Text("📅", fontSize = 20.sp)
            }
            IconButton(
                onClick = { onToggleMode(false) },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = if (!calendarMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Text("📊", fontSize = 20.sp)
            }

            Spacer(Modifier.weight(1f))

            // Secondary action (if applicable)
            if (inPeriod) {
                TextButton(onClick = onEndPeriod) {
                    Text(stringResource(Res.string.home_end_period), style = MaterialTheme.typography.labelMedium)
                }
            } else if (state.recentPeriods.size < 2) {
                TextButton(onClick = onBackfill) {
                    Text(stringResource(Res.string.home_backfill_to_predict), style = MaterialTheme.typography.labelSmall)
                }
            }

            // Primary action FAB
            val primaryAction = if (inPeriod) {
                val todayRecord = state.activePeriod?.dailyRecords?.find { it.date == today }
                if (todayRecord != null) null // already logged, summary card shown above
                else Pair(stringResource(Res.string.home_log_today), onLogDay)
            } else {
                Pair(stringResource(Res.string.btn_record_period), onStartPeriod)
            }

            if (primaryAction != null) {
                SmallSpacer(8)
                FloatingActionButton(
                    onClick = primaryAction.second,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.height(48.dp),
                ) {
                    Row(
                        Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        SmallSpacer(6)
                        Text(primaryAction.first, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier,
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            SmallSpacer(4)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                SmallSpacer(4)
                Text(unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun TodaySummaryCard(record: DailyRecord, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(50),
        modifier = Modifier.fillMaxWidth().height(48.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val moodIcon = when (record.mood) {
                com.haodong.yimalaile.domain.menstrual.Mood.HAPPY -> "😊"
                com.haodong.yimalaile.domain.menstrual.Mood.NEUTRAL -> "😐"
                com.haodong.yimalaile.domain.menstrual.Mood.SAD -> "😔"
                com.haodong.yimalaile.domain.menstrual.Mood.VERY_SAD -> "😢"
                null -> null
            }
            if (moodIcon != null) {
                Text(moodIcon, fontSize = 16.sp)
                SmallSpacer(8)
            }
            if (record.intensity != null) {
                Text(
                    when (record.intensity) {
                        com.haodong.yimalaile.domain.menstrual.Intensity.LIGHT -> stringResource(Res.string.intensity_light)
                        com.haodong.yimalaile.domain.menstrual.Intensity.MEDIUM -> stringResource(Res.string.intensity_medium)
                        com.haodong.yimalaile.domain.menstrual.Intensity.HEAVY -> stringResource(Res.string.intensity_heavy)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Spacer(Modifier.weight(1f))
            Text("→", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
        }
    }
}
