package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.app_name
import yimalaile.composeapp.generated.resources.record_save_success

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

    // Auto-refresh when data changes (e.g. record detail edit from calendar)
    LaunchedEffect(Unit) {
        sheetManager.dataChanged.collect { viewModel.refresh() }
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        when (val s = uiState) {
            is HomeUiState.Loading -> {}
            is HomeUiState.Ready -> {
                val inPeriod = s.cycleState.inPeriod

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
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        // ── Always show calendar ──
                        HomeCalendar(
                            state = s.cycleState,
                            phaseInfo = s.phaseInfo,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }

                    // ── Bottom toolbar ──
                    BottomSection(
                        inPeriod = inPeriod,
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
                        onNavigateStatistics = onNavigateStatistics,
                    )
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
}
