package com.haodong.yimalaile.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CycleCalculatorTest {

    private val calc = CycleCalculator()

    private fun record(id: String, year: Int, month: Int, day: Int) = MenstrualRecord(
        id = id,
        startDate = LocalDateKey(year, month, day),
        createdAtEpochMillis = 0,
        updatedAtEpochMillis = 0,
    )

    private fun recordWithEnd(
        id: String,
        startYear: Int, startMonth: Int, startDay: Int,
        endYear: Int, endMonth: Int, endDay: Int
    ) = MenstrualRecord(
        id = id,
        startDate = LocalDateKey(startYear, startMonth, startDay),
        endDate = LocalDateKey(endYear, endMonth, endDay),
        createdAtEpochMillis = 0,
        updatedAtEpochMillis = 0,
    )

    @Test
    fun returns_null_with_fewer_than_two_records() {
        assertNull(calc.calculateAverageCycleLength(emptyList()))
        assertNull(calc.calculateAverageCycleLength(listOf(record("1", 2025, 1, 1))))
    }

    @Test
    fun calculates_average_of_two_consecutive_cycles() {
        // Jan 1 → Feb 1 = 31 days
        val records = listOf(
            record("1", 2025, 1, 1),
            record("2", 2025, 2, 1),
        )
        assertEquals(31, calc.calculateAverageCycleLength(records))
    }

    @Test
    fun calculates_average_across_three_cycles() {
        // Jan 1 → Feb 1 = 31d, Feb 1 → Mar 4 = 31d … average = 31
        val records = listOf(
            record("1", 2025, 1, 1),
            record("2", 2025, 2, 1),  // +31
            record("3", 2025, 3, 4),  // +31
        )
        assertEquals(31, calc.calculateAverageCycleLength(records))
    }

    @Test
    fun ignores_deleted_records() {
        val deleted = record("del", 2025, 1, 15).copy(isDeleted = true)
        val records = listOf(
            record("1", 2025, 1, 1),
            deleted,
            record("2", 2025, 2, 1),
        )
        assertEquals(31, calc.calculateAverageCycleLength(records))
    }

    @Test
    fun predict_next_period_returns_null_for_single_record() {
        assertNull(calc.predictNextPeriod(listOf(record("1", 2025, 1, 1))))
    }

    // --- calculateAveragePeriodLength ---

    @Test
    fun period_length_returns_null_when_no_records_have_end_date() {
        assertNull(calc.calculateAveragePeriodLength(emptyList()))
        assertNull(calc.calculateAveragePeriodLength(listOf(record("1", 2025, 1, 1))))
    }

    @Test
    fun period_length_counts_inclusive_days_for_single_record() {
        // Jan 1 – Jan 5 = 5 days inclusive
        val records = listOf(recordWithEnd("1", 2025, 1, 1, 2025, 1, 5))
        assertEquals(5, calc.calculateAveragePeriodLength(records))
    }

    @Test
    fun period_length_averages_multiple_records() {
        // 5 days + 7 days = average 6
        val records = listOf(
            recordWithEnd("1", 2025, 1, 1, 2025, 1, 5),  // 5 days
            recordWithEnd("2", 2025, 2, 1, 2025, 2, 7),  // 7 days
        )
        assertEquals(6, calc.calculateAveragePeriodLength(records))
    }

    @Test
    fun period_length_ignores_records_without_end_date() {
        val records = listOf(
            record("1", 2025, 1, 1),                              // no endDate
            recordWithEnd("2", 2025, 2, 1, 2025, 2, 5),          // 5 days
        )
        assertEquals(5, calc.calculateAveragePeriodLength(records))
    }

    @Test
    fun period_length_ignores_deleted_records() {
        val records = listOf(
            recordWithEnd("1", 2025, 1, 1, 2025, 1, 10).copy(isDeleted = true), // 10 days, deleted
            recordWithEnd("2", 2025, 2, 1, 2025, 2, 5),                         // 5 days
        )
        assertEquals(5, calc.calculateAveragePeriodLength(records))
    }

    @Test
    fun predict_next_period_adds_average_to_last_start() {
        // two records 28 days apart → predict 28 days after Feb 1
        val records = listOf(
            record("1", 2025, 1, 4),
            record("2", 2025, 2, 1),  // Jan 4 → Feb 1 = 28 days
        )
        val prediction = calc.predictNextPeriod(records)
        assertEquals(LocalDateKey(2025, 3, 1), prediction)
    }

    @Test
    fun irregular_cycles_average_correctly() {
        // gaps: 20, 40 → average 30
        val records = listOf(
            record("1", 2025, 1, 1),
            record("2", 2025, 1, 21),  // +20
            record("3", 2025, 3, 2),   // +40
        )
        assertEquals(30, calc.calculateAverageCycleLength(records))
    }

    @Test
    fun predict_next_period_uses_most_recent_start_as_base() {
        // Records out of insertion order — prediction should use the latest start date
        val records = listOf(
            record("2", 2025, 2, 1),
            record("1", 2025, 1, 1),  // Jan 1 → Feb 1 = 31d
        )
        // avg = 31, latest start = Feb 1, prediction = Mar 4
        val prediction = calc.predictNextPeriod(records)
        assertEquals(LocalDateKey(2025, 3, 4), prediction)
    }

    @Test
    fun all_deleted_records_treated_as_empty() {
        val records = listOf(
            record("1", 2025, 1, 1).copy(isDeleted = true),
            record("2", 2025, 2, 1).copy(isDeleted = true),
        )
        assertNull(calc.calculateAverageCycleLength(records))
        assertNull(calc.predictNextPeriod(records))
    }

    @Test
    fun predict_next_period_spans_leap_year_feb() {
        // Jan 1 2024 → Feb 1 2024 = 31 days; predict 31 days from Feb 1 = Mar 3 (2024 is leap)
        val records = listOf(
            record("1", 2024, 1, 1),
            record("2", 2024, 2, 1),
        )
        // Feb 1 + 31 = Mar 3 (Feb has 29 days in 2024, so Feb 1 + 31 = Mar 3)
        val prediction = calc.predictNextPeriod(records)
        assertEquals(LocalDateKey(2024, 3, 3), prediction)
    }
}
