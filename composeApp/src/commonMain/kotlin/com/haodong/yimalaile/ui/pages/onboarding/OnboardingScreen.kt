package com.haodong.yimalaile.ui.pages.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.AddRecordResult
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.ui.components.CycleCalendarGrid
import com.haodong.yimalaile.ui.components.CycleCalendarLegend
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.theme.expressiveShapes
import com.haodong.yimalaile.ui.components.SmallSpacer
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.until
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    service: MenstrualService,
    onComplete: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var step by remember { mutableIntStateOf(0) }
    var periodStart by remember { mutableStateOf<LocalDate?>(null) }
    var periodEnd by remember { mutableStateOf<LocalDate?>(null) }
    var stillInPeriod by remember { mutableStateOf(false) }

    // Estimated past periods for step 2
    var estimated by remember { mutableStateOf(listOf<Pair<LocalDate, LocalDate>>()) }
    var adjustingIndex by remember { mutableIntStateOf(-1) }
    var adjustStart by remember { mutableStateOf<LocalDate?>(null) }
    var adjustEnd by remember { mutableStateOf<LocalDate?>(null) }

    // Calendar state — build records from user input + estimated for display
    val calendarRecords = remember(periodStart, periodEnd, stillInPeriod, estimated) {
        val records = mutableListOf<MenstrualRecord>()
        if (periodStart != null) {
            records.add(MenstrualRecord(
                id = "onboard_current", startDate = periodStart!!,
                endDate = if (stillInPeriod) null else periodEnd,
                endConfirmed = periodEnd != null && !stillInPeriod,
                createdAtEpochMillis = 0, updatedAtEpochMillis = 0,
            ))
        }
        estimated.forEachIndexed { i, (s, e) ->
            records.add(MenstrualRecord(
                id = "onboard_est_$i", startDate = s, endDate = e,
                endConfirmed = true, createdAtEpochMillis = 0, updatedAtEpochMillis = 0,
            ))
        }
        records.toList()
    }
    val calendarState = remember(calendarRecords) {
        CycleState(records = calendarRecords, predictions = emptyList(), currentPeriod = null, inPredictedPeriod = false)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
    Column(
        Modifier.fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (step) {
            // ── Step 0: Welcome ──
            0 -> {
                Spacer(Modifier.weight(0.5f))

                // Logo
                androidx.compose.foundation.Image(
                    org.jetbrains.compose.resources.painterResource(Res.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                )
                SmallSpacer(16)

                // App name
                Text(
                    stringResource(Res.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                SmallSpacer(32)

                // Welcome text in rounded card
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(40.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .padding(28.dp),
                ) {
                    Text(
                        stringResource(Res.string.onboarding_welcome),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(Modifier.weight(1f))
                PrimaryCta(
                    text = stringResource(Res.string.onboarding_next),
                    onClick = { step = 1 },
                )
            }

            // ── Step 1: When did your last period start? ──
            1 -> {
                Text(
                    stringResource(Res.string.onboarding_when_last_start),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                SmallSpacer(16)
                CycleCalendarGrid(
                    state = calendarState,
                    phaseInfo = null,
                    selectedDate = periodStart,
                    onDateClick = { periodStart = it },
                    modifier = Modifier.weight(1f),
                    monthRange = -6..0,
                )
                SmallSpacer(16)
                PrimaryCta(
                    text = stringResource(Res.string.onboarding_next),
                    onClick = { step = 2 },
                    enabled = periodStart != null,
                )
            }

            // ── Step 2: When did it end? ──
            2 -> {
                Text(
                    stringResource(Res.string.onboarding_when_end),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                SmallSpacer(4)
                // Show selected start date
                Text(
                    "${stringResource(Res.string.onboarding_when_last_start).substringBefore("？").substringBefore("?")} ${periodStart!!.monthNumber}/${periodStart!!.dayOfMonth}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                SmallSpacer(8)
                CycleCalendarLegend()
                SmallSpacer(8)

                // Show start date on calendar by using selectedStart
                CycleCalendarGrid(
                    state = calendarState,
                    phaseInfo = null,
                    selectedStart = periodStart,
                    selectedEnd = periodEnd,
                    onDateClick = { if (it >= periodStart!!) periodEnd = it },
                    isDateEnabled = { it >= periodStart!! },
                    modifier = Modifier.weight(1f),
                    monthRange = -6..0,
                )

                SmallSpacer(16)
                // "Still in period" as action button
                OutlinedButton(
                    onClick = {
                        stillInPeriod = true
                        periodEnd = null
                        val start = periodStart!!
                        val duration = 5
                        val cycleLen = 28
                        estimated = listOf(
                            start.minus(cycleLen, DateTimeUnit.DAY) to
                                    start.minus(cycleLen, DateTimeUnit.DAY).plus(duration - 1, DateTimeUnit.DAY),
                            start.minus(cycleLen * 2, DateTimeUnit.DAY) to
                                    start.minus(cycleLen * 2, DateTimeUnit.DAY).plus(duration - 1, DateTimeUnit.DAY),
                        )
                        step = 3
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    Text(stringResource(Res.string.onboarding_still_in_period))
                }
                SmallSpacer(8)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { step = 1 },
                        modifier = Modifier.weight(1f).height(56.dp),
                    ) { Text(stringResource(Res.string.onboarding_back)) }
                    PrimaryCta(
                        text = stringResource(Res.string.onboarding_next),
                        onClick = {
                            val start = periodStart!!
                            val duration = start.until(periodEnd!!, DateTimeUnit.DAY).toInt() + 1
                            val cycleLen = 28
                            estimated = listOf(
                                start.minus(cycleLen, DateTimeUnit.DAY) to
                                        start.minus(cycleLen, DateTimeUnit.DAY).plus(duration - 1, DateTimeUnit.DAY),
                                start.minus(cycleLen * 2, DateTimeUnit.DAY) to
                                        start.minus(cycleLen * 2, DateTimeUnit.DAY).plus(duration - 1, DateTimeUnit.DAY),
                            )
                            step = 3
                        },
                        enabled = periodEnd != null,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // ── Step 3: Confirm estimated past cycles ──
            3 -> {
                val periodLabels = listOf(
                    stringResource(Res.string.onboarding_period_prev),
                    stringResource(Res.string.onboarding_period_prev2),
                )

                if (adjustingIndex >= 0) {
                    // Adjusting a specific period
                    Text(
                        periodLabels.getOrElse(adjustingIndex) { "" },
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    SmallSpacer(8)
                    CycleCalendarGrid(
                        state = calendarState,
                        phaseInfo = null,
                        selectedStart = adjustStart,
                        selectedEnd = adjustEnd,
                        onDateClick = { date ->
                            if (adjustStart == null || adjustEnd != null) {
                                adjustStart = date; adjustEnd = null
                            } else {
                                if (date < adjustStart!!) { adjustEnd = adjustStart; adjustStart = date }
                                else adjustEnd = date
                            }
                        },
                        modifier = Modifier.weight(1f),
                        monthRange = -12..0,
                    )
                    SmallSpacer(16)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { adjustingIndex = -1 },
                            modifier = Modifier.weight(1f).height(56.dp),
                        ) { Text(stringResource(Res.string.onboarding_back)) }
                        PrimaryCta(
                            text = stringResource(Res.string.onboarding_confirm),
                            onClick = {
                                if (adjustStart != null && adjustEnd != null) {
                                    estimated = estimated.toMutableList().also {
                                        it[adjustingIndex] = adjustStart!! to adjustEnd!!
                                    }
                                }
                                adjustingIndex = -1
                            },
                            enabled = adjustStart != null && adjustEnd != null,
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    // Show estimated periods
                    Text(
                        stringResource(Res.string.onboarding_confirm_cycles),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    SmallSpacer(8)
                    Text(
                        stringResource(Res.string.onboarding_confirm_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    SmallSpacer(24)

                    // User's manually input period — tap edit icon to go back to step 1
                    val userDateRange = if (stillInPeriod) {
                        "${periodStart!!.monthNumber}/${periodStart!!.dayOfMonth} — ${stringResource(Res.string.history_in_progress)}"
                    } else {
                        "${periodStart!!.monthNumber}/${periodStart!!.dayOfMonth} — ${periodEnd!!.monthNumber}/${periodEnd!!.dayOfMonth}"
                    }
                    Surface(
                        tonalElevation = 2.dp,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    stringResource(Res.string.onboarding_period_current),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                SmallSpacer(4)
                                Text(userDateRange, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            if (!stillInPeriod) {
                                val userDays = periodStart!!.until(periodEnd!!, DateTimeUnit.DAY).toInt() + 1
                                Text("${userDays}${stringResource(Res.string.unit_days)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            SmallSpacer(8)
                            IconButton(onClick = { step = 1 }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    SmallSpacer(8)

                    // Estimated past periods
                    estimated.forEachIndexed { index, (start, end) ->
                        val days = start.until(end, DateTimeUnit.DAY).toInt() + 1
                        Surface(
                            tonalElevation = 1.dp,
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        periodLabels.getOrElse(index) { "" },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    SmallSpacer(4)
                                    Text(
                                        "${start.monthNumber}/${start.dayOfMonth} — ${end.monthNumber}/${end.dayOfMonth}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                Text(
                                    "${days}${stringResource(Res.string.unit_days)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                SmallSpacer(8)
                                IconButton(
                                    onClick = {
                                        adjustingIndex = index
                                        adjustStart = start
                                        adjustEnd = end
                                    },
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                        SmallSpacer(8)
                    }

                    Spacer(Modifier.weight(1f))

                    PrimaryCta(
                        text = stringResource(Res.string.onboarding_looks_right),
                        onClick = {
                            scope.launch {
                                // Save the first period
                                val start = periodStart!!
                                if (stillInPeriod) {
                                    service.recordPeriodStart(start)
                                } else {
                                    service.backfillPeriod(start, periodEnd!!)
                                }
                                // Save estimated periods
                                estimated.forEach { (s, e) ->
                                    service.backfillPeriod(s, e)
                                }
                                step = 4
                            }
                        },
                    )
                }
            }

            // ── Step 4: All set! ──
            4 -> {
                Spacer(Modifier.weight(1f))
                Text(
                    stringResource(Res.string.onboarding_all_set),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                SmallSpacer(12)
                Text(
                    stringResource(Res.string.onboarding_all_set_hint),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.weight(1f))
                PrimaryCta(
                    text = stringResource(Res.string.onboarding_get_started),
                    onClick = onComplete,
                )
            }
        }
    }
    }
}
