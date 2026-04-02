package com.haodong.yimalaile.domain.menstrual

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate

enum class Intensity { LIGHT, MEDIUM, HEAVY }
enum class Mood { HAPPY, NEUTRAL, SAD, VERY_SAD }
enum class RecordSource { MANUAL, PREDICTION }

data class DailyRecord(
    val date: LocalDate,
    val intensity: Intensity? = null,
    val mood: Mood? = null,
    val symptoms: List<String> = emptyList(),
    val notes: String? = null
)

data class MenstrualRecord(
    val id: String,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val dailyRecords: List<DailyRecord> = emptyList(),
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val source: RecordSource = RecordSource.MANUAL,
    val isDeleted: Boolean = false
)

object Ids {
    fun newId(prefix: String): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val random = (0..999).random()
        return "${prefix}_${timestamp}_${random}"
    }
}

sealed class AddRecordResult {
    data class Success(val record: MenstrualRecord) : AddRecordResult()
    object DuplicateStartDate : AddRecordResult()
    object TooCloseToOtherRecord : AddRecordResult()
    data class Error(val message: String) : AddRecordResult()
}
