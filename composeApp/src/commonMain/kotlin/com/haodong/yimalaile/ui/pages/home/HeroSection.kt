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
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.ui.components.SmallSpacer
import com.haodong.yimalaile.ui.pages.sheet.SheetManager
import com.haodong.yimalaile.ui.theme.expressiveShapes
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

    val allRecords = cycleState.records.filter { !it.isDeleted }
    val sortedAsc = allRecords.sortedBy { it.startDate }

    // Compute stats — always based on last 6 cycles
    val completedRecords = sortedAsc.filter { it.endDate != null && it.endConfirmed }
    val cycleGaps = sortedAsc.zipWithNext().map { (a, b) ->
        a.startDate.until(b.startDate, DateTimeUnit.DAY).toInt()
    }
    val last6Gaps = cycleGaps.takeLast(6)
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
            )
        }

        // ── Legend ──
        item {
            LegendRow()
        }

        // ── Averages ──
        if (avgPeriod != null || avgCycle != null) {
            item {
                AveragesCard(avgPeriod = avgPeriod, avgCycle = avgCycle)
            }
        }

        // ── History Table ──
        item {
            HistoryCard(
                records = allRecords,
                sortedAsc = sortedAsc,
                onRecordClick = { record ->
                    scope.launch {
                        sheetManager.showAndHandleRecordDetail(record)
                        onRefresh()
                    }
                },
            )
        }

        // Bottom padding so content doesn't hide behind floating toolbar
        item {
            SmallSpacer(64)
        }
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
) {
    data class BarData(val label: String, val days: Int, val predicted: Boolean)

    val actualBars = records.zipWithNext().map { (cur, next) ->
        val cycleLen = cur.startDate.until(next.startDate, DateTimeUnit.DAY).toInt()
        BarData(
            label = "${cur.startDate.monthNumber}/${cur.startDate.dayOfMonth}",
            days = cycleLen,
            predicted = false,
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
    val maxVal = allBars.maxOf { it.days }.coerceAtLeast(1)
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
            Text(
                stringResource(Res.string.stats_chart_title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
                        // Outlier: deviates >30% from average
                        val isOutlier = avgCycle != null && avgCycle > 0 && !bar.predicted &&
                                kotlin.math.abs(bar.days - avgCycle) > avgCycle * 0.3f
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
                                Surface (
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(barH),
                                    shape = RoundedCornerShape(barWidth / 2),
                                    color = thisBarColor,
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(top = 4.dp),
                                    ) {
                                        // Icon inside bar for outliers
                                        if (isOutlier && barH >= 52.dp) {
                                            Box(
                                                modifier = Modifier
                                                    .size(barWidth - 8.dp)
                                                    .clip(MaterialTheme.expressiveShapes.sunny)
                                                    .background(
                                                        White.copy(alpha = 0.3f),
                                                    ),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(
                                                    Icons.Rounded.PriorityHigh,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                )
                                            }
                                            SmallSpacer(2)
                                        }
                                        // Number
                                        if (barH >= 28.dp) {
                                            SmallSpacer(4)
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
        Row(
            Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(Res.string.stats_avg_section_title, 6),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            // Period days
            Text(
                stringResource(Res.string.stats_period_days),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SmallSpacer(4)
            Text(
                "${avgPeriod ?: "-"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            SmallSpacer(16)
            // Cycle days
            Text(
                stringResource(Res.string.stats_cycle_days),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SmallSpacer(4)
            Text(
                "${avgCycle ?: "-"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════
// History List
// ════════════════════════════════════════════════════════════════

@Composable
private fun HistoryCard(
    records: List<MenstrualRecord>,
    sortedAsc: List<MenstrualRecord>,
    onRecordClick: (MenstrualRecord) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        records.forEach { record ->
            val periodDays = record.endDate?.let {
                record.startDate.until(it, DateTimeUnit.DAY).toInt() + 1
            }
            val ascIdx = sortedAsc.indexOf(record)
            val cycleLen = if (ascIdx > 0) {
                sortedAsc[ascIdx - 1].startDate.until(record.startDate, DateTimeUnit.DAY).toInt()
            } else null
            val isCurrent = record == records.first() && !record.endConfirmed
            val dateStr = "${record.startDate.monthNumber}/${record.startDate.dayOfMonth}"

            Surface(
                onClick = { onRecordClick(record) },
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.large,
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Date
                    Text(
                        "$dateStr ${record.startDate.year}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                    )

                    // Period days: icon + number
                    if (periodDays != null) {
                        Icon(
                            Icons.Rounded.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(14.dp),
                        )
                        SmallSpacer(3)
                        Text(
                            "$periodDays",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    SmallSpacer(16)

                    // Cycle length: icon + number, or "Current" badge
                    if (isCurrent) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(50),
                        ) {
                            Text(
                                stringResource(Res.string.stats_current_cycle),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    } else {
                        Icon(
                            Icons.Rounded.Autorenew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp),
                        )
                        SmallSpacer(3)
                        Text(
                            if (cycleLen != null) "$cycleLen" else "-",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}
