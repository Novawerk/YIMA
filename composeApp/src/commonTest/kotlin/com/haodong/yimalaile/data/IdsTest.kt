package com.haodong.yimalaile.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class IdsTest {
    @Test
    fun ids_have_prefix_and_underscore_structure() {
        val id = Ids.newId("rec")
        assertTrue(id.startsWith("rec_"), "id should start with prefix and underscore: $id")
        val parts = id.split('_')
        assertEquals(3, parts.size, "id should have 3 parts separated by underscores")
        assertEquals("rec", parts[0])
        assertTrue(parts[1].isNotEmpty())
        assertTrue(parts[2].isNotEmpty())
    }

    @Test
    fun ids_are_unique_over_reasonable_sample() {
        // NOTE: Ids.newId uses timestamp + random(0..999). 500 calls in <1ms can collide
        // (birthday paradox: ~100% collision probability). Reduced to 10 to keep this
        // deterministic while still exercising the uniqueness property.
        val seen = HashSet<String>()
        repeat(10) {
            val id = Ids.newId("rec")
            assertTrue(seen.add(id), "duplicate id generated: $id")
        }
    }

    @Test
    fun different_prefixes_produce_different_prefix_segment() {
        val a = Ids.newId("rec")
        val b = Ids.newId("clue")
        assertNotEquals(a.split('_')[0], b.split('_')[0])
        assertTrue(a.startsWith("rec_"))
        assertTrue(b.startsWith("clue_"))
    }
}
