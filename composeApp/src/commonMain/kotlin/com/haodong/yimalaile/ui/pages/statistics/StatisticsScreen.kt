package com.haodong.yimalaile.ui.pages.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.AddRecordResult
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.components.SmallSpacer
import com.haodong.yimalaile.ui.pages.sheet.SheetManager
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.until
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

@Composable
fun StatisticsScreen(
    service: MenstrualService,
    sheetManager: SheetManager,
    onBack: () -> Unit,
) {
    var state by remember { mutableStateOf<CycleState?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val successMsg = stringResource(Res.string.record_save_success)
    val deletedMsg = stringResource(Res.string.record_deleted)

    fun refresh() { scope.launch { state = service.getCycleState() } }
    LaunchedEffect(Unit) { state = service.getCycleState() }

    val s = state
    val allRecords = s?.records?.filter { !it.isDeleted } ?: emptyList()
    val sortedAsc = allRecords.sortedBy { it.startDate }

    // Compute stats
    val completedRecords = sortedAsc.filter { it.endDate != null && it.endConfirmed }
    val cycleGaps = sortedAsc.zipWithNext().map { (a, b) ->
        a.startDate.until(b.startDate, DateTimeUnit.DAY).toInt()
    }
    val avgPeriod = if (completedRecords.isNotEmpty()) {
        completedRecords.map { it.startDate.until(it.endDate!!, DateTimeUnit.DAY).toInt() + 1 }.average().toInt()
    } else null
    val avgCycle = if (cycleGaps.isNotEmpty()) {
        cycleGaps.average().toInt()
    } else null
    val cycleCount = cycleGaps.size.coerceAtLeast(completedRecords.size)

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(horizontal = 20.dp),
    ) {
        // ── App Bar ──
        SmallSpacer(8)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(Res.string.history_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(40.dp))
        }
        SmallSpacer(8)

        if (allRecords.isEmpty()) {
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(Res.string.stats_empty_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.weight(1f))
        } else {
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                // ── Bar Chart ──
                item {
                    ChartSection(
                        records = sortedAsc,
                        cycleGaps = cycleGaps,
                    )
                }

                // ── Legend ──
                item {
                    LegendRow()
                }

                // ── Averages ──
                if (avgPeriod != null || avgCycle != null) {
                    item {
                        AveragesSection(
                            cycleCount = cycleCount,
                            avgPeriod = avgPeriod,
                            avgCycle = avgCycle,
                        )
                    }
                }

                // ── History Table ──
                item {
                    HistoryTableHeader()
                }

                items(allRecords, key = { it.id }) { record ->
                    val periodDays = record.endDate?.let {
                        record.startDate.until(it, DateTimeUnit.DAY).toInt() + 1
                    }
                    val idx = sortedAsc.indexOf(record)
                    val cycleLen = if (idx > 0) {
                        sortedAsc[idx - 1].startDate.until(record.startDate, DateTimeUnit.DAY).toInt()
                    } else null
                    val isCurrent = record == allRecords.first() && !record.endConfirmed

                    HistoryTableRow(
                        record = record,
                        periodDays = periodDays,
                        cycleLength = cycleLen,
                        isCurrent = isCurrent,
                        onClick = {
                            scope.launch {
                                sheetManager.showAndHandleRecordDetail(record)
                                refresh()
                            }
                        },
                    )
                }
            }
        }

        // ── Bottom CTA ──
        SmallSpacer(8)
        PrimaryCta(
            text = "✦ ${stringResource(Res.string.history_backfill)}",
            onClick = {
                scope.launch {
                    val result = sheetManager.backfillPeriod() ?: return@launch
                    if (result is AddRecordResult.Success) { refresh(); snackbar.showSnackbar(successMsg) }
                }
            },
        )
        SmallSpacer(16)

        SnackbarHost(snackbar)
    }
}

// ════════════════════════════════════════════════════════════════
// Bar Chart
// ════════════════════════════════════════════════════════════════

