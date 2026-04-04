package com.haodong.yimalaile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.CyclePhase
import com.haodong.yimalaile.domain.menstrual.CyclePhaseInfo
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.ui.pages.home.phaseShape
import com.haodong.yimalaile.ui.theme.expressiveShapes
import kotlinx.datetime.*
import kotlin.time.Clock

// ============================================================
// Day type for calendar coloring
// ============================================================

enum class DayType {
    NONE, PERIOD, ACTIVE_PERIOD, PREDICTED_PERIOD, OVULATION, PREDICTED_OVULATION,
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

    state.recentPeriods.forEach { record ->
        val end = record.endDate ?: return@forEach
        var d = record.startDate
        while (d <= end) { map[d] = DayType.PERIOD; d = d.plus(1, DateTimeUnit.DAY) }
        val ovStart = record.startDate.plus((avgCycle * 0.46).toInt(), DateTimeUnit.DAY)
        val ovEnd = record.startDate.plus((avgCycle * 0.57).toInt(), DateTimeUnit.DAY)
        var od = ovStart
        while (od <= ovEnd) { if (od !in map) map[od] = DayType.OVULATION; od = od.plus(1, DateTimeUnit.DAY) }
    }
    state.activePeriod?.let { active ->
        var d = active.startDate
        while (d <= today) { map[d] = DayType.ACTIVE_PERIOD; d = d.plus(1, DateTimeUnit.DAY) }
    }
    state.predictions.forEach { pred ->
        val pEnd = pred.predictedEnd ?: pred.predictedStart.plus(avgPeriod - 1, DateTimeUnit.DAY)
        var d = pred.predictedStart
        while (d <= pEnd) { if (d !in map) map[d] = DayType.PREDICTED_PERIOD; d = d.plus(1, DateTimeUnit.DAY) }
        val ovStart = pred.predictedStart.plus((avgCycle * 0.46).toInt(), DateTimeUnit.DAY)
        val ovEnd = pred.predictedStart.plus((avgCycle * 0.57).toInt(), DateTimeUnit.DAY)
        var od = ovStart
        while (od <= ovEnd) { if (od !in map) map[od] = DayType.PREDICTED_OVULATION; od = od.plus(1, DateTimeUnit.DAY) }
    }
    return map
}

// ============================================================
// Reusable Cycle Calendar Grid
// ============================================================

private data class YearMonth(val year: Int, val month: Month)

private val TODAY_COLOR = Color(0xFF4CAF50)

