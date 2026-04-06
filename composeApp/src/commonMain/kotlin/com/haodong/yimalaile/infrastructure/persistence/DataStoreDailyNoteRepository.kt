package com.haodong.yimalaile.infrastructure.persistence

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.haodong.yimalaile.domain.menstrual.DailyNote
import com.haodong.yimalaile.domain.menstrual.DailyNoteRepository
import com.haodong.yimalaile.domain.menstrual.Mood
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import me.tatarka.inject.annotations.Inject

private val NOTES_KEY = stringPreferencesKey("daily_notes")

@Inject
class DataStoreDailyNoteRepository(
    private val dataStore: DataStore<Preferences>
) : DailyNoteRepository {

    override suspend fun getNote(date: LocalDate): DailyNote? =
        loadAll().find { it.date == date }

    override suspend fun getAllNotes(): List<DailyNote> = loadAll()

    override suspend fun saveNote(note: DailyNote) {
        val existing = loadAll().toMutableList()
        val idx = existing.indexOfFirst { it.date == note.date }
        if (idx >= 0) existing[idx] = note else existing.add(note)
        save(existing)
    }

    override suspend fun deleteNote(date: LocalDate) {
        save(loadAll().filter { it.date != date })
    }

    private suspend fun loadAll(): List<DailyNote> {
        val json = dataStore.data.first()[NOTES_KEY] ?: return emptyList()
        return json.parseNotes()
    }

    private suspend fun save(notes: List<DailyNote>) {
        dataStore.edit { prefs -> prefs[NOTES_KEY] = notes.toJson() }
    }
}

private fun List<DailyNote>.toJson(): String =
    "[" + joinToString(",") { n ->
        """{"date":"${n.date}","mood":"${n.mood?.name ?: ""}","notes":"${(n.notes ?: "").escapeJson()}"}"""
    } + "]"

private fun String.escapeJson(): String =
    replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")

private fun String.unescapeJson(): String =
    replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\")

private fun String.parseNotes(): List<DailyNote> {
    if (this == "[]" || isBlank()) return emptyList()
    return try {
        removeSurrounding("[", "]")
            .split("},{")
            .map { it.removePrefix("{").removeSuffix("}") }
            .map { entry ->
                val map = mutableMapOf<String, String>()
                // Parse key-value pairs handling escaped quotes
                var i = 0
                while (i < entry.length) {
                    val keyStart = entry.indexOf('"', i)
                    if (keyStart < 0) break
                    val keyEnd = entry.indexOf('"', keyStart + 1)
                    val key = entry.substring(keyStart + 1, keyEnd)
                    val colonIdx = entry.indexOf(':', keyEnd)
                    val valStart = entry.indexOf('"', colonIdx)
                    // Find unescaped closing quote
                    var valEnd = valStart + 1
                    while (valEnd < entry.length) {
                        if (entry[valEnd] == '"' && entry[valEnd - 1] != '\\') break
                        valEnd++
                    }
                    map[key] = entry.substring(valStart + 1, valEnd).unescapeJson()
                    i = valEnd + 1
                }
                DailyNote(
                    date = LocalDate.parse(map["date"] ?: ""),
                    mood = map["mood"]?.takeIf { it.isNotBlank() }?.let { Mood.valueOf(it) },
                    notes = map["notes"]?.takeIf { it.isNotBlank() },
                )
            }
    } catch (_: Exception) {
        emptyList()
    }
}
