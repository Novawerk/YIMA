package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.AddRecordResult
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.ui.components.DecorShape
import com.haodong.yimalaile.ui.components.GrowSpacer
import com.haodong.yimalaile.ui.components.SmallSpacer
import com.haodong.yimalaile.ui.pages.sheet.SheetManager
import com.haodong.yimalaile.ui.theme.expressiveShapes
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.until
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.app_name
import yimalaile.composeapp.generated.resources.record_save_success
import kotlin.time.Clock

@Composable
fun HomeScreen(
    service: MenstrualService,
    sheetManager: SheetManager,
    settings: com.haodong.yimalaile.domain.settings.SettingsRepository,
    onNavigateStatistics: () -> Unit,
    onNavigateSettings: () -> Unit,
) {
    val viewModel = remember { HomeViewModel(service) }
    val uiState by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val successMsg = stringResource(Res.string.record_save_success)

    // Auto-refresh when data changes (e.g. record detail edit from calendar)
    LaunchedEffect(Unit) {
        sheetManager.dataChanged.collect { viewModel.refresh() }
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        when (val s = uiState) {
            is HomeUiState.Loading -> {}
            is HomeUiState.Ready -> {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                var showPhaseSheet by remember { mutableStateOf(false) }
                var showCalendarSheet by remember { mutableStateOf(false) }
                val inPeriod = s.cycleState.inPeriod
                val periodStart = s.cycleState.currentPeriod?.startDate
                    ?: if (s.cycleState.inPredictedPeriod) s.cycleState.predictions.firstOrNull { pred ->
                        val avgP = s.phaseInfo?.periodLength ?: 5
                        val pEnd = pred.predictedEnd ?: pred.predictedStart.plus(avgP - 1, DateTimeUnit.DAY)
                        today in pred.predictedStart..pEnd
                    }?.predictedStart else null
                val dayCount = periodStart?.let {
                    it.until(today, DateTimeUnit.DAY).toInt() + 1
                }
                val heroNumber = if (inPeriod) dayCount ?: 0 else s.phaseInfo?.daysUntilNextPeriod ?: 0

                var calendarMode by remember { mutableStateOf(true) }
                LaunchedEffect(Unit) { calendarMode = settings.getHomeMode() == "calendar" }

                Column(Modifier.fillMaxSize()) {
                    // ── App Bar ──
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DecorShape(
                            24,
                            shape = MaterialTheme.expressiveShapes.cookie7,
                            color = MaterialTheme.colorScheme.primary
                        )
                        SmallSpacer(8)
                        Text(
                            stringResource(Res.string.app_name),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        GrowSpacer()
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = onNavigateStatistics, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Outlined.History, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = onNavigateSettings, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Outlined.Settings, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        // ── Main area ──
                        if (calendarMode) {
                            HomeCalendar(
                                state = s.cycleState,
                                phaseInfo = s.phaseInfo,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        } else {
                            SmallSpacer(24)
                            HomeStatistics(
                                inPeriod = inPeriod,
                                heroNumber = heroNumber,
                                dayCount = dayCount,
                                phaseInfo = s.phaseInfo,
                                onPhaseClick = { showPhaseSheet = true },
                            ) { calendarMode = true }
                        }
                    }


                    // ── Bottom toolbar ──
                    BottomSection(
                        inPeriod = inPeriod,
                        calendarMode = calendarMode,
                        onToggleMode = { mode ->
                            calendarMode = mode
                            scope.launch { settings.setHomeMode(if (mode) "calendar" else "stats") }
                        },
                        onPeriodArrived = {
                            scope.launch {
                                val result = sheetManager.recordPeriodStart() ?: return@launch
                                if (result is AddRecordResult.Success) {
                                    viewModel.refresh(); snackbar.showSnackbar(successMsg)
                                }
                            }
                        },
                        onPeriodGone = {
                            scope.launch {
                                val ok = sheetManager.recordPeriodEnd() ?: return@launch
                                if (ok) {
                                    viewModel.refresh(); snackbar.showSnackbar(successMsg)
                                }
                            }
                        },
                    )
                }

                // ── Sheets ──
                if (showPhaseSheet && s.phaseInfo != null) {
                    PhaseExplanationSheet(
                        phaseInfo = s.phaseInfo,
                        onDismiss = { showPhaseSheet = false },
                    )
                }
                if (showCalendarSheet) {
                    CycleCalendarSheet(
                        state = s.cycleState,
                        phaseInfo = s.phaseInfo,
                        onDismiss = { showCalendarSheet = false },
                    )
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
}
