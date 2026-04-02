package com.haodong.yimalaile.ui.record

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.dialog_cancel
import yimalaile.composeapp.generated.resources.record_date_range_error
import yimalaile.composeapp.generated.resources.record_end_date
import yimalaile.composeapp.generated.resources.record_save_btn
import yimalaile.composeapp.generated.resources.record_start_date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackfillSheet(
    onDismiss: () -> Unit,
    onSave: (start: LocalDate, end: LocalDate) -> Unit,
) {
    var step by remember { mutableStateOf(0) } // 0 = pick start, 1 = pick end
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    val startState = rememberDatePickerState()
    val endState = rememberDatePickerState()
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (step == 0) stringResource(Res.string.record_start_date) else stringResource(Res.string.record_end_date)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (step == 1) step = 0 else onDismiss()
                    }) { Icon(Icons.Default.Close, stringResource(Res.string.dialog_cancel)) }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp)
        ) {
            if (step == 0) {
                DatePicker(
                    state = startState,
                    title = null, headline = null, showModeToggle = false,
                    colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = {
                        val millis = startState.selectedDateMillis ?: return@Button
                        startDate = Instant.fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.UTC).date
                        step = 1
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = startState.selectedDateMillis != null,
                ) { Text("Next") }
            } else {
                DatePicker(
                    state = endState,
                    title = null, headline = null, showModeToggle = false,
                    colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.weight(1f),
                )
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                }
                Button(
                    onClick = {
                        val millis = endState.selectedDateMillis ?: return@Button
                        val end = Instant.fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.UTC).date
                        val start = startDate!!
                        if (end < start) {
                            error = "End date cannot be before start date"
                        } else {
                            onSave(start, end)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = endState.selectedDateMillis != null,
                ) { Text(stringResource(Res.string.record_save_btn)) }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
