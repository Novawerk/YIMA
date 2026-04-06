package com.haodong.yimalaile.ui.pages.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.haodong.yimalaile.ui.pages.sheet.sheets.EndPeriodSheet
import com.haodong.yimalaile.ui.pages.sheet.sheets.GenericDatePickerSheet
import com.haodong.yimalaile.ui.pages.sheet.sheets.LogDaySheet
import com.haodong.yimalaile.ui.pages.sheet.sheets.PredictionDetailSheet
import com.haodong.yimalaile.ui.pages.sheet.sheets.RecordDetailSheet
import com.haodong.yimalaile.ui.pages.sheet.sheets.StartPeriodSheet

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
            avgPeriodLength = r.avgPeriodLength,
            onDismiss = { manager.dismiss() },
            onConfirm = { start, end -> r.result.complete(start to end) },
        )

        is SheetRequest.EndPeriod -> EndPeriodSheet(
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

        is SheetRequest.RecordDetail -> RecordDetailSheet(
            record = r.record,
            allRecords = r.allRecords,
            defaultCycleLength = r.defaultCycleLength,
            service = manager.getService(),
            onDismiss = { manager.dismiss() },
            onEditStart = { r.result.complete(DetailAction.EditStart) },
            onEditEnd = { r.result.complete(DetailAction.EditEnd) },
            onDelete = { r.result.complete(DetailAction.Delete) },
        )

        is SheetRequest.PredictionDetail -> PredictionDetailSheet(
            prediction = r.prediction,
            avgPeriodLength = r.avgPeriodLength,
            onDismiss = { manager.dismiss() },
        )

        is SheetRequest.DatePicker -> GenericDatePickerSheet(
            title = r.title,
            hint = r.hint,
            minDate = r.minDate,
            maxDate = r.maxDate,
            defaultDate = r.defaultDate,
            onDismiss = { manager.dismiss() },
            onConfirm = { date -> r.result.complete(date) },
        )

        null -> {}
    }
}
