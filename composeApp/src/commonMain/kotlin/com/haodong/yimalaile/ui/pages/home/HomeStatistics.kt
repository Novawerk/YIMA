package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.ui.components.SmallSpacer
import com.haodong.yimalaile.ui.pages.sheet.SheetManager
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.until
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*
import kotlin.math.roundToInt

/**
 * Inline statistics view shown when user toggles to stats mode on the home screen.
 * Displays bar chart, averages, and history table.
 */
@Composable
internal fun HomeStatistics(
    cycleState: CycleState,
    sheetManager: SheetManager,
    onRefresh: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var showLegendDialog by remember { mutableStateOf(false) }

    val allRecords = cycleState.records.filter { !it.isDeleted }
    val sortedAsc = allRecords.sortedBy { it.startDate }

    // Compute stats — always based on last 6 cycles
    val completedRecords = sortedAsc.filter { it.endDate != null && it.endConfirmed }
    
    // 过滤掉周期小于 14 天的异常记录进行统计
    val validCycleGaps = sortedAsc.zipWithNext().mapNotNull { (a, b) ->
        val gap = a.startDate.until(b.startDate, DateTimeUnit.DAY).toInt()
        if (gap >= 14) gap else null
    }
    
    val last6Gaps = validCycleGaps.takeLast(6)
    val last6Completed = completedRecords.takeLast(6)
    val avgPeriod = if (last6Completed.isNotEmpty()) {
        last6Completed.map { it.startDate.until(it.endDate!!, DateTimeUnit.DAY).toInt() + 1 }.average().roundToInt()
    } else null
    val avgCycle = if (last6Gaps.isNotEmpty()) {
        last6Gaps.average().roundToInt()
    } else null

    if (allRecords.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                stringResource(Res.string.stats_empty_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
            )
        }
        return
    }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        // ── Bar Chart ──
        item {
            BarChartSection(
                records = sortedAsc,
                avgCycle = avgCycle,
                predictedCycleLength = avgCycle,
                onHelpClick = { showLegendDialog = true },
            )
        }

        // ── Averages ──
        if (avgPeriod != null || avgCycle != null) {
            item {
                AveragesCard(avgPeriod = avgPeriod, avgCycle = avgCycle)
            }
        }

        // ── History Title ──
        item {
            Text(
                stringResource(Res.string.stats_history_title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, start = 8.dp),
            )
        }

        // ── History Table ──
        val defaultCycleLength = avgCycle ?: 28
        sortedAsc.reversed().forEach { record ->
            item(key = record.id) {
                val isCurrent = record == sortedAsc.last() && !record.endConfirmed
                RecordCard(
                    record = record,
                    sortedAsc = sortedAsc,
                    isCurrent = isCurrent,
                    defaultCycleLength = defaultCycleLength,
                    onClick = {
                        scope.launch {
                            sheetManager.showAndHandleRecordDetail(record, defaultCycleLength)
                            onRefresh()
                        }
                    },
                )
            }
        }

        // Bottom padding so content doesn't hide behind floating toolbar
        item {
            SmallSpacer(64)
        }
    }

    // Legend dialog
    if (showLegendDialog) {
        val barColor = MaterialTheme.colorScheme.primary
        val predictedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        AlertDialog(
            onDismissRequest = { showLegendDialog = false },
            confirmButton = {},
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(Res.string.stats_chart_title), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(16.dp).clip(RoundedCornerShape(4.dp)).background(barColor))
                        SmallSpacer(12)
                        Text(stringResource(Res.string.stats_legend_actual), style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(16.dp).clip(RoundedCornerShape(4.dp)).background(predictedColor))
                        SmallSpacer(12)
                        Text(stringResource(Res.string.stats_legend_predicted), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
        )
    }
}

// ════════════════════════════════════════════════════════════════
// Bar Chart
// ════════════════════════════════════════════════════════════════

