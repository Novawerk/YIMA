package com.haodong.yimalaile.ui.statistics

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
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
import com.haodong.yimalaile.domain.menstrual.AddRecordResult
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.ui.record.BackfillSheet
import com.haodong.yimalaile.ui.record.DayPickerSheet
import com.haodong.yimalaile.ui.record.LogDaySheet
import com.haodong.yimalaile.ui.record.RecordDetailSheet
import com.haodong.yimalaile.ui.record.StartPeriodSheet
import com.haodong.yimalaile.ui.theme.AppColors
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlinx.datetime.until
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.record_save_success
import yimalaile.composeapp.generated.resources.stats_empty_message
import yimalaile.composeapp.generated.resources.unit_days

@Composable
fun StatisticsScreen(
    service: MenstrualService,
    onBack: () -> Unit,
) {
    var state by remember { mutableStateOf<CycleState?>(null) }
    var showBackfill by remember { mutableStateOf(false) }
    var detailRecord by remember { mutableStateOf<MenstrualRecord?>(null) }
    var editStartFor by remember { mutableStateOf<MenstrualRecord?>(null) }
    var editEndFor by remember { mutableStateOf<MenstrualRecord?>(null) }
    var logDayFor by remember { mutableStateOf<MenstrualRecord?>(null) }
    var logDayDate by remember { mutableStateOf<kotlinx.datetime.LocalDate?>(null) }
    var logDayRecordId by remember { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val successMsg = stringResource(Res.string.record_save_success)
    val daysStr = stringResource(Res.string.unit_days)

    fun refresh() { scope.launch { state = service.getCycleState() } }
    LaunchedEffect(Unit) { state = service.getCycleState() }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val s = state
        if (s != null) {
            val allRecords = (listOfNotNull(s.activePeriod) + s.recentPeriods)

            LazyColumn(
                Modifier.fillMaxSize().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Spacer(Modifier.height(40.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(40.dp).clip(CircleShape)
                                .background(AppColors.WarmPeach.copy(alpha = 0.5f)),
                        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AppColors.DeepRose) }
                        Spacer(Modifier.weight(1f))
                        Text("历史记录", style = MaterialTheme.typography.titleLarge, color = AppColors.DarkCoffee)
                        Spacer(Modifier.weight(1f))
                        Box(Modifier.size(40.dp))
                    }
                }

                if (allRecords.isEmpty()) {
                    item {
                        Spacer(Modifier.height(80.dp))
                        Text(
                            stringResource(Res.string.stats_empty_message),
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppColors.DarkCoffee.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
                        )
                    }
                } else {
                    items(allRecords, key = { it.id }) { record ->
                        RecordCard(
                            record = record,
                            daysStr = daysStr,
                            onClick = { detailRecord = record },
                        )
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { showBackfill = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = AppColors.DeepRose, contentColor = AppColors.SoftCream,
            shape = RoundedCornerShape(28.dp),
            icon = { Icon(Icons.Default.Add, null) }, text = { Text("补录") },
        )
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    // Backfill sheet
    if (showBackfill) {
        BackfillSheet(
            existingRecords = state?.let { it.recentPeriods + listOfNotNull(it.activePeriod) } ?: emptyList(),
            onDismiss = { showBackfill = false },
            onSave = { start, end ->
                scope.launch {
                    val result = service.backfillPeriod(start, end)
                    showBackfill = false
                    if (result is AddRecordResult.Success) { refresh(); snackbar.showSnackbar(successMsg) }
                }
            }
        )
    }

    // Detail sheet
    if (detailRecord != null) {
        RecordDetailSheet(
            record = detailRecord!!,
            onDismiss = { detailRecord = null },
            onEditStart = { editStartFor = detailRecord; detailRecord = null },
            onEditEnd = { editEndFor = detailRecord; detailRecord = null },
            onLogDay = { logDayFor = detailRecord; detailRecord = null },
            onDelete = {
                scope.launch {
                    service.deleteRecord(detailRecord!!.id)
                    detailRecord = null
                    refresh()
                    snackbar.showSnackbar("已删除")
                }
            },
        )
    }

    // Edit start date
    if (editStartFor != null) {
        val rec = editStartFor!!
        StartPeriodSheet(
            existingRecords = state?.recentPeriods?.filter { it.id != rec.id } ?: emptyList(),
            onDismiss = { editStartFor = null },
            onConfirm = { newStart ->
                scope.launch {
                    service.editRecordDates(rec.id, newStart, null)
                    editStartFor = null
                    refresh()
                    snackbar.showSnackbar(successMsg)
                }
            },
        )
    }

    // Edit end date — reuse StartPeriodSheet in single mode with minDate
    if (editEndFor != null) {
        val rec = editEndFor!!
        com.haodong.yimalaile.ui.record.EndPeriodSheet(
            startDate = rec.startDate,
            dailyRecords = rec.dailyRecords,
            existingRecords = state?.recentPeriods?.filter { it.id != rec.id } ?: emptyList(),
            onDismiss = { editEndFor = null },
            onConfirm = { newEnd ->
                scope.launch {
                    service.editRecordDates(rec.id, null, newEnd)
                    editEndFor = null
                    refresh()
                    snackbar.showSnackbar(successMsg)
                }
            },
        )
    }

    // Log day — step 1: pick which day
    if (logDayFor != null) {
        val rec = logDayFor!!
        DayPickerSheet(
            record = rec,
            onDismiss = { logDayFor = null },
            onDaySelected = { date ->
                logDayDate = date
                logDayRecordId = rec.id
                logDayFor = null
            },
        )
    }

    // Log day — step 2: fill in details
    if (logDayDate != null && logDayRecordId != null) {
        val targetDate = logDayDate!!
        val targetId = logDayRecordId!!
        LogDaySheet(
            onDismiss = { logDayDate = null; logDayRecordId = null },
            onSave = { intensity, mood, symptoms, notes ->
                scope.launch {
                    val day = com.haodong.yimalaile.domain.menstrual.DailyRecord(
                        date = targetDate, intensity = intensity, mood = mood,
                        symptoms = symptoms, notes = notes,
                    )
                    service.logDay(targetId, day)
                    logDayDate = null; logDayRecordId = null
                    refresh()
                    snackbar.showSnackbar(successMsg)
                }
            }
        )
    }
}

@Composable
private fun RecordCard(record: MenstrualRecord, daysStr: String, onClick: () -> Unit) {
    val days = record.endDate?.let { record.startDate.until(it, DateTimeUnit.DAY) + 1 }
    val isActive = record.endDate == null

    Box(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.BlushPink.copy(alpha = 0.25f))
            .clickable(onClick = onClick)
            .padding(20.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${record.startDate.monthNumber}月${record.startDate.dayOfMonth}日",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.DarkCoffee,
                    )
                    if (record.endDate != null) {
                        Text(
                            " — ${record.endDate.monthNumber}月${record.endDate.dayOfMonth}日",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.DarkCoffee.copy(alpha = 0.5f),
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                    val dotCount = (days ?: 1).coerceAtMost(10)
                    repeat(dotCount) { i ->
                        val alpha = 0.25f + (0.5f * (1f - i.toFloat() / dotCount))
                        Box(Modifier.size(8.dp).clip(CircleShape).background(AppColors.DeepRose.copy(alpha = alpha)))
                    }
                    if (record.dailyRecords.isNotEmpty()) {
                        Spacer(Modifier.width(6.dp))
                        Text("${record.dailyRecords.size}条记录", style = MaterialTheme.typography.labelSmall, color = AppColors.DarkCoffee.copy(alpha = 0.4f))
                    }
                }
            }

            if (isActive) {
                Box(
                    Modifier.clip(RoundedCornerShape(8.dp))
                        .background(AppColors.DeepRose.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("进行中", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = AppColors.DeepRose)
                }
            } else if (days != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$days", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = AppColors.DarkCoffee)
                    Text(daysStr, style = MaterialTheme.typography.labelSmall, color = AppColors.DarkCoffee.copy(alpha = 0.5f))
                }
            }
        }
    }
}
