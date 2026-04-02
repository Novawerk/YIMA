package com.haodong.yimalaile.ui.home

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.ui.components.HeartDecoration
import com.haodong.yimalaile.ui.components.IllustrationPlaceholder
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.components.StatCardsRow
import com.haodong.yimalaile.ui.components.StatusPill
import com.haodong.yimalaile.ui.record.EndPeriodSheet
import com.haodong.yimalaile.ui.record.LogDaySheet
import com.haodong.yimalaile.ui.record.StartPeriodSheet
import com.haodong.yimalaile.ui.theme.AppColors
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.until
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.btn_record_period
import yimalaile.composeapp.generated.resources.record_save_success
import yimalaile.composeapp.generated.resources.stat_cycle_length
import yimalaile.composeapp.generated.resources.stat_period_length
import yimalaile.composeapp.generated.resources.status_here_desc
import yimalaile.composeapp.generated.resources.status_no_prediction
import yimalaile.composeapp.generated.resources.unit_days

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

    var showStartSheet by remember { mutableStateOf(false) }
    var showEndSheet by remember { mutableStateOf(false) }
    var showLogSheet by remember { mutableStateOf(false) }

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
                )
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    if (showStartSheet) {
        StartPeriodSheet(
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
}

@Composable
private fun HomeContent(
    state: CycleState,
    onNavigateSettings: () -> Unit,
    onNavigateStatistics: () -> Unit,
    onStartPeriod: () -> Unit,
    onEndPeriod: () -> Unit,
    onLogDay: () -> Unit,
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
                    .background(AppColors.WarmPeach.copy(alpha = 0.5f)),
            ) {
                Icon(Icons.Outlined.BarChart, null, tint = AppColors.DeepRose)
            }
            // Settings button
            IconButton(
                onClick = onNavigateSettings,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AppColors.WarmPeach.copy(alpha = 0.5f)),
            ) {
                Icon(Icons.Outlined.Settings, null, tint = AppColors.DeepRose)
            }
        }

        Spacer(Modifier.height(24.dp))

        if (state.activePeriod != null) {
            // ---- In period state ----
            val dayCount = state.activePeriod.startDate.until(today, DateTimeUnit.DAY) + 1
            val avgPeriod = state.predictions.firstOrNull()?.predictedEnd?.let { end ->
                state.activePeriod.startDate.until(end, DateTimeUnit.DAY) + 1
            }

            Text(
                "照顾好自己",
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.DarkCoffee.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Top) {
                Text(
                    "经期中",
                    style = MaterialTheme.typography.headlineLarge,
                    color = AppColors.DarkCoffee,
                )
                HeartDecoration()
            }

            Spacer(Modifier.height(16.dp))

            StatusPill("第 ${dayCount} 天")

            Spacer(Modifier.height(32.dp))
            IllustrationPlaceholder()
            Spacer(Modifier.weight(1f))

            // Primary action: log today
            PrimaryCta(text = "✦ 记录今天", onClick = onLogDay)
            Spacer(Modifier.height(12.dp))
            // Secondary action: end period — outlined style
            OutlinedButton(
                onClick = onEndPeriod,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.5.dp, AppColors.DeepRose.copy(alpha = 0.4f)),
            ) {
                Text("经期结束", style = MaterialTheme.typography.titleMedium, color = AppColors.DeepRose)
            }

        } else {
            // ---- Not in period state ----
            val prediction = state.predictions.firstOrNull()
            val daysUntil = prediction?.let {
                today.until(it.predictedStart, DateTimeUnit.DAY)
            }

            // Determine hero text based on days until
            val (question, heroText) = when {
                daysUntil == null -> "—" to stringResource(Res.string.status_no_prediction)
                daysUntil < 0 -> "姨妈还没来吗？" to "该来了"
                daysUntil <= 3 -> "姨妈快来了吗？" to "快了"
                else -> "离姨妈还远吗？" to "还早"
            }

            Text(
                question,
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.DarkCoffee.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Top) {
                Text(
                    heroText,
                    style = MaterialTheme.typography.headlineLarge,
                    color = AppColors.DarkCoffee,
                )
                HeartDecoration()
            }

            Spacer(Modifier.height(16.dp))

            if (prediction != null) {
                val startDate = prediction.predictedStart
                val label = if (daysUntil != null && daysUntil < 0) {
                    "预计 ${startDate.monthNumber}月${startDate.dayOfMonth}日 (已推迟${-daysUntil}天)"
                } else if (daysUntil != null) {
                    "预计 ${startDate.monthNumber}月${startDate.dayOfMonth}日 (${daysUntil}天后)"
                } else ""
                StatusPill(label)
            }

            Spacer(Modifier.height(24.dp))

            // Stat cards
            val cycleLenStr = stringResource(Res.string.stat_cycle_length)
            val periodLenStr = stringResource(Res.string.stat_period_length)
            val daysStr = stringResource(Res.string.unit_days)
            // Compute from recent periods
            val records = state.recentPeriods
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

            StatCardsRow(
                label1 = cycleLenStr, value1 = avgCycle, unit1 = daysStr,
                label2 = periodLenStr, value2 = avgPeriod, unit2 = daysStr,
            )

            Spacer(Modifier.height(24.dp))
            IllustrationPlaceholder()
            Spacer(Modifier.weight(1f))

            PrimaryCta(
                text = "✦ ${stringResource(Res.string.btn_record_period)}",
                onClick = onStartPeriod,
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}
