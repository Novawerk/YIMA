package com.haodong.yimalaile.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.ui.components.BigStatCard
import com.haodong.yimalaile.ui.components.HeartDecoration
import com.haodong.yimalaile.ui.components.PeriodDurationChart
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.components.StatusPill
import com.haodong.yimalaile.ui.record.BackfillSheet
import com.haodong.yimalaile.ui.record.EndPeriodSheet
import com.haodong.yimalaile.ui.record.LogDaySheet
import com.haodong.yimalaile.ui.record.StartPeriodSheet
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.until
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

@Composable
fun HomeScreen(
    service: MenstrualService,
    onNavigateStatistics: () -> Unit,
    onNavigateSettings: () -> Unit,
) {
    val viewModel = remember { HomeViewModel(service) }
    val uiState by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val successMsg = stringResource(Res.string.record_save_success)

    var logSpecificDate by remember { mutableStateOf<LocalDate?>(null) }
    var showStartSheet by remember { mutableStateOf(false) }
    var showEndSheet by remember { mutableStateOf(false) }
    var showLogSheet by remember { mutableStateOf(false) }
    var showBackfillSheet by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (val s = uiState) {
            is HomeUiState.Loading -> {}
            is HomeUiState.Ready -> {
                HomeContent(
                    state = s.cycleState,
                    onNavigateSettings = onNavigateSettings,
                    onNavigateStatistics = onNavigateStatistics,
                    onStartPeriod = { showStartSheet = true },
                    onEndPeriod = { showEndSheet = true },
                    onLogDay = { showLogSheet = true },
                    onLogSpecificDay = { date -> logSpecificDate = date },
                    onBackfill = { showBackfillSheet = true },
                )
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    val allRecords = (uiState as? HomeUiState.Ready)?.cycleState?.let {
        it.recentPeriods + listOfNotNull(it.activePeriod)
    } ?: emptyList()

    if (showStartSheet) {
        StartPeriodSheet(
            existingRecords = allRecords,
            onDismiss = { showStartSheet = false },
            onConfirm = { date ->
                viewModel.startPeriod(date) {
                    showStartSheet = false
                    scope.launch { snackbar.showSnackbar(successMsg) }
                }
            }
        )
    }
    if (showEndSheet) {
        val active = (uiState as? HomeUiState.Ready)?.cycleState?.activePeriod
        if (active != null) {
            EndPeriodSheet(
                startDate = active.startDate,
                dailyRecords = active.dailyRecords,
                existingRecords = allRecords.filter { it.id != active.id },
                onDismiss = { showEndSheet = false },
                onConfirm = { date ->
                    viewModel.endPeriod(date) {
                        showEndSheet = false
                        scope.launch { snackbar.showSnackbar(successMsg) }
                    }
                }
            )
        }
    }
    if (showLogSheet) {
        LogDaySheet(
            onDismiss = { showLogSheet = false },
            onSave = { intensity, mood, symptoms, notes ->
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                viewModel.logDay(today, intensity, mood, symptoms, notes) {
                    showLogSheet = false
                    scope.launch { snackbar.showSnackbar(successMsg) }
                }
            }
        )
    }
    if (logSpecificDate != null) {
        val targetDate = logSpecificDate!!
        LogDaySheet(
            targetDate = targetDate,
            onDismiss = { logSpecificDate = null },
            onSave = { intensity, mood, symptoms, notes ->
                viewModel.logDay(targetDate, intensity, mood, symptoms, notes) {
                    logSpecificDate = null
                    scope.launch { snackbar.showSnackbar(successMsg) }
                }
            }
        )
    }
    if (showBackfillSheet) {
        val records = (uiState as? HomeUiState.Ready)?.cycleState?.let {
            it.recentPeriods + listOfNotNull(it.activePeriod)
        } ?: emptyList()
        BackfillSheet(
            existingRecords = records,
            onDismiss = { showBackfillSheet = false },
            onSave = { start, end ->
                viewModel.backfillPeriod(start, end) {
                    showBackfillSheet = false
                    scope.launch { snackbar.showSnackbar(successMsg) }
                }
            }
        )
    }
}

@Composable
private fun HomeContent(
    state: CycleState,
    onNavigateSettings: () -> Unit,
    onNavigateStatistics: () -> Unit,
    onStartPeriod: () -> Unit,
    onEndPeriod: () -> Unit,
    onLogDay: () -> Unit,
    onLogSpecificDay: (LocalDate) -> Unit,
    onBackfill: () -> Unit,
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top bar
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            // Stats button
            IconButton(
                onClick = onNavigateStatistics,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)),
            ) {
                Icon(Icons.Outlined.History, null, tint = MaterialTheme.colorScheme.primary)
            }
            // Settings button
            IconButton(
                onClick = onNavigateSettings,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)),
            ) {
                Icon(Icons.Outlined.Settings, null, tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(24.dp))

        if (state.activePeriod != null) {
            // ---- In period state ----
            val dayCount = state.activePeriod.startDate.until(today, DateTimeUnit.DAY) + 1

            // Compute average period length from history
            val completedPeriods = state.recentPeriods.filter { it.endDate != null }
            val avgPeriodLen = if (completedPeriods.isNotEmpty()) {
                completedPeriods.sumOf { it.startDate.until(it.endDate!!, DateTimeUnit.DAY) + 1 } / completedPeriods.size
            } else null
            val remainingDays = avgPeriodLen?.let { (it - dayCount).coerceAtLeast(0) }

            Text(
                stringResource(Res.string.home_take_care),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Top) {
                Text(
                    stringResource(Res.string.home_in_period),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                HeartDecoration()
            }

            Spacer(Modifier.height(16.dp))

            StatusPill(stringResource(Res.string.home_day_n, dayCount))

            // Estimated remaining
            if (remainingDays != null) {
                Spacer(Modifier.height(8.dp))
                if (remainingDays > 0) {
                    Text(
                        stringResource(Res.string.home_remaining_days, remainingDays),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        stringResource(Res.string.home_exceeded_avg),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    )
                }
            }

            // Day-by-day timeline
            Spacer(Modifier.height(24.dp))
            DayTimeline(
                activePeriod = state.activePeriod,
                today = today,
                onLogDay = onLogDay,
                onLogSpecificDay = onLogSpecificDay,
            )

            Spacer(Modifier.weight(1f))

            // Primary action: log today
            PrimaryCta(text = stringResource(Res.string.home_log_today), onClick = onLogDay)
            Spacer(Modifier.height(12.dp))
            // Secondary action: end period — outlined style
            OutlinedButton(
                onClick = onEndPeriod,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
            ) {
                Text(stringResource(Res.string.home_end_period), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }

        } else {
            // ---- Not in period state ----
            val prediction = state.predictions.firstOrNull()
            val daysUntil = prediction?.let {
                today.until(it.predictedStart, DateTimeUnit.DAY)
            }

            // Determine hero text based on days until
            val (question, heroText) = when {
                daysUntil == null -> "" to stringResource(Res.string.status_no_prediction)
                daysUntil < 0 -> stringResource(Res.string.home_hero_overdue_sub) to stringResource(Res.string.home_hero_overdue)
                daysUntil <= 3 -> stringResource(Res.string.home_hero_soon_sub) to stringResource(Res.string.home_hero_soon)
                else -> stringResource(Res.string.home_hero_early_sub) to stringResource(Res.string.home_hero_early)
            }

            Text(
                question,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Top) {
                Text(
                    heroText,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                HeartDecoration()
            }

            Spacer(Modifier.height(16.dp))

            if (prediction != null) {
                val startDate = prediction.predictedStart
                val label = if (daysUntil != null && daysUntil < 0) {
                    stringResource(Res.string.home_predicted_delayed, startDate.monthNumber, startDate.dayOfMonth, -daysUntil)
                } else if (daysUntil != null) {
                    stringResource(Res.string.home_predicted_in_days, startDate.monthNumber, startDate.dayOfMonth, daysUntil)
                } else ""
                StatusPill(label)
            }

            Spacer(Modifier.height(24.dp))

            // Stat cards
            val daysStr = stringResource(Res.string.unit_days)
            val records = state.recentPeriods
            val avgCycle = if (records.size >= 2) {
                val sorted = records.sortedBy { it.startDate }
                val gaps = sorted.zipWithNext().map { (a, b) ->
                    a.startDate.until(b.startDate, DateTimeUnit.DAY)
                }
                gaps.sum() / gaps.size
            } else null
            val avgPeriod = records.filter { it.endDate != null }.let { list ->
                if (list.isEmpty()) null
                else list.sumOf { it.startDate.until(it.endDate!!, DateTimeUnit.DAY) + 1 } / list.size
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BigStatCard(
                    stringResource(Res.string.stat_cycle_length),
                    avgCycle?.toString() ?: "--", daysStr,
                    Modifier.weight(1f),
                )
                BigStatCard(
                    stringResource(Res.string.stat_period_length),
                    avgPeriod?.toString() ?: "--", daysStr,
                    Modifier.weight(1f),
                )
            }

            // Period duration dots
            val withEnd = records.filter { it.endDate != null }.sortedBy { it.startDate }
            if (withEnd.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                PeriodDurationChart(withEnd, daysStr, onViewAll = onNavigateStatistics)
            }

            // Confidence
            if (avgCycle != null && records.size >= 2) {
                Spacer(Modifier.height(12.dp))
                val sorted = records.sortedBy { it.startDate }
                val gaps = sorted.zipWithNext().map { (a, b) -> a.startDate.until(b.startDate, DateTimeUnit.DAY) }
                val variance = gaps.map { (it - avgCycle) * (it - avgCycle) }.average()
                val confidence = when {
                    sorted.size >= 6 && variance < 9 -> stringResource(Res.string.stats_confidence_high)
                    sorted.size >= 3 -> stringResource(Res.string.stats_confidence_medium)
                    else -> stringResource(Res.string.stats_confidence_low)
                }
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(Res.string.home_prediction_confidence), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    StatusPill(confidence)
                }
            }

            Spacer(Modifier.weight(1f))

            // Secondary: backfill more history to unlock predictions
            if (avgCycle == null) {
                OutlinedButton(
                    onClick = onBackfill,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                ) {
                    Text(stringResource(Res.string.home_backfill_to_predict), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(12.dp))
            }

            PrimaryCta(
                text = "✦ ${stringResource(Res.string.btn_record_period)}",
                onClick = onStartPeriod,
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DayTimeline(
    activePeriod: com.haodong.yimalaile.domain.menstrual.MenstrualRecord,
    today: LocalDate,
    onLogDay: () -> Unit,
    onLogSpecificDay: (LocalDate) -> Unit = { onLogDay() },
) {
    val dailyMap = activePeriod.dailyRecords.associateBy { it.date }
    val days = buildList {
        var d = activePeriod.startDate
        while (d <= today) { add(d); d = d.plus(1, DateTimeUnit.DAY) }
    }

    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        days.forEach { date ->
            val record = dailyMap[date]
            val isEmpty = record == null
            val isToday = date == today

            Row(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else Color.Transparent
                    )
                    .then(if (isEmpty) Modifier.clickable { onLogSpecificDay(date) } else Modifier)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Day number circle
                Box(
                    Modifier.size(36.dp).clip(CircleShape)
                        .background(
                            if (record != null) MaterialTheme.colorScheme.primaryContainer
                            else if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "${date.dayOfMonth}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(Modifier.width(12.dp))

                // Content area
                if (record != null) {
                    // Show recorded info
                    Row(
                        Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (record.mood != null) {
                            Text(
                                when (record.mood) {
                                    com.haodong.yimalaile.domain.menstrual.Mood.HAPPY -> "😊"
                                    com.haodong.yimalaile.domain.menstrual.Mood.NEUTRAL -> "😐"
                                    com.haodong.yimalaile.domain.menstrual.Mood.SAD -> "😔"
                                    com.haodong.yimalaile.domain.menstrual.Mood.VERY_SAD -> "😢"
                                },
                                fontSize = 18.sp,
                            )
                        }
                        if (record.intensity != null) {
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    when (record.intensity) {
                                        com.haodong.yimalaile.domain.menstrual.Intensity.LIGHT -> stringResource(Res.string.intensity_light)
                                        com.haodong.yimalaile.domain.menstrual.Intensity.MEDIUM -> stringResource(Res.string.intensity_medium)
                                        com.haodong.yimalaile.domain.menstrual.Intensity.HEAVY -> stringResource(Res.string.intensity_heavy)
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        if (record.symptoms.isNotEmpty()) {
                            Text(
                                record.symptoms.joinToString(" · "),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    }
                } else {
                    // Empty — invite to record
                    Text(
                        if (isToday) stringResource(Res.string.home_log_today_hint) else stringResource(Res.string.home_tap_to_add),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "+",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                    )
                }
            }
        }
    }
}
