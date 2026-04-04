package com.haodong.yimalaile.ui.pages.record

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
 * "姨妈来了" sheet — two-phase date picker.
 *
 * Phase 1: Pick start date.
 * Phase 2: If start > 3 days ago, pick end date (auto-filled from avg period length,
 *          user can adjust by tapping a different date).
 *
 * Tapping the start date chip resets to phase 1.
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
    // Explicit phase tracking: false = picking start, true = picking end
    var selectingEnd by remember { mutableStateOf(false) }

    val needsEndDate = selectedStart != null &&
            selectedStart!!.until(today, DateTimeUnit.DAY).toInt() > 3

    // When start is picked and > 3 days ago, switch to end-selection mode
    LaunchedEffect(selectedStart) {
        if (needsEndDate && !selectingEnd) {
            selectingEnd = true
            selectedEnd = selectedStart!!.plus(avgPeriodLength - 1, DateTimeUnit.DAY)
        } else if (!needsEndDate) {
            selectingEnd = false
            selectedEnd = null
        }
    }

    val minDate = existingRecords
        .filter { it.endDate != null }
        .maxByOrNull { it.endDate!! }
        ?.endDate?.plus(1, DateTimeUnit.DAY)

    val calendarState = remember(existingRecords) {
        CycleState(records = existingRecords, predictions = emptyList(), currentPeriod = null, inPredictedPeriod = false)
    }

    // Title and hint text
    val title = if (selectingEnd)
        stringResource(Res.string.end_period_question)
    else
        stringResource(Res.string.start_period_question)

    val hint = when {
        selectingEnd && selectedStart != null && selectedEnd != null ->
            "${selectedStart!!.monthNumber}/${selectedStart!!.dayOfMonth} — ${selectedEnd!!.monthNumber}/${selectedEnd!!.dayOfMonth}"
        selectedStart != null ->
            "${selectedStart!!.monthNumber}/${selectedStart!!.dayOfMonth}"
        else -> stringResource(Res.string.start_period_hint)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            SmallSpacer(4)
            // Date chip — tappable to reset when in end-selection mode
            if (selectingEnd && selectedStart != null) {
                Row {
                    Text(
                        hint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = {
                        selectingEnd = false
                        selectedEnd = null
                        selectedStart = null
                    }) {
                        Text(
                            stringResource(Res.string.onboarding_adjust),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            } else {
                Text(
                    hint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SmallSpacer(12)
            CycleCalendarLegend()
            SmallSpacer(8)
            CycleCalendarGrid(
                state = calendarState,
                phaseInfo = null,
                selectedStart = if (selectingEnd) selectedStart else null,
                selectedEnd = if (selectingEnd) selectedEnd else null,
                selectedDate = if (!selectingEnd) selectedStart else null,
                onDateClick = { date ->
                    if (date == selectedStart || date == selectedEnd) {
                        // Tap on already-selected date → clear all
                        selectedStart = null
                        selectedEnd = null
                        selectingEnd = false
                    } else if (selectingEnd && selectedStart != null) {
                        if (date >= selectedStart!!) {
                            selectedEnd = date
                        } else {
                            selectingEnd = false
                            selectedEnd = null
                            selectedStart = date
                        }
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
