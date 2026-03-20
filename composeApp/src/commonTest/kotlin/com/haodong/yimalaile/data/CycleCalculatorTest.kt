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
}
