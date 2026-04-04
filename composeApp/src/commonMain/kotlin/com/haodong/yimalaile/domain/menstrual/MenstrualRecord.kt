package com.haodong.yimalaile.domain.menstrual

import kotlinx.datetime.LocalDate

enum class RecordSource { MANUAL, PREDICTION, AUTO_CONFIRMED }

data class MenstrualRecord(
    val id: String,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val endConfirmed: Boolean = false,
    val dailyRecords: List<DailyRecord> = emptyList(),
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val source: RecordSource = RecordSource.MANUAL,
    val isDeleted: Boolean = false
)
