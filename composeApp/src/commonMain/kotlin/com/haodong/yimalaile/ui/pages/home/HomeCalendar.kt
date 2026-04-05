package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.CyclePhaseInfo
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.ui.components.*
import com.haodong.yimalaile.ui.pages.sheet.LocalSheetManager
import io.github.adrcotfas.datetime.names.TextStyle
import io.github.adrcotfas.datetime.names.getDisplayName
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*
import kotlin.time.Clock


/**
 * Minimal home calendar — square grid with M3 Expressive theme colors.
 */
@Composable
internal fun HomeCalendar(
    state: CycleState,
    phaseInfo: CyclePhaseInfo?,
    modifier: Modifier = Modifier,
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val dateMap = buildDateMap(state, phaseInfo, today)
    val inPeriod = state.inPeriod
    val periodStart = state.currentPeriod?.startDate
        ?: if (state.inPredictedPeriod) state.predictions.firstOrNull { pred ->
            val avgP = phaseInfo?.periodLength ?: 5
            val pEnd = pred.predictedEnd ?: pred.predictedStart.plus(avgP - 1, DateTimeUnit.DAY)
            today in pred.predictedStart..pEnd
        }?.predictedStart else null
    val dayCount = periodStart?.let {
        it.until(today, DateTimeUnit.DAY).toInt() + 1
    }

    val periodColor = MaterialTheme.colorScheme.error
    val periodLight = MaterialTheme.colorScheme.error.copy(alpha = 0.45f)

    // Build set of next predicted period dates
    val nextPredictedDates = if (state.predictions.isNotEmpty()) {
        val pred = state.predictions.first()
        val avgPeriod = phaseInfo?.periodLength ?: 5
        val pEnd = pred.predictedEnd ?: pred.predictedStart.plus(avgPeriod - 1, DateTimeUnit.DAY)
        buildSet {
            var d = pred.predictedStart
            while (d <= pEnd) { add(d); d = d.plus(1, DateTimeUnit.DAY) }
        }
    } else emptySet()

    val months = (0..1).map { offset ->
        val m = today.plus(offset, DateTimeUnit.MONTH)
        YearMonth(m.year, m.month)
    }

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        // Status header with phase shape
        if (phaseInfo != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                DecorShape(
                    size = 16,
                    shape = phaseShape(phaseInfo.phase),
                    color = phaseColor(phaseInfo.phase),
                )
                SmallSpacer(8)
                Text(
                    phaseDisplayName(phaseInfo.phase),
                    style = MaterialTheme.typography.bodyMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            SmallSpacer(4)
            Text(
                when {
                    inPeriod -> stringResource(Res.string.home_day_n, dayCount ?: 0)
                    phaseInfo.daysUntilNextPeriod <= 0 -> stringResource(Res.string.legend_today)
                    else -> "${phaseInfo.daysUntilNextPeriod} ${stringResource(Res.string.unit_days)}"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
            )
            Text(
                when {
                    inPeriod -> stringResource(Res.string.home_in_period)
                    phaseInfo.daysUntilNextPeriod <= 0 -> stringResource(Res.string.home_hero_due_today)
                    else -> stringResource(Res.string.home_next_period_starts)
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SmallSpacer(16)
        }
        GrowSpacer()
        Column(Modifier.fillMaxWidth(0.6f), ) {
            // This month
            MonthBlock(months[0], today, dateMap, periodColor, periodLight, nextPredictedDates, state.records)

            // Info between months — plain text
            SmallSpacer(32)
            val lastPeriod = state.records.maxByOrNull { it.startDate }
            if (lastPeriod != null) {
                Text(
                    stringResource(Res.string.home_last_period),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    formatDateWithWeekday(lastPeriod.startDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            SmallSpacer(12)
            if (phaseInfo?.nextPeriodStart != null) {
                Text(
                    stringResource(Res.string.home_next_period_starts),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    formatDateWithWeekday(phaseInfo.nextPeriodStart),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            SmallSpacer(32)

            // Next month
            MonthBlock(months[1], today, dateMap, periodColor, periodLight, nextPredictedDates, state.records)

        }
        GrowSpacer()
        SmallSpacer(128)

    }
}

private fun formatYearMonth(year: Int, month: Month): String {
    val monthName = month.getDisplayName(TextStyle.FULL)
    return "$monthName $year"
}

private fun formatDateWithWeekday(date: LocalDate): String {
    val formatted = date.format(LocalDate.Format {
        monthNumber()
        chars("/")
        dayOfMonth()
    })
    val weekday = date.dayOfWeek.getDisplayName(TextStyle.FULL)
    return "$formatted $weekday"
}

private data class YearMonth(val year: Int, val month: Month)

@Composable
private fun MonthBlock(
    yearMonth: YearMonth,
    today: LocalDate,
    dateMap: Map<LocalDate, DayType>,
    periodColor: Color,
    periodLight: Color,
    nextPredictedDates: Set<LocalDate>,
    records: List<MenstrualRecord> = emptyList(),
) {
    val sheetManager = LocalSheetManager.current
    val scope = rememberCoroutineScope()
    val firstDay = LocalDate(yearMonth.year, yearMonth.month, 1)
    val startOffset = firstDay.dayOfWeek.ordinal
    val daysInMonth = when (yearMonth.month) {
        Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY,
        Month.AUGUST, Month.OCTOBER, Month.DECEMBER -> 31
        Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
        Month.FEBRUARY -> if (yearMonth.year % 4 == 0 && (yearMonth.year % 100 != 0 || yearMonth.year % 400 == 0)) 29 else 28
    }
    val rows = (startOffset + daysInMonth + 6) / 7
    val cellShape = MaterialTheme.shapes.extraSmall  // 8dp rounded from theme
    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End,
    ) {
        // Month label
        Text(
            formatYearMonth(yearMonth.year, yearMonth.month),
            style = MaterialTheme.typography.bodyMedium,
            color = onSurface.copy(alpha = 0.7f),
        )
        SmallSpacer(6)

        // Grid
        for (row in 0 until rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.padding(vertical = 1.5.dp),
            ) {
                for (col in 0 until 7) {
                    val dayNum = row * 7 + col - startOffset + 1
                    if (dayNum !in 1..daysInMonth) {
                        Spacer(Modifier.size(30.dp))
                    } else {
                        val date = LocalDate(yearMonth.year, yearMonth.month, dayNum)
                        val type = dateMap[date] ?: DayType.NONE
                        val isToday = date == today
                        val isFuture = date > today

                        val isPeriod = type == DayType.PERIOD
                        val isPredictedPeriod = type == DayType.PREDICTED_PERIOD
                        val isNextPredicted = date in nextPredictedDates
                        val bgColor = when {
                            isPeriod -> periodColor
                            isPredictedPeriod || isNextPredicted -> periodLight
                            isToday -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        val textColor = when {
                            isPeriod -> Color.White
                            isPredictedPeriod || isNextPredicted -> Color.White
                            isToday -> Color.White
                            isFuture -> onSurface.copy(alpha = 0.15f)
                            else -> onSurface.copy(alpha = 0.45f)
                        }
                        val showNumber = isToday || isPeriod || isPredictedPeriod || isNextPredicted
                        val size = if (isToday) 34.dp else 30.dp

                        // Find record covering this date for click handling
                        val clickable = isPeriod && records.isNotEmpty()
                        Surface(
                            modifier = Modifier.size(size).then(
                                if (clickable) Modifier.clickable {
                                    val record = records.find { r ->
                                        val rEnd = r.endDate ?: today
                                        date in r.startDate..rEnd
                                    }
                                    if (record != null) {
                                        scope.launch { sheetManager.showAndHandleRecordDetail(record) }
                                    }
                                } else Modifier
                            ),
                            shape = cellShape,
                            color = bgColor
                        ) {
                            Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
                                if (showNumber) {
                                    Text(
                                        "$dayNum",
                                        fontSize = if (isToday) 13.sp else 11.sp,
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
}
