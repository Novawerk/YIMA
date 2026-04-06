package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.*
import io.github.adrcotfas.datetime.names.TextStyle
import io.github.adrcotfas.datetime.names.getDisplayName
import com.haodong.yimalaile.ui.components.DecorShape
import com.haodong.yimalaile.ui.components.SmallSpacer
import com.haodong.yimalaile.ui.theme.expressiveShapes
import com.haodong.yimalaile.ui.pages.sheet.SheetManager
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
    val ovulationDates = remember(sortedAsc, phaseInfo) {
        if (phaseInfo == null) return@remember emptySet<LocalDate>()
        val cycleLen = phaseInfo.cycleLength
        val ovStart = (cycleLen * 0.46).toInt()
        val ovEnd = (cycleLen * 0.57).toInt()
        buildSet {
            sortedAsc.forEach { record ->
                for (day in ovStart until ovEnd) add(record.startDate.plus(day, DateTimeUnit.DAY))
            }
            cycleState.predictions.forEach { pred ->
                for (day in ovStart until ovEnd) add(pred.predictedStart.plus(day, DateTimeUnit.DAY))
            }
        }
    }
    // Ovulation peak day (midpoint of ovulation window)
    val ovulationPeakDates = remember(sortedAsc, phaseInfo, cycleState.predictions) {
        if (phaseInfo == null) return@remember emptySet<LocalDate>()
        val cycleLen = phaseInfo.cycleLength
        val peakDay = (cycleLen * 0.5).toInt()
        buildSet {
            sortedAsc.forEach { record -> add(record.startDate.plus(peakDay, DateTimeUnit.DAY)) }
            cycleState.predictions.forEach { pred -> add(pred.predictedStart.plus(peakDay, DateTimeUnit.DAY)) }
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

    // Determine phase for selected date based on actual records
    val selectedPhaseInfo = remember(selectedDate, phaseInfo, sortedAsc) {
        if (phaseInfo == null || selectedDate == null || sortedAsc.isEmpty()) return@remember null
        val date = selectedDate!!
        val cycleLen = phaseInfo.cycleLength
        val periodLen = phaseInfo.periodLength
        val today2 = Clock.System.todayIn(TimeZone.currentSystemDefault())

        // Find the most recent period that started on or before this date
        val refRecord = sortedAsc.lastOrNull { it.startDate <= date }
            ?: return@remember null

        val dayInCycle = refRecord.startDate.until(date, DateTimeUnit.DAY).toInt() + 1
        if (dayInCycle < 1 || dayInCycle > cycleLen * 2) return@remember null

        // Check if this date actually falls within a recorded period
        val inActualPeriod = sortedAsc.any { r ->
            val rEnd = r.endDate ?: today2
            date in r.startDate..rEnd
        }

        val actualPeriodLen = refRecord.endDate?.let {
            refRecord.startDate.until(it, DateTimeUnit.DAY).toInt() + 1
        } ?: periodLen

        val phase = when {
            inActualPeriod -> CyclePhase.MENSTRUAL
            dayInCycle <= actualPeriodLen -> CyclePhase.MENSTRUAL
            dayInCycle <= (cycleLen * 0.46).toInt() -> CyclePhase.FOLLICULAR
            dayInCycle <= (cycleLen * 0.57).toInt() -> CyclePhase.OVULATION
            else -> CyclePhase.LUTEAL
        }
        val progress = (dayInCycle.toFloat() / cycleLen).coerceIn(0f, 1f)
        Triple(phase, dayInCycle, progress)
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
                        Icon(Icons.Outlined.HelpOutline, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
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
                    ovulationDates = ovulationDates,
                    ovulationPeakDates = ovulationPeakDates,
                    periodStartDates = periodStartDates,
                    periodEndDates = periodEndDates,
                    selectedDate = selectedDate,
                    onDateClick = { selectedDate = it },
                )
            }
        }

        // ── Spacer between calendar and detail ──
        item { SmallSpacer(8) }

        // ── Day detail — list items ──
        if (selectedDate != null && selectedPhaseInfo != null) {
            val (phase, dayInCycle, _) = selectedPhaseInfo!!
            val cycleLen = phaseInfo!!.cycleLength
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
                val pColor = phaseColor(phase)
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
                                Text(phaseDisplayName(phase), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = pColor)
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
                            phaseDescription(phase),
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
                    isStart && containingRecord != null -> {
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
                    isMidPeriod && containingRecord != null -> {
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

    // Legend dialog
    if (showLegendDialog) {
        val ovColor = Color(0xFF7C4DFF)
        val periodColor = MaterialTheme.colorScheme.error
        AlertDialog(
            onDismissRequest = { showLegendDialog = false },
            confirmButton = {
                TextButton(onClick = { showLegendDialog = false }) {
                    Text(stringResource(Res.string.dialog_confirm))
                }
            },
            title = { Text(stringResource(Res.string.home_cycle_calendar)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Period
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(24.dp).height(4.dp).clip(RoundedCornerShape(50)).background(periodColor))
                        SmallSpacer(12)
                        Text(stringResource(Res.string.legend_period), style = MaterialTheme.typography.bodyMedium)
                    }
                    // Predicted
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(24.dp).height(4.dp).clip(RoundedCornerShape(50)).background(periodColor.copy(alpha = 0.5f)))
                        SmallSpacer(12)
                        Text(stringResource(Res.string.legend_predicted), style = MaterialTheme.typography.bodyMedium)
                    }
                    // Ovulation
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(24.dp).height(4.dp).clip(RoundedCornerShape(50)).background(ovColor))
                        SmallSpacer(12)
                        Text(stringResource(Res.string.detail_ovulation), style = MaterialTheme.typography.bodyMedium)
                    }
                    // Ovulation peak
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(24.dp), contentAlignment = Alignment.Center) {
                            DecorShape(size = 14, shape = MaterialTheme.expressiveShapes.flower, color = ovColor)
                        }
                        SmallSpacer(12)
                        Text(stringResource(Res.string.detail_ovulation_day), style = MaterialTheme.typography.bodyMedium)
                    }
                    // Period start
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(24.dp), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = periodColor)
                        }
                        SmallSpacer(12)
                        Text(stringResource(Res.string.record_start_date), style = MaterialTheme.typography.bodyMedium)
                    }
                    // Period end
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(24.dp), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Pause, contentDescription = null, modifier = Modifier.size(16.dp), tint = periodColor)
                        }
                        SmallSpacer(12)
                        Text(stringResource(Res.string.record_end_date), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
        )
    }
}

