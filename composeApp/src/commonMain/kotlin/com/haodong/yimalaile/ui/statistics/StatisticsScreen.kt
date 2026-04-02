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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.AddRecordResult
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.ui.components.StatusPill
import com.haodong.yimalaile.ui.record.BackfillSheet
import com.haodong.yimalaile.ui.theme.AppColors
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.until
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.record_save_success
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
    val daysStr = stringResource(Res.string.unit_days)

    LaunchedEffect(Unit) { state = service.getCycleState() }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val s = state
        if (s != null) {
            LazyColumn(
                Modifier.fillMaxSize().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Top bar
                item {
                    Spacer(Modifier.height(40.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(40.dp).clip(CircleShape)
                                .background(AppColors.WarmPeach.copy(alpha = 0.5f)),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AppColors.DeepRose)
                        }
                        Spacer(Modifier.weight(1f))
                        Text(stringResource(Res.string.stats_title), style = MaterialTheme.typography.titleLarge, color = AppColors.DarkCoffee)
                        Spacer(Modifier.weight(1f))
                        Box(Modifier.size(40.dp))
                    }
                }

                if (s.recentPeriods.isEmpty() && s.activePeriod == null) {
                    item {
                        Spacer(Modifier.height(80.dp))
                        Text(
                            stringResource(Res.string.stats_empty_message),
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppColors.DarkCoffee.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                } else {
                    val records = s.recentPeriods + listOfNotNull(s.activePeriod)
                    val sorted = records.sortedBy { it.startDate }

                    val avgCycle = if (sorted.size >= 2) {
                        val gaps = sorted.zipWithNext().map { (a, b) -> a.startDate.until(b.startDate, DateTimeUnit.DAY) }
                        gaps.sum() / gaps.size
                    } else null
                    val avgPeriod = records.filter { it.endDate != null }.let { list ->
                        if (list.isEmpty()) null
                        else list.sumOf { it.startDate.until(it.endDate!!, DateTimeUnit.DAY) + 1 } / list.size
                    }

                    // Summary cards
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            BigStatCard("周期长度", avgCycle?.toString() ?: "--", daysStr, Modifier.weight(1f))
                            BigStatCard("经期时长", avgPeriod?.toString() ?: "--", daysStr, Modifier.weight(1f))
                        }
                    }

                    // Cycle trend
                    if (sorted.size >= 2) {
                        item {
                            SectionHeader("周期趋势", "过去 ${sorted.size} 个周期")
                        }
                        item {
                            CycleTrendChart(sorted, avgCycle)
                        }
                    }

                    // Period duration
                    val withEnd = records.filter { it.endDate != null }.sortedBy { it.startDate }
                    if (withEnd.isNotEmpty()) {
                        item {
                            SectionHeader("经期时长")
                        }
                        item {
                            PeriodDurationChart(withEnd, daysStr)
                        }
                    }

                    // Confidence
                    if (avgCycle != null) {
                        item {
                            val gaps = sorted.zipWithNext().map { (a, b) -> a.startDate.until(b.startDate, DateTimeUnit.DAY) }
                            val variance = gaps.map { (it - avgCycle) * (it - avgCycle) }.average()
                            val confidence = when {
                                sorted.size >= 6 && variance < 9 -> "高"
                                sorted.size >= 3 -> "中"
                                else -> "低"
                            }
                            Box(
                                Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(AppColors.WarmPeach.copy(alpha = 0.25f))
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("预测置信度", style = MaterialTheme.typography.labelMedium, color = AppColors.DarkCoffee.copy(alpha = 0.5f))
                                    Spacer(Modifier.height(4.dp))
                                    Text(confidence, style = MaterialTheme.typography.titleLarge, color = AppColors.DeepRose, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Encouragement
                    item {
                        Box(
                            Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(AppColors.BlushPink.copy(alpha = 0.3f))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "\"你的身体很有规律，继续保持记录吧\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.DarkCoffee.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    // History
                    item { SectionHeader("历史记录") }

                    itemsIndexed(s.recentPeriods) { index, record ->
                        RecordRow(record, daysStr, index)
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }

        FloatingActionButton(
            onClick = { showBackfill = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = AppColors.DeepRose,
            contentColor = AppColors.SoftCream,
            shape = CircleShape,
        ) {
            Icon(Icons.Default.Add, "补录")
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

// ---------- Components ----------

@Composable
private fun SectionHeader(title: String, subtitle: String? = null) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = AppColors.DarkCoffee)
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.labelMedium, color = AppColors.DarkCoffee.copy(alpha = 0.4f))
        }
    }
}

@Composable
private fun BigStatCard(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(24.dp))
            .background(AppColors.BlushPink.copy(alpha = 0.45f))
            .padding(20.dp),
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = AppColors.DarkCoffee.copy(alpha = 0.5f))
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = AppColors.DarkCoffee)
                Spacer(Modifier.width(4.dp))
                Text(unit, style = MaterialTheme.typography.bodyLarge, color = AppColors.DarkCoffee.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}

@Composable
private fun CycleTrendChart(records: List<MenstrualRecord>, avgCycle: Int?) {
    if (records.size < 2) return
    val gaps = records.zipWithNext().map { (a, b) -> a.startDate.until(b.startDate, DateTimeUnit.DAY) }
    val maxGap = (gaps.maxOrNull() ?: 1).coerceAtLeast(1)

    Box(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.BlushPink.copy(alpha = 0.25f))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            gaps.forEachIndexed { index, gap ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Label
                    Text(
                        "${index + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.DarkCoffee.copy(alpha = 0.35f),
                        modifier = Modifier.width(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    // Bar
                    Box(
                        Modifier
                            .height(16.dp)
                            .fillMaxWidth(fraction = gap.toFloat() / maxGap.toFloat())
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (avgCycle != null && kotlin.math.abs(gap - avgCycle) <= 2)
                                    AppColors.DeepRose.copy(alpha = 0.6f)
                                else AppColors.DeepRose.copy(alpha = 0.3f)
                            )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "$gap",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.DarkCoffee.copy(alpha = 0.6f),
                    )
                }
            }
            // Average line label
            if (avgCycle != null) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.height(1.dp).fillMaxWidth(fraction = avgCycle.toFloat() / maxGap.toFloat())
                        .background(AppColors.DeepRose))
                    Spacer(Modifier.width(8.dp))
                    Text("avg $avgCycle", style = MaterialTheme.typography.labelSmall, color = AppColors.DeepRose)
                }
            }
        }
    }
}

