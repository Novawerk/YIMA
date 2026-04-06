package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.East
import androidx.compose.material.icons.filled.West
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.*
import com.haodong.yimalaile.ui.components.DecorShape
import com.haodong.yimalaile.ui.components.SmallSpacer
import com.haodong.yimalaile.ui.pages.sheet.sheets.PhaseExplanationSheet
import com.haodong.yimalaile.ui.theme.expressiveShapes
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*
import kotlin.time.Clock

private const val CENTER_PAGE = 500

@Composable
internal fun DetailCalendarView(
    cycleState: CycleState,
    phaseInfo: CyclePhaseInfo?,
    service: MenstrualService,
    onRefresh: () -> Unit,
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val scope = rememberCoroutineScope()
    val allRecords = cycleState.records.filter { !it.isDeleted }
    val sortedAsc = allRecords.sortedBy { it.startDate }
    var showLegendDialog by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(initialPage = CENTER_PAGE) { CENTER_PAGE * 2 }
    val currentPage by remember { derivedStateOf { pagerState.currentPage } }
    val displayYearMonth = remember(currentPage) {
        val offset = currentPage - CENTER_PAGE
        val base = today.plus(offset, DateTimeUnit.MONTH)
        base.year to base.month
    }
    val (displayYear, displayMonth) = displayYearMonth

    var selectedDate by remember { mutableStateOf<LocalDate?>(today) }

    // Clear selection when month changes
    LaunchedEffect(currentPage) {
        if (currentPage == CENTER_PAGE) {
            selectedDate = today
        } else {
            selectedDate = null
        }
    }

    // Period date sets
    val periodDates = remember(allRecords) {
        buildSet {
            allRecords.forEach { record ->
                val end = record.endDate ?: today
                var d = record.startDate
                while (d <= end) { add(d); d = d.plus(1, DateTimeUnit.DAY) }
            }
        }
    }
    val predictedPeriodDates = remember(cycleState.predictions, phaseInfo) {
        val avgPeriod = phaseInfo?.periodLength ?: 5
        buildSet {
            cycleState.predictions.forEach { pred ->
                val pEnd = pred.predictedEnd ?: pred.predictedStart.plus(avgPeriod - 1, DateTimeUnit.DAY)
                var d = pred.predictedStart
                while (d <= pEnd) { add(d); d = d.plus(1, DateTimeUnit.DAY) }
            }
        }
    }

    // Ovulation peak day (the single ovulation day)
    val ovulationPeakDates = remember(sortedAsc, phaseInfo, cycleState.predictions) {
        if (phaseInfo == null) return@remember emptySet<LocalDate>()
        buildSet {
            sortedAsc.forEach { record ->
                val cycle = service.getMenstrualCycle(record, sortedAsc, cycleState.predictions, phaseInfo.cycleLength)
                add(cycle.ovulationPeakDate)
            }
            cycleState.predictions.forEach { pred ->
                val tempRecord = MenstrualRecord(
                    id = "temp",
                    startDate = pred.predictedStart,
                    endDate = pred.predictedEnd,
                    createdAtEpochMillis = 0L,
                    updatedAtEpochMillis = 0L
                )
                val cycle = service.getMenstrualCycle(tempRecord, sortedAsc, cycleState.predictions, phaseInfo.cycleLength)
                add(cycle.ovulationPeakDate)
            }
        }
    }

    val periodStartDates = remember(allRecords, cycleState.predictions) {
        buildSet {
            allRecords.forEach { add(it.startDate) }
            cycleState.predictions.forEach { add(it.predictedStart) }
        }
    }
    val periodEndDates = remember(allRecords, cycleState.predictions, phaseInfo) {
        val avgPeriod = phaseInfo?.periodLength ?: 5
        buildSet {
            allRecords.mapNotNull { it.endDate }.forEach { add(it) }
            cycleState.predictions.forEach { pred ->
                add(pred.predictedEnd ?: pred.predictedStart.plus(avgPeriod - 1, DateTimeUnit.DAY))
            }
        }
    }

    val isCurrentMonth = displayYear == today.year && displayMonth == today.month

    // Determine phase for selected date based on unified logic
    val selectedPhaseInfo = remember(selectedDate, phaseInfo, cycleState, sortedAsc) {
        if (phaseInfo == null || selectedDate == null) return@remember null
        CyclePhaseInfo.getPhaseInfo(selectedDate!!, cycleState, phaseInfo.cycleLength)
    }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
    ) {
        // ── Month header ──
        item {
            Box(Modifier.fillMaxWidth()) {
                Text(
                    "${monthDisplayName(displayMonth)} $displayYear",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Center),
                )
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
                    }
                    IconButton(
                        onClick = { showLegendDialog = true },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.HelpOutline, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    }
                }
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!isCurrentMonth) {
                        IconButton(onClick = { scope.launch { pagerState.animateScrollToPage(CENTER_PAGE) } }) {
                            Icon(Icons.Outlined.Replay, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    IconButton(onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                }
            }
        }

        // ── Calendar grid ──
        item {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
                val offset = page - CENTER_PAGE
                val m = today.plus(offset, DateTimeUnit.MONTH)
                DetailMonthGrid(
                    year = m.year,
                    month = m.month,
                    today = today,
                    periodDates = periodDates,
                    predictedPeriodDates = predictedPeriodDates,
                    ovulationPeakDates = ovulationPeakDates,
                    periodStartDates = periodStartDates,
                    periodEndDates = periodEndDates,
                    selectedDate = selectedDate,
                    onDateClick = { selectedDate = it },
                    allRecords = allRecords,
                    predictions = cycleState.predictions,
                    phaseInfo = phaseInfo,
                )
            }
        }

        // ── Spacer between calendar and detail ──
        item { SmallSpacer(8) }

        // ── Day detail — list items ──
        if (selectedDate != null && selectedPhaseInfo != null) {
            val phase = selectedPhaseInfo.phase
            val dayInCycle = selectedPhaseInfo.dayInCycle
            val cycleLen = selectedPhaseInfo.cycleLength
            val date = selectedDate!!

            // ListItem 1: Cycle status
            item {
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.detail_cycle_status), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailingContent = {
                        Text(stringResource(Res.string.home_day_in_cycle, dayInCycle, cycleLen), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier.clip(MaterialTheme.shapes.extraLarge),
                )
            }

            // ListItem 2: Current phase (clickable → opens phase sheet)
            item {
                val pColor = phase.color()
                val isOvPeak = date in ovulationPeakDates
                val ovColor = Color(0xFF7C4DFF)
                var showPhaseSheet by remember { mutableStateOf(false) }
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.detail_current_phase), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isOvPeak) {
                                DecorShape(size = 14, shape = MaterialTheme.expressiveShapes.flower, color = ovColor)
                                SmallSpacer(6)
                                Text(stringResource(Res.string.detail_ovulation_day), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ovColor)
                            } else {
                                Box(Modifier.size(10.dp).clip(RoundedCornerShape(50)).background(pColor))
                                SmallSpacer(6)
                                Text(phase.displayName(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = pColor)
                            }
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier.clip(MaterialTheme.shapes.extraLarge).clickable { showPhaseSheet = true },
                )
                if (showPhaseSheet && phaseInfo != null) {
                    PhaseExplanationSheet(
                        phaseInfo = phaseInfo,
                        onDismiss = { showPhaseSheet = false },
                    )
                }
            }

            // Phase tips
            item {
                Surface(
                    tonalElevation = 1.dp,
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            stringResource(Res.string.detail_tips),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        SmallSpacer(4)
                        Text(
                            phase.description(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            // Item 4: Period action (only for today or past dates)
            if (date <= today) item {
                val containingRecord = allRecords.find { r ->
                    val rEnd = r.endDate ?: today
                    date in r.startDate..rEnd
                }
                val isStart = containingRecord?.startDate == date
                val isEnd = containingRecord?.endDate == date
                val isMidPeriod = containingRecord != null && !isStart && !isEnd
                val extendableRecord = if (containingRecord == null) {
                    allRecords.find { r ->
                        r.endDate != null && (
                            // 3 days after end
                            (date > r.endDate && date <= r.endDate.plus(3, DateTimeUnit.DAY)) ||
                            // 3 days before start
                            (date < r.startDate && date >= r.startDate.minus(3, DateTimeUnit.DAY))
                        )
                    }
                } else null

                when {
                    isStart -> {
                        FilledTonalButton(
                            onClick = { scope.launch { service.deleteRecord(containingRecord.id); onRefresh() } },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50),
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            SmallSpacer(6)
                            Text(stringResource(Res.string.detail_delete_period))
                        }
                    }
                    isMidPeriod -> {
                        FilledTonalButton(
                            onClick = { scope.launch { service.editRecordDates(containingRecord.id, newStart = null, newEnd = date); onRefresh() } },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50),
                        ) {
                            Icon(Icons.Filled.West, contentDescription = null, modifier = Modifier.size(18.dp))
                            SmallSpacer(6)
                            Text(stringResource(Res.string.detail_shorten_period))
                        }
                    }
                    extendableRecord != null -> {
                        val extendBefore = date < extendableRecord.startDate
                        FilledTonalButton(
                            onClick = {
                                scope.launch {
                                    if (extendBefore) {
                                        service.editRecordDates(extendableRecord.id, newStart = date, newEnd = null)
                                    } else {
                                        service.editRecordDates(extendableRecord.id, newStart = null, newEnd = date)
                                    }
                                    onRefresh()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50),
                        ) {
                            Icon(Icons.Filled.East, contentDescription = null, modifier = Modifier.size(18.dp))
                            SmallSpacer(6)
                            Text(stringResource(Res.string.detail_extend_period))
                        }
                    }
                    containingRecord == null -> {
                        FilledTonalButton(
                            onClick = { scope.launch { service.recordPeriodStart(date); onRefresh() } },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50),
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            SmallSpacer(6)
                            Text(stringResource(Res.string.detail_add_period))
                        }
                    }
                }
            }
        } else {
            // Empty state hint
            item {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Outlined.TouchApp,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    )
                    SmallSpacer(12)
                    Text(
                        stringResource(Res.string.detail_no_selection),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }

    if (showLegendDialog) {
        CalendarLegendDialog(onDismiss = { showLegendDialog = false })
    }
}


// ════════════════════════════════════════════════════════════════
// Month Grid
// ════════════════════════════════════════════════════════════════

@Composable
private fun DetailMonthGrid(
    year: Int,
    month: Month,
    today: LocalDate,
    periodDates: Set<LocalDate>,
    predictedPeriodDates: Set<LocalDate>,
    ovulationPeakDates: Set<LocalDate>,
    periodStartDates: Set<LocalDate>,
    periodEndDates: Set<LocalDate>,
    selectedDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit,
    allRecords: List<MenstrualRecord>,
    predictions: List<PredictedCycle>,
    phaseInfo: CyclePhaseInfo?,
) {
    val firstDay = LocalDate(year, month, 1)
    val startOffset = firstDay.dayOfWeek.ordinal
    val days = daysInMonth(year, month)
    val rows = (startOffset + days + 6) / 7

    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary
    val periodColor = MaterialTheme.colorScheme.error
    val ovulationColor = Color(0xFF7C4DFF)

    Column(Modifier.fillMaxWidth()) {
        // Weekday header
        Row(Modifier.fillMaxWidth()) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                Text(
                    day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurface.copy(alpha = 0.4f),
                )
            }
        }
        SmallSpacer(4)

        val cellHeight = 48.dp
        val underlineH = 4.dp

        for (row in 0 until rows) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                for (col in 0 until 7) {
                    val dayNum = row * 7 + col - startOffset + 1
                    if (dayNum < 1 || dayNum > days) {
                        Spacer(Modifier.weight(1f).height(cellHeight))
                    } else {
                        val date = LocalDate(year, month, dayNum)
                        val isToday = date == today
                        val isSelected = date == selectedDate
                        val isFuture = date > today
                        val isPeriod = date in periodDates
                        val isPredicted = date in predictedPeriodDates && !isPeriod
                        val isPeriodStart = date in periodStartDates
                        val isPeriodEnd = date in periodEndDates

                        // Phase calculation for each day in grid to determine ovulation underline
                        val currentPhaseInfo = CyclePhaseInfo.getPhaseInfo(date, CycleState(allRecords, predictions, null, false), phaseInfo?.cycleLength ?: 28)
                        val isOvulation = currentPhaseInfo?.phase == CyclePhase.OVULATION && !isPeriod && !isPredicted

                        val isOvulationPeak = date in ovulationPeakDates
                        val textColor = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            isToday -> onSurface.copy(alpha = 0.8f)
                            isFuture -> onSurface.copy(alpha = 0.25f)
                            else -> onSurface.copy(alpha = 0.8f)
                        }

                        val cellBgModifier = if (isToday && !isSelected) {
                            Modifier.padding(top = 4.dp).size(32.dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), MaterialTheme.shapes.small)
                        } else Modifier

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(cellHeight)
                                .clickable { onDateClick(date) },
                            contentAlignment = Alignment.TopCenter,
                        ) {
                            // Today background
                            if (isToday && !isSelected) {
                                Box(modifier = cellBgModifier)
                            }

                            // Selected background
                            if (isSelected) {
                                Surface(
                                    color = primary,
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.padding(top = 4.dp).size(32.dp),
                                ) {}
                            }

                            // Day number
                            Text(
                                "$dayNum",
                                fontSize = 14.sp,
                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                color = textColor,
                                modifier = Modifier.padding(top = 10.dp),
                            )

                            // Per-cell underline
                            val underlineColor = when {
                                isPeriod -> periodColor
                                isPredicted -> periodColor.copy(alpha = 0.5f)
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
                            // Ovulation peak flower (same position as period end icon)
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
                }
            }
        }
    }
}


// ════════════════════════════════════════════════════════════════
// Helpers
// ════════════════════════════════════════════════════════════════

private fun daysInMonth(year: Int, month: Month): Int = when (month) {
    Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY,
    Month.AUGUST, Month.OCTOBER, Month.DECEMBER -> 31
    Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
    Month.FEBRUARY -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
}

private fun monthDisplayName(month: Month): String = when (month) {
    Month.JANUARY -> "January"; Month.FEBRUARY -> "February"
    Month.MARCH -> "March"; Month.APRIL -> "April"
    Month.MAY -> "May"; Month.JUNE -> "June"
    Month.JULY -> "July"; Month.AUGUST -> "August"
    Month.SEPTEMBER -> "September"; Month.OCTOBER -> "October"
    Month.NOVEMBER -> "November"; Month.DECEMBER -> "December"
}
