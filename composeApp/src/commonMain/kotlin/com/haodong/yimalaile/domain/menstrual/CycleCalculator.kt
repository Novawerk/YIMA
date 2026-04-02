package com.haodong.yimalaile.domain.menstrual

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.until
import me.tatarka.inject.annotations.Inject

@Inject
class CycleCalculator {

    fun calculateAverageCycleLength(records: List<MenstrualRecord>): Int? {
        val sorted = records
            .filter { !it.isDeleted }
            .sortedBy { it.startDate }
        if (sorted.size < 2) return null

        val gaps = sorted.zipWithNext().map { (a, b) ->
            a.startDate.until(b.startDate, DateTimeUnit.DAY)
        }
        return gaps.sum() / gaps.size
    }

    fun calculateAveragePeriodLength(records: List<MenstrualRecord>): Int? {
        val lengths = records
            .filter { !it.isDeleted && it.endDate != null }
            .map { it.startDate.until(it.endDate!!, DateTimeUnit.DAY) + 1 }
        if (lengths.isEmpty()) return null
        return lengths.sum() / lengths.size
    }

    fun predictNextPeriod(records: List<MenstrualRecord>): LocalDate? {
        val avgLength = calculateAverageCycleLength(records) ?: return null
        val lastStart = records
            .filter { !it.isDeleted }
            .maxByOrNull { it.startDate }
            ?.startDate ?: return null
        return lastStart.plus(avgLength, DateTimeUnit.DAY)
    }
}
