package com.haodong.yimalaile.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DateUtilsTest {
    @Test
    fun compare_and_ordering_works() {
        val a = LocalDateKey(2024, 2, 28)
        val b = LocalDateKey(2024, 2, 29)
        val c = LocalDateKey(2024, 3, 1)
        assertTrue(a < b)
        assertTrue(b < c)
        assertTrue(c > a)
        assertEquals(0, a.compareTo(LocalDateKey(2024,2,28)))
    }

    @Test
    fun leap_year_logic_and_days_in_month() {
        assertTrue(isLeapYear(2000))
        assertTrue(!isLeapYear(1900))
        assertTrue(isLeapYear(2024))
        assertTrue(!isLeapYear(2023))
        assertEquals(29, daysInMonth(2024, 2))
        assertEquals(28, daysInMonth(2023, 2))
        assertEquals(31, daysInMonth(2024, 1))
        assertEquals(30, daysInMonth(2024, 4))
    }

    @Test
    fun next_and_prev_day_across_month_and_year() {
        val feb28 = LocalDateKey(2024, 2, 28)
        val feb29 = feb28.nextDay()
        assertEquals(LocalDateKey(2024, 2, 29), feb29)
        val mar1 = feb29.nextDay()
        assertEquals(LocalDateKey(2024, 3, 1), mar1)

        val jan1 = LocalDateKey(2024, 1, 1)
        val dec31prevYear = jan1.prevDay()
        assertEquals(LocalDateKey(2023, 12, 31), dec31prevYear)
    }

    @Test
    fun days_between_positive_negative_zero() {
        val a = LocalDateKey(2024, 12, 30)
        val b = LocalDateKey(2025, 1, 2)
        assertEquals(3, daysBetween(a, b)) // 30->31->1->2
        assertEquals(-3, daysBetween(b, a))
        assertEquals(0, daysBetween(a, a))
    }

    @Test
    fun plusDays_forward_across_year_boundary() {
        // Dec 30 + 3 = Jan 2
        val a = LocalDateKey(2024, 12, 30)
        assertEquals(LocalDateKey(2025, 1, 2), a.plusDays(3))
    }

    @Test
    fun plusDays_backward_is_prevDay_equivalent() {
        // Jan 1 - 1 = Dec 31
        val jan1 = LocalDateKey(2025, 1, 1)
        assertEquals(LocalDateKey(2024, 12, 31), jan1.plusDays(-1))
    }

    @Test
    fun plusDays_zero_returns_same_date() {
        val d = LocalDateKey(2025, 6, 15)
        assertEquals(d, d.plusDays(0))
    }

    @Test
    fun fromString_and_toString_roundtrip() {
        val original = LocalDateKey(2025, 3, 5)
        val str = original.toString()
        assertEquals("2025-03-05", str)
        assertEquals(original, LocalDateKey.fromString(str))
    }

    @Test
    fun fromString_and_toString_leading_zeros_preserved() {
        assertEquals("2025-01-09", LocalDateKey(2025, 1, 9).toString())
    }

    @Test
    fun dayOfWeek_known_values() {
        // 2025-01-01 is a Wednesday (3)
        assertEquals(3, dayOfWeek(2025, 1, 1))
        // 2024-02-29 is a Thursday (4) — leap day
        assertEquals(4, dayOfWeek(2024, 2, 29))
        // 2023-12-31 is a Sunday (0)
        assertEquals(0, dayOfWeek(2023, 12, 31))
    }
}
