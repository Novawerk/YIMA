package com.haodong.yimalaile.data

import kotlin.math.abs
import kotlinx.datetime.Clock

data class LocalDateKey(val year: Int, val month: Int, val day: Int) : Comparable<LocalDateKey> {
    override fun compareTo(other: LocalDateKey): Int {
        if (this.year != other.year) return this.year - other.year
        if (this.month != other.month) return this.month - other.month
        return this.day - other.day
    }

    override fun toString(): String = "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"

    fun getMonthName(): String {
        return when (month) {
            1 -> "JAN"
            2 -> "FEB"
            3 -> "MAR"
            4 -> "APR"
            5 -> "MAY"
            6 -> "JUN"
            7 -> "JUL"
            8 -> "AUG"
            9 -> "SEP"
            10 -> "OCT"
            11 -> "NOV"
            12 -> "DEC"
            else -> ""
        }
    }

    fun toEpochMillis(): Long {
        // Simplified conversion assuming UTC
        val daysSinceEpoch = daysBetween(LocalDateKey(1970, 1, 1), this)
        return daysSinceEpoch * 24L * 60 * 60 * 1000
    }

    companion object {
        fun fromEpochMillis(millis: Long): LocalDateKey {
            val daysSinceEpoch = (millis / (24L * 60 * 60 * 1000)).toInt()
            return LocalDateKey(1970, 1, 1).plusDays(daysSinceEpoch)
        }

        /** Parse an ISO date string "YYYY-MM-DD". */
        fun fromString(iso: String): LocalDateKey {
            val parts = iso.split("-")
            return LocalDateKey(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        }
    }
}

fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}

fun daysInMonth(year: Int, month: Int): Int {
    return when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 0
    }
}

fun LocalDateKey.nextDay(): LocalDateKey {
    var y = year
    var m = month
    var d = day + 1
    if (d > daysInMonth(y, m)) {
        d = 1
        m++
        if (m > 12) {
            m = 1
            y++
        }
    }
    return LocalDateKey(y, m, d)
}

fun LocalDateKey.prevDay(): LocalDateKey {
    var y = year
    var m = month
    var d = day - 1
    if (d < 1) {
        m--
        if (m < 1) {
            m = 12
            y--
        }
        d = daysInMonth(y, m)
    }
    return LocalDateKey(y, m, d)
}

fun daysBetween(a: LocalDateKey, b: LocalDateKey): Int {
    if (a == b) return 0
    var count = 0
    if (a < b) {
        var current = a
        while (current < b) {
            current = current.nextDay()
            count++
        }
        return count
    } else {
        var current = b
        while (current < a) {
            current = current.nextDay()
            count--
        }
        return count
    }
}

fun LocalDateKey.plusDays(days: Int): LocalDateKey {
    var current = this
    if (days >= 0) {
        repeat(days) {
            current = current.nextDay()
        }
    } else {
        repeat(abs(days)) {
            current = current.prevDay()
        }
    }
    return current
}

data class DateRange(val start: LocalDateKey, val end: LocalDateKey) {
    fun overlaps(other: DateRange): Boolean {
        return this.start <= other.end && other.start <= this.end
    }

    fun isContiguousWith(other: DateRange): Boolean {
        return this.end.nextDay() == other.start || other.end.nextDay() == this.start
    }

    fun mergeIfMergeable(other: DateRange): DateRange? {
        if (overlaps(other) || isContiguousWith(other)) {
            val minStart = if (this.start < other.start) this.start else other.start
            val maxEnd = if (this.end > other.end) this.end else other.end
            return DateRange(minStart, maxEnd)
        }
        return null
    }
}

fun currentEpochMillis(): Long = Clock.System.now().toEpochMilliseconds()
