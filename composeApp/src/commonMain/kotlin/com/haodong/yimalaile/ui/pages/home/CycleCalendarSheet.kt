package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.CyclePhaseInfo
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.ui.components.SmallSpacer
import kotlinx.datetime.*
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*
import kotlin.time.Clock

// ============================================================
// Day type for calendar coloring
// ============================================================

private enum class DayType {
    NONE,
    PERIOD,           // recorded past period
    ACTIVE_PERIOD,    // currently active period
    PREDICTED_PERIOD, // predicted future period
    OVULATION,        // estimated ovulation window
    PREDICTED_OVULATION, // predicted future ovulation
}

// ============================================================
// Build date → type mapping
// ============================================================

private fun buildDateMap(
    state: CycleState,
    phaseInfo: CyclePhaseInfo?,
    today: LocalDate,
): Map<LocalDate, DayType> {
    val map = mutableMapOf<LocalDate, DayType>()
    val avgCycle = phaseInfo?.cycleLength ?: 28
    val avgPeriod = phaseInfo?.periodLength ?: 5

    // Past recorded periods
    state.recentPeriods.forEach { record ->
        val end = record.endDate ?: return@forEach
        var d = record.startDate
        while (d <= end) { map[d] = DayType.PERIOD; d = d.plus(1, DateTimeUnit.DAY) }

        // Ovulation window for this cycle (approx day 14 of cycle = startDate + cycleLength*0.46)
        val ovStart = record.startDate.plus((avgCycle * 0.46).toInt(), DateTimeUnit.DAY)
        val ovEnd = record.startDate.plus((avgCycle * 0.57).toInt(), DateTimeUnit.DAY)
        var od = ovStart
        while (od <= ovEnd) {
            if (od !in map) map[od] = DayType.OVULATION
            od = od.plus(1, DateTimeUnit.DAY)
        }
    }

    // Active period
    state.activePeriod?.let { active ->
        var d = active.startDate
        while (d <= today) { map[d] = DayType.ACTIVE_PERIOD; d = d.plus(1, DateTimeUnit.DAY) }
    }

    // Predicted periods + predicted ovulation
    state.predictions.forEach { pred ->
        val pEnd = pred.predictedEnd ?: pred.predictedStart.plus(avgPeriod - 1, DateTimeUnit.DAY)
        var d = pred.predictedStart
        while (d <= pEnd) {
            if (d !in map) map[d] = DayType.PREDICTED_PERIOD
            d = d.plus(1, DateTimeUnit.DAY)
        }

        // Predicted ovulation for this cycle
        val ovStart = pred.predictedStart.plus((avgCycle * 0.46).toInt(), DateTimeUnit.DAY)
        val ovEnd = pred.predictedStart.plus((avgCycle * 0.57).toInt(), DateTimeUnit.DAY)
        var od = ovStart
        while (od <= ovEnd) {
            if (od !in map) map[od] = DayType.PREDICTED_OVULATION
            od = od.plus(1, DateTimeUnit.DAY)
        }
    }

    return map
}

// ============================================================
// Calendar Sheet
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CycleCalendarSheet(
    state: CycleState,
    phaseInfo: CyclePhaseInfo?,
    onDismiss: () -> Unit,
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val dateMap = buildDateMap(state, phaseInfo, today)

    val periodColor = MaterialTheme.colorScheme.error
    val ovulationColor = MaterialTheme.colorScheme.tertiary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val primary = MaterialTheme.colorScheme.primary
    val periodShape = phaseShape(com.haodong.yimalaile.domain.menstrual.CyclePhase.MENSTRUAL)
    val ovulationShape = phaseShape(com.haodong.yimalaile.domain.menstrual.CyclePhase.OVULATION)

    // 6-month range: 3 past + current + 2 future
    val months = (-3..2).map { offset ->
        val m = today.plus(offset, DateTimeUnit.MONTH)
        YearMonth(m.year, m.month)
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = 3) // center on current month
    LaunchedEffect(Unit) { listState.scrollToItem(3) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp)) {
            // Title
            Text(
                stringResource(Res.string.home_cycle_calendar),
                style = MaterialTheme.typography.titleLarge,
            )
            SmallSpacer(12)

            // Legend — using phase shapes
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                LegendItem(color = periodColor, shape = phaseShape(com.haodong.yimalaile.domain.menstrual.CyclePhase.MENSTRUAL), label = stringResource(Res.string.legend_period))
                LegendItem(color = ovulationColor, shape = phaseShape(com.haodong.yimalaile.domain.menstrual.CyclePhase.OVULATION), label = stringResource(Res.string.legend_ovulation))
                LegendItem(color = periodColor, shape = phaseShape(com.haodong.yimalaile.domain.menstrual.CyclePhase.MENSTRUAL), label = stringResource(Res.string.legend_predicted), dashed = true)
            }
            SmallSpacer(16)

            // Weekday header
            Row(Modifier.fillMaxWidth()) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                    Text(
                        day.take(1),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceVariant.copy(alpha = 0.5f),
                    )
                }
            }
            SmallSpacer(8)

            // Calendar months
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth().height(400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(months, key = { "${it.year}-${it.month}" }) { ym ->
                    CalendarMonth(
                        yearMonth = ym,
                        today = today,
                        dateMap = dateMap,
                        periodColor = periodColor,
                        ovulationColor = ovulationColor,
                        onSurface = onSurface,
                        primary = primary,
                        periodShape = periodShape,
                        ovulationShape = ovulationShape,
                    )
                }
            }
        }
    }
}

