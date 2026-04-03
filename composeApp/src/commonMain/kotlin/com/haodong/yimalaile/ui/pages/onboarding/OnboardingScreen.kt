package com.haodong.yimalaile.ui.pages.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.AddRecordResult
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.components.RangeCalendar
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

@Composable
fun OnboardingScreen(
    service: MenstrualService,
    onComplete: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf(0) }
    var backfillCount by remember { mutableStateOf(0) }

    // Track records created during onboarding so calendar shows them
    val createdRecords = remember { mutableStateListOf<MenstrualRecord>() }

    // Single date selection state (for step 1: start period)
    var selectedSingle by remember { mutableStateOf<LocalDate?>(null) }

    // Range selection state (for step 3: backfill)
    var rangeStart by remember { mutableStateOf<LocalDate?>(null) }
    var rangeEnd by remember { mutableStateOf<LocalDate?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (step) {
            // Ask current period status
            0 -> {
                Spacer(Modifier.weight(0.3f))
                Text(stringResource(Res.string.onboarding_ask_period), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(32.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    PrimaryCta(stringResource(Res.string.onboarding_yes), onClick = { step = 1 }, modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = { step = 2 }, modifier = Modifier.weight(1f).height(56.dp)) {
                        Text(stringResource(Res.string.onboarding_no))
                    }
                }
                Spacer(Modifier.weight(1f))
            }

            // Pick current period start date
            1 -> {
                Text(stringResource(Res.string.onboarding_when_start), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(8.dp))
                RangeCalendar(
                    existingRecords = createdRecords,
                    selectedStart = selectedSingle,
                    selectedEnd = selectedSingle,
                    onDateClick = { selectedSingle = it },
                    singleSelectMode = true,
                    modifier = Modifier.weight(1f),
                )
                PrimaryCta(
                    text = stringResource(Res.string.onboarding_confirm),
                    onClick = {
                        scope.launch {
                            val result = service.startPeriod(selectedSingle!!)
                            if (result is AddRecordResult.Success) {
                                createdRecords.add(result.record)
                            }
                            step = 2
                        }
                    },
                    enabled = selectedSingle != null,
                )
            }

            // Ask about past periods
            2 -> {
                Spacer(Modifier.weight(0.3f))
                Text(
                    if (backfillCount == 0) stringResource(Res.string.onboarding_ask_past) else stringResource(Res.string.onboarding_ask_past_more),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(8.dp))
                val remaining = (2 - backfillCount).coerceAtLeast(0) // need 2 backfills for 3 total
                if (backfillCount > 0) {
                    Text(
                        if (remaining > 0) stringResource(Res.string.onboarding_progress_feedback, backfillCount, remaining)
                        else stringResource(Res.string.onboarding_enough_feedback, backfillCount),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (remaining == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        stringResource(Res.string.onboarding_past_hint),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(32.dp))
                if (remaining <= 0) {
                    // Enough data — only show Done
                    PrimaryCta(stringResource(Res.string.onboarding_done), onClick = onComplete)
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        PrimaryCta(stringResource(Res.string.onboarding_backfill), onClick = {
                            rangeStart = null; rangeEnd = null; step = 3
                        }, modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = onComplete, modifier = Modifier.weight(1f).height(56.dp)) {
                            Text(if (backfillCount == 0) stringResource(Res.string.onboarding_skip) else stringResource(Res.string.onboarding_done))
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
            }

            // RangeCalendar for past period
            3 -> {
                Text(stringResource(Res.string.onboarding_select_range), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(8.dp))
                RangeCalendar(
                    existingRecords = createdRecords,
                    selectedStart = rangeStart,
                    selectedEnd = rangeEnd,
                    onDateClick = { date ->
                        if (rangeStart == null || rangeEnd != null) {
                            rangeStart = date; rangeEnd = null
                        } else {
                            if (date < rangeStart!!) {
                                rangeEnd = rangeStart; rangeStart = date
                            } else {
                                rangeEnd = date
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { step = 2 }, Modifier.weight(1f)) { Text(stringResource(Res.string.onboarding_back)) }
                    PrimaryCta(
                        text = stringResource(Res.string.onboarding_save),
                        onClick = {
                            scope.launch {
                                val result = service.backfillPeriod(rangeStart!!, rangeEnd!!)
                                if (result is AddRecordResult.Success) {
                                    backfillCount++
                                    createdRecords.add(result.record)
                                }
                                step = 2
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = rangeStart != null && rangeEnd != null,
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
