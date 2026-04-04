package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.haodong.yimalaile.domain.menstrual.CyclePhaseInfo
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.ui.components.DayType
import com.haodong.yimalaile.ui.components.SmallSpacer
import com.haodong.yimalaile.ui.components.buildDateMap
import kotlinx.datetime.*
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*
import kotlin.time.Clock

private val PERIOD_COLOR = Color(0xFFD32F2F)
private val PREDICTED_COLOR = Color(0xFFE8A0A0)
private val TODAY_COLOR = Color(0xFF4CAF50)

/**
 * Minimal home calendar — matches reference design.
 * Square cells, no weekday headers, tight grid, month-by-month.
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

    val months = (0..1).map { offset ->
        val m = today.plus(offset, DateTimeUnit.MONTH)
        YearMonth(m.year, m.month)
    }

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        // Status header
        if (phaseInfo != null) {
            Text(
                phaseDisplayName(phaseInfo.phase),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                if (inPeriod) stringResource(Res.string.home_day_n, dayCount ?: 0)
                else "${phaseInfo.daysUntilNextPeriod} ${stringResource(Res.string.unit_days)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            SmallSpacer(12)
        }

        // Month grids
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(months, key = { "${it.year}-${it.month}" }) { ym ->
                MonthBlock(ym, today, dateMap)
            }

            // Info text
            item {
                SmallSpacer(8)
                // Last period
                val lastPeriod = state.recentPeriods.maxByOrNull { it.startDate }
                if (lastPeriod != null) {
                    InfoLine(
                        label = stringResource(Res.string.home_past_records),
                        value = "${lastPeriod.startDate.monthNumber}/${lastPeriod.startDate.dayOfMonth}",
                    )
                }
                // Next predicted
                if (phaseInfo?.nextPeriodStart != null) {
                    InfoLine(
                        label = stringResource(Res.string.home_next_period_starts),
                        value = "${phaseInfo.nextPeriodStart.monthNumber}/${phaseInfo.nextPeriodStart.dayOfMonth}",
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
    ) {
        Text(
            "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SmallSpacer(4)
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private data class YearMonth(val year: Int, val month: Month)

@Composable
private fun MonthBlock(
    yearMonth: YearMonth,
    today: LocalDate,
    dateMap: Map<LocalDate, DayType>,
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
    val cellSize = 28.dp
    val gap = 2.dp
    val cornerShape = RoundedCornerShape(4.dp)

    val onSurface = MaterialTheme.colorScheme.onSurface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Month label
        Text(
            "${yearMonth.year}年${yearMonth.month.number}月",
            style = MaterialTheme.typography.labelSmall,
            color = onSurface.copy(alpha = 0.5f),
        )
        SmallSpacer(4)

        // Grid
        for (row in 0 until rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(gap),
                modifier = Modifier.padding(vertical = gap / 2),
            ) {
                for (col in 0 until 7) {
                    val dayNum = row * 7 + col - startOffset + 1
                    if (dayNum < 1 || dayNum > daysInMonth) {
                        // Empty cell
                        Box(Modifier.size(cellSize).clip(cornerShape).background(Color.Transparent))
                    } else {
                        val date = LocalDate(yearMonth.year, yearMonth.month, dayNum)
                        val type = dateMap[date] ?: DayType.NONE
                        val isToday = date == today

                        val bgColor = when {
                            isToday && type != DayType.NONE -> TODAY_COLOR
                            type == DayType.PERIOD || type == DayType.ACTIVE_PERIOD -> PERIOD_COLOR
                            type == DayType.OVULATION -> MaterialTheme.colorScheme.tertiary
                            type == DayType.PREDICTED_PERIOD -> PREDICTED_COLOR
                            type == DayType.PREDICTED_OVULATION -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                            else -> surfaceVariant.copy(alpha = 0.4f)
                        }
                        val textColor = when {
                            isToday && type != DayType.NONE -> Color.White
                            type == DayType.PERIOD || type == DayType.ACTIVE_PERIOD -> Color.White
                            type == DayType.OVULATION -> Color.White
                            isToday -> TODAY_GREEN
                            else -> onSurface.copy(alpha = if (date > today) 0.2f else 0.6f)
                        }
                        val showNumber = isToday ||
                                type == DayType.PERIOD || type == DayType.ACTIVE_PERIOD ||
                                type == DayType.PREDICTED_PERIOD ||
                                type == DayType.OVULATION || type == DayType.PREDICTED_OVULATION

                        Box(
                            Modifier.size(cellSize).clip(cornerShape)
                                .background(bgColor)
                                .then(
                                    if (isToday && type == DayType.NONE)
                                        Modifier.border(1.5.dp, TODAY_GREEN, cornerShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (showNumber) {
                                Text(
                                    "$dayNum",
                                    fontSize = 10.sp,
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

private val TODAY_GREEN = Color(0xFF4CAF50)
