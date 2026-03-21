package com.haodong.yimalaile.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConflictHelpersTest {
    private fun d(y:Int,m:Int,d:Int) = LocalDateKey(y,m,d)

    @Test
    fun overlaps_is_inclusive_of_boundaries() {
        val a = DateRange(d(2025,1,1), d(2025,1,5))
        val b = DateRange(d(2025,1,5), d(2025,1,10))
        val c = DateRange(d(2025,1,6), d(2025,1,7))
        assertTrue(a.overlaps(b)) // share boundary day 5
        assertTrue(b.overlaps(c)) // overlap inside
    }

    @Test
    fun contiguity_detects_adjacent_ranges() {
        val a = DateRange(d(2025,1,1), d(2025,1,3))
        val b = DateRange(d(2025,1,4), d(2025,1,6))
        assertTrue(a.isContiguousWith(b))
    }

    @Test
    fun merge_if_mergeable_merges_overlap_and_contiguous() {
        val a = DateRange(d(2025,1,1), d(2025,1,3))
        val b = DateRange(d(2025,1,3), d(2025,1,6)) // overlap on day 3
        val c = DateRange(d(2025,1,7), d(2025,1,8))
        val dct = DateRange(d(2025,1,4), d(2025,1,6))

        val ab = a.mergeIfMergeable(b)
        assertNotNull(ab)
        assertEquals(DateRange(d(2025,1,1), d(2025,1,6)), ab)

        val bc = b.mergeIfMergeable(c)
        assertNotNull(bc)
        assertEquals(DateRange(d(2025,1,3), d(2025,1,8)), bc)

        val ad = a.mergeIfMergeable(dct)
        assertNotNull(ad)
        assertEquals(DateRange(d(2025,1,1), d(2025,1,6)), ad)

        val ac = a.mergeIfMergeable(c)
        assertNull(ac) // not mergeable, gap of more than 1 day
    }


    @Test
    fun menstrual_record_as_range_single_or_span() {
        val r1 = MenstrualRecord(
            id = "id1",
            startDate = d(2025,2,1),
            endDate = null,
            createdAtEpochMillis = 0,
            updatedAtEpochMillis = 0,
        )
        assertEquals(DateRange(d(2025,2,1), d(2025,2,1)), r1.asRange())

        val r2 = MenstrualRecord(
            id = "id2",
            startDate = d(2025,2,1),
            endDate = d(2025,2,3),
            createdAtEpochMillis = 0,
            updatedAtEpochMillis = 0,
        )
        assertEquals(DateRange(d(2025,2,1), d(2025,2,3)), r2.asRange())
    }
}
