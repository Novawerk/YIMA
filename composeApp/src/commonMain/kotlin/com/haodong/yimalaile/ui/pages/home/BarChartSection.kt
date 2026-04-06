package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.ui.components.SmallSpacer
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.until
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*
import kotlin.math.roundToInt
@Composable
fun BarChartSection(
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