// ════════════════════════════════════════════════════════════════
// Phase Timeline — dots connected by line, current phase highlighted
// ════════════════════════════════════════════════════════════════

@Composable
private fun PhaseTimeline(
    dayInCycle: Int,
    phaseInfo: CyclePhaseInfo,
) {
    val phases = listOf(CyclePhase.MENSTRUAL, CyclePhase.FOLLICULAR, CyclePhase.OVULATION, CyclePhase.LUTEAL)
    val currentPhase = phases.first { p ->
        val pLen = phaseInfo.periodLength
        val cLen = phaseInfo.cycleLength
        when (p) {
            CyclePhase.MENSTRUAL -> dayInCycle <= pLen
            CyclePhase.FOLLICULAR -> dayInCycle in (pLen + 1)..(cLen * 0.46).toInt()
            CyclePhase.OVULATION -> dayInCycle in ((cLen * 0.46).toInt() + 1)..(cLen * 0.57).toInt()
            CyclePhase.LUTEAL -> dayInCycle > (cLen * 0.57).toInt()
        }
    }
    val lineColor = MaterialTheme.colorScheme.outlineVariant

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        phases.forEachIndexed { index, phase ->
            val isActive = phase == currentPhase
            val color = phaseColor(phase)
            val dotSize = if (isActive) 14.dp else 10.dp

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier
                        .size(dotSize)
                        .clip(RoundedCornerShape(50))
                        .background(if (isActive) color else color.copy(alpha = 0.3f))
                )
                SmallSpacer(6)
                Text(
                    phaseDisplayName(phase),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = if (isActive) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                )
            }

            // Connecting line between dots
            if (index < phases.lastIndex) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(bottom = 18.dp) // align with dots, above labels
                        .background(lineColor)
                )
            }
        }
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
    ovulationDates: Set<LocalDate>,
    ovulationPeakDates: Set<LocalDate>,
    periodStartDates: Set<LocalDate>,
    periodEndDates: Set<LocalDate>,
    selectedDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit,
) {
    val firstDay = LocalDate(year, month, 1)
    val startOffset = firstDay.dayOfWeek.ordinal
    val days = daysInMonth(year, month)
    val rows = (startOffset + days + 6) / 7

    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
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
                        val isSelected = date == selectedDate && !isToday
                        val isFuture = date > today
                        val isPeriod = date in periodDates
                        val isPredicted = date in predictedPeriodDates && !isPeriod
                        val isOvulation = date in ovulationDates && !isPeriod && !isPredicted
                        val isPeriodStart = date in periodStartDates
                        val isPeriodEnd = date in periodEndDates
                        val isOvulationPeak = date in ovulationPeakDates
                        val textColor = when {
                            isToday -> Color.White
                            isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                            isFuture -> onSurface.copy(alpha = 0.25f)
                            else -> onSurface.copy(alpha = 0.8f)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(cellHeight)
                                .clickable { onDateClick(date) },
                            contentAlignment = Alignment.TopCenter,
                        ) {
                            // Selected background
                            if (isSelected) {
                                Surface(
                                    color = primaryContainer,
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.padding(top = 4.dp).size(32.dp),
                                ) {}
                            }

                            // Day number
                            if (isToday) {
                                Surface(
                                    color = primary,
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.padding(top = 4.dp).size(32.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("$dayNum", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            } else {
                                Text(
                                    "$dayNum",
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = textColor,
                                    modifier = Modifier.padding(top = 10.dp),
                                )
                            }

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
// Legend
// ════════════════════════════════════════════════════════════════

@Composable
private fun DetailLegend() {
    val periodColor = MaterialTheme.colorScheme.error
    val ovulationColor = Color(0xFF7C4DFF)

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(18.dp).height(4.dp).clip(RoundedCornerShape(50)).background(periodColor))
        SmallSpacer(4)
        Text(stringResource(Res.string.legend_period), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SmallSpacer(16)
        Box(Modifier.width(18.dp).height(4.dp).clip(RoundedCornerShape(50)).background(periodColor.copy(alpha = 0.5f)))
        SmallSpacer(4)
        Text(stringResource(Res.string.legend_predicted), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SmallSpacer(16)
        Box(Modifier.width(18.dp).height(4.dp).clip(RoundedCornerShape(50)).background(ovulationColor))
        SmallSpacer(4)
        Text(stringResource(Res.string.detail_ovulation), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