@Composable
private fun PeriodDurationChart(records: List<MenstrualRecord>, daysStr: String) {
    Box(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.BlushPink.copy(alpha = 0.25f))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            records.takeLast(6).forEach { record ->
                val days = record.startDate.until(record.endDate!!, DateTimeUnit.DAY) + 1
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${record.startDate.monthNumber}/${record.startDate.dayOfMonth}",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.DarkCoffee.copy(alpha = 0.4f),
                        modifier = Modifier.width(40.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    // Dots
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(days.coerceAtMost(10)) { i ->
                            val alpha = 0.3f + (0.5f * (1f - i.toFloat() / days.coerceAtMost(10)))
                            Box(
                                Modifier.size(14.dp).clip(CircleShape)
                                    .background(AppColors.DeepRose.copy(alpha = alpha))
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        "$days$daysStr",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.DarkCoffee.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordRow(record: MenstrualRecord, daysStr: String, index: Int) {
    Box(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.BlushPink.copy(alpha = if (index % 2 == 0) 0.3f else 0.2f))
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(
                    "${record.startDate.year}/${record.startDate.monthNumber}/${record.startDate.dayOfMonth}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.DarkCoffee,
                )
                if (record.endDate != null) {
                    Text(
                        "→ ${record.endDate.monthNumber}/${record.endDate.dayOfMonth}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.DarkCoffee.copy(alpha = 0.4f),
                    )
                }
            }
            if (record.endDate != null) {
                val days = record.startDate.until(record.endDate, DateTimeUnit.DAY) + 1
                StatusPill("$days $daysStr")
            } else {
                StatusPill("进行中")
            }
        }
    }
}
