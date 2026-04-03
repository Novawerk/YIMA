package com.haodong.yimalaile.ui.pages.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.haodong.yimalaile.ui.pages.record.BackfillSheet
import com.haodong.yimalaile.ui.pages.record.EndPeriodSheet
import com.haodong.yimalaile.ui.pages.record.LogDaySheet
import com.haodong.yimalaile.ui.pages.record.StartPeriodSheet

/**
 * Global sheet host — renders the currently active sheet from [SheetManager].
 * Place this at the root composable level (App.kt) so sheets overlay everything.
 */
@Composable
fun SheetHost(manager: SheetManager) {
    val request by manager.activeSheet.collectAsState()

    when (val r = request) {
        is SheetRequest.StartPeriod -> StartPeriodSheet(
            existingRecords = r.records,
            onDismiss = { manager.dismiss() },
            onConfirm = { date -> r.result.complete(date) },
        )

        is SheetRequest.EndPeriod -> EndPeriodSheet(
            startDate = r.startDate,
            dailyRecords = r.dailyRecords,
            existingRecords = r.records,
            onDismiss = { manager.dismiss() },
            onConfirm = { date -> r.result.complete(date) },
        )

        is SheetRequest.LogDay -> LogDaySheet(
            targetDate = r.targetDate,
            onDismiss = { manager.dismiss() },
            onSave = { intensity, mood, symptoms, notes ->
                r.result.complete(LogDayResult(intensity, mood, symptoms, notes))
            },
        )

        is SheetRequest.Backfill -> BackfillSheet(
            existingRecords = r.records,
            onDismiss = { manager.dismiss() },
            onSave = { start, end -> r.result.complete(start to end) },
        )

        null -> {} // nothing to show
    }
}
