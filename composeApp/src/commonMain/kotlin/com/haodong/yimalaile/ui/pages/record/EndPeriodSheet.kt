package com.haodong.yimalaile.ui.pages.record

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.ui.components.CycleCalendarGrid
import com.haodong.yimalaile.ui.components.CycleCalendarLegend
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.components.SmallSpacer
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EndPeriodSheet(
    existingRecords: List<MenstrualRecord> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    var selected by remember { mutableStateOf<LocalDate?>(null) }

    val calendarState = remember(existingRecords) {
        CycleState(records = existingRecords, predictions = emptyList(), currentPeriod = null, inPredictedPeriod = false)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp)) {
            Text(stringResource(Res.string.end_period_question), style = MaterialTheme.typography.titleLarge)
            SmallSpacer(12)
            CycleCalendarLegend()
            SmallSpacer(8)
            CycleCalendarGrid(
                state = calendarState, phaseInfo = null,
                selectedDate = selected,
                onDateClick = { selected = it },
                modifier = Modifier.height(350.dp),
                monthRange = -3..0,
            )
            SmallSpacer(16)
            PrimaryCta(
                text = stringResource(Res.string.onboarding_confirm),
                onClick = {
                    val endDate = selected ?: return@PrimaryCta
                    onConfirm(endDate)
                },
                enabled = selected != null,
            )
        }
    }
}
