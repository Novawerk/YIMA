package com.haodong.yimalaile.domain.menstrual

sealed class AddRecordResult {
    data class Success(val record: MenstrualRecord) : AddRecordResult()
    object DuplicateStartDate : AddRecordResult()
    object TooCloseToOtherRecord : AddRecordResult()
}
