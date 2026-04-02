package com.haodong.yimalaile.ui.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.haodong.yimalaile.domain.menstrual.MenstrualService

@Composable
fun OnboardingScreen(
    service: MenstrualService,
    onComplete: () -> Unit,
) {
    // TODO: implement multi-step onboarding
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Onboarding — TODO")
    }
}
