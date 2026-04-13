package com.haodong.yimalaile.domain.health

import com.haodong.yimalaile.domain.menstrual.RecordSource
import com.haodong.yimalaile.domain.menstrual.RecordsRepository
import com.haodong.yimalaile.domain.settings.SettingsRepository
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Inject

data class SyncResult(val imported: Int, val exported: Int)

@Inject
class HealthSyncManager(
    private val healthService: HealthService,
    private val recordsRepository: RecordsRepository,
    private val settingsRepository: SettingsRepository,
) {

    suspend fun getAuthStatus(): HealthAuthStatus = healthService.getAuthStatus()

    suspend fun requestAuthorization(): Boolean = healthService.requestAuthorization()

    suspend fun sync(): SyncResult {
        val tz = TimeZone.currentSystemDefault()
        val now = kotlin.time.Clock.System.now()
        val today = now.toLocalDateTime(tz).date

        val lastSyncMillis = settingsRepository.getHealthLastSync()
        val startDate = if (lastSyncMillis > 0L) {
            Instant.fromEpochMilliseconds(lastSyncMillis).toLocalDateTime(tz).date
        } else {
            today.minus(1, DateTimeUnit.YEAR)
        }

        val imported = importFromHealth(startDate, today)
        val exported = exportToHealth()

        settingsRepository.setHealthLastSync(now.toEpochMilliseconds())
        return SyncResult(imported = imported, exported = exported)
    }

    private suspend fun importFromHealth(startDate: LocalDate, endDate: LocalDate): Int {
        val healthRecords = healthService.readPeriods(startDate, endDate)
        if (healthRecords.isEmpty()) return 0

        val existingRecords = recordsRepository.getAllRecords()
        var importedCount = 0

        for (record in healthRecords) {
            val overlaps = existingRecords.any { existing ->
                val existingEnd = existing.endDate ?: existing.startDate
                val recordEnd = record.endDate ?: record.startDate
                existing.startDate <= recordEnd && record.startDate <= existingEnd
            }
            if (!overlaps) {
                recordsRepository.insertRecord(record)
                importedCount++
            }
        }
        return importedCount
    }

    private suspend fun exportToHealth(): Int {
        val records = recordsRepository.getAllRecords()
            .filter { it.source != RecordSource.HEALTH_IMPORT && it.endDate != null }
        if (records.isEmpty()) return 0
        healthService.writePeriods(records)
        return records.size
    }
}
