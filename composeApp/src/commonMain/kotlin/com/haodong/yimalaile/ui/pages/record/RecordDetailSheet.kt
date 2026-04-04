package com.haodong.yimalaile.ui.pages.record

import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.DailyRecord
import com.haodong.yimalaile.domain.menstrual.Intensity
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.domain.menstrual.Mood
import com.haodong.yimalaile.ui.components.SmallSpacer
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.until
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Full-screen record detail — shows all days in the period with existing/missing records.
 * Missing days have a "+" button to add a daily record for that date.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDetailSheet(
    record: MenstrualRecord,
    onDismiss: () -> Unit,
    onEditStart: () -> Unit,
    onEditEnd: () -> Unit,
    onLogDay: () -> Unit,
    onDelete: () -> Unit,
    onLogSpecificDay: ((LocalDate) -> Unit)? = null,
) {
    val days = record.endDate?.let { record.startDate.until(it, DateTimeUnit.DAY).toInt() + 1 }
    val isActive = record.endDate == null
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Build list of all days in the period
    val endDate = record.endDate ?: today
    val allDays = buildList {
        var d = record.startDate
        while (d <= endDate) { add(d); d = d.plus(1, DateTimeUnit.DAY) }
    }
    val dailyMap = record.dailyRecords.associateBy { it.date }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(Modifier.fillMaxWidth()) {
            // ── App Bar ──
            val dateRange = "${record.startDate.monthNumber}/${record.startDate.dayOfMonth}" +
                    if (record.endDate != null) " — ${record.endDate.monthNumber}/${record.endDate.dayOfMonth}" else ""
            val subtitle = buildString {
                append(record.startDate.year)
                if (days != null) append(" · $days${stringResource(Res.string.unit_days)}")
                if (isActive) append(" · ${stringResource(Res.string.history_in_progress)}")
            }

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(dateRange, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // ── Day-by-day list ──
            SmallSpacer(8)
            Text(
                stringResource(Res.string.detail_daily_records),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            SmallSpacer(8)

            LazyColumn(
                Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(allDays, key = { it.toString() }) { date ->
                    val existing = dailyMap[date]
                    if (existing != null) {
                        DailyRecordCard(existing)
                    } else {
                        EmptyDayCard(
                            date = date,
                            onClick = { onLogSpecificDay?.invoke(date) ?: onLogDay() },
                        )
                    }
                }
            }

            // ── All actions together at bottom ──
            SmallSpacer(8)
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ActionChip(stringResource(Res.string.detail_edit_start), onClick = onEditStart, modifier = Modifier.weight(1f))
                ActionChip(stringResource(Res.string.detail_edit_end), onClick = onEditEnd, modifier = Modifier.weight(1f))
                ActionChip(stringResource(Res.string.detail_delete), onClick = { showDeleteConfirm = true }, modifier = Modifier.weight(1f), destructive = true)
            }
            SmallSpacer(16)
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(Res.string.detail_delete_title)) },
            text = { Text(stringResource(Res.string.detail_delete_body)) },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) {
                    Text(stringResource(Res.string.detail_delete_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(Res.string.dialog_cancel)) }
            },
        )
    }
}

@Composable
private fun ActionChip(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, destructive: Boolean = false) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
    ) {
        Box(Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun DailyRecordCard(day: DailyRecord) {
    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "${day.date.monthNumber}/${day.date.dayOfMonth}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(40.dp),
            )
            if (day.mood != null) {
                Text(moodLabel(day.mood), fontSize = 16.sp)
            }
            if (day.intensity != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        when (day.intensity) {
                            Intensity.LIGHT -> stringResource(Res.string.intensity_light)
                            Intensity.MEDIUM -> stringResource(Res.string.intensity_medium)
                            Intensity.HEAVY -> stringResource(Res.string.intensity_heavy)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }
            if (day.symptoms.isNotEmpty()) {
                Text(
                    day.symptoms.joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun EmptyDayCard(date: LocalDate, onClick: () -> Unit) {
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    val cornerRadius = 16.dp
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().drawBehind {
            val stroke = 1.5.dp.toPx()
            val dash = 6.dp.toPx()
            val gap = 4.dp.toPx()
            drawRoundRect(
                color = borderColor,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx()),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = stroke,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(dash, gap)),
                ),
            )
        },
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${date.monthNumber}/${date.dayOfMonth}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(40.dp),
            )
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private fun moodLabel(m: Mood) = when (m) {
    Mood.HAPPY -> "😊"; Mood.NEUTRAL -> "😐"
    Mood.SAD -> "😔"; Mood.VERY_SAD -> "😢"
}
