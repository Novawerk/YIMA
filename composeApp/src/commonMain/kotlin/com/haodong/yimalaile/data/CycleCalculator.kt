package com.haodong.yimalaile.data

/**
 * Pure functions for cycle length calculation and next-period prediction.
 * All calculations use the existing LocalDateKey math (daysBetween, plusDays).
 */
class CycleCalculator {

    /**
     * Calculates the average cycle length in days from consecutive period start dates.
     *
     * Algorithm: sort non-deleted records by start date, compute gaps between each
     * adjacent pair, return the mean. Requires ≥ 2 records; returns null otherwise.
     */
    fun calculateAverageCycleLength(records: List<MenstrualRecord>): Int? {
        val sorted = records
            .filter { !it.isDeleted }
            .sortedBy { it.startDate }
        if (sorted.size < 2) return null

        val gaps = sorted.zipWithNext().map { (a, b) ->
            daysBetween(a.startDate, b.startDate)
        }
        return gaps.sum() / gaps.size
    }

    /**
     * Calculates the average period length in days from records that have a non-null endDate.
     * Each period's length = daysBetween(startDate, endDate) + 1 (inclusive).
     *
     * Returns null if no records have an endDate.
     */
    fun calculateAveragePeriodLength(records: List<MenstrualRecord>): Int? {
        val lengths = records
            .filter { !it.isDeleted && it.endDate != null }
            .map { daysBetween(it.startDate, it.endDate!!) + 1 }
        if (lengths.isEmpty()) return null
        return lengths.sum() / lengths.size
    }

    /**
     * Returns a list of cycle lengths (days between consecutive start dates), oldest first.
     * Requires ≥ 2 records; returns empty list otherwise.
     */
    fun getCycleLengths(records: List<MenstrualRecord>): List<Int> {
        val sorted = records
            .filter { !it.isDeleted }
            .sortedBy { it.startDate }
        if (sorted.size < 2) return emptyList()
        return sorted.zipWithNext().map { (a, b) ->
            daysBetween(a.startDate, b.startDate)
        }
    }

    /**
     * Returns a list of period durations (inclusive days) for records with a non-null endDate,
     * in the same order as the input.
     */
    fun getPeriodLengths(records: List<MenstrualRecord>): List<Int> {
        return records
            .filter { !it.isDeleted && it.endDate != null }
            .sortedBy { it.startDate }
            .map { daysBetween(it.startDate, it.endDate!!) + 1 }
    }

    /**
     * Predicts the next period start date as: lastStart + averageCycleLength.
     *
     * Returns null if there are fewer than 2 records (cannot compute average).
     */
    fun predictNextPeriod(records: List<MenstrualRecord>): LocalDateKey? {
        val avgLength = calculateAverageCycleLength(records) ?: return null
        val lastStart = records
            .filter { !it.isDeleted }
            .maxByOrNull { it.startDate }
            ?.startDate ?: return null
        return lastStart.plusDays(avgLength)
    }
}
