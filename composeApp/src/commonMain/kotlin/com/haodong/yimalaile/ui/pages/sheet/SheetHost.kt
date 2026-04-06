package com.haodong.yimalaile.ui.pages.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.haodong.yimalaile.ui.pages.sheet.sheets.GenericDatePickerSheet
import com.haodong.yimalaile.ui.pages.sheet.sheets.LogDaySheet
import com.haodong.yimalaile.ui.pages.sheet.sheets.PredictionDetailSheet
import com.haodong.yimalaile.ui.pages.sheet.sheets.RecordDetailSheet

/**
 * Global sheet host — renders the currently active sheet from [SheetViewModel].
 * Place this at the root composable level (App.kt) so sheets overlay everything.
 */
@Composable
fun SheetHost(viewModel: SheetViewModel) {
    val request by viewModel.activeSheet.collectAsState()

    when (val r = request) {
        is SheetRequest.LogDay -> LogDaySheet(
            targetDate = r.targetDate,
            onDismiss = { viewModel.dismiss() },
            onSave = { intensity, mood, symptoms, notes ->
                r.result.complete(LogDayResult(intensity, mood, symptoms, notes))
            },
        )

        is SheetRequest.RecordDetail -> RecordDetailSheet(
            record = r.record,
            allRecords = r.allRecords,
            defaultCycleLength = r.defaultCycleLength,
            service = viewModel.getService(),
            onDismiss = { viewModel.dismiss() },
            onEditStart = { r.result.complete(DetailAction.EditStart) },
            onEditEnd = { r.result.complete(DetailAction.EditEnd) },
            onDelete = { r.result.complete(DetailAction.Delete) },
        )

        is SheetRequest.PredictionDetail -> PredictionDetailSheet(
            prediction = r.prediction,
            avgPeriodLength = r.avgPeriodLength,
            onDismiss = { viewModel.dismiss() },
        )

        is SheetRequest.DatePicker -> GenericDatePickerSheet(
            titleRes = r.titleRes,
            hintRes = r.hintRes,
            hint = r.hint,
            minDate = r.minDate,
            maxDate = r.maxDate,
            defaultDate = r.defaultDate,
            onDismiss = { viewModel.dismiss() },
            onConfirm = { date -> r.result.complete(date) },
        )

        null -> {}
    }
}
