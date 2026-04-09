package com.haodong.yimalaile.ui.pages.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.domain.settings.SettingsRepository
import androidx.compose.ui.platform.testTag
import com.haodong.yimalaile.ui.components.CycleCalendarGrid
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.components.SmallSpacer
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    service: MenstrualService,
    settings: SettingsRepository,
    onComplete: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var step by remember { mutableIntStateOf(0) }
    var periodStart by remember { mutableStateOf<LocalDate?>(null) }
    var periodEnd by remember { mutableStateOf<LocalDate?>(null) }
    var cycleLength by remember { mutableIntStateOf(28) }
    var periodDuration by remember { mutableIntStateOf(5) }

    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    // Empty calendar state for the grid (no existing records)
    val calendarState = remember {
        CycleState(records = emptyList(), predictions = emptyList(), currentPeriod = null, inPredictedPeriod = false)
    }

    // Hint text for calendar selection
    val selectionHint = when {
        periodStart != null && periodEnd != null ->
            "${periodStart!!.month.number}/${periodStart!!.day} — ${periodEnd!!.month.number}/${periodEnd!!.day}"
        periodStart != null ->
            "${periodStart!!.month.number}/${periodStart!!.day} — ${stringResource(Res.string.onboarding_select_end)}"
        else -> stringResource(Res.string.start_period_hint)
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

                androidx.compose.foundation.Image(
                    org.jetbrains.compose.resources.painterResource(Res.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                )
                SmallSpacer(16)

                Text(
                    stringResource(Res.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                SmallSpacer(32)

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
                    modifier = Modifier.testTag("onboarding_next"),
                )
            }

            // ── Step 1: Select last period range on calendar ──
            1 -> {
                Text(
                    stringResource(Res.string.onboarding_select_period_range),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                SmallSpacer(4)
                Text(
                    selectionHint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                SmallSpacer(12)

                CycleCalendarGrid(
                    state = calendarState,
                    phaseInfo = null,
                    selectedStart = periodStart,
                    selectedEnd = periodEnd,
                    onDateClick = { date ->
                        if (periodStart == null) {
                            // First tap: set start
                            periodStart = date
                            periodEnd = null
                        } else if (periodEnd == null) {
                            // Second tap: set end (swap if before start)
                            if (date < periodStart!!) {
                                periodEnd = periodStart
                                periodStart = date
                            } else if (date == periodStart) {
                                // Tap same date: clear
                                periodStart = null
                            } else {
                                periodEnd = date
                            }
                        } else {
                            // Both set: restart selection
                            periodStart = date
                            periodEnd = null
                        }
                    },
                    modifier = Modifier.weight(1f),
                    monthRange = -3..1,
                )

                SmallSpacer(16)
                PrimaryCta(
                    text = stringResource(Res.string.onboarding_next),
                    onClick = {
                        // Use the actual duration as default for the slider
                        if (periodStart != null && periodEnd != null) {
                            periodDuration = periodStart!!.until(periodEnd!!, DateTimeUnit.DAY).toInt() + 1
                        }
                        step = 2
                    },
                    enabled = periodStart != null && periodEnd != null,
                )
            }

            // ── Step 2: Set period duration + cycle length ──
            2 -> {
                Text(
                    stringResource(Res.string.onboarding_cycle_settings_title),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                SmallSpacer(8)
                Text(
                    stringResource(Res.string.onboarding_cycle_settings_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.weight(0.3f))

                // Period duration slider
                Text(
                    stringResource(Res.string.onboarding_period_duration_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                SmallSpacer(8)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "$periodDuration",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(Res.string.unit_days),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                SmallSpacer(8)
                Slider(
                    value = periodDuration.toFloat(),
                    onValueChange = { periodDuration = it.toInt() },
                    valueRange = 2f..10f,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                )
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("2", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("10", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                SmallSpacer(40)

                // Cycle length slider
                Text(
                    stringResource(Res.string.onboarding_cycle_length_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                SmallSpacer(8)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "$cycleLength",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(Res.string.unit_days),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                SmallSpacer(8)
                Slider(
                    value = cycleLength.toFloat(),
                    onValueChange = { cycleLength = it.toInt() },
                    valueRange = 20f..45f,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                )
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("20", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("45", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(Modifier.weight(1f))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { step = 1 },
                        modifier = Modifier.weight(1f).height(56.dp),
                    ) { Text(stringResource(Res.string.onboarding_back)) }
                    PrimaryCta(
                        text = stringResource(Res.string.onboarding_next),
                        onClick = {
                            scope.launch {
                                val start = periodStart!!
                                val end = periodEnd!!

                                // Save user's current period
                                service.backfillPeriod(start, end)

                                // Auto-generate 5 past periods
                                for (i in 1..5) {
                                    val pastStart = start.minus(cycleLength * i, DateTimeUnit.DAY)
                                    val pastEnd = pastStart.plus(periodDuration - 1, DateTimeUnit.DAY)
                                    service.backfillPeriod(pastStart, pastEnd)
                                }

                                // Save settings
                                settings.setCycleLength(cycleLength)
                                settings.setPeriodDuration(periodDuration)

                                step = 3
                            }
                        },
                        modifier = Modifier.weight(1f),
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
                    stringResource(Res.string.onboarding_all_set_hint_simple),
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
