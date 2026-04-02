package com.haodong.yimalaile.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.AddRecordResult
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.ui.components.HeartDecoration
import com.haodong.yimalaile.ui.components.IllustrationPlaceholder
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.components.StatusPill
import com.haodong.yimalaile.ui.theme.AppColors
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
private val PastOnlyDates = object : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
        utcTimeMillis <= Clock.System.now().toEpochMilliseconds()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    service: MenstrualService,
    onComplete: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf(0) }
    var backfillCount by remember { mutableStateOf(0) }

    val startPickerState = rememberDatePickerState(selectableDates = PastOnlyDates)
    val rangePickerState = rememberDateRangePickerState(selectableDates = PastOnlyDates)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (step) {
            // Welcome
            0 -> {
                Spacer(Modifier.weight(0.3f))
                Row(verticalAlignment = Alignment.Top) {
                    Text("你好", style = MaterialTheme.typography.headlineLarge, color = AppColors.DarkCoffee)
                    HeartDecoration()
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "让我们开始记录你的故事",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.DarkCoffee.copy(alpha = 0.6f),
                )
                Spacer(Modifier.height(32.dp))
                IllustrationPlaceholder()
                Spacer(Modifier.height(32.dp))
                StatusPill("还没有任何记录")
                Spacer(Modifier.weight(1f))
                PrimaryCta("✦ 开始第一次记录", onClick = { step = 1 })
            }

            // Ask current period status
            1 -> {
                Spacer(Modifier.weight(0.3f))
                Text("你现在在经期中吗？", style = MaterialTheme.typography.headlineMedium, color = AppColors.DarkCoffee)
                Spacer(Modifier.height(32.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    PrimaryCta("是的", onClick = { step = 2 }, modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = { step = 3 }, modifier = Modifier.weight(1f).height(56.dp)) {
                        Text("不在")
                    }
                }
                Spacer(Modifier.weight(1f))
            }

            // Pick current period start date (past only)
            2 -> {
                Text("哪天开始的？", style = MaterialTheme.typography.titleLarge, color = AppColors.DarkCoffee)
                Spacer(Modifier.height(8.dp))
                DatePicker(
                    state = startPickerState,
                    title = null, headline = null, showModeToggle = false,
                    colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
                    modifier = Modifier.weight(1f),
                )
                PrimaryCta(
                    text = "确认",
                    onClick = {
                        val millis = startPickerState.selectedDateMillis ?: return@PrimaryCta
                        val date = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
                        scope.launch {
                            service.startPeriod(date)
                            step = 3
                        }
                    },
                    enabled = startPickerState.selectedDateMillis != null,
                )
            }

            // Ask about past periods
            3 -> {
                Spacer(Modifier.weight(0.3f))
                Text(
                    if (backfillCount == 0) "你还记得上次经期吗？" else "继续补录？",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AppColors.DarkCoffee,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "补录过去的记录可以帮助预测更准确",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.DarkCoffee.copy(alpha = 0.6f),
                )
                Spacer(Modifier.height(32.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    PrimaryCta("补录", onClick = { step = 4 }, modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = onComplete, modifier = Modifier.weight(1f).height(56.dp)) {
                        Text(if (backfillCount == 0) "跳过" else "完成")
                    }
                }
                Spacer(Modifier.weight(1f))
            }

            // DateRangePicker for past period (start + end in one view, past only)
            4 -> {
                Text("选择经期日期范围", style = MaterialTheme.typography.titleLarge, color = AppColors.DarkCoffee)
                Spacer(Modifier.height(8.dp))
                DateRangePicker(
                    state = rangePickerState,
                    title = null,
                    headline = null,
                    showModeToggle = false,
                    colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
                    modifier = Modifier.weight(1f),
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { step = 3 }, Modifier.weight(1f)) { Text("返回") }
                    PrimaryCta(
                        text = "保存",
                        onClick = {
                            val startMillis = rangePickerState.selectedStartDateMillis ?: return@PrimaryCta
                            val endMillis = rangePickerState.selectedEndDateMillis ?: return@PrimaryCta
                            val start = Instant.fromEpochMilliseconds(startMillis).toLocalDateTime(TimeZone.UTC).date
                            val end = Instant.fromEpochMilliseconds(endMillis).toLocalDateTime(TimeZone.UTC).date
                            scope.launch {
                                val result = service.backfillPeriod(start, end)
                                if (result is AddRecordResult.Success) backfillCount++
                                step = 3
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = rangePickerState.selectedStartDateMillis != null
                                && rangePickerState.selectedEndDateMillis != null,
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
