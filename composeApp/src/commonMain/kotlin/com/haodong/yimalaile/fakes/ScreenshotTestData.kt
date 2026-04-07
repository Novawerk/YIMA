package com.haodong.yimalaile.fakes

import com.haodong.yimalaile.domain.menstrual.*
import kotlinx.datetime.*

/**
 * In-memory [RecordsRepository] for screenshot testing.
 */
class FakeRecordsRepository(
    private val records: MutableList<MenstrualRecord> = mutableListOf(),
) : RecordsRepository {

    override suspend fun insertRecord(record: MenstrualRecord): AddRecordResult {
        records.add(record)
        return AddRecordResult.Success(record)
    }

    override suspend fun getAllRecords(): List<MenstrualRecord> = records.toList()

    override suspend fun updateRecord(record: MenstrualRecord): Boolean {
        val idx = records.indexOfFirst { it.id == record.id }
        if (idx == -1) return false
        records[idx] = record
        return true
    }

    override suspend fun deleteRecord(id: String): Boolean {
        return records.removeAll { it.id == id }
    }

    override suspend fun clearAll() {
        records.clear()
    }
}

/**
 * Creates beautiful, realistic test data for screenshot tests.
 * Varied cycle lengths (26-33 days) and period durations (4-7 days)
 * produce visually distinct charts.
 */
fun createScreenshotTestData(): List<MenstrualRecord> {
    val now = 1_712_400_000_000L

    // Varied cycle lengths: 26, 31, 28, 33, 27, 30 days
    // Varied period durations: 4, 6, 5, 7, 4, 5, 6 days

    val r1Start = LocalDate(2025, 10, 5)
    val r1End = LocalDate(2025, 10, 8)

    val r2Start = LocalDate(2025, 10, 31)
    val r2End = LocalDate(2025, 11, 5)

    val r3Start = LocalDate(2025, 11, 28)
    val r3End = LocalDate(2025, 12, 2)

    val r4Start = LocalDate(2025, 12, 31)
    val r4End = LocalDate(2026, 1, 6)

    val r5Start = LocalDate(2026, 1, 27)
    val r5End = LocalDate(2026, 1, 30)

    val r6Start = LocalDate(2026, 2, 26)
    val r6End = LocalDate(2026, 3, 2)

    val r7Start = LocalDate(2026, 3, 27)
    val r7End = LocalDate(2026, 4, 1)

    return listOf(
        createRecord("r1", r1Start, r1End, now, periodPattern = 0),
        createRecord("r2", r2Start, r2End, now, periodPattern = 1),
        createRecord("r3", r3Start, r3End, now, periodPattern = 2),
        createRecord("r4", r4Start, r4End, now, periodPattern = 0),
        createRecord("r5", r5Start, r5End, now, periodPattern = 1),
        createRecord("r6", r6Start, r6End, now, periodPattern = 2),
        createRecord("r7", r7Start, r7End, now, periodPattern = 0),
    )
}

private fun createRecord(
    id: String,
    start: LocalDate,
    end: LocalDate,
    baseTime: Long,
    periodPattern: Int,
): MenstrualRecord {
    val days = start.until(end, DateTimeUnit.DAY).toInt() + 1
    val dailyRecords = (0 until days).map { dayOffset ->
        val date = start.plus(dayOffset, DateTimeUnit.DAY)
        val (intensity, mood) = when (periodPattern) {
            0 -> intensityMoodPattern1(dayOffset, days)
            1 -> intensityMoodPattern2(dayOffset, days)
            else -> intensityMoodPattern3(dayOffset, days)
        }
        DailyRecord(date = date, intensity = intensity, mood = mood)
    }
    return MenstrualRecord(
        id = id,
        startDate = start,
        endDate = end,
        endConfirmed = true,
        dailyRecords = dailyRecords,
        createdAtEpochMillis = baseTime,
        updatedAtEpochMillis = baseTime,
        source = RecordSource.MANUAL,
    )
}

private fun intensityMoodPattern1(dayOffset: Int, totalDays: Int): Pair<Intensity, Mood> {
    val intensity = when {
        dayOffset == 0 -> Intensity.LIGHT
        dayOffset <= totalDays / 3 -> Intensity.HEAVY
        dayOffset <= totalDays * 2 / 3 -> Intensity.MEDIUM
        else -> Intensity.LIGHT
    }
    val mood = when {
        dayOffset == 0 -> Mood.NEUTRAL
        dayOffset <= 1 -> Mood.SAD
        dayOffset <= totalDays / 2 -> Mood.VERY_SAD
        else -> Mood.NEUTRAL
    }
    return intensity to mood
}

private fun intensityMoodPattern2(dayOffset: Int, totalDays: Int): Pair<Intensity, Mood> {
    val intensity = when {
        dayOffset <= 1 -> Intensity.MEDIUM
        dayOffset <= totalDays / 2 -> Intensity.HEAVY
        else -> Intensity.LIGHT
    }
    val mood = when {
        dayOffset == 0 -> Mood.HAPPY
        dayOffset <= 2 -> Mood.NEUTRAL
        else -> Mood.HAPPY
    }
    return intensity to mood
}

private fun intensityMoodPattern3(dayOffset: Int, totalDays: Int): Pair<Intensity, Mood> {
    val intensity = when {
        dayOffset <= 1 -> Intensity.HEAVY
        dayOffset <= totalDays / 2 -> Intensity.MEDIUM
        else -> Intensity.LIGHT
    }
    val mood = when {
        dayOffset <= 1 -> Mood.SAD
        dayOffset == totalDays - 1 -> Mood.HAPPY
        else -> Mood.NEUTRAL
    }
    return intensity to mood
}
