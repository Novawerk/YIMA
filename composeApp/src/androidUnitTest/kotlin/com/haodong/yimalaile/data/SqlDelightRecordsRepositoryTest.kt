package com.haodong.yimalaile.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SqlDelightRecordsRepositoryTest {

    private fun createRepo(): SqlDelightRecordsRepository {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        YimalaileDatabase.Schema.create(driver)
        return SqlDelightRecordsRepository(driver)
    }

    private fun record(id: String, year: Int, month: Int, day: Int) = MenstrualRecord(
        id = id,
        startDate = LocalDateKey(year, month, day),
        createdAtEpochMillis = 0,
        updatedAtEpochMillis = 0,
    )

    // --- getAllRecords ---

    @Test
    fun getAllRecords_returns_empty_initially() = runBlocking {
        // Given / When
        val all = createRepo().getAllRecords()

        // Then
        assertTrue(all.isEmpty())
    }

    // --- insertRecord ---

    @Test
    fun insertRecord_success_and_getAllRecords_returns_it() = runBlocking {
        // Given
        val repo = createRepo()
        val r = record("1", 2025, 1, 1)

        // When
        val result = repo.insertRecord(r)

        // Then
        assertIs<AddRecordResult.Success>(result)
        val all = repo.getAllRecords()
        assertEquals(1, all.size)
        assertEquals(r.startDate, all.first().startDate)
    }

    @Test
    fun insertRecord_persists_all_fields() = runBlocking {
        // Given
        val repo = createRepo()
        val r = MenstrualRecord(
            id = "full",
            startDate = LocalDateKey(2025, 3, 1),
            endDate = LocalDateKey(2025, 3, 5),
            intensity = Intensity.HEAVY,
            mood = Mood.HAPPY,
            symptoms = listOf("cramps", "headache"),
            notes = "test notes",
            createdAtEpochMillis = 1000L,
            updatedAtEpochMillis = 2000L,
        )

        // When
        repo.insertRecord(r)
        val loaded = repo.getAllRecords().first()

        // Then
        assertEquals(r.startDate, loaded.startDate)
        assertEquals(r.endDate, loaded.endDate)
        assertEquals(r.intensity, loaded.intensity)
        assertEquals(r.mood, loaded.mood)
        assertEquals(r.symptoms, loaded.symptoms)
        assertEquals(r.notes, loaded.notes)
    }

    @Test
    fun insertRecord_rejects_duplicate_start_date() = runBlocking {
        // Given
        val repo = createRepo()
        repo.insertRecord(record("1", 2025, 1, 1))

        // When
        val result = repo.insertRecord(record("2", 2025, 1, 1))

        // Then
        assertIs<AddRecordResult.DuplicateStartDate>(result)
        assertEquals(1, repo.getAllRecords().size) // original still there
    }

    @Test
    fun insertRecord_rejects_record_within_15_days() = runBlocking {
        // Given
        val repo = createRepo()
        repo.insertRecord(record("1", 2025, 1, 1))

        // When: 9 days later — too close
        val result = repo.insertRecord(record("2", 2025, 1, 10))

        // Then
        assertIs<AddRecordResult.TooCloseToOtherRecord>(result)
        assertEquals(1, repo.getAllRecords().size)
    }

    @Test
    fun insertRecord_accepts_record_exactly_15_days_apart() = runBlocking {
        // Given
        val repo = createRepo()
        repo.insertRecord(record("1", 2025, 1, 1))

        // When: exactly 15 days later
        val result = repo.insertRecord(record("2", 2025, 1, 16))

        // Then
        assertIs<AddRecordResult.Success>(result)
        assertEquals(2, repo.getAllRecords().size)
    }

    // --- updateRecord ---

    @Test
    fun updateRecord_modifies_existing_record() = runBlocking {
        // Given
        val repo = createRepo()
        val r = record("1", 2025, 1, 1)
        repo.insertRecord(r)

        // When
        val updated = r.copy(endDate = LocalDateKey(2025, 1, 5), intensity = Intensity.MEDIUM)
        val ok = repo.updateRecord(updated)

        // Then
        assertTrue(ok)
        val loaded = repo.getAllRecords().first()
        assertEquals(LocalDateKey(2025, 1, 5), loaded.endDate)
        assertEquals(Intensity.MEDIUM, loaded.intensity)
    }

    @Test
    fun updateRecord_returns_false_for_nonexistent_id() = runBlocking {
        // Given / When
        val result = createRepo().updateRecord(record("ghost", 2025, 1, 1))

        // Then
        assertFalse(result)
    }

    // --- deleteRecord ---

    @Test
    fun deleteRecord_soft_deletes_and_hides_from_getAllRecords() = runBlocking {
        // Given
        val repo = createRepo()
        repo.insertRecord(record("1", 2025, 1, 1))

        // When
        val ok = repo.deleteRecord("1")

        // Then: deleted, and getAllRecords filters it out
        assertTrue(ok)
        assertTrue(repo.getAllRecords().isEmpty())
    }

    @Test
    fun deleteRecord_returns_false_for_nonexistent_id() = runBlocking {
        // Given / When
        val result = createRepo().deleteRecord("ghost")

        // Then
        assertFalse(result)
    }

    @Test
    fun deleted_start_date_can_be_reused_after_deletion() = runBlocking {
        // Given: insert and delete a record
        val repo = createRepo()
        repo.insertRecord(record("1", 2025, 1, 1))
        repo.deleteRecord("1")

        // When: inserting same start date — conflict check ignores soft-deleted rows
        // (selectAll already filters is_deleted=0, so deleted record not counted)
        // Note: insertOrReplace with same id would restore it; use different id
        val result = repo.insertRecord(record("2", 2025, 1, 1))

        // Then: allowed because deleted records are not counted as conflicts
        assertTrue(result is AddRecordResult.Success, "Expected Success but got $result")
    }
}
