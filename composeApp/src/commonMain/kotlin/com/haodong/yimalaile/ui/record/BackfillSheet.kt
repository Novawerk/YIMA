package com.haodong.yimalaile.ui.record

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.components.RangeCalendar
import com.haodong.yimalaile.ui.theme.AppColors
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackfillSheet(
    existingRecords: List<MenstrualRecord> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (start: LocalDate, end: LocalDate) -> Unit,
) {
    var selectedStart by remember { mutableStateOf<LocalDate?>(null) }
    var selectedEnd by remember { mutableStateOf<LocalDate?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.backfill_title)) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, stringResource(Res.string.dialog_cancel))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(Res.string.backfill_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (selectedStart != null && selectedEnd == null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(Res.string.backfill_selected_start, selectedStart!!.monthNumber, selectedStart!!.dayOfMonth),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else if (selectedStart != null && selectedEnd != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(Res.string.backfill_selected_range, selectedStart!!.monthNumber, selectedStart!!.dayOfMonth, selectedEnd!!.monthNumber, selectedEnd!!.dayOfMonth),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(12.dp))
            RangeCalendar(
                existingRecords = existingRecords,
                selectedStart = selectedStart,
                selectedEnd = selectedEnd,
                onDateClick = { date ->
                    if (selectedStart == null || selectedEnd != null) {
                        selectedStart = date
                        selectedEnd = null
                    } else {
                        if (date < selectedStart!!) {
                            selectedEnd = selectedStart
                            selectedStart = date
                        } else {
                            selectedEnd = date
                        }
                    }
                },
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.height(8.dp))
            PrimaryCta(
                text = stringResource(Res.string.record_save_btn),
                onClick = { onSave(selectedStart!!, selectedEnd!!) },
                enabled = selectedStart != null && selectedEnd != null,
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}
