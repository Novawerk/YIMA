package com.haodong.yimalaile.data

import app.cash.sqldelight.db.SqlDriver
import com.haodong.yimalaile.DatabaseDriverFactory
import kotlin.math.abs

// ---------- Extended repository interface with suspend functions ----------

interface SuspendRecordsRepository {
    suspend fun insertRecord(record: MenstrualRecord): AddRecordResult
    suspend fun getAllRecords(): List<MenstrualRecord>
    suspend fun updateRecord(record: MenstrualRecord): Boolean
    suspend fun deleteRecord(id: String): Boolean
}

// ---------- SQLDelight-backed implementation ----------

class SqlDelightRecordsRepository(driver: SqlDriver) : SuspendRecordsRepository {

    private val database = YimalaileDatabase(driver)
    private val queries = database.menstrualRecordQueries

    override suspend fun insertRecord(record: MenstrualRecord): AddRecordResult {
        val existing = queries.selectAll().executeAsList()
        val active = existing.filter { it.is_deleted == 0L }

        if (active.any { it.start_date == record.startDate.toString() }) {
            return AddRecordResult.DuplicateStartDate
        }

        val tooClose = active.any {
            abs(daysBetween(LocalDateKey.fromString(it.start_date), record.startDate)) < 15
        }
        if (tooClose) return AddRecordResult.TooCloseToOtherRecord

        queries.insertOrReplace(
            id = record.id,
            start_date = record.startDate.toString(),
            end_date = record.endDate?.toString(),
            intensity = record.intensity?.name,
            mood = record.mood?.name,
            symptoms = record.symptoms.joinToString(","),
            notes = record.notes,
            created_at = record.createdAtEpochMillis,
            updated_at = record.updatedAtEpochMillis,
            source = record.source.name,
            is_deleted = if (record.isDeleted) 1L else 0L
        )
        return AddRecordResult.Success(record)
    }

    override suspend fun getAllRecords(): List<MenstrualRecord> =
        queries.selectAll().executeAsList().map { it.toDomain() }

    override suspend fun updateRecord(record: MenstrualRecord): Boolean {
        queries.selectById(record.id).executeAsOneOrNull() ?: return false
        queries.insertOrReplace(
            id = record.id,
            start_date = record.startDate.toString(),
            end_date = record.endDate?.toString(),
            intensity = record.intensity?.name,
            mood = record.mood?.name,
            symptoms = record.symptoms.joinToString(","),
            notes = record.notes,
            created_at = record.createdAtEpochMillis,
            updated_at = record.updatedAtEpochMillis,
            source = record.source.name,
            is_deleted = if (record.isDeleted) 1L else 0L
        )
        return true
    }

    override suspend fun deleteRecord(id: String): Boolean {
        queries.selectById(id).executeAsOneOrNull() ?: return false
        queries.softDeleteById(
            updated_at = currentEpochMillis(),
            id = id
        )
        return true
    }
}

// ---------- Mapping helpers ----------

private fun com.haodong.yimalaile.data.Menstrual_record.toDomain(): MenstrualRecord =
    MenstrualRecord(
        id = id,
        startDate = LocalDateKey.fromString(start_date),
        endDate = end_date?.let { LocalDateKey.fromString(it) },
        intensity = intensity?.let { Intensity.valueOf(it) },
        mood = mood?.let { Mood.valueOf(it) },
        symptoms = if (symptoms.isBlank()) emptyList() else symptoms.split(","),
        notes = notes,
        createdAtEpochMillis = created_at,
        updatedAtEpochMillis = updated_at,
        source = RecordSource.valueOf(source),
        isDeleted = is_deleted != 0L
    )

// ---------- Database singleton (simple DI) ----------

object AppDatabase {
    private var repository: SuspendRecordsRepository? = null

    fun init(factory: DatabaseDriverFactory) {
        if (repository == null) {
            repository = SqlDelightRecordsRepository(factory.createDriver())
        }
    }

    fun requireRepository(): SuspendRecordsRepository =
        repository ?: error("AppDatabase not initialized. Call AppDatabase.init() first.")
}
