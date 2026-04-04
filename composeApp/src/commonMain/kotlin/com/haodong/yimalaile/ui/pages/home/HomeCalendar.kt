package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.CyclePhaseInfo
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.ui.components.DecorShape
import com.haodong.yimalaile.ui.components.DayType
import com.haodong.yimalaile.ui.components.SmallSpacer
import com.haodong.yimalaile.ui.components.buildDateMap
import kotlinx.datetime.*
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*
import kotlin.time.Clock

private val TODAY_COLOR = Color(0xFF4CAF50)

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
    val inPeriod = state.activePeriod != null
    val dayCount = if (inPeriod) {
        state.activePeriod!!.startDate.until(today, DateTimeUnit.DAY).toInt() + 1
    } else null

    val periodColor = MaterialTheme.colorScheme.error
    val periodLight = MaterialTheme.colorScheme.errorContainer
    val ovulationColor = MaterialTheme.colorScheme.tertiary
    val ovulationLight = MaterialTheme.colorScheme.tertiaryContainer

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
                if (inPeriod) stringResource(Res.string.home_day_n, dayCount ?: 0)
                else "${phaseInfo.daysUntilNextPeriod} ${stringResource(Res.string.unit_days)}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
            )
            SmallSpacer(16)
        }

        // Month grids
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(months, key = { "${it.year}-${it.month}" }) { ym ->
                MonthBlock(
                    ym, today, dateMap,
                    periodColor, periodLight,
                    ovulationColor, ovulationLight,
                )
            }

            // Info cards
            item {
                SmallSpacer(8)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val lastPeriod = state.recentPeriods.maxByOrNull { it.startDate }
                    if (lastPeriod != null) {
                        InfoChip(
                            label = stringResource(Res.string.home_past_records),
                            value = "${lastPeriod.startDate.monthNumber}/${lastPeriod.startDate.dayOfMonth}",
                            color = periodColor,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (phaseInfo?.nextPeriodStart != null) {
                        InfoChip(
                            label = stringResource(Res.string.home_next_period_starts),
                            value = "${phaseInfo.nextPeriodStart.monthNumber}/${phaseInfo.nextPeriodStart.dayOfMonth}",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier,
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            SmallSpacer(2)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

private data class YearMonth(val year: Int, val month: Month)

@Composable
private fun MonthBlock(
    yearMonth: YearMonth,
    today: LocalDate,
    dateMap: Map<LocalDate, DayType>,
    periodColor: Color,
    periodLight: Color,
    ovulationColor: Color,
    ovulationLight: Color,
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
    val cellShape = MaterialTheme.shapes.extraSmall  // 8dp rounded from theme
    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Month label
        Text(
            "${yearMonth.year}年${yearMonth.month.number}月",
            style = MaterialTheme.typography.labelSmall,
            color = onSurface.copy(alpha = 0.4f),
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
                    if (dayNum < 1 || dayNum > daysInMonth) {
                        Spacer(Modifier.size(30.dp))
                    } else {
                        val date = LocalDate(yearMonth.year, yearMonth.month, dayNum)
                        val type = dateMap[date] ?: DayType.NONE
                        val isToday = date == today
                        val isFuture = date > today

                        val bgColor = when {
                            isToday && type != DayType.NONE -> TODAY_COLOR
                            type == DayType.PERIOD || type == DayType.ACTIVE_PERIOD -> periodColor
                            type == DayType.OVULATION -> ovulationColor
                            type == DayType.PREDICTED_PERIOD -> periodLight
                            type == DayType.PREDICTED_OVULATION -> ovulationLight
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        }
                        val textColor = when {
                            isToday && type != DayType.NONE -> Color.White
                            type == DayType.PERIOD || type == DayType.ACTIVE_PERIOD -> Color.White
                            type == DayType.OVULATION -> Color.White
                            isToday -> TODAY_COLOR
                            type == DayType.PREDICTED_PERIOD -> periodColor
                            type == DayType.PREDICTED_OVULATION -> ovulationColor
                            isFuture -> onSurface.copy(alpha = 0.15f)
                            else -> onSurface.copy(alpha = 0.5f)
                        }
                        val showNumber = isToday ||
                                type != DayType.NONE

                        Box(
                            Modifier.size(30.dp).clip(cellShape)
                                .background(bgColor)
                                .then(
                                    if (isToday && type == DayType.NONE)
                                        Modifier.border(2.dp, TODAY_COLOR, cellShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (showNumber) {
                                Text(
                                    "$dayNum",
                                    fontSize = 11.sp,
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