/**
 * Reusable cycle-aware calendar grid.
 * Shows cycle context (periods, ovulation, predictions) and optionally supports date picking.
 *
 * @param state Cycle data for coloring
 * @param phaseInfo Phase info for calculating ovulation windows
 * @param selectedDate Currently selected date (for picker mode)
 * @param selectedStart Start of selected range (for range picker mode)
 * @param selectedEnd End of selected range (for range picker mode)
 * @param onDateClick Called when a date is tapped (null = read-only)
 * @param isDateEnabled Controls which dates are tappable (default: all non-future)
 * @param monthRange How many months to show (-3..2 = 3 past + current + 2 future)
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
        SmallSpacer(8)

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
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
    }
}

// ============================================================
// Legend row
// ============================================================

@Composable
fun CycleCalendarLegend(modifier: Modifier = Modifier) {
    val periodColor = MaterialTheme.colorScheme.error
    val ovulationColor = MaterialTheme.colorScheme.tertiary
    val todayShape = MaterialTheme.expressiveShapes.diamond

    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        LegendDot(color = periodColor, shape = phaseShape(CyclePhase.MENSTRUAL), label = "●")
        LegendDot(color = ovulationColor, shape = phaseShape(CyclePhase.OVULATION), label = "●")
        LegendDot(color = periodColor, dashed = true, label = "◌")
        LegendDot(color = TODAY_COLOR, shape = todayShape, label = "◆")
    }
}

@Composable
private fun LegendDot(
    color: Color,
    shape: Shape = CircleShape,
    dashed: Boolean = false,
    label: String,
) {
    if (dashed) {
        Box(
            Modifier.size(10.dp).drawBehind {
                drawCircle(color = color, style = Stroke(width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 2.dp.toPx()))))
            }
        )
    } else {
        Box(Modifier.size(10.dp).clip(shape).background(color))
    }
}

// ============================================================
// Month grid — resolves all colors from MaterialTheme internally
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
    // Resolve all colors from theme — no params needed
    val periodColor = MaterialTheme.colorScheme.error
    val ovulationColor = MaterialTheme.colorScheme.tertiary
    val todayColor = TODAY_COLOR
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary
    val periodShape = phaseShape(CyclePhase.MENSTRUAL)
    val ovulationShape = phaseShape(CyclePhase.OVULATION)
    val todayShape = MaterialTheme.expressiveShapes.diamond
    val selectedColor = primary

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
            modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = onSurface,
        )

        for (row in 0 until rows) {
            Row(Modifier.fillMaxWidth().height(36.dp)) {
                for (col in 0 until 7) {
                    val dayNum = row * 7 + col - startOffset + 1
                    if (dayNum < 1 || dayNum > daysInMonth) {
                        Box(Modifier.weight(1f))
                    } else {
                        val date = LocalDate(yearMonth.year, yearMonth.month, dayNum)
                        val type = dateMap[date] ?: DayType.NONE
                        val isToday = date == today
                        val isFuture = date > today
                        val enabled = !isFuture && isDateEnabled(date)
                        val isSelected = date == selectedDate ||
                                date == selectedStart || date == selectedEnd
                        val isInRange = selectedStart != null && selectedEnd != null &&
                                date > selectedStart && date < selectedEnd

                        val dayShape = when {
                            isSelected -> CircleShape
                            type == DayType.PERIOD || type == DayType.ACTIVE_PERIOD -> periodShape
                            type == DayType.OVULATION -> ovulationShape
                            isToday -> todayShape
                            else -> CircleShape
                        }
                        val bgColor = when {
                            isSelected -> selectedColor
                            type == DayType.PERIOD || type == DayType.ACTIVE_PERIOD -> periodColor
                            type == DayType.OVULATION -> ovulationColor
                            isInRange -> primary.copy(alpha = 0.15f)
                            else -> Color.Transparent
                        }
                        val textColor = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            type == DayType.PERIOD || type == DayType.ACTIVE_PERIOD -> Color.White
                            type == DayType.OVULATION -> Color.White
                            type == DayType.PREDICTED_PERIOD -> periodColor.copy(alpha = if (isFuture) 0.5f else 1f)
                            type == DayType.PREDICTED_OVULATION -> ovulationColor.copy(alpha = 0.5f)
                            isFuture -> onSurface.copy(alpha = 0.2f)
                            isToday -> todayColor
                            else -> onSurface
                        }
                        val isPredicted = type == DayType.PREDICTED_PERIOD || type == DayType.PREDICTED_OVULATION
                        val dashedBorderColor = when {
                            isSelected -> null
                            type == DayType.PREDICTED_PERIOD -> periodColor
                            type == DayType.PREDICTED_OVULATION -> ovulationColor
                            else -> null
                        }

                        Box(
                            Modifier.weight(1f).fillMaxHeight(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                Modifier.size(30.dp).clip(dayShape)
                                    .background(bgColor)
                                    .then(
                                        if (dashedBorderColor != null) {
                                            val dc = dashedBorderColor
                                            Modifier.drawBehind {
                                                drawCircle(color = dc, style = Stroke(width = 1.5.dp.toPx(),
                                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx()))))
                                            }
                                        } else if (isToday && !isSelected) {
                                            Modifier.border(2.5.dp, todayColor, todayShape)
                                        } else Modifier
                                    )
                                    .then(if (enabled && onDateClick != null) Modifier.clickable { onDateClick(date) } else Modifier),
                                contentAlignment = Alignment.Center,
                            ) {
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
