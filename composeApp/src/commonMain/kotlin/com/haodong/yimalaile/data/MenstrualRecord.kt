package com.haodong.yimalaile.data

import kotlin.math.abs

enum class Intensity { LIGHT, MEDIUM, HEAVY }
enum class Mood { HAPPY, NEUTRAL, SAD, VERY_SAD }
enum class RecordSource { MANUAL, PREDICTION }

data class MenstrualRecord(
    val id: String,
    val startDate: LocalDateKey,
    val endDate: LocalDateKey? = null,
    val intensity: Intensity? = null,
    val mood: Mood? = null,
    val symptoms: List<String> = emptyList(),
    val notes: String? = null,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val source: RecordSource = RecordSource.MANUAL,
    val isDeleted: Boolean = false
) {
    fun asRange(): DateRange = DateRange(startDate, endDate ?: startDate)
}

object Ids {
    fun newId(prefix: String): String {
        val timestamp = currentEpochMillis()
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

interface RecordsRepository {
    fun add(record: MenstrualRecord): AddRecordResult
    fun getAll(): List<MenstrualRecord>
}

class InMemoryRecordsRepository : RecordsRepository {
    private val records = mutableListOf<MenstrualRecord>()

    override fun add(record: MenstrualRecord): AddRecordResult {
        if (records.any { it.startDate == record.startDate && !it.isDeleted }) {
            return AddRecordResult.DuplicateStartDate
        }
        
        // Check for minimum interval (15 days)
        val tooClose = records.any { !it.isDeleted && abs(daysBetween(it.startDate, record.startDate)) < 15 }
        if (tooClose) {
            return AddRecordResult.TooCloseToOtherRecord
        }
        
        records.add(record)
        return AddRecordResult.Success(record)
    }

    override fun getAll(): List<MenstrualRecord> = records.filter { !it.isDeleted }
}
