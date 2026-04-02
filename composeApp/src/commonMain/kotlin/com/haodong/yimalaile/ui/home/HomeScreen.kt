package com.haodong.yimalaile.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.domain.menstrual.PredictedCycle
import com.haodong.yimalaile.ui.record.EndPeriodSheet
import com.haodong.yimalaile.ui.record.LogDaySheet
import com.haodong.yimalaile.ui.record.StartPeriodSheet
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.until
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.home_title
import yimalaile.composeapp.generated.resources.record_save_success
import yimalaile.composeapp.generated.resources.status_here
import yimalaile.composeapp.generated.resources.status_here_desc
import yimalaile.composeapp.generated.resources.status_no_prediction
import yimalaile.composeapp.generated.resources.status_not_here
import yimalaile.composeapp.generated.resources.status_relax_desc
import yimalaile.composeapp.generated.resources.stats_see_more
import yimalaile.composeapp.generated.resources.unit_days

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    service: MenstrualService,
    onNavigateStatistics: () -> Unit,
    onNavigateSettings: () -> Unit,
) {
    val viewModel = remember { HomeViewModel(service) }
    val uiState by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val successMsg = stringResource(Res.string.record_save_success)

    var showStartSheet by remember { mutableStateOf(false) }
    var showEndSheet by remember { mutableStateOf(false) }
    var showLogSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.home_title)) },
                actions = {
                    IconButton(onClick = onNavigateStatistics) {
                        Icon(Icons.Default.BarChart, stringResource(Res.string.stats_see_more))
                    }
                    IconButton(onClick = onNavigateSettings) {
                        Icon(Icons.Default.Settings, null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        when (val s = uiState) {
            is HomeUiState.Loading -> {
                CircularProgressIndicator(Modifier.fillMaxSize().padding(padding))
            }
            is HomeUiState.Ready -> {
                HomeContent(
                    state = s.cycleState,
                    modifier = Modifier.padding(padding),
                    onStartPeriod = { showStartSheet = true },
                    onEndPeriod = { showEndSheet = true },
                    onLogDay = { showLogSheet = true },
                )
            }
        }
    }

    if (showStartSheet) {
        StartPeriodSheet(
            onDismiss = { showStartSheet = false },
            onConfirm = { date ->
                viewModel.startPeriod(date) {
                    showStartSheet = false
                    scope.launch { snackbar.showSnackbar(successMsg) }
                }
            }
        )
    }

    if (showEndSheet) {
        val active = (uiState as? HomeUiState.Ready)?.cycleState?.activePeriod
        if (active != null) {
            EndPeriodSheet(
                startDate = active.startDate,
                onDismiss = { showEndSheet = false },
                onConfirm = { date ->
                    viewModel.endPeriod(date) {
                        showEndSheet = false
                        scope.launch { snackbar.showSnackbar(successMsg) }
                    }
                }
            )
        }
    }

    if (showLogSheet) {
        LogDaySheet(
            onDismiss = { showLogSheet = false },
            onSave = { intensity, mood, symptoms, notes ->
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                viewModel.logDay(today, intensity, mood, symptoms, notes) {
                    showLogSheet = false
                    scope.launch { snackbar.showSnackbar(successMsg) }
                }
            }
        )
    }
}

@Composable
private fun HomeContent(
    state: CycleState,
    modifier: Modifier = Modifier,
    onStartPeriod: () -> Unit,
    onEndPeriod: () -> Unit,
    onLogDay: () -> Unit,
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Status card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (state.activePeriod != null) {
                    val dayCount = state.activePeriod.startDate.until(today, DateTimeUnit.DAY) + 1
                    Text(
                        stringResource(Res.string.status_here),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "$dayCount ${stringResource(Res.string.unit_days)}",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(Res.string.status_here_desc),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onLogDay) { Text("Log Today") }
                        Button(onClick = onEndPeriod) { Text("End") }
                    }
                } else {
                    val prediction = state.predictions.firstOrNull()
                    if (prediction != null) {
                        val daysUntil = today.until(prediction.predictedStart, DateTimeUnit.DAY)
                        Text(
                            stringResource(Res.string.status_not_here),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "$daysUntil ${stringResource(Res.string.unit_days)}",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(Res.string.status_relax_desc),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    } else {
                        Text(
                            stringResource(Res.string.status_no_prediction),
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onStartPeriod) {
                        Text(stringResource(Res.string.status_here) + "!")
                    }
                }
            }
        }

        // Predictions
        if (state.predictions.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text("Predictions", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            state.predictions.forEach { prediction ->
                PredictionCard(prediction)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PredictionCard(prediction: PredictedCycle) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "${prediction.predictedStart}",
                style = MaterialTheme.typography.bodyLarge,
            )
            if (prediction.predictedEnd != null) {
                Text(
                    "→ ${prediction.predictedEnd}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
