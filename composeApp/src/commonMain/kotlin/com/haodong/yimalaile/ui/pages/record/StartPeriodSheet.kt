package com.haodong.yimalaile.ui.pages.record

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.components.SmallSpacer
import kotlinx.datetime.*
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*
import kotlin.time.Clock

/**
 * "姨妈来了" sheet — simple square-grid calendar matching HomeCalendar style.
 * If start date is > 3 days ago, also shows end date selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartPeriodSheet(
    existingRecords: List<MenstrualRecord> = emptyList(),
    avgPeriodLength: Int = 5,
    onDismiss: () -> Unit,
    onConfirm: (start: LocalDate, end: LocalDate?) -> Unit,
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    var selectedStart by remember { mutableStateOf<LocalDate?>(null) }
    var selectedEnd by remember { mutableStateOf<LocalDate?>(null) }

    // Whether the selected start is old enough to also require end date
    val needsEndDate = selectedStart != null &&
            selectedStart!!.until(today, DateTimeUnit.DAY).toInt() > 3

    // Auto-fill end date when start is selected and > 3 days ago
    LaunchedEffect(selectedStart) {
        if (needsEndDate && selectedEnd == null && selectedStart != null) {
            selectedEnd = selectedStart!!.plus(avgPeriodLength - 1, DateTimeUnit.DAY)
        }
    }

    val minDate = existingRecords
        .filter { it.endDate != null }
        .maxByOrNull { it.endDate!! }
        ?.endDate?.plus(1, DateTimeUnit.DAY)

    // Collect existing period dates for coloring
    val periodDates = remember(existingRecords) {
        buildSet {
            existingRecords.forEach { record ->
                val end = record.endDate ?: today
                var d = record.startDate
                while (d <= end) { add(d); d = d.plus(1, DateTimeUnit.DAY) }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp)) {
            Text(
                stringResource(Res.string.start_period_question),
                style = MaterialTheme.typography.titleLarge,
            )
            SmallSpacer(4)
            // Show selected range feedback
            val hint = when {
                selectedStart != null && needsEndDate && selectedEnd != null ->
                    "${selectedStart!!.monthNumber}/${selectedStart!!.dayOfMonth} — ${selectedEnd!!.monthNumber}/${selectedEnd!!.dayOfMonth}"
                selectedStart != null ->
                    "${selectedStart!!.monthNumber}/${selectedStart!!.dayOfMonth}"
                else -> stringResource(Res.string.start_period_hint)
            }
            Text(
                hint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            SmallSpacer(16)

            // Simple square-grid calendar (HomeCalendar style)
            val months = (-3..0).map { offset ->
                val m = today.plus(offset, DateTimeUnit.MONTH)
                YearMonth(m.year, m.month)
            }

            val periodColor = MaterialTheme.colorScheme.error
            val todayColor = Color(0xFF7C4DFF) // purple for today

            for (ym in months) {
                PickerMonthBlock(
                    yearMonth = ym,
                    today = today,
                    periodDates = periodDates,
                    selectedStart = selectedStart,
                    selectedEnd = if (needsEndDate) selectedEnd else null,
                    periodColor = periodColor,
                    todayColor = todayColor,
                    isDateEnabled = { date ->
                        date <= today && (minDate == null || date >= minDate)
                    },
                    onDateClick = { date ->
                        if (needsEndDate && selectedStart != null && date > selectedStart!!) {
                            // Clicking after start in end-date mode → set end
                            selectedEnd = date
                        } else {
                            selectedStart = date
                            selectedEnd = null
                        }
                    },
                )
                SmallSpacer(12)
            }

            // End date section — show when > 3 days ago
            if (needsEndDate) {
                SmallSpacer(4)
                Text(
                    stringResource(Res.string.end_period_question),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SmallSpacer(4)
            }

            SmallSpacer(8)
            PrimaryCta(
                text = stringResource(Res.string.onboarding_confirm),
                onClick = {
                    selectedStart?.let { start ->
                        onConfirm(start, if (needsEndDate) selectedEnd else null)
                    }
                },
                enabled = selectedStart != null && (!needsEndDate || selectedEnd != null),
            )
        }
    }
}

private data class YearMonth(val year: Int, val month: Month)

/**
 * Simple square-grid month block — matches HomeCalendar style.
 * No ovulation, no complex shapes, just rounded-rect cells.
 */
@Composable
private fun PickerMonthBlock(
    yearMonth: YearMonth,
    today: LocalDate,
    periodDates: Set<LocalDate>,
    selectedStart: LocalDate?,
    selectedEnd: LocalDate?,
    periodColor: Color,
    todayColor: Color,
    isDateEnabled: (LocalDate) -> Boolean,
    onDateClick: (LocalDate) -> Unit,
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
    val cellShape = MaterialTheme.shapes.extraSmall
    val onSurface = MaterialTheme.colorScheme.onSurface
    val selectedColor = MaterialTheme.colorScheme.primary

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "${yearMonth.year}年${yearMonth.month.number}月",
            style = MaterialTheme.typography.bodyMedium,
            color = onSurface.copy(alpha = 0.7f),
        )
        SmallSpacer(6)

        for (row in 0 until rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.padding(vertical = 1.5.dp),
            ) {
                for (col in 0 until 7) {
                    val dayNum = row * 7 + col - startOffset + 1
                    if (dayNum !in 1..daysInMonth) {
                        Spacer(Modifier.size(36.dp))
                    } else {
                        val date = LocalDate(yearMonth.year, yearMonth.month, dayNum)
                        val isToday = date == today
                        val isFuture = date > today
                        val isPeriod = date in periodDates
                        val isSelected = date == selectedStart || date == selectedEnd
                        val isInRange = selectedStart != null && selectedEnd != null &&
                                date > selectedStart && date < selectedEnd
                        val enabled = isDateEnabled(date)

                        val bgColor = when {
                            isSelected -> selectedColor
                            isInRange -> selectedColor.copy(alpha = 0.2f)
                            isPeriod -> periodColor
                            isToday -> todayColor
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        val textColor = when {
                            isSelected -> Color.White
                            isInRange -> selectedColor
                            isPeriod -> Color.White
                            isToday -> Color.White
                            isFuture -> onSurface.copy(alpha = 0.15f)
                            !enabled -> onSurface.copy(alpha = 0.15f)
                            else -> onSurface.copy(alpha = 0.45f)
                        }
                        val showNumber = isToday || isPeriod || isSelected || isInRange

                        Surface(
                            modifier = Modifier.size(36.dp)
                                .then(if (enabled) Modifier.clickable { onDateClick(date) } else Modifier),
                            shape = cellShape,
                            color = bgColor,
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                if (showNumber) {
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
}
