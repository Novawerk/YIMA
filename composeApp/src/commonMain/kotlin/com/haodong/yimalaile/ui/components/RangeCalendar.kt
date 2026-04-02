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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.ui.theme.AppColors
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

private val WEEKDAY_LABELS = listOf("一", "二", "三", "四", "五", "六", "日")

/**
 * Custom calendar with period record highlights and date selection.
 *
 * @param singleSelectMode true = single date pick, false = range pick (two taps)
 */
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

    // Build set of occupied dates from existing records
    val occupiedDates = buildSet {
        existingRecords.forEach { record ->
            val end = record.endDate ?: today // active period extends to today
            var d = record.startDate
            while (d <= end) {
                add(d)
                d = d.plus(1, DateTimeUnit.DAY)
            }
        }
    }

    // Generate months in chronological order (oldest first, newest last)
    val months = (11 downTo 0).map { offset ->
        val d = today.minus(offset, DateTimeUnit.MONTH)
        YearMonth(d.year, d.month)
    }

    // Start scrolled to bottom (current month)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = months.size - 1)
    LaunchedEffect(Unit) {
        listState.scrollToItem(months.size - 1)
    }

    Column(modifier) {
        // Weekday header
        Row(Modifier.fillMaxWidth()) {
            WEEKDAY_LABELS.forEach { label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.DarkCoffee.copy(alpha = 0.5f),
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        LazyColumn(state = listState) {
            items(months, key = { "${it.year}-${it.month}" }) { ym ->
                MonthGrid(
                    yearMonth = ym,
                    today = today,
                    occupiedDates = occupiedDates,
                    selectedStart = selectedStart,
                    selectedEnd = selectedEnd,
                    onDateClick = onDateClick,
                    minDate = minDate,
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
) {
    val firstDay = LocalDate(yearMonth.year, yearMonth.month, 1)
    // dayOfWeek: Monday=1 ... Sunday=7
    val startOffset = (firstDay.dayOfWeek.ordinal) // Monday=0
    val daysInMonth = when (yearMonth.month) {
        Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY,
        Month.AUGUST, Month.OCTOBER, Month.DECEMBER -> 31
        Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
        Month.FEBRUARY -> if (yearMonth.year % 4 == 0 && (yearMonth.year % 100 != 0 || yearMonth.year % 400 == 0)) 29 else 28
        else -> 30
    }
    val totalCells = startOffset + daysInMonth
    val rows = (totalCells + 6) / 7

    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        // Month label
        Text(
            "${yearMonth.year}年${yearMonth.month.number}月",
            style = MaterialTheme.typography.labelLarge,
            color = AppColors.DarkCoffee.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 8.dp),
        )

        for (row in 0 until rows) {
            Row(Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNum = cellIndex - startOffset + 1

                    if (dayNum < 1 || dayNum > daysInMonth) {
                        // Empty cell
                        Box(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val date = LocalDate(yearMonth.year, yearMonth.month, dayNum)
                        val isFuture = date > today
                        val isOccupied = date in occupiedDates
                        val isBelowMin = minDate != null && date < minDate
                        val isDisabled = isFuture || isOccupied || isBelowMin
                        val isToday = date == today

                        val isSelectedStart = date == selectedStart
                        val isSelectedEnd = date == selectedEnd
                        val isInRange = selectedStart != null && selectedEnd != null &&
                                date > selectedStart && date < selectedEnd

                        val bgColor = when {
                            isSelectedStart || isSelectedEnd -> AppColors.DeepRose
                            isInRange -> AppColors.WarmPeach.copy(alpha = 0.6f)
                            isOccupied -> AppColors.DeepRose.copy(alpha = 0.2f)
                            else -> Color.Transparent
                        }
                        val textColor = when {
                            isSelectedStart || isSelectedEnd -> Color.White
                            isDisabled -> AppColors.DarkCoffee.copy(alpha = 0.2f)
                            isToday -> AppColors.DeepRose
                            else -> AppColors.DarkCoffee
                        }

                        Box(
                            Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(bgColor)
                                .then(
                                    if (isToday && !isSelectedStart && !isSelectedEnd && !isOccupied)
                                        Modifier.border(1.5.dp, AppColors.DeepRose, CircleShape)
                                    else Modifier
                                )
                                .then(
                                    if (!isDisabled) Modifier.clickable { onDateClick(date) }
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "$dayNum",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelectedStart || isSelectedEnd) FontWeight.Bold else FontWeight.Normal,
                                color = textColor,
                            )
                        }
                    }
                }
            }
        }
    }
}
