package com.haodong.yimalaile.ui.pages.sheet.sheets

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
import kotlinx.datetime.number
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import yimalaile.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EndPeriodSheet(
    existingRecords: List<MenstrualRecord> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    // Find the current period's start date to limit the list
    val currentPeriodStart = remember(existingRecords) {
        existingRecords
            .filter { !it.isDeleted && !it.endConfirmed && it.endDate != null }
            .maxByOrNull { it.startDate }
            ?.startDate
    }

    // Default to predicted end date or today
    val defaultDate = remember(existingRecords) {
        val currentPeriod = existingRecords
            .filter { !it.isDeleted && !it.endConfirmed && it.endDate != null }
            .maxByOrNull { it.startDate }
        if (currentPeriod != null) {
            val completed = existingRecords.filter { !it.isDeleted && it.endDate != null && it.endConfirmed }
            val avgPeriod = if (completed.isNotEmpty()) {
                completed.map { it.startDate.until(it.endDate!!, DateTimeUnit.DAY).toInt() + 1 }.average().toInt()
            } else 5
            val predictedEnd = currentPeriod.startDate.plus(avgPeriod - 1, DateTimeUnit.DAY)
            if (predictedEnd <= today) predictedEnd else today
        } else {
            today
        }
    }
    var selected by remember { mutableStateOf<LocalDate?>(defaultDate) }

    // Build date list from today back to period start (or 1 month ago)
    val dates = remember(today, currentPeriodStart) {
        val earliest = currentPeriodStart ?: today.minus(1, DateTimeUnit.MONTH)
        buildList {
            var d = today
            while (d >= earliest) {
                add(d)
                d = d.minus(1, DateTimeUnit.DAY)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp)) {
            Text(stringResource(Res.string.end_period_question), style = MaterialTheme.typography.titleLarge)
            SmallSpacer(4)
            Text(
                if (selected != null) "${selected!!.month.number}/${selected!!.day}"
                else stringResource(Res.string.start_period_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            SmallSpacer(12)

            LazyColumn(
                modifier = Modifier.height(350.dp),
                state = rememberLazyListState(),
            ) {
                items(dates, key = { it.toEpochDays() }) { date ->
                    EndDateListItem(
                        date = date,
                        today = today,
                        isSelected = date == selected,
                        onClick = { selected = date },
                    )
                }
            }

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

@Composable
private fun EndDateListItem(
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
                formatEndDateDisplay(date),
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

private fun formatEndDateDisplay(date: LocalDate): String {
    val weekday = date.dayOfWeek.getDisplayName(TextStyle.SHORT)
    return "${date.month.number}/${date.day} $weekday"
}
