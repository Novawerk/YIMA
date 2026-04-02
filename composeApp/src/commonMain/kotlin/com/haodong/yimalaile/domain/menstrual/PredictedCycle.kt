package com.haodong.yimalaile.domain.menstrual

import kotlinx.datetime.LocalDate

data class PredictedCycle(
    val predictedStart: LocalDate,
    val predictedEnd: LocalDate?
)
