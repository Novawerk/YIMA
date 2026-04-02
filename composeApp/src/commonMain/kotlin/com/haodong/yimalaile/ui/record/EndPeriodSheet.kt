package com.haodong.yimalaile.ui.record

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.DailyRecord
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.theme.AppColors
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.dialog_cancel
import yimalaile.composeapp.generated.resources.dialog_confirm
import yimalaile.composeapp.generated.resources.record_end_date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EndPeriodSheet(
    startDate: LocalDate,
    dailyRecords: List<DailyRecord>,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val startMillis = startDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
    val nowMillis = Clock.System.now().toEpochMilliseconds()
    val dateState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) =
                utcTimeMillis in startMillis..nowMillis
        }
    )

    var pendingEndDate by remember { mutableStateOf<LocalDate?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AppColors.SoftCream,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(Modifier.padding(24.dp)) {
            Text(
                stringResource(Res.string.record_end_date),
                style = MaterialTheme.typography.titleLarge,
                color = AppColors.DarkCoffee,
            )
            Spacer(Modifier.height(8.dp))
            DatePicker(
                state = dateState,
                title = null, headline = null, showModeToggle = false,
                colors = DatePickerDefaults.colors(containerColor = AppColors.SoftCream),
            )
            Spacer(Modifier.height(8.dp))
            PrimaryCta(
                text = stringResource(Res.string.dialog_confirm),
                onClick = {
                    val millis = dateState.selectedDateMillis ?: return@PrimaryCta
                    val endDate = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
                    val overflow = dailyRecords.count { it.date > endDate }
                    if (overflow > 0) {
                        pendingEndDate = endDate
                    } else {
                        onConfirm(endDate)
                    }
                },
                enabled = dateState.selectedDateMillis != null,
            )
            Spacer(Modifier.height(16.dp))
        }
    }

    // Confirmation dialog when daily records would be truncated
    val pending = pendingEndDate
    if (pending != null) {
        val overflow = dailyRecords.count { it.date > pending }
        AlertDialog(
            onDismissRequest = { pendingEndDate = null },
            title = { Text("移除多余记录？") },
            text = { Text("结束日期之后有 ${overflow} 条每日记录，确认后将被移除。") },
            confirmButton = {
                TextButton(onClick = {
                    pendingEndDate = null
                    onConfirm(pending)
                }) {
                    Text(stringResource(Res.string.dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingEndDate = null }) {
                    Text(stringResource(Res.string.dialog_cancel))
                }
            },
        )
    }
}
