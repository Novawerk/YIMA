package com.haodong.yimalaile.ui.record

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.DailyRecord
import com.haodong.yimalaile.domain.menstrual.Intensity
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.domain.menstrual.Mood
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.until
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecordDetailSheet(
    record: MenstrualRecord,
    onDismiss: () -> Unit,
    onEditStart: () -> Unit,
    onEditEnd: () -> Unit,
    onLogDay: () -> Unit,
    onDelete: () -> Unit,
) {
    val days = record.endDate?.let { record.startDate.until(it, DateTimeUnit.DAY) + 1 }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Hero duration circle
            Box(
                Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (days != null) "$days" else "~",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(stringResource(Res.string.detail_unit_days), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Date range
            Text(
                if (record.endDate != null) stringResource(Res.string.detail_date_range, record.startDate.monthNumber, record.startDate.dayOfMonth, record.endDate.monthNumber, record.endDate.dayOfMonth)
                else stringResource(Res.string.detail_date_start_only, record.startDate.monthNumber, record.startDate.dayOfMonth, stringResource(Res.string.detail_in_progress)),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                "${record.startDate.year}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )

            // Period dots — decorative arc
            if (days != null && days > 0) {
                Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    repeat(days.coerceAtMost(14)) { i ->
                        val progress = i.toFloat() / days.coerceAtMost(14)
                        val alpha = 0.7f - (0.5f * progress)
                        val size = (14 - 4 * progress).dp
                        Box(
                            Modifier
                                .padding(horizontal = 2.dp)
                                .size(size)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Action buttons — two rows, rounded cards
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionCard("📅", stringResource(Res.string.detail_edit_start), onClick = onEditStart, modifier = Modifier.weight(1f))
                ActionCard("🏁", stringResource(Res.string.detail_edit_end), onClick = onEditEnd, modifier = Modifier.weight(1f))
                ActionCard("📝", stringResource(Res.string.detail_add_record), onClick = onLogDay, modifier = Modifier.weight(1f))
            }

            // Daily records
            if (record.dailyRecords.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Text(
                    stringResource(Res.string.detail_daily_records),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                record.dailyRecords.sortedBy { it.date }.forEach { day ->
                    DailyRecordRow(day)
                    Spacer(Modifier.height(8.dp))
                }
            } else {
                Spacer(Modifier.height(20.dp))
                Box(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        stringResource(Res.string.detail_no_records),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Delete
            Spacer(Modifier.height(32.dp))
            Text(
                stringResource(Res.string.detail_delete),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                modifier = Modifier.clickable { showDeleteConfirm = true }.padding(8.dp),
            )
            Spacer(Modifier.height(8.dp))
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
private fun ActionCard(emoji: String, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(emoji, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DailyRecordRow(day: DailyRecord) {
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Date + mood column
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (day.mood != null) {
                Text(moodLabel(day.mood), fontSize = 24.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "${day.date.monthNumber}/${day.date.dayOfMonth}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.width(16.dp))

        // Divider line
        Box(
            Modifier
                .width(2.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        )

        Spacer(Modifier.width(16.dp))

        // Details
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Intensity
            if (day.intensity != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Intensity dots
                    val dotCount = when (day.intensity) {
                        Intensity.LIGHT -> 1; Intensity.MEDIUM -> 2; Intensity.HEAVY -> 3
                    }
                    repeat(dotCount) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)))
                    }
                    Text(
                        when (day.intensity) {
                            Intensity.LIGHT -> stringResource(Res.string.intensity_light)
                            Intensity.MEDIUM -> stringResource(Res.string.intensity_medium)
                            Intensity.HEAVY -> stringResource(Res.string.intensity_heavy)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Symptoms
            if (day.symptoms.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    day.symptoms.forEach { symptom ->
                        Box(
                            Modifier.clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(symptom, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            // Notes
            if (!day.notes.isNullOrBlank()) {
                Text(
                    day.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 2,
                )
            }
        }
    }
}

private fun moodLabel(m: Mood) = when (m) { Mood.HAPPY -> "😊"; Mood.NEUTRAL -> "😐"; Mood.SAD -> "😔"; Mood.VERY_SAD -> "😢" }
