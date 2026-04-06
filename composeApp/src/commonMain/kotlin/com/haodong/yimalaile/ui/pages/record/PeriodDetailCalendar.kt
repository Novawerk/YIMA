package com.haodong.yimalaile.ui.pages.record

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.ui.components.DecorShape
import com.haodong.yimalaile.ui.theme.expressiveShapes
import kotlinx.datetime.*
import kotlin.time.Clock

@Composable
fun PeriodDetailCalendar(
    record: MenstrualRecord,
    cycleEndDate: LocalDate,
    ovulationDates: Set<LocalDate> = emptySet(),
    ovulationPeakDate: LocalDate? = null,
    modifier: Modifier = Modifier
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val startDate = record.startDate
    val endDate = record.endDate ?: today

    // Calculate dates to show: from 4 days before startDate to 4 days after cycleEndDate.
    // Ensure we start on a Monday and end on a Sunday to maintain a grid-like week structure.
    val datesToShow = remember(startDate, cycleEndDate) {
        val startWithPadding = startDate.minus(4, DateTimeUnit.DAY)
        val endWithPadding = cycleEndDate.plus(4, DateTimeUnit.DAY)

        // Find the Monday of the starting week
        var firstDay = startWithPadding
        while (firstDay.dayOfWeek != DayOfWeek.MONDAY) {
            firstDay = firstDay.minus(1, DateTimeUnit.DAY)
        }

        // Find the Sunday of the ending week
        var lastDay = endWithPadding
        while (lastDay.dayOfWeek != DayOfWeek.SUNDAY) {
            lastDay = lastDay.plus(1, DateTimeUnit.DAY)
        }

        val list = mutableListOf<LocalDate>()
        var current = firstDay
        while (current <= lastDay) {
            list.add(current)
            current = current.plus(1, DateTimeUnit.DAY)
        }
        list
    }

    val periodDates = remember(startDate, endDate) {
        val set = mutableSetOf<LocalDate>()
        var d = startDate
        while (d <= endDate) {
            set.add(d)
            d = d.plus(1, DateTimeUnit.DAY)
        }
        set
    }

    val cycleDates = remember(startDate, cycleEndDate) {
        val set = mutableSetOf<LocalDate>()
        var d = startDate
        while (d <= cycleEndDate) {
            set.add(d)
            d = d.plus(1, DateTimeUnit.DAY)
        }
        set
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Month name as a small, subtle label if it spans across months
        val startMonth = startDate.month
        val endMonth = cycleEndDate.month
        val monthLabel = if (startMonth == endMonth) {
            "${startDate.year}年 ${startDate.month.number}月"
        } else {
            "${startDate.year}年 ${startDate.month.number}月 - ${cycleEndDate.year}年 ${cycleEndDate.month.number}月"
        }

        Text(
            monthLabel,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )

        // Date Grid
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            datesToShow.chunked(7).forEach { weekDates ->
                Row(Modifier.fillMaxWidth()) {
                    weekDates.forEach { date ->
                        DayCell(
                            date = date,
                            dayNum = date.day,
                            showMonthLabel = date.day == 1,
                            isToday = date == today,
                            isHighlighted = date in cycleDates,
                            isPeriod = date in periodDates,
                            isOvulation = date in ovulationDates,
                            isPeriodStart = date == startDate,
                            isPeriodEnd = date == record.endDate,
                            isOvulationPeak = date == ovulationPeakDate,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    dayNum: Int,
    showMonthLabel: Boolean,
    isToday: Boolean,
    isHighlighted: Boolean,
    isPeriod: Boolean,
    isOvulation: Boolean,
    isPeriodStart: Boolean,
    isPeriodEnd: Boolean,
    isOvulationPeak: Boolean,
    modifier: Modifier = Modifier
) {
    val cellHeight = 48.dp
    val underlineH = 4.dp
    val onSurface = MaterialTheme.colorScheme.onSurface
    val periodColor = MaterialTheme.colorScheme.error
    val ovulationColor = Color(0xFF7C4DFF)

    Box(
        modifier = modifier.height(cellHeight),
        contentAlignment = Alignment.TopCenter
    ) {
        // Today background
        if (isToday) {
            Box(
                Modifier.padding(top = 4.dp).size(32.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), MaterialTheme.shapes.small)
            )
        }

        // Month label or Day number
        Row(
            modifier = Modifier.padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (showMonthLabel) {
                Surface(
                    color = onSurface.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(
                        "${date.month.number}月",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                        color = onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                "$dayNum",
                fontSize = 14.sp,
                fontWeight = if (isToday || isHighlighted) FontWeight.Bold else FontWeight.Normal,
                color = if (isHighlighted) onSurface else onSurface.copy(alpha = 0.2f),
                maxLines = 1
            )
        }

        // Underline for marking
        val underlineColor = when {
            isPeriod -> periodColor
            isOvulation -> ovulationColor
            else -> null
        }

        if (underlineColor != null) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 6.dp, end = 6.dp, bottom = 4.dp)
                    .fillMaxWidth()
                    .height(underlineH)
                    .clip(RoundedCornerShape(50))
                    .background(underlineColor)
            )
        }

        // Period start icon
        if (isPeriodStart) {
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(12.dp).align(Alignment.TopCenter).offset(x = (-10).dp, y = 26.dp),
                tint = periodColor.copy(alpha = 0.7f),
            )
        }
        // Period end icon
        if (isPeriodEnd) {
            Icon(
                Icons.Filled.Pause,
                contentDescription = null,
                modifier = Modifier.size(12.dp).align(Alignment.TopCenter).offset(x = 10.dp, y = 26.dp),
                tint = periodColor.copy(alpha = 0.7f),
            )
        }
        // Ovulation peak flower
        if (isOvulationPeak) {
            DecorShape(
                size = 10,
                shape = MaterialTheme.expressiveShapes.flower,
                color = ovulationColor,
                modifier = Modifier.align(Alignment.TopCenter).offset(x = 10.dp, y = 26.dp),
            )
        }
    }
}

