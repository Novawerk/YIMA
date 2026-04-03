package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.domain.menstrual.Intensity
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.domain.menstrual.Mood
import com.haodong.yimalaile.ui.components.HeartDecoration
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.components.StatusPill
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.until
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

/** Home screen content when a period is currently active. */
@Composable
internal fun ColumnScope.InPeriodContent(
    state: CycleState,
    today: LocalDate,
    onEndPeriod: () -> Unit,
    onLogDay: () -> Unit,
    onLogSpecificDay: (LocalDate) -> Unit,
) {
    val activePeriod = state.activePeriod ?: return
    val dayCount = activePeriod.startDate.until(today, DateTimeUnit.DAY).toInt() + 1

    val completedPeriods = state.recentPeriods.filter { it.endDate != null }
    val avgPeriodLen = if (completedPeriods.isNotEmpty()) {
        completedPeriods.sumOf { it.startDate.until(it.endDate!!, DateTimeUnit.DAY).toInt() + 1 } / completedPeriods.size
    } else null
    val remainingDays = avgPeriodLen?.let { (it - dayCount).coerceAtLeast(0) }

    Text(
        stringResource(Res.string.home_take_care),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(8.dp))

    Row(verticalAlignment = Alignment.Top) {
        Text(
            stringResource(Res.string.home_in_period),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        HeartDecoration()
    }

    Spacer(Modifier.height(16.dp))

    StatusPill(stringResource(Res.string.home_day_n, dayCount))

    if (remainingDays != null) {
        Spacer(Modifier.height(8.dp))
        Text(
            if (remainingDays > 0) stringResource(Res.string.home_remaining_days, remainingDays)
            else stringResource(Res.string.home_exceeded_avg),
            style = MaterialTheme.typography.bodyMedium,
            color = if (remainingDays > 0) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
        )
    }

    Spacer(Modifier.height(24.dp))
    DayTimeline(
        activePeriod = activePeriod,
        today = today,
        onLogDay = onLogDay,
        onLogSpecificDay = onLogSpecificDay,
    )

    Spacer(Modifier.weight(1f))

    PrimaryCta(text = stringResource(Res.string.home_log_today), onClick = onLogDay)
    Spacer(Modifier.height(12.dp))
    OutlinedButton(
        onClick = onEndPeriod,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
    ) {
        Text(stringResource(Res.string.home_end_period), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
    }
}

// ============================================================
// Day-by-day timeline (shared between states)
// ============================================================

@Composable
internal fun DayTimeline(
    activePeriod: MenstrualRecord,
    today: LocalDate,
    onLogDay: () -> Unit,
    onLogSpecificDay: (LocalDate) -> Unit = { onLogDay() },
) {
    val dailyMap = activePeriod.dailyRecords.associateBy { it.date }
    val days = buildList {
        var d = activePeriod.startDate
        while (d <= today) { add(d); d = d.plus(1, DateTimeUnit.DAY) }
    }

    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        days.forEach { date ->
            val record = dailyMap[date]
            val isEmpty = record == null
            val isToday = date == today

            Row(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else Color.Transparent
                    )
                    .then(if (isEmpty) Modifier.clickable { onLogSpecificDay(date) } else Modifier)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(36.dp).clip(CircleShape)
                        .background(
                            if (record != null) MaterialTheme.colorScheme.primaryContainer
                            else if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "${date.dayOfMonth}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(Modifier.width(12.dp))

                if (record != null) {
                    Row(
                        Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (record.mood != null) {
                            Text(
                                when (record.mood) {
                                    Mood.HAPPY -> "😊"; Mood.NEUTRAL -> "😐"
                                    Mood.SAD -> "😔"; Mood.VERY_SAD -> "😢"
                                },
                                fontSize = 18.sp,
                            )
                        }
                        if (record.intensity != null) {
                            Box(
                                Modifier.clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    when (record.intensity) {
                                        Intensity.LIGHT -> stringResource(Res.string.intensity_light)
                                        Intensity.MEDIUM -> stringResource(Res.string.intensity_medium)
                                        Intensity.HEAVY -> stringResource(Res.string.intensity_heavy)
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        if (record.symptoms.isNotEmpty()) {
                            Text(
                                record.symptoms.joinToString(" · "),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    }
                } else {
                    Text(
                        if (isToday) stringResource(Res.string.home_log_today_hint) else stringResource(Res.string.home_tap_to_add),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "+",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                    )
                }
            }
        }
    }
}
