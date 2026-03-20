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
