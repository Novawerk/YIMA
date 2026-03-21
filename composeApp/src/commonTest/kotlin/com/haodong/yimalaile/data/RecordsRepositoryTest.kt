package com.haodong.yimalaile.data

import kotlin.test.Test
import kotlin.test.assertTrue

class RecordsRepositoryTest {
    @Test
    fun duplicate_start_date_rejected() {
        val repo = InMemoryRecordsRepository()
        val start = LocalDateKey(2025, 8, 21)
        val r1 = MenstrualRecord(
            id = "1",
            startDate = start,
            createdAtEpochMillis = 0,
            updatedAtEpochMillis = 0,
        )
        val r2 = MenstrualRecord(
            id = "2",
            startDate = start,
            createdAtEpochMillis = 0,
            updatedAtEpochMillis = 0,
        )
        val res1 = repo.add(r1)
        assertTrue(res1 is AddRecordResult.Success)
        val res2 = repo.add(r2)
        assertTrue(res2 is AddRecordResult.DuplicateStartDate)
    }
}
