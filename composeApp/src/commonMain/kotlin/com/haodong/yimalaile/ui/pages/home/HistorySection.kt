package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.CyclePhaseInfo
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.domain.menstrual.DailyRecord
import com.haodong.yimalaile.ui.components.CycleCalendarGrid
import com.haodong.yimalaile.ui.components.CycleCalendarLegend
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.components.SmallSpacer
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.until
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

/**
 * Bottom section with calendar/stats toggle.
 * Default: calendar mode showing CycleCalendarGrid inline + phase chip.
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

    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(Modifier.padding(16.dp)) {
            // Toggle: calendar / stats
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Phase chip
                if (phaseInfo != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(50),
                    ) {
                        Row(
                            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            com.haodong.yimalaile.ui.components.DecorShape(
                                size = 10,
                                shape = phaseShape(phaseInfo.phase),
                                color = phaseColor(phaseInfo.phase),
                            )
                            Text(
                                phaseDisplayName(phaseInfo.phase),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            if (!inPeriod) {
                                Text(
                                    "· ${phaseInfo.daysUntilNextPeriod}${stringResource(Res.string.unit_days)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                // Toggle
                val calLabel = stringResource(Res.string.home_cycle_calendar)
                val statsLabel = stringResource(Res.string.home_past_records)
                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = calendarMode,
                        onClick = { onToggleMode(true) },
                        shape = SegmentedButtonDefaults.itemShape(0, 2),
                    ) { Text("📅", style = MaterialTheme.typography.labelSmall) }
                    SegmentedButton(
                        selected = !calendarMode,
                        onClick = { onToggleMode(false) },
                        shape = SegmentedButtonDefaults.itemShape(1, 2),
                    ) { Text("📊", style = MaterialTheme.typography.labelSmall) }
                }
            }

            SmallSpacer(12)

            if (!calendarMode) {
                // Stats mode — compact stats
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
                    else list.sumOf {
                        it.startDate.until(it.endDate!!, DateTimeUnit.DAY).toInt() + 1
                    } / list.size
                }
                val daysStr = stringResource(Res.string.unit_days)

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        label = stringResource(Res.string.stats_avg_cycle),
                        value = avgCycle?.toString() ?: "--",
                        unit = daysStr,
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = stringResource(Res.string.stats_avg_period),
                        value = avgPeriod?.toString() ?: "--",
                        unit = daysStr,
                        modifier = Modifier.weight(1f),
                    )
                }
                SmallSpacer(8)
                Text(
                    "${records.size} ${stringResource(Res.string.home_past_records)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SmallSpacer(12)

            // CTA buttons
            if (inPeriod) {
                val todayRecord = state.activePeriod?.dailyRecords?.find { it.date == today }
                if (todayRecord != null) {
                    TodaySummaryCard(todayRecord, onClick = onLogDay)
                } else {
                    PrimaryCta(text = stringResource(Res.string.home_log_today), onClick = onLogDay)
                }
                SmallSpacer(8)
                OutlinedButton(
                    onClick = onEndPeriod,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                ) {
                    Text(stringResource(Res.string.home_end_period), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                if (state.recentPeriods.size < 2) {
                    OutlinedButton(
                        onClick = onBackfill,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                    ) {
                        Text(stringResource(Res.string.home_backfill_to_predict), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    SmallSpacer(8)
                }
                PrimaryCta(text = "✦ ${stringResource(Res.string.btn_record_period)}", onClick = onStartPeriod)
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
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth().height(56.dp),
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
                Text(moodIcon, style = MaterialTheme.typography.titleMedium)
                SmallSpacer(8)
            }
            if (record.intensity != null) {
                Text(
                    when (record.intensity) {
                        com.haodong.yimalaile.domain.menstrual.Intensity.LIGHT -> stringResource(Res.string.intensity_light)
                        com.haodong.yimalaile.domain.menstrual.Intensity.MEDIUM -> stringResource(Res.string.intensity_medium)
                        com.haodong.yimalaile.domain.menstrual.Intensity.HEAVY -> stringResource(Res.string.intensity_heavy)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Spacer(Modifier.weight(1f))
            Text("→", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
        }
    }
}
