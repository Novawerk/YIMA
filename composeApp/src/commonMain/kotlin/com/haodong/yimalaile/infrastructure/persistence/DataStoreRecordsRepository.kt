package com.haodong.yimalaile.infrastructure.persistence

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.haodong.yimalaile.domain.common.LocalDateKey
import com.haodong.yimalaile.domain.common.currentEpochMillis
import com.haodong.yimalaile.domain.common.daysBetween
import com.haodong.yimalaile.domain.menstrual.AddRecordResult
import com.haodong.yimalaile.domain.menstrual.DailyRecord
import com.haodong.yimalaile.domain.menstrual.Intensity
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.domain.menstrual.Mood
import com.haodong.yimalaile.domain.menstrual.RecordSource
import com.haodong.yimalaile.domain.menstrual.RecordsRepository
import kotlinx.coroutines.flow.first
import me.tatarka.inject.annotations.Inject
import kotlin.math.abs

private val RECORDS_KEY = stringPreferencesKey("menstrual_records")

@Inject
class DataStoreRecordsRepository(
    private val dataStore: DataStore<Preferences>
) : RecordsRepository {

    override suspend fun insertRecord(record: MenstrualRecord): AddRecordResult {
        val existing = loadRecords()
        val active = existing.filter { !it.isDeleted }

        if (active.any { it.startDate == record.startDate }) {
            return AddRecordResult.DuplicateStartDate
        }

        val tooClose = active.any { abs(daysBetween(it.startDate, record.startDate)) < 15 }
        if (tooClose) return AddRecordResult.TooCloseToOtherRecord

        saveRecords(existing + record)
        return AddRecordResult.Success(record)
    }

    override suspend fun getAllRecords(): List<MenstrualRecord> =
        loadRecords().filter { !it.isDeleted }

    override suspend fun updateRecord(record: MenstrualRecord): Boolean {
        val existing = loadRecords()
        val index = existing.indexOfFirst { it.id == record.id }
        if (index == -1) return false
        val updated = existing.toMutableList()
        updated[index] = record
        saveRecords(updated)
        return true
    }

    override suspend fun deleteRecord(id: String): Boolean {
        val existing = loadRecords()
        val index = existing.indexOfFirst { it.id == id }
        if (index == -1) return false
        val updated = existing.toMutableList()
        updated[index] = updated[index].copy(
            isDeleted = true,
            updatedAtEpochMillis = currentEpochMillis()
        )
        saveRecords(updated)
        return true
    }

    private suspend fun loadRecords(): List<MenstrualRecord> {
        val json = dataStore.data.first()[RECORDS_KEY] ?: return emptyList()
        return json.toRecordList()
    }

    private suspend fun saveRecords(records: List<MenstrualRecord>) {
        dataStore.edit { prefs ->
            prefs[RECORDS_KEY] = records.toJson()
        }
    }
}

// ---------- JSON serialization ----------

private fun List<MenstrualRecord>.toJson(): String {
    return "[" + joinToString(",") { r ->
        val dailyJson = r.dailyRecords.dailyToJson()
        """{"id":"${r.id}","startDate":"${r.startDate}","endDate":"${r.endDate ?: ""}","dailyRecords":$dailyJson,"createdAt":${r.createdAtEpochMillis},"updatedAt":${r.updatedAtEpochMillis},"source":"${r.source.name}","isDeleted":${r.isDeleted}}"""
    } + "]"
}

private fun List<DailyRecord>.dailyToJson(): String {
    return "[" + joinToString(",") { dr ->
        """{"date":"${dr.date}","intensity":"${dr.intensity?.name ?: ""}","mood":"${dr.mood?.name ?: ""}","symptoms":"${dr.symptoms.joinToString("|")}","notes":"${dr.notes ?: ""}"}"""
    } + "]"
}

private fun String.toRecordList(): List<MenstrualRecord> {
    if (this == "[]" || this.isBlank()) return emptyList()
    try {
        val content = removeSurrounding("[", "]")
        if (content.isBlank()) return emptyList()

        // Split top-level records by "},{ but only at the record boundary
        // Records contain nested dailyRecords arrays, so we need bracket-aware splitting
        val records = mutableListOf<String>()
        var depth = 0
        var start = 0
        for (i in content.indices) {
            when (content[i]) {
                '{', '[' -> depth++
                '}', ']' -> depth--
            }
            if (depth == 0 && i > start) {
                records.add(content.substring(start, i + 1))
                start = i + 2 // skip the comma
            }
        }

        return records.map { it.parseRecord() }
    } catch (_: Exception) {
        return emptyList()
    }
}

private fun String.parseRecord(): MenstrualRecord {
    val s = removeSurrounding("{", "}")

    // Extract dailyRecords array first (it contains nested objects)
    val dailyStart = s.indexOf("\"dailyRecords\":[") + "\"dailyRecords\":[".length
    val dailyEnd = findMatchingBracket(s, dailyStart - 1)
    val dailyJson = s.substring(dailyStart, dailyEnd)
    val dailyRecords = dailyJson.parseDailyRecords()

    // Remove dailyRecords section to parse flat fields
    val flat = s.removeRange(s.indexOf("\"dailyRecords\":"), dailyEnd + 1)
        .replace(",,", ",").trim(',')

    val map = flat.split(",").associate { pair ->
        val key = pair.substringAfter("\"").substringBefore("\"")
        val value = pair.substringAfter(":").trim('"')
        key to value
    }

    return MenstrualRecord(
        id = map["id"] ?: "",
        startDate = LocalDateKey.fromString(map["startDate"] ?: ""),
        endDate = map["endDate"]?.takeIf { it.isNotBlank() }?.let { LocalDateKey.fromString(it) },
        dailyRecords = dailyRecords,
        createdAtEpochMillis = map["createdAt"]?.toLongOrNull() ?: 0L,
        updatedAtEpochMillis = map["updatedAt"]?.toLongOrNull() ?: 0L,
        source = map["source"]?.takeIf { it.isNotBlank() }?.let { RecordSource.valueOf(it) } ?: RecordSource.MANUAL,
        isDeleted = map["isDeleted"] == "true"
    )
}

private fun findMatchingBracket(s: String, openIndex: Int): Int {
    var depth = 0
    for (i in openIndex until s.length) {
        when (s[i]) {
            '[' -> depth++
            ']' -> { depth--; if (depth == 0) return i }
        }
    }
    return s.length - 1
}

private fun String.parseDailyRecords(): List<DailyRecord> {
    if (this.isBlank()) return emptyList()
    try {
        return split("},{")
            .map { it.removePrefix("{").removeSuffix("}") }
            .map { entry ->
                val map = entry.split("\",\"").associate {
                    val pair = it.split("\":\"")
                    val key = pair[0].replace("\"", "")
                    val value = if (pair.size > 1) pair[1].replace("\"", "") else ""
                    key to value
                }
                DailyRecord(
                    date = LocalDateKey.fromString(map["date"] ?: ""),
                    intensity = map["intensity"]?.takeIf { it.isNotBlank() }?.let { Intensity.valueOf(it) },
                    mood = map["mood"]?.takeIf { it.isNotBlank() }?.let { Mood.valueOf(it) },
                    symptoms = map["symptoms"]?.takeIf { it.isNotBlank() }?.split("|") ?: emptyList(),
                    notes = map["notes"]?.takeIf { it.isNotBlank() }
                )
            }
    } catch (_: Exception) {
        return emptyList()
    }
}
