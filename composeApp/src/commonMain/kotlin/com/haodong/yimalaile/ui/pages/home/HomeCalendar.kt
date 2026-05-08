package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.haodong.yimalaile.domain.menstrual.CyclePhaseInfo
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.ui.components.DayType
import com.haodong.yimalaile.ui.components.DecorShape
import com.haodong.yimalaile.ui.components.GrowSpacer
import com.haodong.yimalaile.ui.components.SmallSpacer
import com.haodong.yimalaile.ui.components.buildDateMap
import com.haodong.yimalaile.ui.pages.sheet.LocalSheetViewModel
import com.haodong.yimalaile.ui.pages.sheet.sheets.PhaseExplanationSheet
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
    val defaultCycleLength = phaseInfo?.cycleLength ?: 28
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val dateMap = buildDateMap(state, phaseInfo, today)
    val inPeriod = state.inPeriod
    val dayCount = phaseInfo?.dayInCycle

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

    val currentMonth = YearMonth(today.year, today.month)

    var showPhaseSheet by remember { mutableStateOf(false) }

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (phaseInfo != null) {
            HeroCountdown(
                phaseInfo = phaseInfo,
                inPeriod = inPeriod,
                dayCount = dayCount,
                onClick = { showPhaseSheet = true },
            )
        }
        GrowSpacer()
        Column(Modifier.fillMaxWidth(0.65f)) {
            MonthBlock(currentMonth, today, dateMap, periodColor, periodLight, nextPredictedDates, state.records, defaultCycleLength)

            SmallSpacer(28)
            val lastPeriod = state.records.maxByOrNull { it.startDate }
            val nextStart = phaseInfo?.nextPeriodStart
            if (lastPeriod != null || nextStart != null) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    InfoColumn(
                        label = stringResource(Res.string.home_last_period),
                        value = lastPeriod?.let { formatDateWithWeekday(it.startDate) },
                        modifier = Modifier.weight(1f),
                    )
                    VerticalDivider(
                        modifier = Modifier.height(36.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    InfoColumn(
                        label = stringResource(Res.string.home_next_period_starts),
                        value = nextStart?.let { formatDateWithWeekday(it) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        GrowSpacer()
        SmallSpacer(128)
    }

    // Phase explanation sheet
    if (showPhaseSheet && phaseInfo != null) {
        PhaseExplanationSheet(
            phaseInfo = phaseInfo,
            onDismiss = { showPhaseSheet = false },
        )
    }
}

@Composable
private fun HeroCountdown(
    phaseInfo: CyclePhaseInfo,
    inPeriod: Boolean,
    dayCount: Int?,
    onClick: () -> Unit,
) {
    val phase = phaseInfo.phase
    val phaseColor = phase.color()
    val dueToday = phaseInfo.daysUntilNextPeriod <= 0 && !inPeriod

    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                DecorShape(size = 10, shape = phase.shape(), color = phaseColor)
                Text(
                    phaseInfo.dayLabel(),
                    style = MaterialTheme.typography.labelMedium,
                    color = phaseColor,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            SmallSpacer(6)
            when {
                inPeriod -> {
                    Text(
                        stringResource(Res.string.home_day_n, dayCount ?: 0),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                    )
                }
                dueToday -> {
                    Text(
                        stringResource(Res.string.legend_today),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                    )
                }
                else -> {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            "${phaseInfo.daysUntilNextPeriod}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            stringResource(Res.string.unit_days),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                }
            }
            SmallSpacer(2)
            Text(
                when {
                    inPeriod -> stringResource(Res.string.home_in_period)
                    dueToday -> stringResource(Res.string.home_hero_due_today)
                    else -> stringResource(Res.string.home_next_period_starts)
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    SmallSpacer(16)
}

@Composable
private fun InfoColumn(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SmallSpacer(2)
        Text(
            value ?: "—",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
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
        day()
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
    defaultCycleLength: Int = 28,
) {
    val sheetViewModel = LocalSheetViewModel.current
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

        // Grid — each cell uses weight(1f) to fill width evenly
        for (row in 0 until rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 1.5.dp),
            ) {
                for (col in 0 until 7) {
                    val dayNum = row * 7 + col - startOffset + 1
                    if (dayNum !in 1..daysInMonth) {
                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val date = LocalDate(yearMonth.year, yearMonth.month, dayNum)
                        val type = dateMap[date] ?: DayType.NONE
                        val isToday = date == today
                        val isFuture = date > today

                        val isPeriod = type == DayType.PERIOD
                        val isPredictedPeriod = type == DayType.PREDICTED_PERIOD
                        val isNextPredicted = date in nextPredictedDates
                        val bgColor = when {
                            isToday -> MaterialTheme.colorScheme.primary
                            isPeriod -> periodColor
                            isPredictedPeriod || isNextPredicted -> periodLight
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        val textColor = when {
                            isToday -> Color.White
                            isPeriod -> Color.White
                            isPredictedPeriod || isNextPredicted -> Color.White
                            isFuture -> onSurface.copy(alpha = 0.15f)
                            else -> onSurface.copy(alpha = 0.45f)
                        }
                        val showNumber = isToday || isPeriod || isPredictedPeriod || isNextPredicted

                        val clickable = isPeriod && records.isNotEmpty()
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .then(
                                    if (isToday) Modifier.zIndex(1f).graphicsLayer {
                                        scaleX = 1.15f
                                        scaleY = 1.15f
                                    } else Modifier
                                )
                                .then(
                                    if (clickable) Modifier.clip(cellShape).clickable {
                                        val record = records.find { r ->
                                            val rEnd = r.endDate ?: today
                                            date in r.startDate..rEnd
                                        }
                                        if (record != null) {
                                            scope.launch { sheetViewModel.showAndHandleRecordDetail(record, defaultCycleLength) }
                                        }
                                    } else Modifier
                                ),
                            shape = cellShape,
                            color = bgColor,
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                if (showNumber) {
                                    val todayOverlap = isToday && (isPeriod || isPredictedPeriod || isNextPredicted)
                                    Text(
                                        "$dayNum",
                                        fontSize = when {
                                            todayOverlap -> 13.sp
                                            isToday -> 12.sp
                                            else -> 11.sp
                                        },
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