// ============================================================
// Legend item
// ============================================================

@Composable
private fun LegendItem(
    color: Color,
    shape: androidx.compose.ui.graphics.Shape,
    label: String,
    dashed: Boolean = false,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (dashed) {
            // Dashed outline circle
            Box(
                Modifier.size(12.dp).drawBehind {
                    drawCircle(
                        color = color,
                        style = Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 2.dp.toPx())),
                        ),
                    )
                }
            )
        } else {
            Box(Modifier.size(12.dp).clip(shape).background(color))
        }
        SmallSpacer(4)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ============================================================
// Month grid
// ============================================================

private data class YearMonth(val year: Int, val month: Month)

@Composable
private fun CalendarMonth(
    yearMonth: YearMonth,
    today: LocalDate,
    dateMap: Map<LocalDate, DayType>,
    periodColor: Color,
    ovulationColor: Color,
    onSurface: Color,
    primary: Color,
    periodShape: androidx.compose.ui.graphics.Shape,
    ovulationShape: androidx.compose.ui.graphics.Shape,
) {
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
        // Month label
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
                    if (dayNum !in 1..daysInMonth) {
                        Box(Modifier.weight(1f))
                    } else {
                        val date = LocalDate(yearMonth.year, yearMonth.month, dayNum)
                        val type = dateMap[date] ?: DayType.NONE
                        val isToday = date == today

                        // Pick shape: actual → phase shape, predicted → circle
                        val dayShape = when (type) {
                            DayType.PERIOD, DayType.ACTIVE_PERIOD -> periodShape
                            DayType.OVULATION -> ovulationShape
                            DayType.PREDICTED_PERIOD, DayType.PREDICTED_OVULATION -> CircleShape
                            DayType.NONE -> CircleShape
                        }
                        val bgColor = when (type) {
                            DayType.PERIOD -> periodColor
                            DayType.ACTIVE_PERIOD -> periodColor
                            DayType.OVULATION -> ovulationColor
                            DayType.PREDICTED_PERIOD, DayType.PREDICTED_OVULATION -> Color.Transparent
                            DayType.NONE -> Color.Transparent
                        }
                        val textColor = when (type) {
                            DayType.PERIOD, DayType.ACTIVE_PERIOD -> Color.White
                            DayType.OVULATION -> Color.White
                            DayType.PREDICTED_PERIOD -> periodColor
                            DayType.PREDICTED_OVULATION -> ovulationColor
                            DayType.NONE -> if (isToday) primary else onSurface
                        }
                        val dashedBorderColor = when (type) {
                            DayType.PREDICTED_PERIOD -> periodColor
                            DayType.PREDICTED_OVULATION -> ovulationColor
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
                                            Modifier.drawBehind {
                                                drawCircle(
                                                    color = dashedBorderColor,
                                                    style = Stroke(
                                                        width = 1.5.dp.toPx(),
                                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx())),
                                                    ),
                                                )
                                            }
                                        } else if (isToday && type == DayType.NONE) {
                                            Modifier.border(1.5.dp, primary, CircleShape)
                                        } else if (type == DayType.ACTIVE_PERIOD) {
                                            Modifier.border(2.dp, periodColor, periodShape)
                                        } else Modifier
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    "$dayNum",
                                    fontSize = 12.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
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
