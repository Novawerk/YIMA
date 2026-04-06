package com.haodong.yimalaile.domain.menstrual

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.until

/**
 * Common calculations for menstrual cycle data.
 */
internal fun averagePeriodLength(records: List<MenstrualRecord>): Int? {
    val lengths = records
        .filter { !it.isDeleted && it.endDate != null }
        .map { it.startDate.until(it.endDate!!, DateTimeUnit.DAY).toInt() + 1 }
    return if (lengths.isEmpty()) null else lengths.sum() / lengths.size
}
