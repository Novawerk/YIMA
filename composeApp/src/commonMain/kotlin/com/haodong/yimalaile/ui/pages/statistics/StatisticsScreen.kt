package com.haodong.yimalaile.ui.pages.statistics

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
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.theme.expressiveShapes
import com.haodong.yimalaile.ui.components.SmallSpacer
import com.haodong.yimalaile.ui.pages.sheet.DetailAction
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
    val allRecords = s?.let {
        (listOfNotNull(it.activePeriod) + it.recentPeriods)
            .distinctBy { r -> r.id }
            .sortedByDescending { r -> r.startDate }
    } ?: emptyList()

    Column(
        Modifier.fillMaxSize()
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
                    val isActive = record.endDate == null
                    RecordCard(
                        record = record,
                        daysStr = daysStr,
                        onClick = {
                            scope.launch {
                                val action = sheetManager.showRecordDetail(record, isActive) ?: return@launch
                                when (action) {
                                    is DetailAction.EditStart -> { sheetManager.startPeriod(); refresh() }
                                    is DetailAction.EditEnd -> {
                                        val ok = sheetManager.endPeriod() ?: return@launch
                                        if (ok) { refresh(); snackbar.showSnackbar(successMsg) }
                                    }
                                    is DetailAction.LogDay -> {
                                        val ok = sheetManager.logDay() ?: return@launch
                                        if (ok) { refresh(); snackbar.showSnackbar(successMsg) }
                                    }
                                    is DetailAction.Delete -> {
                                        service.deleteRecord(record.id)
                                        refresh()
                                        snackbar.showSnackbar(deletedMsg)
                                    }
                                }
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
    val isActive = record.endDate == null

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
            com.haodong.yimalaile.ui.components.DecorShape(
                size = 20,
                shape = if (isActive) MaterialTheme.expressiveShapes.heart
                        else MaterialTheme.expressiveShapes.cookie4,
                color = if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
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
                if (record.dailyRecords.isNotEmpty()) {
                    SmallSpacer(4)
                    Text(
                        stringResource(Res.string.history_n_daily_records, record.dailyRecords.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (isActive) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        stringResource(Res.string.history_in_progress),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            } else if (days != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$days", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(daysStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
