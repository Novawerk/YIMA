package com.haodong.yimalaile.ui.pages.statistics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.AddRecordResult
import com.haodong.yimalaile.domain.menstrual.CyclePhaseInfo
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.ui.components.SmallSpacer
import com.haodong.yimalaile.ui.pages.sheet.DetailAction
import com.haodong.yimalaile.ui.pages.sheet.SheetManager
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.until
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

@Composable
fun StatisticsScreen(
    service: MenstrualService,
    sheetManager: SheetManager,
    onBack: () -> Unit,
) {
    var state by remember { mutableStateOf<CycleState?>(null) }
    var phaseInfo by remember { mutableStateOf<CyclePhaseInfo?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val successMsg = stringResource(Res.string.record_save_success)
    val deletedMsg = stringResource(Res.string.record_deleted)
    val daysStr = stringResource(Res.string.unit_days)

    val today: LocalDate = remember {
        Clock.System.todayIn(TimeZone.currentSystemDefault())
    }

    fun refresh() {
        scope.launch {
            val s = service.getCycleState()
            state = s
            phaseInfo = service.getCurrentPhase(s, today)
        }
    }
    LaunchedEffect(Unit) { refresh() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    scope.launch {
                        val result = sheetManager.backfillPeriod() ?: return@launch
                        if (result is AddRecordResult.Success) { refresh(); snackbar.showSnackbar(successMsg) }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(28.dp),
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text(stringResource(Res.string.history_backfill)) },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        val s = state ?: return@Scaffold
        val allRecords = (listOfNotNull(s.activePeriod) + s.recentPeriods)
            .distinctBy { it.id }
            .sortedByDescending { it.startDate }

        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // ── Top bar ──
            item {
                SmallSpacer(48)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        stringResource(Res.string.history_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.weight(1f))
                    Box(Modifier.size(40.dp))
                }
                SmallSpacer(16)
            }

            // ── Stats summary ──
            if (allRecords.size >= 2) {
                item {
                    StatsSummaryRow(phaseInfo = phaseInfo, daysStr = daysStr)
                    SmallSpacer(20)
                }
            }

            // ── Empty state ──
            if (allRecords.isEmpty()) {
                item {
                    SmallSpacer(80)
                    Text(
                        stringResource(Res.string.stats_empty_message),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                // ── Timeline records ──
                itemsIndexed(allRecords, key = { _, r -> r.id }) { index, record ->
                    val isActive = record.endDate == null
                    val isLast = index == allRecords.lastIndex

                    TimelineRecordRow(
                        record = record,
                        daysStr = daysStr,
                        isActive = isActive,
                        isLast = isLast,
                        today = today,
                        onClick = {
                            scope.launch {
                                val action = sheetManager.showRecordDetail(record, isActive) ?: return@launch
                                when (action) {
                                    is DetailAction.EditStart -> {
                                        sheetManager.startPeriod()
                                        refresh()
                                    }
                                    is DetailAction.EditEnd -> {
                                        val ok = sheetManager.endPeriod() ?: return@launch
                                        if (ok) { refresh(); snackbar.showSnackbar(successMsg) }
                                    }
                                    is DetailAction.LogDay -> {
                                        val ok = sheetManager.logDay() ?: return@launch
                                        if (ok) { refresh(); snackbar.showSnackbar(successMsg) }
                                    }
                                    is DetailAction.Delete -> {
                                        service.deleteRecord(record.id)
                                        refresh()
                                        snackbar.showSnackbar(deletedMsg)
                                    }
                                }
                            }
                        },
                    )
                }
            }

            item { SmallSpacer(100) }
        }
    }
}

// ============================================================
// Stats summary
// ============================================================

@Composable
private fun StatsSummaryRow(phaseInfo: CyclePhaseInfo?, daysStr: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatChip(
            label = stringResource(Res.string.stats_avg_cycle),
            value = phaseInfo?.cycleLength?.toString() ?: "—",
            unit = daysStr,
            color = MaterialTheme.colorScheme.primaryContainer,
            textColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.weight(1f),
        )
        StatChip(
            label = stringResource(Res.string.stats_avg_period),
            value = phaseInfo?.periodLength?.toString() ?: "—",
            unit = daysStr,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            textColor = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    unit: String,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = color,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = textColor.copy(alpha = 0.7f),
            )
            SmallSpacer(8)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    value,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    unit,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
        }
    }
}

// ============================================================
// Timeline record row
// ============================================================

