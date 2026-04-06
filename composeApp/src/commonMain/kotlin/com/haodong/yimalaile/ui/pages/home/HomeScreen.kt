package com.haodong.yimalaile.ui.pages.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.AddRecordResult
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.ui.components.DecorShape
import com.haodong.yimalaile.ui.components.GrowSpacer
import com.haodong.yimalaile.ui.components.SmallSpacer
import com.haodong.yimalaile.ui.components.SuccessOverlay
import com.haodong.yimalaile.ui.pages.sheet.SheetManager
import com.haodong.yimalaile.ui.theme.expressiveShapes
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.app_name
import yimalaile.composeapp.generated.resources.record_save_success

@Composable
fun HomeScreen(
    service: MenstrualService,
    sheetManager: SheetManager,
    settings: com.haodong.yimalaile.domain.settings.SettingsRepository,
    onNavigateSettings: () -> Unit,
) {
    val viewModel = remember { HomeViewModel(service, settings) }
    val uiState by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val successMsg = stringResource(Res.string.record_save_success)
    var successMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        sheetManager.dataChanged.collect { viewModel.refresh() }
    }

    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        when (val s = uiState) {
            is HomeUiState.Loading -> {}
            is HomeUiState.Ready -> {
                val inPeriod = s.cycleState.inPeriod
                var homeMode by remember { mutableStateOf(HomeMode.CALENDAR) }
                LaunchedEffect(Unit) { homeMode = HomeMode.fromKey(settings.getHomeMode()) }

                Box(Modifier.fillMaxSize()) {
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
                            IconButton(onClick = onNavigateSettings, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Outlined.Settings, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // ── Main area ──
                        AnimatedContent(
                            targetState = homeMode,
                            modifier = Modifier.weight(1f),
                            transitionSpec = {
                                val fromIdx = targetState.ordinal
                                val toIdx = initialState.ordinal
                                val direction = if (fromIdx > toIdx) 1 else -1
                                (fadeIn(tween(300)) + slideIn(tween(300)) { IntOffset(direction * it.width / 6, 0) })
                                    .togetherWith(fadeOut(tween(200)) + slideOut(tween(200)) { IntOffset(-direction * it.width / 6, 0) })
                            },
                            label = "home_mode",
                        ) { mode ->
                            when (mode) {
                                HomeMode.CALENDAR -> {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                    ) {
                                        HomeCalendar(
                                            state = s.cycleState,
                                            phaseInfo = s.phaseInfo,
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                        )
                                    }
                                }
                                HomeMode.DETAIL -> {
                                    DetailCalendarView(
                                        cycleState = s.cycleState,
                                        phaseInfo = s.phaseInfo,
                                        service = service,
                                        onRefresh = { viewModel.refresh() },
                                    )
                                }
                                HomeMode.STATS -> {
                                    HomeStatistics(
                                        cycleState = s.cycleState,
                                        sheetManager = sheetManager,
                                        onRefresh = { viewModel.refresh() },
                                    )
                                }
                            }
                        }
                    }

                    // ── Floating bottom toolbar ──
                    Box(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
                    ) {
                        BottomSection(
                            inPeriod = inPeriod,
                            homeMode = homeMode,
                            onToggleMode = { mode ->
                                homeMode = mode
                                scope.launch { settings.setHomeMode(mode.key) }
                            },
                            onPeriodArrived = {
                                scope.launch {
                                    val result = sheetManager.recordPeriodStart() ?: return@launch
                                    if (result is AddRecordResult.Success) {
                                        viewModel.refresh(); successMessage = successMsg
                                    }
                                }
                            },
                            onPeriodGone = {
                                scope.launch {
                                    val ok = sheetManager.recordPeriodEnd() ?: return@launch
                                    if (ok) { viewModel.refresh(); successMessage = successMsg }
                                }
                            },
                            onBackfill = {
                                scope.launch {
                                    val result = sheetManager.backfillPeriod() ?: return@launch
                                    if (result is AddRecordResult.Success) {
                                        viewModel.refresh(); successMessage = successMsg
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }

        SuccessOverlay(
            message = successMessage,
            onDismiss = { successMessage = null },
        )
    }
}
