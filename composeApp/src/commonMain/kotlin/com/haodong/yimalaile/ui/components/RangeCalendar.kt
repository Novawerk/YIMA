package com.haodong.yimalaile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

@Composable
fun RangeCalendar(
    existingRecords: List<MenstrualRecord>,
    selectedStart: LocalDate?,
    selectedEnd: LocalDate?,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    singleSelectMode: Boolean = false,
    minDate: LocalDate? = null,
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val surface = MaterialTheme.colorScheme.onSurface
    val surfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val rangeBand = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    val occupiedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)

    val occupiedDates = buildSet {
        existingRecords.forEach { record ->
            val end = record.endDate ?: today
            var d = record.startDate
            while (d <= end) { add(d); d = d.plus(1, DateTimeUnit.DAY) }
        }
    }

    val months = (11 downTo 0).map { offset ->
        val d = today.minus(offset, DateTimeUnit.MONTH)
        YearMonth(d.year, d.month)
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = months.size - 1)
    LaunchedEffect(Unit) { listState.scrollToItem(months.size - 1) }

    val weekdayLabels = listOf(
        stringResource(Res.string.weekday_mon),
        stringResource(Res.string.weekday_tue),
        stringResource(Res.string.weekday_wed),
        stringResource(Res.string.weekday_thu),
        stringResource(Res.string.weekday_fri),
        stringResource(Res.string.weekday_sat),
        stringResource(Res.string.weekday_sun),
    )

    Column(modifier) {
        // Weekday header
        Row(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(vertical = 8.dp),
        ) {
            weekdayLabels.forEachIndexed { i, label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (i >= 5) primary.copy(alpha = 0.7f) else surfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        LazyColumn(state = listState) {
            items(months, key = { "${it.year}-${it.month}" }) { ym ->
                MonthGrid(
                    yearMonth = ym, today = today,
                    occupiedDates = occupiedDates,
                    selectedStart = selectedStart, selectedEnd = selectedEnd,
                    onDateClick = onDateClick, minDate = minDate,
                    primary = primary, onPrimary = onPrimary,
                    primaryContainer = primaryContainer,
                    rangeBand = rangeBand, occupiedColor = occupiedColor,
                    surface = surface, surfaceVariant = surfaceVariant,
                )
            }
        }
    }
}

private data class YearMonth(val year: Int, val month: Month)

@Composable
private fun MonthGrid(
    yearMonth: YearMonth,
    today: LocalDate,
    occupiedDates: Set<LocalDate>,
    selectedStart: LocalDate?,
    selectedEnd: LocalDate?,
    onDateClick: (LocalDate) -> Unit,
    minDate: LocalDate?,
    primary: Color, onPrimary: Color, primaryContainer: Color,
    rangeBand: Color, occupiedColor: Color,
    surface: Color, surfaceVariant: Color,
) {
    val firstDay = LocalDate(yearMonth.year, yearMonth.month, 1)
    val startOffset = firstDay.dayOfWeek.ordinal
    val daysInMonth = when (yearMonth.month) {
        Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY,
        Month.AUGUST, Month.OCTOBER, Month.DECEMBER -> 31
        Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
        Month.FEBRUARY -> if (yearMonth.year % 4 == 0 && (yearMonth.year % 100 != 0 || yearMonth.year % 400 == 0)) 29 else 28
        else -> 30
    }
    val rows = (startOffset + daysInMonth + 6) / 7

    Column(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        // Month label — clear "Month Year" format
        Text(
            "${monthName(yearMonth.month)} ${yearMonth.year}",
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 20.dp, bottom = 12.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = surface,
        )

        for (row in 0 until rows) {
            Row(Modifier.fillMaxWidth().height(48.dp)) {
                for (col in 0 until 7) {
                    val dayNum = row * 7 + col - startOffset + 1

                    if (dayNum < 1 || dayNum > daysInMonth) {
                        Box(Modifier.weight(1f).fillMaxHeight())
                    } else {
                        val date = LocalDate(yearMonth.year, yearMonth.month, dayNum)
                        val isFuture = date > today
                        val isOccupied = date in occupiedDates
                        val isBelowMin = minDate != null && date < minDate
                        val isDisabled = isFuture || isOccupied || isBelowMin
                        val isToday = date == today
                        val isWeekend = col >= 5

                        val isStart = date == selectedStart
                        val isEnd = date == selectedEnd
                        val isInRange = selectedStart != null && selectedEnd != null &&
                                date > selectedStart && date < selectedEnd

                        // Background band for range continuity
                        val bandModifier = when {
                            isStart && selectedEnd != null && selectedStart != selectedEnd ->
                                Modifier.background(rangeBand, RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50))
                            isEnd && selectedStart != null && selectedStart != selectedEnd ->
                                Modifier.background(rangeBand, RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50))
                            isInRange -> Modifier.background(rangeBand)
                            else -> Modifier
                        }

                        Box(
                            Modifier.weight(1f).fillMaxHeight().then(bandModifier),
                            contentAlignment = Alignment.Center,
                        ) {
                            // Circle
                            val circleBg = when {
                                isStart || isEnd -> primary
                                isOccupied -> occupiedColor
                                else -> Color.Transparent
                            }
                            val textColor = when {
                                isStart || isEnd -> onPrimary
                                isDisabled -> surfaceVariant.copy(alpha = 0.25f)
                                isToday -> primary
                                isWeekend -> primary.copy(alpha = 0.6f)
                                else -> surface
                            }

                            Box(
                                Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(circleBg)
                                    .then(
                                        if (isToday && !isStart && !isEnd)
                                            Modifier.border(1.5.dp, primary, CircleShape)
                                        else Modifier
                                    )
                                    .then(if (!isDisabled) Modifier.clickable { onDateClick(date) } else Modifier),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    "$dayNum",
                                    fontSize = 14.sp,
                                    fontWeight = if (isStart || isEnd || isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = textColor,
                                )
                            }

                        }
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
