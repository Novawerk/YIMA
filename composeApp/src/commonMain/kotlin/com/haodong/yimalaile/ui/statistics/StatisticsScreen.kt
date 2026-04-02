package com.haodong.yimalaile.ui.statistics

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.AddRecordResult
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.ui.components.StatusPill
import com.haodong.yimalaile.ui.record.BackfillSheet
import com.haodong.yimalaile.ui.theme.AppColors
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.until
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.record_save_success
import yimalaile.composeapp.generated.resources.stats_empty_message
import yimalaile.composeapp.generated.resources.unit_days

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
    val daysStr = stringResource(Res.string.unit_days)

    LaunchedEffect(Unit) { state = service.getCycleState() }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val s = state
        if (s != null) {
            LazyColumn(
                Modifier.fillMaxSize().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Top bar
                item {
                    Spacer(Modifier.height(40.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(40.dp).clip(CircleShape)
                                .background(AppColors.WarmPeach.copy(alpha = 0.5f)),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AppColors.DeepRose)
                        }
                        Spacer(Modifier.weight(1f))
                        Text("历史记录", style = MaterialTheme.typography.titleLarge, color = AppColors.DarkCoffee)
                        Spacer(Modifier.weight(1f))
                        Box(Modifier.size(40.dp))
                    }
                }

                val allRecords = s.recentPeriods

                if (allRecords.isEmpty() && s.activePeriod == null) {
                    item {
                        Spacer(Modifier.height(80.dp))
                        Text(
                            stringResource(Res.string.stats_empty_message),
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppColors.DarkCoffee.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                } else {
                    // Active period at top
                    if (s.activePeriod != null) {
                        item {
                            RecordCard(s.activePeriod, daysStr, isActive = true)
                        }
                    }

                    // Completed periods
                    if (allRecords.isNotEmpty()) {
                        item {
                            Text(
                                "已完成 (${allRecords.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = AppColors.DarkCoffee,
                            )
                        }
                    }

                    itemsIndexed(allRecords) { index, record ->
                        RecordCard(record, daysStr)
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }

        // Extended FAB with text
        ExtendedFloatingActionButton(
            onClick = { showBackfill = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = AppColors.DeepRose,
            contentColor = AppColors.SoftCream,
            shape = RoundedCornerShape(28.dp),
            icon = { Icon(Icons.Default.Add, null) },
            text = { Text("补录") },
        )

        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
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
private fun RecordCard(record: MenstrualRecord, daysStr: String, isActive: Boolean = false) {
    Box(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isActive) AppColors.WarmPeach.copy(alpha = 0.35f)
                else AppColors.BlushPink.copy(alpha = 0.3f)
            )
            .padding(20.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // Date info
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isActive) {
                        Box(
                            Modifier.size(8.dp).clip(CircleShape)
                                .background(AppColors.DeepRose)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        "${record.startDate.year}/${record.startDate.monthNumber}/${record.startDate.dayOfMonth}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.DarkCoffee,
                    )
                }
                if (record.endDate != null) {
                    Text(
                        "→ ${record.endDate.year}/${record.endDate.monthNumber}/${record.endDate.dayOfMonth}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.DarkCoffee.copy(alpha = 0.4f),
                    )
                }
            }

            // Duration or status
            if (isActive) {
                StatusPill("进行中")
            } else if (record.endDate != null) {
                val days = record.startDate.until(record.endDate, DateTimeUnit.DAY) + 1
                StatusPill("$days $daysStr")
            }
        }
    }
}
