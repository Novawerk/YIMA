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
import com.haodong.yimalaile.ui.pages.sheet.SheetViewModel
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
    sheetViewModel: SheetViewModel,
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
                            sheetViewModel.showAndHandleRecordDetail(record, defaultCycleLength)
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

    if (showLegendDialog) {
        ChartLegendDialog(onDismiss = { showLegendDialog = false })
    }
}

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