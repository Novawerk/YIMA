package com.haodong.yimalaile.ui.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.theme.AppColors
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.dialog_cancel
import yimalaile.composeapp.generated.resources.record_save_btn
import yimalaile.composeapp.generated.resources.record_start_date

@OptIn(ExperimentalMaterial3Api::class)
private val PastOnlyDates = object : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
        utcTimeMillis <= Clock.System.now().toEpochMilliseconds()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackfillSheet(
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
