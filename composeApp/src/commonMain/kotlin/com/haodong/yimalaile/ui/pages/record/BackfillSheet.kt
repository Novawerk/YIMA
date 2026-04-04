package com.haodong.yimalaile.ui.pages.record

import androidx.compose.foundation.layout.*
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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

/**
 * Full-screen backfill sheet — select a past period date range.
 */
@Composable
fun BackfillSheet(
    existingRecords: List<MenstrualRecord> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (start: LocalDate, end: LocalDate) -> Unit,
) {
    var selectedStart by remember { mutableStateOf<LocalDate?>(null) }
    var selectedEnd by remember { mutableStateOf<LocalDate?>(null) }

    val calendarState = remember(existingRecords) {
        CycleState(records = existingRecords, predictions = emptyList(), currentPeriod = null, inPredictedPeriod = false)
    }

    // Occupied dates from existing records
    val occupiedDates = remember(existingRecords) {
        buildSet {
            existingRecords.forEach { record ->
                val end = record.endDate ?: return@forEach
                var d = record.startDate
                while (d <= end) { add(d); d = d.plus(1, DateTimeUnit.DAY) }
            }
        }
    }

    Scaffold(
        topBar = {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).safeDrawingPadding(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) { Text(stringResource(Res.string.onboarding_back)) }
                Spacer(Modifier.weight(1f))
                Text(stringResource(Res.string.backfill_title), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(48.dp)) // balance
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp)) {
            // Feedback text
            val hint = when {
                selectedStart != null && selectedEnd != null ->
                    stringResource(Res.string.backfill_selected_range, selectedStart!!.monthNumber, selectedStart!!.dayOfMonth, selectedEnd!!.monthNumber, selectedEnd!!.dayOfMonth)
                selectedStart != null ->
                    stringResource(Res.string.backfill_selected_start, selectedStart!!.monthNumber, selectedStart!!.dayOfMonth)
                else -> stringResource(Res.string.backfill_hint)
            }
            Text(hint, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            SmallSpacer(12)
            CycleCalendarLegend()
            SmallSpacer(8)

            CycleCalendarGrid(
                state = calendarState, phaseInfo = null,
                selectedStart = selectedStart,
                selectedEnd = selectedEnd,
                onDateClick = { date ->
                    when {
                        selectedStart == null -> { selectedStart = date; selectedEnd = null }
                        selectedEnd == null -> {
                            if (date < selectedStart!!) { selectedEnd = selectedStart; selectedStart = date }
                            else selectedEnd = date
                        }
                        else -> { selectedStart = date; selectedEnd = null }
                    }
                },
                isDateEnabled = { it !in occupiedDates },
                modifier = Modifier.weight(1f),
                monthRange = -12..0,
            )

            SmallSpacer(16)
            PrimaryCta(
                text = stringResource(Res.string.onboarding_save),
                onClick = { if (selectedStart != null && selectedEnd != null) onSave(selectedStart!!, selectedEnd!!) },
                enabled = selectedStart != null && selectedEnd != null,
            )
            SmallSpacer(16)
        }
    }
}
