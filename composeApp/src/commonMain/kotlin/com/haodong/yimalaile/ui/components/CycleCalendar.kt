package com.haodong.yimalaile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.CyclePhaseInfo
import com.haodong.yimalaile.domain.menstrual.CycleState
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*
import kotlinx.datetime.*
import kotlin.time.Clock

// ============================================================
// Day type for calendar coloring
// ============================================================

enum class DayType {
    NONE, PERIOD, PREDICTED_PERIOD, OVULATION, PREDICTED_OVULATION,
}

// ============================================================
// Build date → type mapping from cycle data
// ============================================================

fun buildDateMap(
    state: CycleState,
    phaseInfo: CyclePhaseInfo?,
    today: LocalDate,
): Map<LocalDate, DayType> {
    val map = mutableMapOf<LocalDate, DayType>()
    val avgCycle = phaseInfo?.cycleLength ?: 28
    val avgPeriod = phaseInfo?.periodLength ?: 5

    state.records.forEach { record ->
        val end = record.endDate ?: today
        var d = record.startDate
        while (d <= end) { map[d] = DayType.PERIOD; d = d.plus(1, DateTimeUnit.DAY) }
    }
    state.predictions.forEach { pred ->
        val pEnd = pred.predictedEnd ?: pred.predictedStart.plus(avgPeriod - 1, DateTimeUnit.DAY)
        var d = pred.predictedStart
        while (d <= pEnd) { if (d !in map) map[d] = DayType.PREDICTED_PERIOD; d = d.plus(1, DateTimeUnit.DAY) }
    }
    return map
}

// ============================================================
// Reusable Cycle Calendar Grid — simple square cells
// ============================================================

private data class YearMonth(val year: Int, val month: Month)

private val TODAY_COLOR = Color(0xFF7C4DFF) // purple

/**
 * Reusable cycle-aware calendar grid with simple square cells.
 * No ovulation display, no complex shapes — just rounded-rect cells like HomeCalendar.
 */
@Composable
fun CycleCalendarGrid(
    state: CycleState,
    phaseInfo: CyclePhaseInfo?,
    modifier: Modifier = Modifier,
    selectedDate: LocalDate? = null,
    selectedStart: LocalDate? = null,
    selectedEnd: LocalDate? = null,
    onDateClick: ((LocalDate) -> Unit)? = null,
    isDateEnabled: (LocalDate) -> Boolean = { true },
    monthRange: IntRange = -3..2,
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val dateMap = buildDateMap(state, phaseInfo, today)

    val months = monthRange.map { offset ->
        val m = today.plus(offset, DateTimeUnit.MONTH)
        YearMonth(m.year, m.month)
    }
    val initialIndex = (-monthRange.first).coerceIn(0, months.size - 1)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    LaunchedEffect(Unit) { listState.scrollToItem(initialIndex) }

    Column(modifier) {
        // Weekday header
        Row(Modifier.fillMaxWidth()) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                Text(
                    day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }
        }
        SmallSpacer(4)

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxWidth().weight(1f),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(months, key = { "${it.year}-${it.month}" }) { ym ->
            CalendarMonth(
                yearMonth = ym,
                today = today,
                dateMap = dateMap,
                selectedDate = selectedDate,
                selectedStart = selectedStart,
                selectedEnd = selectedEnd,
                onDateClick = onDateClick,
                isDateEnabled = isDateEnabled,
            )
        }
    }
    } // Column
}

// ============================================================
// Legend row — simplified (no ovulation)
// ============================================================

@Composable
fun CycleCalendarLegend(modifier: Modifier = Modifier) {
    val periodColor = MaterialTheme.colorScheme.error
    val cellShape = MaterialTheme.shapes.extraSmall

    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        LegendItem(color = periodColor, text = stringResource(Res.string.legend_period))
        LegendItem(color = periodColor.copy(alpha = 0.4f), text = stringResource(Res.string.legend_predicted))
        LegendItem(color = TODAY_COLOR, text = stringResource(Res.string.legend_today))
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(10.dp).background(color, MaterialTheme.shapes.extraSmall))
        Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ============================================================
