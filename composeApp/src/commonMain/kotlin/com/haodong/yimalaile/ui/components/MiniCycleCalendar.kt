package com.haodong.yimalaile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.CyclePhaseInfo
import com.haodong.yimalaile.domain.menstrual.CycleState
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*
import kotlin.time.Clock

private val TODAY_GREEN = Color(0xFF4CAF50)

/**
 * Minimal horizontal scrolling calendar strip showing ~90 days.
 * Period days colored, today highlighted green, with "Today" button to scroll back.
 */
@Composable
fun MiniCycleCalendar(
    state: CycleState,
    phaseInfo: CyclePhaseInfo?,
    modifier: Modifier = Modifier,
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val dateMap = buildDateMap(state, phaseInfo, today)
    val scope = rememberCoroutineScope()

    // Build 90 days: 45 past + today + 44 future
    val days = (-45..44).map { today.plus(it, DateTimeUnit.DAY) }
    val todayIndex = 45
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = todayIndex - 3)
    LaunchedEffect(Unit) { listState.scrollToItem(todayIndex - 3) }

    val periodColor = MaterialTheme.colorScheme.error
    val ovulationColor = MaterialTheme.colorScheme.tertiary
    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(modifier) {
        // Month + Year header + Today button
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Show current visible month
            Text(
                "${monthName(today.month)} ${today.year}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.weight(1f))
            TextButton(onClick = {
                scope.launch { listState.animateScrollToItem(todayIndex - 3) }
            }) {
                Text(stringResource(Res.string.legend_today), style = MaterialTheme.typography.labelSmall)
            }
        }

        SmallSpacer(8)

        // Day strip
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(days) { date ->
                val type = dateMap[date] ?: DayType.NONE
                val isToday = date == today
                val isNewMonth = date.dayOfMonth == 1

                val bgColor = when {
                    isToday && type != DayType.NONE -> TODAY_GREEN
                    type == DayType.PERIOD || type == DayType.ACTIVE_PERIOD -> periodColor
                    type == DayType.OVULATION -> ovulationColor
                    else -> Color.Transparent
                }
                val textColor = when {
                    isToday -> if (type != DayType.NONE) Color.White else TODAY_GREEN
                    type == DayType.PERIOD || type == DayType.ACTIVE_PERIOD || type == DayType.OVULATION -> Color.White
                    type == DayType.PREDICTED_PERIOD -> periodColor.copy(alpha = 0.4f)
                    type == DayType.PREDICTED_OVULATION -> ovulationColor.copy(alpha = 0.4f)
                    date > today -> onSurface.copy(alpha = 0.25f)
                    else -> onSurface
                }
                val isPredicted = type == DayType.PREDICTED_PERIOD || type == DayType.PREDICTED_OVULATION

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(36.dp),
                ) {
                    // Weekday
                    Text(
                        date.dayOfWeek.name.take(1),
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurface.copy(alpha = 0.3f),
                        fontSize = 9.sp,
                    )
                    SmallSpacer(2)
                    // Day circle
                    Box(
                        Modifier.size(28.dp).clip(CircleShape)
                            .background(bgColor)
                            .then(
                                when {
                                    isToday && type == DayType.NONE -> Modifier.border(2.dp, TODAY_GREEN, CircleShape)
                                    isPredicted -> Modifier.border(1.dp, if (type == DayType.PREDICTED_PERIOD) periodColor.copy(alpha = 0.3f) else ovulationColor.copy(alpha = 0.3f), CircleShape)
                                    else -> Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${date.dayOfMonth}",
                            fontSize = 11.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = textColor,
                        )
                    }
                    // Month label on 1st
                    if (isNewMonth) {
                        Text(
                            shortMonthName(date.month),
                            fontSize = 8.sp,
                            color = onSurface.copy(alpha = 0.4f),
                        )
                    } else {
                        SmallSpacer(10)
                    }
                }
            }
        }
    }
}

private fun monthName(month: Month): String = when (month) {
    Month.JANUARY -> "January"; Month.FEBRUARY -> "February"
    Month.MARCH -> "March"; Month.APRIL -> "April"
    Month.MAY -> "May"; Month.JUNE -> "June"
    Month.JULY -> "July"; Month.AUGUST -> "August"
    Month.SEPTEMBER -> "September"; Month.OCTOBER -> "October"
    Month.NOVEMBER -> "November"; Month.DECEMBER -> "December"
}

private fun shortMonthName(month: Month): String = when (month) {
    Month.JANUARY -> "Jan"; Month.FEBRUARY -> "Feb"
    Month.MARCH -> "Mar"; Month.APRIL -> "Apr"
    Month.MAY -> "May"; Month.JUNE -> "Jun"
    Month.JULY -> "Jul"; Month.AUGUST -> "Aug"
    Month.SEPTEMBER -> "Sep"; Month.OCTOBER -> "Oct"
    Month.NOVEMBER -> "Nov"; Month.DECEMBER -> "Dec"
}
