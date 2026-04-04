package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    onNavigateStatistics: () -> Unit,
    onNavigateSettings: () -> Unit,
) {
    val viewModel = remember { HomeViewModel(service) }
    val uiState by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val successMsg = stringResource(Res.string.record_save_success)

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        when (val s = uiState) {
            is HomeUiState.Loading -> {}
            is HomeUiState.Ready -> {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                var showPhaseSheet by remember { mutableStateOf(false) }
                var showCalendarSheet by remember { mutableStateOf(false) }
                val inPeriod = s.cycleState.activePeriod != null
                val dayCount = if (inPeriod) {
                    s.cycleState.activePeriod.startDate.until(today, DateTimeUnit.DAY).toInt() + 1
                } else null
                val heroNumber = if (inPeriod) dayCount ?: 0 else s.phaseInfo?.daysUntilNextPeriod ?: 0

                var calendarMode by remember { mutableStateOf(true) }

                Column(Modifier.fillMaxSize()) {
                    // ── App Bar ──
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DecorShape(24, shape = MaterialTheme.expressiveShapes.cookie7, color = MaterialTheme.colorScheme.primary)
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

                    // ── Main area: Calendar or Hero ──
                    if (calendarMode) {
                        // Calendar replaces hero
                        Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                            com.haodong.yimalaile.ui.components.CycleCalendarLegend()
                            SmallSpacer(8)
                            com.haodong.yimalaile.ui.components.CycleCalendarGrid(
                                state = s.cycleState,
                                phaseInfo = s.phaseInfo,
                                modifier = Modifier.weight(1f),
                                monthRange = -2..2,
                            )
                        }
                    } else {
                        SmallSpacer(24)
                        HeroSection(
                            inPeriod = inPeriod,
                            heroNumber = heroNumber,
                            dayCount = dayCount,
                            phaseInfo = s.phaseInfo,
                            onPhaseClick = { showPhaseSheet = true },
                            onCalendarClick = { calendarMode = true },
                        )
                    }

                    // ── Bottom: phase chip + toggle + CTA ──
                    BottomSection(
                        state = s.cycleState,
                        phaseInfo = s.phaseInfo,
                        today = today,
                        inPeriod = inPeriod,
                        calendarMode = calendarMode,
                        onToggleMode = { calendarMode = it },
                        onStartPeriod = {
                            scope.launch {
                                val result = sheetManager.startPeriod() ?: return@launch
                                if (result is AddRecordResult.Success) { viewModel.refresh(); snackbar.showSnackbar(successMsg) }
                            }
                        },
                        onEndPeriod = {
                            scope.launch {
                                val ok = sheetManager.endPeriod() ?: return@launch
                                if (ok) { viewModel.refresh(); snackbar.showSnackbar(successMsg) }
                            }
                        },
                        onLogDay = {
                            scope.launch {
                                val ok = sheetManager.logDay() ?: return@launch
                                if (ok) { viewModel.refresh(); snackbar.showSnackbar(successMsg) }
                            }
                        },
                        onBackfill = {
                            scope.launch {
                                val result = sheetManager.backfillPeriod() ?: return@launch
                                if (result is AddRecordResult.Success) { viewModel.refresh(); snackbar.showSnackbar(successMsg) }
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
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
}
