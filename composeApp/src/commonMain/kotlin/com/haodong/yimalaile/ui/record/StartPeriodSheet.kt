package com.haodong.yimalaile.ui.record

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.components.RangeCalendar
import com.haodong.yimalaile.ui.theme.AppColors
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.dialog_confirm
import yimalaile.composeapp.generated.resources.record_start_date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartPeriodSheet(
    existingRecords: List<MenstrualRecord> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    var selected by remember { mutableStateOf<LocalDate?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AppColors.SoftCream,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text(
                stringResource(Res.string.record_start_date),
                style = MaterialTheme.typography.titleLarge,
                color = AppColors.DarkCoffee,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Spacer(Modifier.height(8.dp))
            RangeCalendar(
                existingRecords = existingRecords,
                selectedStart = selected,
                selectedEnd = selected, // same = single highlight
                onDateClick = { selected = it },
                singleSelectMode = true,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.height(8.dp))
            PrimaryCta(
                text = stringResource(Res.string.dialog_confirm),
                onClick = { onConfirm(selected!!) },
                enabled = selected != null,
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}
