package com.haodong.yimalaile.domain.menstrual

import kotlinx.datetime.LocalDate

enum class Intensity { LIGHT, MEDIUM, HEAVY }
enum class Mood { HAPPY, NEUTRAL, SAD, VERY_SAD }

data class DailyRecord(
    val date: LocalDate,
    val intensity: Intensity? = null,
    val mood: Mood? = null,
    val symptoms: List<String> = emptyList(),
    val notes: String? = null
)
