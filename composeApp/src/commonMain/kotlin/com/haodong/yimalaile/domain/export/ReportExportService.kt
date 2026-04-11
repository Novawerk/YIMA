package com.haodong.yimalaile.domain.export

import com.haodong.yimalaile.domain.menstrual.MenstrualRecord

/**
 * Renders the user's menstrual history into a single long PNG image
 * (the "long screenshot" that works well for sharing on WeChat /
 * social apps) and saves it into a user-accessible gallery folder.
 *
 * Platform implementations are provided via
 * [com.haodong.yimalaile.di.AppComponent].
 */
interface ReportExportService {
    /**
     * @param records All records to include in the report. Callers are
     *   expected to filter out deleted records and sort them by date.
     * @param language ISO code for the report body language.
     *   "zh" for Chinese, anything else defaults to English.
     */
    suspend fun exportCycleReport(
        records: List<MenstrualRecord>,
        language: String,
    ): ExportResult
}

sealed class ExportResult {
    /** Success. [displayLocation] is a human-readable location. */
    data class Success(val displayLocation: String) : ExportResult()

    /** Failure. [message] is a developer hint, not user-facing copy. */
    data class Failure(val message: String) : ExportResult()
}
