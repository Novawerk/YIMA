package com.haodong.yimalaile.ui.record

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.theme.AppColors
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.until
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.dialog_cancel
import yimalaile.composeapp.generated.resources.record_save_btn

@OptIn(ExperimentalMaterial3Api::class)
private val PastOnlyDates = object : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
        utcTimeMillis <= Clock.System.now().toEpochMilliseconds()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackfillSheet(
    existingRecords: List<MenstrualRecord> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (start: LocalDate, end: LocalDate) -> Unit,
) {
    val rangeState = rememberDateRangePickerState(selectableDates = PastOnlyDates)

    Scaffold(
        containerColor = AppColors.SoftCream,
        topBar = {
            TopAppBar(
                title = { Text("补录经期") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, stringResource(Res.string.dialog_cancel))
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            // Existing records banner
            if (existingRecords.isNotEmpty()) {
                Text(
                    "已有记录",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.DarkCoffee.copy(alpha = 0.5f),
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    existingRecords
                        .sortedByDescending { it.startDate }
                        .forEach { record ->
                            ExistingRecordChip(record)
                        }
                }
                Spacer(Modifier.height(12.dp))
            }

            DateRangePicker(
                state = rangeState,
                title = null,
                headline = null,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(containerColor = AppColors.SoftCream),
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.height(8.dp))
            PrimaryCta(
                text = stringResource(Res.string.record_save_btn),
                onClick = {
                    val startMillis = rangeState.selectedStartDateMillis ?: return@PrimaryCta
                    val endMillis = rangeState.selectedEndDateMillis ?: return@PrimaryCta
                    val start = Instant.fromEpochMilliseconds(startMillis).toLocalDateTime(TimeZone.UTC).date
                    val end = Instant.fromEpochMilliseconds(endMillis).toLocalDateTime(TimeZone.UTC).date
                    onSave(start, end)
                },
                enabled = rangeState.selectedStartDateMillis != null
                        && rangeState.selectedEndDateMillis != null,
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ExistingRecordChip(record: MenstrualRecord) {
    Row(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.WarmPeach.copy(alpha = 0.4f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(AppColors.DeepRose))
        Spacer(Modifier.width(6.dp))
        val text = if (record.endDate != null) {
            val days = record.startDate.until(record.endDate, DateTimeUnit.DAY) + 1
            "${record.startDate.monthNumber}/${record.startDate.dayOfMonth} — ${record.endDate.monthNumber}/${record.endDate.dayOfMonth} (${days}天)"
        } else {
            "${record.startDate.monthNumber}/${record.startDate.dayOfMonth} — 进行中"
        }
        Text(text, style = MaterialTheme.typography.labelSmall, color = AppColors.DarkCoffee)
    }
}
