package com.haodong.yimalaile.ui.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.AddRecordResult
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.ui.record.BackfillSheet
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.until
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.calendar_no_records
import yimalaile.composeapp.generated.resources.nav_back
import yimalaile.composeapp.generated.resources.record_save_success
import yimalaile.composeapp.generated.resources.stats_avg_cycle
import yimalaile.composeapp.generated.resources.stats_avg_period
import yimalaile.composeapp.generated.resources.stats_empty_message
import yimalaile.composeapp.generated.resources.stats_title
import yimalaile.composeapp.generated.resources.unit_days

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    service: MenstrualService,
    onBack: () -> Unit,
) {
    var state by remember { mutableStateOf<CycleState?>(null) }
    var showBackfill by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val successMsg = stringResource(Res.string.record_save_success)

    LaunchedEffect(Unit) { state = service.getCycleState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.stats_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.nav_back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showBackfill = true }) {
                Icon(Icons.Default.Add, "Backfill")
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        val s = state
        if (s == null) return@Scaffold

        if (s.recentPeriods.isEmpty() && s.activePeriod == null) {
            Column(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    stringResource(Res.string.stats_empty_message),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Summary cards
                item {
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val records = s.recentPeriods + listOfNotNull(s.activePeriod)
                        val avgCycle = if (records.size >= 2) {
                            val sorted = records.sortedBy { it.startDate }
                            val gaps = sorted.zipWithNext().map { (a, b) ->
                                a.startDate.until(b.startDate, DateTimeUnit.DAY)
                            }
                            gaps.sum() / gaps.size
                        } else null

                        val avgPeriod = records.filter { it.endDate != null }.let { list ->
                            if (list.isEmpty()) null
                            else list.sumOf { it.startDate.until(it.endDate!!, DateTimeUnit.DAY) + 1 } / list.size
                        }

                        StatSummaryCard(
                            label = stringResource(Res.string.stats_avg_cycle),
                            value = avgCycle?.let { "$it ${stringResource(Res.string.unit_days)}" } ?: "--",
                            modifier = Modifier.weight(1f),
                        )
                        StatSummaryCard(
                            label = stringResource(Res.string.stats_avg_period),
                            value = avgPeriod?.let { "$it ${stringResource(Res.string.unit_days)}" } ?: "--",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // History
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("History", style = MaterialTheme.typography.titleMedium)
                }

                if (s.recentPeriods.isEmpty()) {
                    item { Text(stringResource(Res.string.calendar_no_records)) }
                } else {
                    items(s.recentPeriods) { record ->
                        RecordRow(record)
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (showBackfill) {
        BackfillSheet(
            onDismiss = { showBackfill = false },
            onSave = { start, end ->
                scope.launch {
                    val result = service.backfillPeriod(start, end)
                    showBackfill = false
                    if (result is AddRecordResult.Success) {
                        state = service.getCycleState()
                        snackbar.showSnackbar(successMsg)
                    }
                }
            }
        )
    }
}

@Composable
private fun StatSummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun RecordRow(record: MenstrualRecord) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${record.startDate}", style = MaterialTheme.typography.bodyLarge)
            if (record.endDate != null) {
                val days = record.startDate.until(record.endDate, DateTimeUnit.DAY) + 1
                Text("$days ${stringResource(Res.string.unit_days)}", style = MaterialTheme.typography.bodyMedium)
            } else {
                Text("(ongoing)", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
