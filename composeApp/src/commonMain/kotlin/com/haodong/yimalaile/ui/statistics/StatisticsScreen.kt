package com.haodong.yimalaile.ui.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.AddRecordResult
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.ui.components.StatCardsRow
import com.haodong.yimalaile.ui.components.StatusPill
import com.haodong.yimalaile.ui.record.BackfillSheet
import com.haodong.yimalaile.ui.theme.AppColors
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.until
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.record_save_success
import yimalaile.composeapp.generated.resources.stat_cycle_length
import yimalaile.composeapp.generated.resources.stat_period_length
import yimalaile.composeapp.generated.resources.stats_empty_message
import yimalaile.composeapp.generated.resources.stats_title
import yimalaile.composeapp.generated.resources.unit_days

@Composable
fun StatisticsScreen(
    service: MenstrualService,
    onBack: () -> Unit,
) {
    var state by remember { mutableStateOf<CycleState?>(null) }
    var showBackfill by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val successMsg = stringResource(Res.string.record_save_success)

    LaunchedEffect(Unit) { state = service.getCycleState() }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize()) {
            // Top bar
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AppColors.WarmPeach.copy(alpha = 0.5f)),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AppColors.DeepRose)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    stringResource(Res.string.stats_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = AppColors.DarkCoffee,
                )
                Spacer(Modifier.weight(1f))
                // Balance spacer
                Box(Modifier.size(40.dp))
            }

            val s = state ?: return@Column

            if (s.recentPeriods.isEmpty() && s.activePeriod == null) {
                Column(
                    Modifier.fillMaxSize().padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        stringResource(Res.string.stats_empty_message),
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppColors.DarkCoffee.copy(alpha = 0.6f),
                    )
                }
            } else {
                val records = s.recentPeriods + listOfNotNull(s.activePeriod)
                val daysStr = stringResource(Res.string.unit_days)

                val avgCycle = if (records.size >= 2) {
                    val sorted = records.sortedBy { it.startDate }
                    val gaps = sorted.zipWithNext().map { (a, b) ->
                        a.startDate.until(b.startDate, DateTimeUnit.DAY)
                    }
                    (gaps.sum() / gaps.size).toString()
                } else "--"
                val avgPeriod = records.filter { it.endDate != null }.let { list ->
                    if (list.isEmpty()) "--"
                    else (list.sumOf { it.startDate.until(it.endDate!!, DateTimeUnit.DAY) + 1 } / list.size).toString()
                }

                LazyColumn(
                    Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        StatCardsRow(
                            label1 = stringResource(Res.string.stat_cycle_length),
                            value1 = avgCycle, unit1 = daysStr,
                            label2 = stringResource(Res.string.stat_period_length),
                            value2 = avgPeriod, unit2 = daysStr,
                        )
                    }

                    // Cycle trend bars
                    if (records.size >= 2) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            Text("周期趋势", style = MaterialTheme.typography.titleMedium, color = AppColors.DarkCoffee)
                            Spacer(Modifier.height(8.dp))
                            CycleTrendChart(records.sortedBy { it.startDate })
                        }
                    }

                    // Period duration dots
                    val withEnd = records.filter { it.endDate != null }.sortedBy { it.startDate }
                    if (withEnd.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            Text("经期时长", style = MaterialTheme.typography.titleMedium, color = AppColors.DarkCoffee)
                            Spacer(Modifier.height(8.dp))
                            PeriodDurationDots(withEnd)
                        }
                    }

                    // History list
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text("历史记录", style = MaterialTheme.typography.titleMedium, color = AppColors.DarkCoffee)
                    }
                    items(s.recentPeriods) { record ->
                        RecordRow(record, daysStr)
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }

        FloatingActionButton(
            onClick = { showBackfill = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = AppColors.DeepRose,
            contentColor = AppColors.SoftCream,
        ) {
            Icon(Icons.Default.Add, "Backfill")
        }

        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    if (showBackfill) {
        BackfillSheet(
            onDismiss = { showBackfill = false },
            onSave = { start, end ->
                scope.launch {
                    val result = service.backfillPeriod(start, end)
                    showBackfill = false
                    if (result is AddRecordResult.Success) {
                        state = service.getCycleState()
                        snackbar.showSnackbar(successMsg)
                    }
                }
            }
        )
    }
}

@Composable
private fun CycleTrendChart(records: List<MenstrualRecord>) {
    if (records.size < 2) return
    val gaps = records.zipWithNext().map { (a, b) ->
        a.startDate.until(b.startDate, DateTimeUnit.DAY)
    }
    val maxGap = gaps.maxOrNull() ?: 1

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.BlushPink.copy(alpha = 0.3f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        gaps.forEach { gap ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .height(12.dp)
                        .fillMaxWidth(fraction = gap.toFloat() / maxGap.toFloat())
                        .clip(RoundedCornerShape(6.dp))
                        .background(AppColors.DeepRose.copy(alpha = 0.6f))
                )
                Spacer(Modifier.padding(start = 8.dp))
                Text("$gap", style = MaterialTheme.typography.labelMedium, color = AppColors.DarkCoffee.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun PeriodDurationDots(records: List<MenstrualRecord>) {
    val maxDays = 10
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.BlushPink.copy(alpha = 0.3f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        records.takeLast(5).forEach { record ->
            val days = record.startDate.until(record.endDate!!, DateTimeUnit.DAY) + 1
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(days.coerceAtMost(maxDays)) {
                    Box(
                        Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(AppColors.DeepRose.copy(alpha = 0.5f))
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordRow(record: MenstrualRecord, daysStr: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.BlushPink.copy(alpha = 0.3f))
            .padding(16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "${record.startDate.monthNumber}/${record.startDate.dayOfMonth}",
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.DarkCoffee,
            )
            if (record.endDate != null) {
                val days = record.startDate.until(record.endDate, DateTimeUnit.DAY) + 1
                StatusPill("$days $daysStr")
            }
        }
    }
}