@Composable
private fun BarChartSection(
    records: List<MenstrualRecord>,
    avgCycle: Int?,
    predictedCycleLength: Int?,
    onHelpClick: () -> Unit,
) {
    data class BarData(val label: String, val days: Int, val predicted: Boolean, val isAnomaly: Boolean = false)

    val actualBars = records.zipWithNext().map { (cur, next) ->
        val cycleLen = cur.startDate.until(next.startDate, DateTimeUnit.DAY).toInt()
        val isAnomaly = cycleLen < 14
        BarData(
            label = "${cur.startDate.monthNumber}/${cur.startDate.dayOfMonth}",
            days = cycleLen,
            predicted = false,
            isAnomaly = isAnomaly
        )
    }

    val predictedBar = if (predictedCycleLength != null && records.isNotEmpty()) {
        val last = records.last()
        listOf(
            BarData(
                label = "${last.startDate.monthNumber}/${last.startDate.dayOfMonth}",
                days = predictedCycleLength,
                predicted = true,
            )
        )
    } else emptyList()

    val allBars = actualBars + predictedBar
    if (allBars.isEmpty()) return

    val barColor = MaterialTheme.colorScheme.primary
    val predictedBarColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
    val warningColor = MaterialTheme.colorScheme.tertiary
    val maxVal = (allBars.maxOf { it.days } * 1.25f).toInt().coerceAtLeast(1)
    val chartHeight = 120.dp
    val barWidth = 32.dp

    // Scroll to end on first composition
    val listState = rememberLazyListState()
    LaunchedEffect(allBars.size) {
        listState.scrollToItem(allBars.lastIndex.coerceAtLeast(0))
    }

    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(Res.string.stats_chart_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onHelpClick,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.Outlined.HelpOutline,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                }
            }
            SmallSpacer(16)

            // Chart area with average line overlay
            Box(Modifier.fillMaxWidth()) {
                LazyRow(
                    state = listState,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    itemsIndexed(allBars) { _, bar ->
                        val fraction = bar.days.toFloat() / maxVal
                        val barH = chartHeight * fraction
                        // Outlier: deviates >30% from average, or is anomaly
                        val isOutlier = (avgCycle != null && avgCycle > 0 && !bar.predicted &&
                                kotlin.math.abs(bar.days - avgCycle) > avgCycle * 0.3f) || bar.isAnomaly
                        val thisBarColor = when {
                            bar.predicted -> predictedBarColor
                            isOutlier -> warningColor
                            else -> barColor
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(barWidth),
                        ) {
                            // Bar area — fixed height, bar grows from bottom
                            Box(
                                modifier = Modifier.height(chartHeight).width(barWidth),
                                contentAlignment = Alignment.BottomCenter,
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(barH),
                                    shape = RoundedCornerShape(barWidth / 2),
                                    color = thisBarColor,
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize(),
                                    ) {
                                        if (barH >= 28.dp) {
                                            Text(
                                                "${bar.days}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                    }
                                }
                            }
                            SmallSpacer(4)
                            Text(
                                bar.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isOutlier) warningColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 9.sp,
                            )
                        }
                    }
                }

                // Average dashed line overlay
                if (avgCycle != null && avgCycle > 0) {
                    val avgFraction = avgCycle.toFloat() / maxVal
                    val lineOffset = chartHeight * (1f - avgFraction)
                    val lineColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .offset(y = lineOffset),
                    ) {
                        val dashW = 6.dp.toPx()
                        val gapW = 4.dp.toPx()
                        var x = 0f
                        while (x < size.width) {
                            drawLine(
                                color = lineColor,
                                start = androidx.compose.ui.geometry.Offset(x, size.height / 2),
                                end = androidx.compose.ui.geometry.Offset(
                                    (x + dashW).coerceAtMost(size.width),
                                    size.height / 2
                                ),
                                strokeWidth = 1.5.dp.toPx(),
                            )
                            x += dashW + gapW
                        }
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// Legend
// ════════════════════════════════════════════════════════════════

@Composable
private fun LegendRow() {
    val dotSize = 10.dp
    val barColor = MaterialTheme.colorScheme.tertiary
    val predictedColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(modifier = Modifier.size(dotSize), color = barColor, shape = MaterialTheme.shapes.extraSmall) {}
        SmallSpacer(6)
        Text(
            stringResource(Res.string.stats_legend_actual),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SmallSpacer(24)
        Surface(modifier = Modifier.size(dotSize), color = predictedColor, shape = MaterialTheme.shapes.extraSmall) {}
        SmallSpacer(6)
        Text(
            stringResource(Res.string.stats_legend_predicted),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ════════════════════════════════════════════════════════════════
// Averages Card
// ════════════════════════════════════════════════════════════════

@Composable
private fun AveragesCard(avgPeriod: Int?, avgCycle: Int?) {
    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
        ) {
            Text(
                stringResource(Res.string.stats_avg_section_title, 6),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SmallSpacer(28)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Period days
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${avgPeriod ?: "-"}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    SmallSpacer(4)
                    Text(
                        stringResource(Res.string.stats_period_days),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                // Cycle days
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${avgCycle ?: "-"}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    SmallSpacer(4)
                    Text(
                        stringResource(Res.string.stats_cycle_days),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// History List
// ════════════════════════════════════════════════════════════════

