package com.haodong.yimalaile.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.AddRecordResult
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    service: MenstrualService,
    onComplete: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf(0) }
    // 0 = ask if in period, 1 = pick start date, 2 = ask about past periods,
    // 3 = pick past start, 4 = pick past end

    val currentDateState = rememberDatePickerState()
    val pastStartState = rememberDatePickerState()
    val pastEndState = rememberDatePickerState()
    var pastStartDate by remember { mutableStateOf<kotlinx.datetime.LocalDate?>(null) }
    var backfillCount by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        when (step) {
            0 -> {
                Text("Are you on your period?", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(32.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = { step = 1 }) { Text("Yes") }
                    OutlinedButton(onClick = { step = 2 }) { Text("No") }
                }
            }
            1 -> {
                Text("When did it start?", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                DatePicker(
                    state = currentDateState,
                    title = null, headline = null, showModeToggle = false,
                    colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = {
                        val millis = currentDateState.selectedDateMillis ?: return@Button
                        val date = Instant.fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.UTC).date
                        scope.launch {
                            service.startPeriod(date)
                            step = 2
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = currentDateState.selectedDateMillis != null,
                ) { Text("Confirm") }
            }
            2 -> {
                Text(
                    if (backfillCount == 0) "Do you remember your last period?"
                    else "Record another past period?",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Spacer(Modifier.height(32.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = { step = 3 }) { Text("Yes") }
                    OutlinedButton(onClick = onComplete) {
                        Text(if (backfillCount == 0) "Skip" else "Done")
                    }
                }
            }
            3 -> {
                Text("Start date", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                DatePicker(
                    state = pastStartState,
                    title = null, headline = null, showModeToggle = false,
                    colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = {
                        val millis = pastStartState.selectedDateMillis ?: return@Button
                        pastStartDate = Instant.fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.UTC).date
                        step = 4
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = pastStartState.selectedDateMillis != null,
                ) { Text("Next") }
            }
            4 -> {
                Text("End date", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                DatePicker(
                    state = pastEndState,
                    title = null, headline = null, showModeToggle = false,
                    colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.weight(1f),
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { step = 3 }, Modifier.weight(1f)) { Text("Back") }
                    Button(
                        onClick = {
                            val millis = pastEndState.selectedDateMillis ?: return@Button
                            val end = Instant.fromEpochMilliseconds(millis)
                                .toLocalDateTime(TimeZone.UTC).date
                            val start = pastStartDate!!
                            scope.launch {
                                val result = service.backfillPeriod(start, end)
                                if (result is AddRecordResult.Success) {
                                    backfillCount++
                                }
                                step = 2
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = pastEndState.selectedDateMillis != null,
                    ) { Text("Save") }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
