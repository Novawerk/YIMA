package com.haodong.yimalaile.domain.health

import com.haodong.yimalaile.domain.menstrual.DailyRecord
import com.haodong.yimalaile.domain.menstrual.Intensity
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.domain.menstrual.RecordSource
import com.viktormykhailiv.kmp.health.HealthDataType
import com.viktormykhailiv.kmp.health.HealthManager
import com.viktormykhailiv.kmp.health.readMenstruationFlow
import com.viktormykhailiv.kmp.health.readMenstruationPeriod
import com.viktormykhailiv.kmp.health.records.MenstruationFlowRecord
import com.viktormykhailiv.kmp.health.records.MenstruationPeriodRecord
import com.viktormykhailiv.kmp.health.records.metadata.Metadata
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Inject
import kotlin.random.Random

enum class HealthAuthStatus { NOT_AVAILABLE, NOT_AUTHORIZED, AUTHORIZED }

@Inject
class HealthService(
    private val healthManager: HealthManager,
) {

    private val readTypes = listOf(
        HealthDataType.MenstruationPeriod,
        HealthDataType.MenstruationFlow,
    )
    private val writeTypes = listOf(
        HealthDataType.MenstruationPeriod,
        HealthDataType.MenstruationFlow,
    )

    suspend fun getAuthStatus(): HealthAuthStatus {
        val available = healthManager.isAvailable().getOrNull() ?: false
        if (!available) return HealthAuthStatus.NOT_AVAILABLE
        val authorized = healthManager.isAuthorized(readTypes, writeTypes).getOrNull() ?: false
        return if (authorized) HealthAuthStatus.AUTHORIZED else HealthAuthStatus.NOT_AUTHORIZED
    }

    suspend fun requestAuthorization(): Boolean =
        healthManager.requestAuthorization(readTypes, writeTypes).getOrNull() ?: false

    suspend fun readPeriods(
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<MenstrualRecord> {
        val tz = TimeZone.currentSystemDefault()
        val startInstant = startDate.atStartOfDayIn(tz)
        val endInstant = endDate.atTime(23, 59, 59).toInstant(tz)

        val periods = healthManager.readMenstruationPeriod(startInstant, endInstant)
            .getOrNull().orEmpty()
        val flows = healthManager.readMenstruationFlow(startInstant, endInstant)
            .getOrNull().orEmpty()

        return periods.map { period -> toMenstrualRecord(period, flows, tz) }
    }

    suspend fun writePeriods(records: List<MenstrualRecord>) {
        val tz = TimeZone.currentSystemDefault()
        val periodRecords = records.mapNotNull { record ->
            val end = record.endDate ?: return@mapNotNull null
            MenstruationPeriodRecord(
                startTime = record.startDate.atStartOfDayIn(tz),
                endTime = end.atTime(23, 59, 59).toInstant(tz),
                metadata = Metadata.manualEntry(),
            )
        }
        val flowRecords = records.flatMap { record ->
            record.dailyRecords.mapNotNull { daily ->
                val flow = daily.intensity?.toFlow() ?: return@mapNotNull null
                MenstruationFlowRecord(
                    time = daily.date.atStartOfDayIn(tz),
                    flow = flow,
                    metadata = Metadata.manualEntry(),
                )
            }
        }
        if (periodRecords.isNotEmpty()) {
            healthManager.writeData(periodRecords)
        }
        if (flowRecords.isNotEmpty()) {
            healthManager.writeData(flowRecords)
        }
    }

    private fun toMenstrualRecord(
        period: MenstruationPeriodRecord,
        allFlows: List<MenstruationFlowRecord>,
        tz: TimeZone,
    ): MenstrualRecord {
        val pStart = period.startTime.toLocalDateTime(tz).date
        val pEnd = period.endTime.toLocalDateTime(tz).date
        val now = Clock.System.now().toEpochMilliseconds()

        val dailyRecords = allFlows
            .filter { flow ->
                val flowDate = flow.time.toLocalDateTime(tz).date
                flowDate in pStart..pEnd
            }
            .map { flow ->
                DailyRecord(
                    date = flow.time.toLocalDateTime(tz).date,
                    intensity = flow.flow.toIntensity(),
                )
            }

        return MenstrualRecord(
            id = "health_${now}_${Random.nextInt(10000)}",
            startDate = pStart,
            endDate = pEnd,
            endConfirmed = true,
            dailyRecords = dailyRecords,
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now,
            source = RecordSource.HEALTH_IMPORT,
        )
    }
}

private fun Intensity.toFlow(): MenstruationFlowRecord.Flow = when (this) {
    Intensity.LIGHT -> MenstruationFlowRecord.Flow.Light
    Intensity.MEDIUM -> MenstruationFlowRecord.Flow.Medium
    Intensity.HEAVY -> MenstruationFlowRecord.Flow.Heavy
}

private fun MenstruationFlowRecord.Flow.toIntensity(): Intensity? = when (this) {
    MenstruationFlowRecord.Flow.Light -> Intensity.LIGHT
    MenstruationFlowRecord.Flow.Medium -> Intensity.MEDIUM
    MenstruationFlowRecord.Flow.Heavy -> Intensity.HEAVY
    else -> null
}
