package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.CyclePhaseInfo
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.ui.components.PrimaryCta
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlinx.datetime.until
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

/**
 * Bottom section: past records row + CTA buttons.
 */
@Composable
internal fun HistorySection(
    state: CycleState,
    phaseInfo: CyclePhaseInfo?,
    today: LocalDate,
    inPeriod: Boolean,
    onStartPeriod: () -> Unit,
    onEndPeriod: () -> Unit,
    onLogDay: () -> Unit,
    onBackfill: () -> Unit,
    onCalendarClick: () -> Unit,
) {
    // Layout: past 3 (old→new) | current cycle | predictions
    val pastRecords = state.recentPeriods.filter { it.endDate != null }
        .sortedBy { it.startDate }
        .takeLast(3)
    val activePeriod = state.activePeriod
    val predictions = state.predictions.take(3)

    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier,
    ) {
        Column(
            Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                stringResource(Res.string.home_past_records),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Light,
            )

            val pastTag = stringResource(Res.string.home_section_past)
            val currentTag = stringResource(Res.string.home_section_current)
            val predictedTag = stringResource(Res.string.home_section_predicted)

            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                items(pastRecords, key = { it.id }) { record ->
                    val days = record.startDate.until(record.endDate!!, DateTimeUnit.DAY).toInt() + 1
                    HistoryCard(
                        label = "${record.startDate.month.number}/${record.startDate.day}",
                        days = days, tag = pastTag,
                        style = HistoryCardStyle.COMPLETED,
                        onClick = onCalendarClick,
                    )
                }
                if (activePeriod != null) {
                    item(key = "active") {
                        val days = activePeriod.startDate.until(today, DateTimeUnit.DAY).toInt() + 1
                        HistoryCard(
                            label = "${activePeriod.startDate.month.number}/${activePeriod.startDate.day}",
                            days = days, tag = currentTag,
                            style = HistoryCardStyle.ACTIVE,
                            onClick = onCalendarClick,
                        )
                    }
                } else if (predictions.isNotEmpty()) {
                    item(key = "current_predicted") {
                        val pred = predictions.first()
                        HistoryCard(
                            label = "${pred.predictedStart.month.number}/${pred.predictedStart.day}",
                            days = phaseInfo?.periodLength ?: 5, tag = currentTag,
                            style = HistoryCardStyle.ACTIVE,
                            onClick = onCalendarClick,
                        )
                    }
                }
                val futurePreds = if (activePeriod == null && predictions.isNotEmpty())
                    predictions.drop(1) else predictions
                items(futurePreds.size, key = { "pred_$it" }) { i ->
                    val pred = futurePreds[i]
                    HistoryCard(
                        label = "${pred.predictedStart.month.number}/${pred.predictedStart.day}",
                        days = phaseInfo?.periodLength ?: 5, tag = predictedTag,
                        style = HistoryCardStyle.PREDICTED,
                        onClick = onCalendarClick,
                    )
                }
            }

            // CTA buttons
            if (inPeriod) {
                val todayRecord = state.activePeriod?.dailyRecords?.find { it.date == today }
                val isTodayLogged = todayRecord != null
                val logText = if (isTodayLogged) {
                    "✓ ${stringResource(Res.string.home_log_today).removePrefix("✦ ")}"
                } else {
                    stringResource(Res.string.home_log_today)
                }

                if (todayRecord != null) {
                    TodaySummaryCard(todayRecord, onClick = onLogDay)
                } else {
                    PrimaryCta(text = logText, onClick = onLogDay)
                }
                OutlinedButton(
                    onClick = onEndPeriod,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Text(
                        stringResource(Res.string.home_end_period),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                if (pastRecords.size < 2) {
                    OutlinedButton(
                        onClick = onBackfill,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                    ) {
                        Text(
                            stringResource(Res.string.home_backfill_to_predict),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                PrimaryCta(
                    text = "✦ ${stringResource(Res.string.btn_record_period)}",
                    onClick = onStartPeriod,
                )
            }
        }
    }
}

// ============================================================
// History card
// ============================================================

internal enum class HistoryCardStyle { COMPLETED, ACTIVE, PREDICTED }

@Composable
internal fun HistoryCard(
    label: String,
    days: Int,
    tag: String,
    style: HistoryCardStyle,
    onClick: () -> Unit = {},
) {
    val daysStr = stringResource(Res.string.unit_days)
    val elevation = when (style) {
        HistoryCardStyle.COMPLETED -> 3.dp
        HistoryCardStyle.ACTIVE -> 1.dp
        HistoryCardStyle.PREDICTED -> 0.dp
    }
    val border = when (style) {
        HistoryCardStyle.ACTIVE -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else -> null
    }
    val textColor = when (style) {
        HistoryCardStyle.COMPLETED -> MaterialTheme.colorScheme.onSurface
        HistoryCardStyle.ACTIVE -> MaterialTheme.colorScheme.primary
        HistoryCardStyle.PREDICTED -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }
    val dashedColor = MaterialTheme.colorScheme.outlineVariant
    val cornerRadius = 12.dp

    Surface(
        onClick = onClick,
        tonalElevation = elevation,
        shape = MaterialTheme.shapes.small,
        border = border,
    ) {
        val dashedModifier = if (style == HistoryCardStyle.PREDICTED) {
            Modifier.drawBehind {
                val strokeWidth = 1.dp.toPx()
                val dash = 6.dp.toPx()
                val gap = 4.dp.toPx()
                drawRoundRect(
                    color = dashedColor,
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(cornerRadius.toPx()),
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, gap)),
                    ),
                )
            }
        } else Modifier
        Column(
            modifier = Modifier.width(78.dp).height(90.dp).then(dashedModifier).padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                tag,
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.4f),
            )
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (style == HistoryCardStyle.PREDICTED) FontWeight.Normal else FontWeight.Bold,
                color = textColor,
            )
            Text(
                "$days$daysStr",
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun TodaySummaryCard(record: com.haodong.yimalaile.domain.menstrual.DailyRecord, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val moodIcon = when (record.mood) {
                com.haodong.yimalaile.domain.menstrual.Mood.HAPPY -> "😊"
                com.haodong.yimalaile.domain.menstrual.Mood.NEUTRAL -> "😐"
                com.haodong.yimalaile.domain.menstrual.Mood.SAD -> "😔"
                com.haodong.yimalaile.domain.menstrual.Mood.VERY_SAD -> "😢"
                null -> null
            }

            if (moodIcon != null) {
                Text(moodIcon, fontSize = 24.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (record.intensity != null) {
                        val intensityLabel = when (record.intensity) {
                            com.haodong.yimalaile.domain.menstrual.Intensity.LIGHT -> stringResource(Res.string.intensity_light)
                            com.haodong.yimalaile.domain.menstrual.Intensity.MEDIUM -> stringResource(Res.string.intensity_medium)
                            com.haodong.yimalaile.domain.menstrual.Intensity.HEAVY -> stringResource(Res.string.intensity_heavy)
                        }
                        Text(
                            intensityLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (record.symptoms.isNotEmpty()) {
                        Text(
                            record.symptoms.joinToString(" · "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                if (!record.notes.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        record.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Text(
                "编辑 >",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
        }
    }
}