// Month grid — simple square cells
// ============================================================

@Composable
private fun CalendarMonth(
    yearMonth: YearMonth,
    today: LocalDate,
    dateMap: Map<LocalDate, DayType>,
    selectedDate: LocalDate?,
    selectedStart: LocalDate?,
    selectedEnd: LocalDate?,
    onDateClick: ((LocalDate) -> Unit)?,
    isDateEnabled: (LocalDate) -> Boolean,
) {
    val periodColor = MaterialTheme.colorScheme.error
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary
    val cellShape = MaterialTheme.shapes.extraSmall

    val firstDay = LocalDate(yearMonth.year, yearMonth.month, 1)
    val startOffset = firstDay.dayOfWeek.ordinal
    val daysInMonth = when (yearMonth.month) {
        Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY,
        Month.AUGUST, Month.OCTOBER, Month.DECEMBER -> 31
        Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
        Month.FEBRUARY -> if (yearMonth.year % 4 == 0 && (yearMonth.year % 100 != 0 || yearMonth.year % 400 == 0)) 29 else 28
    }
    val rows = (startOffset + daysInMonth + 6) / 7

    Column(Modifier.fillMaxWidth()) {
        Text(
            "${monthName(yearMonth.month)} ${yearMonth.year}",
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = onSurface.copy(alpha = 0.5f),
        )

        for (row in 0 until rows) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                for (col in 0 until 7) {
                    val dayNum = row * 7 + col - startOffset + 1
                    if (dayNum < 1 || dayNum > daysInMonth) {
                        Spacer(Modifier.weight(1f).height(32.dp))
                    } else {
                        val date = LocalDate(yearMonth.year, yearMonth.month, dayNum)
                        val type = dateMap[date] ?: DayType.NONE
                        val isToday = date == today
                        val isFuture = date > today
                        val enabled = isDateEnabled(date)
                        val isSelected = date == selectedDate ||
                                date == selectedStart || date == selectedEnd
                        val isInRange = selectedStart != null && selectedEnd != null &&
                                date > selectedStart && date < selectedEnd

                        val isPeriod = type == DayType.PERIOD
                        val isPredictedPeriod = type == DayType.PREDICTED_PERIOD

                        val bgColor = when {
                            isSelected -> primary
                            isInRange -> primary.copy(alpha = 0.12f)
                            isPeriod -> periodColor
                            isPredictedPeriod -> periodColor.copy(alpha = 0.35f)
                            isToday -> TODAY_COLOR
                            else -> Color.Transparent
                        }
                        val textColor = when {
                            isSelected -> Color.White
                            isPeriod -> Color.White
                            isPredictedPeriod -> Color.White
                            isToday -> Color.White
                            isInRange -> primary
                            !enabled -> onSurface.copy(alpha = 0.2f)
                            isFuture -> onSurface.copy(alpha = 0.3f)
                            else -> onSurface
                        }
                        Surface(
                            modifier = Modifier.weight(1f).height(32.dp)
                                .then(if (enabled && onDateClick != null) Modifier.clickable { onDateClick(date) } else Modifier),
                            shape = cellShape,
                            color = bgColor,
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    "$dayNum",
                                    fontSize = 12.sp,
                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
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
    Month.JANUARY -> "Jan"; Month.FEBRUARY -> "Feb"
    Month.MARCH -> "Mar"; Month.APRIL -> "Apr"
    Month.MAY -> "May"; Month.JUNE -> "Jun"
    Month.JULY -> "Jul"; Month.AUGUST -> "Aug"
    Month.SEPTEMBER -> "Sep"; Month.OCTOBER -> "Oct"
    Month.NOVEMBER -> "Nov"; Month.DECEMBER -> "Dec"
}
