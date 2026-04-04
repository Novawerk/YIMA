package com.haodong.yimalaile.ui.pages.onboarding

import androidx.compose.foundation.layout.*
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
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.ui.components.CycleCalendarGrid
import com.haodong.yimalaile.ui.components.PrimaryCta
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

    // Calendar state for display
    val calendarState = remember { CycleState(activePeriod = null, recentPeriods = emptyList(), predictions = emptyList()) }

    Column(
        Modifier.fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (step) {
            // ── Step 0: When did your last period start? ──
            0 -> {
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
                    onClick = { step = 1 },
                    enabled = periodStart != null,
                )
            }

            // ── Step 1: When did it end? ──
            1 -> {
                Text(
                    stringResource(Res.string.onboarding_when_end),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                SmallSpacer(8)

                // "Still in period" toggle
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    FilterChip(
                        selected = stillInPeriod,
                        onClick = {
                            stillInPeriod = !stillInPeriod
                            if (stillInPeriod) periodEnd = null
                        },
                        label = { Text(stringResource(Res.string.onboarding_still_in_period)) },
                    )
                }
                SmallSpacer(8)

                if (!stillInPeriod) {
                    CycleCalendarGrid(
                        state = calendarState,
                        phaseInfo = null,
                        selectedDate = periodEnd,
                        onDateClick = { periodEnd = it },
                        isDateEnabled = { it >= (periodStart ?: it) },
                        modifier = Modifier.weight(1f),
                        monthRange = -6..0,
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }

                SmallSpacer(16)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { step = 0 },
                        modifier = Modifier.weight(1f).height(56.dp),
                    ) { Text(stringResource(Res.string.onboarding_back)) }
                    PrimaryCta(
                        text = stringResource(Res.string.onboarding_next),
                        onClick = {
                            // Generate 2 estimated past periods
                            val start = periodStart!!
                            val duration = if (stillInPeriod) 5 else {
                                start.until(periodEnd!!, DateTimeUnit.DAY).toInt() + 1
                            }
                            val cycleLen = 28
                            estimated = listOf(
                                start.minus(cycleLen, DateTimeUnit.DAY) to
                                        start.minus(cycleLen, DateTimeUnit.DAY).plus(duration - 1, DateTimeUnit.DAY),
                                start.minus(cycleLen * 2, DateTimeUnit.DAY) to
                                        start.minus(cycleLen * 2, DateTimeUnit.DAY).plus(duration - 1, DateTimeUnit.DAY),
                            )
                            step = 2
                        },
                        enabled = stillInPeriod || periodEnd != null,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // ── Step 2: Confirm estimated past cycles ──
            2 -> {
                if (adjustingIndex >= 0) {
                    // Adjusting a specific period
                    Text(
                        stringResource(Res.string.onboarding_period_n, adjustingIndex + 2),
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
                                        stringResource(Res.string.onboarding_period_n, index + 2),
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
                                SmallSpacer(12)
                                TextButton(onClick = {
                                    adjustingIndex = index
                                    adjustStart = start
                                    adjustEnd = end
                                }) {
                                    Text(stringResource(Res.string.onboarding_adjust))
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
                                    service.startPeriod(start)
                                } else {
                                    service.backfillPeriod(start, periodEnd!!)
                                }
                                // Save estimated periods
                                estimated.forEach { (s, e) ->
                                    service.backfillPeriod(s, e)
                                }
                                step = 3
                            }
                        },
                    )
                }
            }

            // ── Step 3: All set! ──
            3 -> {
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
