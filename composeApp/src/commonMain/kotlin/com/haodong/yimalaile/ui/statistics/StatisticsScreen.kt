package com.haodong.yimalaile.ui.statistics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.haodong.yimalaile.domain.menstrual.MenstrualService

@Composable
fun StatisticsScreen(
    service: MenstrualService,
    onBack: () -> Unit,
) {
    // TODO: implement statistics screen
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Statistics — TODO")
    }
}
