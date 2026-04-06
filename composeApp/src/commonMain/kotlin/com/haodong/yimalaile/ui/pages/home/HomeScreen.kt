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
import com.haodong.yimalaile.domain.menstrual.CyclePhaseInfo
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.ui.components.DecorShape
import com.haodong.yimalaile.ui.components.GrowSpacer
import com.haodong.yimalaile.ui.components.SmallSpacer
import com.haodong.yimalaile.ui.components.SuccessOverlay
import com.haodong.yimalaile.ui.pages.sheet.SheetViewModel
import com.haodong.yimalaile.ui.theme.expressiveShapes
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.app_name
import yimalaile.composeapp.generated.resources.record_save_success

@Composable
fun HomeScreen(
    service: MenstrualService,
    sheetViewModel: SheetViewModel,
    settings: com.haodong.yimalaile.domain.settings.SettingsRepository,
    onNavigateSettings: () -> Unit,
) {
    val viewModel = remember { HomeViewModel(service, settings) }
    val cycleState = viewModel.cycleState
    val phaseInfo = viewModel.phaseInfo
    val isLoading = viewModel.isLoading
    val homeMode = viewModel.homeMode
    val scope = rememberCoroutineScope()
    val successMsg = stringResource(Res.string.record_save_success)
    var successMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        sheetViewModel.dataChanged.collect { viewModel.refresh() }
    }

    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        if (!isLoading && cycleState != null) {
            Box(Modifier.fillMaxSize()) {
                Column(Modifier.fillMaxSize()) {
                    HomeAppBar(onNavigateSettings = onNavigateSettings)

                    HomeContent(
                        homeMode = homeMode,
                        cycleState = cycleState,
                        phaseInfo = phaseInfo,
                        service = service,
                        onRefresh = { viewModel.refresh() },
                        sheetViewModel = sheetViewModel,
                        modifier = Modifier.weight(1f)
                    )
                }

                // ── Floating bottom toolbar ──
                Box(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
                ) {
                    BottomSection(
                        inPeriod = cycleState.inPeriod,
                        homeMode = homeMode,
                        onToggleMode = { mode -> viewModel.updateHomeMode(mode) },
                        onPeriodArrived = {
                            scope.launch {
                                val result = sheetViewModel.recordPeriodStart() ?: return@launch
                                if (result is AddRecordResult.Success) {
                                    viewModel.refresh(); successMessage = successMsg
                                }
                            }
                        },
                        onPeriodGone = {
                            scope.launch {
                                val ok = sheetViewModel.recordPeriodEnd() ?: return@launch
                                if (ok) {
                                    viewModel.refresh(); successMessage = successMsg
                                }
                            }
                        },
                    )
                }
            }
        }

        SuccessOverlay(
            message = successMessage,
            onDismiss = { successMessage = null },
        )
    }
}

@Composable
private fun HomeAppBar(
    onNavigateSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
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
}

@Composable
private fun HomeContent(
    homeMode: HomeMode,
    cycleState: CycleState,
    phaseInfo: CyclePhaseInfo?,
    service: MenstrualService,
    onRefresh: () -> Unit,
    sheetViewModel: SheetViewModel,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = homeMode,
        modifier = modifier,
        transitionSpec = {
            val fromIdx = initialState.ordinal
            val toIdx = targetState.ordinal
            val direction = if (toIdx > fromIdx) 1 else -1
            (fadeIn(tween(300)) + slideIn(tween(300)) { IntOffset(direction * it.width / 6, 0) })
                .togetherWith(fadeOut(tween(200)) + slideOut(tween(200)) {
                    IntOffset(-direction * it.width / 6, 0)
                })
        },
        label = "home_mode",
    ) { mode ->
        when (mode) {
            HomeMode.CALENDAR -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    HomeCalendar(
                        state = cycleState,
                        phaseInfo = phaseInfo,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
            HomeMode.DETAIL -> {
                DetailCalendarView(
                    cycleState = cycleState,
                    phaseInfo = phaseInfo,
                    service = service,
                    onRefresh = onRefresh,
                )
            }
            HomeMode.STATS -> {
                HomeStatistics(
                    cycleState = cycleState,
                    sheetViewModel = sheetViewModel,
                    onRefresh = onRefresh,
                )
            }
        }
    }
}
