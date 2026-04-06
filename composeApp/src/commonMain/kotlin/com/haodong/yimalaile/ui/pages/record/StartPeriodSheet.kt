package com.haodong.yimalaile.ui.pages.record

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.components.SmallSpacer
import io.github.adrcotfas.datetime.names.TextStyle
import io.github.adrcotfas.datetime.names.getDisplayName
import kotlinx.datetime.*
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*
import kotlin.time.Clock

/**
 * "姨妈来了" sheet — date list picker.
 *
 * Phase 1: Pick start date from a reverse-chronological list (today → 1 month ago).
 * Phase 2: If start > 3 days ago, pick end date from a list (start date → today).
 *
 * Tapping the adjust button resets to phase 1.
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
    var selectedStart by remember { mutableStateOf<LocalDate?>(today) }
    var selectedEnd by remember { mutableStateOf<LocalDate?>(null) }
    var selectingEnd by remember { mutableStateOf(false) }

    val needsEndDate = selectedStart != null &&
            selectedStart!!.until(today, DateTimeUnit.DAY).toInt() > 3

    LaunchedEffect(selectedStart) {
        if (needsEndDate && !selectingEnd) {
            selectingEnd = true
            selectedEnd = selectedStart!!.plus(avgPeriodLength - 1, DateTimeUnit.DAY)
                .let { if (it > today) today else it }
        } else if (!needsEndDate) {
            selectingEnd = false
            selectedEnd = null
        }
    }

    val minDate = existingRecords
        .filter { it.endDate != null }
        .maxByOrNull { it.endDate!! }
        ?.endDate?.plus(1, DateTimeUnit.DAY)

    // Build date lists
    val oneMonthAgo = today.minus(1, DateTimeUnit.MONTH)
    val startDates = remember(today, minDate) {
        val earliest = if (minDate != null && minDate > oneMonthAgo) minDate else oneMonthAgo
        buildList {
            var d = today
            while (d >= earliest) {
                add(d)
                d = d.minus(1, DateTimeUnit.DAY)
            }
        }
    }

    val endDates = remember(selectedStart, today) {
        if (selectedStart == null) emptyList()
        else buildList {
            var d = today
            while (d >= selectedStart!!) {
                add(d)
                d = d.minus(1, DateTimeUnit.DAY)
            }
        }
    }

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
            if (selectingEnd && selectedStart != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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

            val dates = if (selectingEnd) endDates else startDates
            val selected = if (selectingEnd) selectedEnd else selectedStart

            LazyColumn(
                modifier = Modifier.height(350.dp),
                state = rememberLazyListState(),
            ) {
                items(dates, key = { it.toEpochDays() }) { date ->
                    DateListItem(
                        date = date,
                        today = today,
                        isSelected = date == selected,
                        onClick = {
                            if (selectingEnd) {
                                selectedEnd = date
                            } else {
                                selectedStart = date
                            }
                        },
                    )
                }
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

@Composable
private fun DateListItem(
    date: LocalDate,
    today: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val isToday = date == today
    val daysAgo = date.until(today, DateTimeUnit.DAY).toInt()

    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurface

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                formatDateDisplay(date),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isToday || isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = contentColor,
                modifier = Modifier.weight(1f),
            )
            Text(
                when {
                    isToday -> stringResource(Res.string.legend_today)
                    daysAgo == 1 -> stringResource(Res.string.date_yesterday)
                    else -> stringResource(Res.string.date_n_days_ago, daysAgo)
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) contentColor.copy(alpha = 0.7f)
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

private fun formatDateDisplay(date: LocalDate): String {
    val weekday = date.dayOfWeek.getDisplayName(TextStyle.SHORT)
    return "${date.monthNumber}/${date.dayOfMonth} $weekday"
}