@Composable
private fun TimelineRecordRow(
    record: MenstrualRecord,
    daysStr: String,
    isActive: Boolean,
    isLast: Boolean,
    today: LocalDate,
    onClick: () -> Unit,
) {
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    val dotColor = if (isActive) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)

    Row(Modifier.fillMaxWidth()) {
        // Timeline gutter: dot + connecting line
        Box(
            modifier = Modifier.width(32.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SmallSpacer(24) // align with card content
                // Dot with optional pulse
                Box(contentAlignment = Alignment.Center) {
                    if (isActive) {
                        Box(
                            Modifier.size(18.dp)
                                .clip(CircleShape)
                                .background(dotColor.copy(alpha = 0.2f))
                        )
                    }
                    Box(
                        Modifier.size(10.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
                // Connecting line to next item
                if (!isLast) {
                    Box(
                        Modifier.width(2.dp)
                            .height(80.dp)
                            .drawBehind {
                                drawLine(
                                    color = lineColor,
                                    start = Offset(size.width / 2, 0f),
                                    end = Offset(size.width / 2, size.height),
                                    strokeWidth = 2.dp.toPx(),
                                    pathEffect = if (isActive) PathEffect.dashPathEffect(floatArrayOf(6f, 6f)) else null,
                                )
                            }
                    )
                }
            }
        }

        // Record card
        Box(Modifier.weight(1f).padding(bottom = 12.dp)) {
            RecordCard(
                record = record,
                daysStr = daysStr,
                isActive = isActive,
                today = today,
                onClick = onClick,
            )
        }
    }
}

@Composable
private fun RecordCard(record: MenstrualRecord, daysStr: String, isActive: Boolean, today: LocalDate, onClick: () -> Unit) {
    val days = record.endDate?.let { record.startDate.until(it, DateTimeUnit.DAY).toInt() + 1 }
    val activeDays = if (isActive) record.startDate.until(today, DateTimeUnit.DAY).toInt() + 1 else null

    val border = if (isActive) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else null

    Surface(
        onClick = onClick,
        tonalElevation = if (isActive) 1.dp else 2.dp,
        shape = MaterialTheme.shapes.large,
        border = border,
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            // Top row: date range + status/days
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${record.startDate.monthNumber}/${record.startDate.dayOfMonth}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                        )
                        if (record.endDate != null) {
                            Text(
                                " — ${record.endDate.monthNumber}/${record.endDate.dayOfMonth}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            Text(
                                " — …",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            )
                        }
                    }
                    // Year label if not current year
                    val currentYear = today.year
                    if (record.startDate.year != currentYear) {
                        Text(
                            "${record.startDate.year}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                }

                if (isActive) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(50),
                    ) {
                        Text(
                            stringResource(Res.string.history_in_progress),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        )
                    }
                } else if (days != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$days",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            daysStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Duration bar visualization
            val barDays = days ?: activeDays ?: 0
            if (barDays > 0) {
                SmallSpacer(12)
                DurationBar(days = barDays, isActive = isActive)
            }

            // Bottom info: daily records count + mood summary
            if (record.dailyRecords.isNotEmpty()) {
                SmallSpacer(10)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Mood emojis
                    val moodSummary = record.dailyRecords.mapNotNull { it.mood }.distinct()
                    if (moodSummary.isNotEmpty()) {
                        val emojiStr = moodSummary.joinToString("") { mood ->
                            when (mood) {
                                com.haodong.yimalaile.domain.menstrual.Mood.HAPPY -> "😊"
                                com.haodong.yimalaile.domain.menstrual.Mood.NEUTRAL -> "😐"
                                com.haodong.yimalaile.domain.menstrual.Mood.SAD -> "😔"
                                com.haodong.yimalaile.domain.menstrual.Mood.VERY_SAD -> "😢"
                            }
                        }
                        Text(emojiStr, fontSize = 14.sp)
                    }

                    Text(
                        stringResource(Res.string.history_n_daily_records, record.dailyRecords.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}

// ============================================================
// Duration bar
// ============================================================

@Composable
private fun DurationBar(days: Int, isActive: Boolean) {
    val maxDots = days.coerceAtMost(12)
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 600),
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        repeat(maxDots) { i ->
            val fraction = i.toFloat() / maxDots
            val alpha = if (fraction <= animatedProgress) {
                if (isActive) 0.4f + (0.5f * (1f - fraction))
                else 0.2f + (0.5f * (1f - fraction))
            } else 0.05f

            val color = if (isActive) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.tertiary

            Box(
                Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color.copy(alpha = alpha))
            )
        }
    }
}
