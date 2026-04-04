package com.haodong.yimalaile.domain.menstrual

data class CycleState(
    val records: List<MenstrualRecord>,
    val predictions: List<PredictedCycle>,
    val currentPeriod: MenstrualRecord?,
    val inPredictedPeriod: Boolean,
) {
    val inPeriod: Boolean get() = currentPeriod != null || inPredictedPeriod
}
