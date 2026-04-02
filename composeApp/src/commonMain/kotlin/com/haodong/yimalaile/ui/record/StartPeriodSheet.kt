package com.haodong.yimalaile.ui.record

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.theme.AppColors
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.dialog_confirm
import yimalaile.composeapp.generated.resources.record_start_date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartPeriodSheet(onDismiss: () -> Unit, onConfirm: (LocalDate) -> Unit) {
    val dateState = rememberDatePickerState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AppColors.SoftCream,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(Modifier.padding(24.dp)) {
            Text(
                stringResource(Res.string.record_start_date),
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
                    onConfirm(Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date)
                },
                enabled = dateState.selectedDateMillis != null,
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}
