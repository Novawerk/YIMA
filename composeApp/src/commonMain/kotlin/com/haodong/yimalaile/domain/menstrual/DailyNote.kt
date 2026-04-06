package com.haodong.yimalaile.domain.menstrual

import kotlinx.datetime.LocalDate

data class DailyNote(
    val date: LocalDate,
    val mood: Mood? = null,
    val notes: String? = null,
)
