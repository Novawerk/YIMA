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
import kotlinx.datetime.*
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*
import kotlin.time.Clock

/**
 * "姨妈来了" sheet — uses CycleCalendarGrid for date picking.
 * If start date is > 3 days ago, also allows selecting end date (auto-filled from avg).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartPeriodSheet(
    existingRecords: List<MenstrualRecord> = emptyList(),
    avgPeriodLength: Int = 5,
    onDismiss: () -> Unit,
    onConfirm: (start: LocalDate, end: LocalDate?) -> Unit,
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    var selectedStart by remember { mutableStateOf<LocalDate?>(null) }
    var selectedEnd by remember { mutableStateOf<LocalDate?>(null) }

    // Whether the selected start is old enough to also require end date
    val needsEndDate = selectedStart != null &&
            selectedStart!!.until(today, DateTimeUnit.DAY).toInt() > 3

    // Auto-fill end date when start is selected and > 3 days ago
    LaunchedEffect(selectedStart) {
        if (needsEndDate && selectedEnd == null && selectedStart != null) {
            selectedEnd = selectedStart!!.plus(avgPeriodLength - 1, DateTimeUnit.DAY)
        }
    }

    val minDate = existingRecords
        .filter { it.endDate != null }
        .maxByOrNull { it.endDate!! }
        ?.endDate?.plus(1, DateTimeUnit.DAY)

    val calendarState = remember(existingRecords) {
        CycleState(records = existingRecords, predictions = emptyList(), currentPeriod = null, inPredictedPeriod = false)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp)) {
            Text(
                stringResource(Res.string.start_period_question),
                style = MaterialTheme.typography.titleLarge,
            )
            SmallSpacer(4)
            // Show selected range feedback
            val hint = when {
                selectedStart != null && needsEndDate && selectedEnd != null ->
                    "${selectedStart!!.monthNumber}/${selectedStart!!.dayOfMonth} — ${selectedEnd!!.monthNumber}/${selectedEnd!!.dayOfMonth}"
                selectedStart != null ->
                    "${selectedStart!!.monthNumber}/${selectedStart!!.dayOfMonth}"
                else -> stringResource(Res.string.start_period_hint)
            }
            Text(
                hint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SmallSpacer(12)
            CycleCalendarLegend()
            SmallSpacer(8)
            CycleCalendarGrid(
                state = calendarState,
                phaseInfo = null,
                selectedStart = selectedStart,
                selectedEnd = if (needsEndDate) selectedEnd else null,
                selectedDate = if (!needsEndDate) selectedStart else null,
                onDateClick = { date ->
                    if (needsEndDate && selectedStart != null && date > selectedStart!!) {
                        selectedEnd = date
                    } else {
                        selectedStart = date
                        selectedEnd = null
                    }
                },
                isDateEnabled = { date ->
                    date <= today && (minDate == null || date >= minDate)
                },
                modifier = Modifier.height(350.dp),
                monthRange = -6..0,
            )

            if (needsEndDate) {
                SmallSpacer(4)
                Text(
                    stringResource(Res.string.end_period_question),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SmallSpacer(16)
            PrimaryCta(
                text = stringResource(Res.string.onboarding_confirm),
                onClick = {
                    selectedStart?.let { start ->
                        onConfirm(start, if (needsEndDate) selectedEnd else null)
                    }
                },
                enabled = selectedStart != null && (!needsEndDate || selectedEnd != null),
            )
        }
    }
}
