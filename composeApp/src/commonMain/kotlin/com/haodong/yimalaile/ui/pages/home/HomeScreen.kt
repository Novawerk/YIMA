package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.AddRecordResult
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.ui.pages.sheet.SheetManager
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

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

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (val s = uiState) {
            is HomeUiState.Loading -> {}
            is HomeUiState.Ready -> {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .padding(top = 48.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Top bar
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        IconButton(
                            onClick = onNavigateStatistics,
                            modifier = Modifier.size(40.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)),
                        ) {
                            Icon(Icons.Outlined.History, null, tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(
                            onClick = onNavigateSettings,
                            modifier = Modifier.size(40.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)),
                        ) {
                            Icon(Icons.Outlined.Settings, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    if (s.cycleState.activePeriod != null) {
                        InPeriodContent(
                            state = s.cycleState,
                            today = today,
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
                            onLogSpecificDay = { date ->
                                scope.launch {
                                    val ok = sheetManager.logDay(targetDate = date) ?: return@launch
                                    if (ok) { viewModel.refresh(); snackbar.showSnackbar(successMsg) }
                                }
                            },
                        )
                    } else {
                        NotInPeriodContent(
                            state = s.cycleState,
                            today = today,
                            onStartPeriod = {
                                scope.launch {
                                    val result = sheetManager.startPeriod() ?: return@launch
                                    if (result is AddRecordResult.Success) { viewModel.refresh(); snackbar.showSnackbar(successMsg) }
                                }
                            },
                            onBackfill = {
                                scope.launch {
                                    val result = sheetManager.backfillPeriod() ?: return@launch
                                    if (result is AddRecordResult.Success) { viewModel.refresh(); snackbar.showSnackbar(successMsg) }
                                }
                            },
                            onNavigateStatistics = onNavigateStatistics,
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
}
