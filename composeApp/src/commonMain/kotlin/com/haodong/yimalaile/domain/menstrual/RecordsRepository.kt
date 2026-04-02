package com.haodong.yimalaile.domain.menstrual

interface RecordsRepository {
    suspend fun insertRecord(record: MenstrualRecord): AddRecordResult
    suspend fun getAllRecords(): List<MenstrualRecord>
    suspend fun updateRecord(record: MenstrualRecord): Boolean
    suspend fun deleteRecord(id: String): Boolean
    suspend fun clearAll()
}