@Composable
private fun ChartSection(
    records: List<MenstrualRecord>,
    cycleGaps: List<Int>,
) {
    val chartTitle = stringResource(Res.string.stats_chart_title)
    Text(
        chartTitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    SmallSpacer(8)

    val chartRecords = records.takeLast(8)
    val barColor = MaterialTheme.colorScheme.tertiary
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
    ) {
        if (chartRecords.isEmpty()) return@Canvas

        val periodLengths = chartRecords.map { r ->
            r.endDate?.let { r.startDate.until(it, DateTimeUnit.DAY).toInt() + 1 } ?: 0
        }
        val maxVal = periodLengths.max().coerceAtLeast(1)

        val barCount = chartRecords.size
        val spacing = 8.dp.toPx()
        val labelHeight = 40.dp.toPx()
        val topLabelHeight = 24.dp.toPx()
        val availableWidth = size.width - (spacing * (barCount - 1))
        val barWidth = (availableWidth / barCount).coerceAtMost(48.dp.toPx())
        val totalBarsWidth = barWidth * barCount + spacing * (barCount - 1)
        val startX = (size.width - totalBarsWidth) / 2
        val chartHeight = size.height - labelHeight - topLabelHeight

        chartRecords.forEachIndexed { i, record ->
            val days = periodLengths[i]
            val barH = if (days > 0) (days.toFloat() / maxVal) * chartHeight else 0f
            val x = startX + i * (barWidth + spacing)
            val y = topLabelHeight + chartHeight - barH

            // Bar
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(4.dp.toPx()),
            )

            // Days label on top of bar
            if (days > 0) {
                val daysText = "$days"
                val measured = textMeasurer.measure(
                    daysText,
                    TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                )
                drawText(
                    measured,
                    color = textColor,
                    topLeft = Offset(x + (barWidth - measured.size.width) / 2, y - measured.size.height - 2.dp.toPx()),
                )
            }

            // Date label below bar
            val dateStr = "${record.startDate.monthNumber}/${record.startDate.dayOfMonth}\n${record.startDate.year}"
            val dateMeasured = textMeasurer.measure(
                dateStr,
                TextStyle(fontSize = 9.sp, textAlign = TextAlign.Center),
            )
            drawText(
                dateMeasured,
                color = textColor,
                topLeft = Offset(x + (barWidth - dateMeasured.size.width) / 2, topLabelHeight + chartHeight + 4.dp.toPx()),
            )
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
        // Actual
        Surface(modifier = Modifier.size(dotSize), color = barColor, shape = MaterialTheme.shapes.extraSmall) {}
        SmallSpacer(6)
        Text(
            stringResource(Res.string.stats_legend_actual),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SmallSpacer(24)
        // Predicted
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
// Averages Section
// ════════════════════════════════════════════════════════════════

@Composable
private fun AveragesSection(cycleCount: Int, avgPeriod: Int?, avgCycle: Int?) {
    Text(
        stringResource(Res.string.stats_avg_section_title, cycleCount),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    SmallSpacer(12)
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Period days
        Surface(
            modifier = Modifier.weight(1f),
            tonalElevation = 1.dp,
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "${avgPeriod ?: "-"}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                )
                SmallSpacer(4)
                Text(
                    stringResource(Res.string.stats_period_days),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        // Cycle days
        Surface(
            modifier = Modifier.weight(1f),
            tonalElevation = 1.dp,
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "${avgCycle ?: "-"}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                )
                SmallSpacer(4)
                Text(
                    stringResource(Res.string.stats_cycle_days),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// History Table
// ════════════════════════════════════════════════════════════════

@Composable
private fun HistoryTableHeader() {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            stringResource(Res.string.stats_col_start_date),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1.2f),
        )
        Text(
            stringResource(Res.string.stats_col_period),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(0.8f),
        )
        Text(
            stringResource(Res.string.stats_col_cycle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HistoryTableRow(
    record: MenstrualRecord,
    periodDays: Int?,
    cycleLength: Int?,
    isCurrent: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${record.startDate}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1.2f),
            )
            Text(
                if (periodDays != null) "$periodDays" else "-",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(0.8f),
            )
            Text(
                when {
                    isCurrent -> stringResource(Res.string.stats_current_cycle)
                    cycleLength != null -> "$cycleLength"
                    else -> "-"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isCurrent) FontWeight.Normal else FontWeight.Bold,
                color = if (isCurrent) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
