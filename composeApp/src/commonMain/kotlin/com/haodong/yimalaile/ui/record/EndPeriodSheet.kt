package com.haodong.yimalaile.ui.record

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.DailyRecord
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.components.RangeCalendar
import com.haodong.yimalaile.ui.theme.AppColors
import kotlinx.datetime.LocalDate
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
    existingRecords: List<MenstrualRecord> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    var selected by remember { mutableStateOf<LocalDate?>(null) }
    var pendingEndDate by remember { mutableStateOf<LocalDate?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AppColors.SoftCream,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text(
                stringResource(Res.string.record_end_date),
                style = MaterialTheme.typography.titleLarge,
                color = AppColors.DarkCoffee,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Spacer(Modifier.height(8.dp))
            RangeCalendar(
                existingRecords = existingRecords,
                selectedStart = selected,
                selectedEnd = selected,
                onDateClick = { selected = it },
                singleSelectMode = true,
                minDate = startDate,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.height(8.dp))
            PrimaryCta(
                text = stringResource(Res.string.dialog_confirm),
                onClick = {
                    val endDate = selected!!
                    val overflow = dailyRecords.count { it.date > endDate }
                    if (overflow > 0) {
                        pendingEndDate = endDate
                    } else {
                        onConfirm(endDate)
                    }
                },
                enabled = selected != null,
            )
            Spacer(Modifier.height(16.dp))
        }
    }

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
                }) { Text(stringResource(Res.string.dialog_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingEndDate = null }) {
                    Text(stringResource(Res.string.dialog_cancel))
                }
            },
        )
    }
}
