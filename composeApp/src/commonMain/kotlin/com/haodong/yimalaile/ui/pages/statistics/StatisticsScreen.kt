package com.haodong.yimalaile.ui.pages.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.AddRecordResult
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.ui.components.DecorShape
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.theme.expressiveShapes
import com.haodong.yimalaile.ui.components.SmallSpacer
import com.haodong.yimalaile.ui.pages.sheet.SheetManager
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.until
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

@Composable
fun StatisticsScreen(
    service: MenstrualService,
    sheetManager: SheetManager,
    onBack: () -> Unit,
) {
    var state by remember { mutableStateOf<CycleState?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val successMsg = stringResource(Res.string.record_save_success)
    val deletedMsg = stringResource(Res.string.record_deleted)
    val daysStr = stringResource(Res.string.unit_days)

    fun refresh() { scope.launch { state = service.getCycleState() } }
    LaunchedEffect(Unit) { state = service.getCycleState() }

    val s = state
    val allRecords = s?.records ?: emptyList()

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(horizontal = 20.dp),
    ) {
        // ── App Bar ──
        SmallSpacer(8)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(Res.string.history_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(40.dp))
        }
        SmallSpacer(8)

        // ── Record list ──
        if (allRecords.isEmpty()) {
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(Res.string.stats_empty_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.weight(1f))
        } else {
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(allRecords, key = { it.id }) { record ->
                    RecordCard(
                        record = record,
                        daysStr = daysStr,
                        onClick = {
                            scope.launch {
                                sheetManager.showAndHandleRecordDetail(record)
                                refresh()
                            }
                        },
                    )
                }
            }
        }

        // ── Bottom CTA ──
        SmallSpacer(8)
        PrimaryCta(
            text = "✦ ${stringResource(Res.string.history_backfill)}",
            onClick = {
                scope.launch {
                    val result = sheetManager.backfillPeriod() ?: return@launch
                    if (result is AddRecordResult.Success) { refresh(); snackbar.showSnackbar(successMsg) }
                }
            },
        )
        SmallSpacer(16)

        SnackbarHost(snackbar)
    }
}

@Composable
private fun RecordCard(record: MenstrualRecord, daysStr: String, onClick: () -> Unit) {
    val days = record.endDate?.let { record.startDate.until(it, DateTimeUnit.DAY).toInt() + 1 }

    Surface(
        onClick = onClick,
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DecorShape(
                size = 20,
                shape = MaterialTheme.expressiveShapes.bun,
                color = MaterialTheme.colorScheme.tertiary,
            )
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${record.startDate.monthNumber}/${record.startDate.dayOfMonth}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    if (record.endDate != null) {
                        Text(
                            " — ${record.endDate.monthNumber}/${record.endDate.dayOfMonth}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (days != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (record.dailyRecords.isNotEmpty()) {
                        SmallSpacer(4)
                        Text(
                            stringResource(Res.string.history_n_daily_records, record.dailyRecords.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        SmallSpacer(8)
                    }
                    Text("$days", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    SmallSpacer(4)
                    Text(daysStr, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
