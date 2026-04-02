package com.haodong.yimalaile.domain.menstrual

data class CycleState(
    val activePeriod: MenstrualRecord?,
    val recentPeriods: List<MenstrualRecord>,
    val predictions: List<PredictedCycle>
)
