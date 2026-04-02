package com.haodong.yimalaile.domain.common

import kotlin.math.abs
import kotlinx.datetime.Clock

data class LocalDateKey(val year: Int, val month: Int, val day: Int) : Comparable<LocalDateKey> {
    override fun compareTo(other: LocalDateKey): Int {
        if (this.year != other.year) return this.year - other.year
        if (this.month != other.month) return this.month - other.month
        return this.day - other.day
    }

    override fun toString(): String = "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"



    companion object {

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

fun currentEpochMillis(): Long = Clock.System.now().toEpochMilliseconds()

