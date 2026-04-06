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
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.components.SmallSpacer
import io.github.adrcotfas.datetime.names.TextStyle
import io.github.adrcotfas.datetime.names.getDisplayName
import kotlinx.datetime.*
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*
import kotlin.time.Clock

/**
 * A standardized date selection sheet.
 * Supports:
 * - Title and optional hint
 * - Min/Max date constraints
 * - Default initial selection
 * - Returns a single date
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericDatePickerSheet(
    title: String,
    hint: String? = null,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    defaultDate: LocalDate? = null,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    var selected by remember { mutableStateOf<LocalDate?>(defaultDate ?: today) }

    // Build the list of selectable dates (reverse chronological)
    val dates = remember(today, minDate, maxDate) {
        val start = maxDate ?: today
        val end = minDate ?: today.minus(3, DateTimeUnit.MONTH)
        buildList {
            var d = start
            while (d >= end) {
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
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            SmallSpacer(4)
            Text(
                hint ?: (selected?.let { "${it.monthNumber}/${it.dayOfMonth}" } ?: ""),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            SmallSpacer(12)

            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                state = rememberLazyListState(),
            ) {
                items(dates, key = { it.toEpochDays() }) { date ->
                    DatePickerListItem(
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
                    selected?.let { onConfirm(it) }
                },
                enabled = selected != null,
            )
        }
    }
}

@Composable
private fun DatePickerListItem(
    date: LocalDate,
    today: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val isToday = date == today
    val daysAgo = date.until(today, DateTimeUnit.DAY).toInt()

    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
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
                formatDateListItem(date),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isToday || isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = contentColor,
                modifier = Modifier.weight(1f),
            )
            Text(
                when {
                    isToday -> stringResource(Res.string.legend_today)
                    daysAgo == 1 -> stringResource(Res.string.date_yesterday)
                    daysAgo > 0 -> stringResource(Res.string.date_n_days_ago, daysAgo)
                    else -> "" // Future dates?
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) contentColor.copy(alpha = 0.7f)
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

private fun formatDateListItem(date: LocalDate): String {
    val weekday = date.dayOfWeek.getDisplayName(TextStyle.SHORT)
    return "${date.monthNumber}/${date.dayOfMonth} $weekday"
}
