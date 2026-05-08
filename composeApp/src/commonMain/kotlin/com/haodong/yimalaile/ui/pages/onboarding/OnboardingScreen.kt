package com.haodong.yimalaile.ui.pages.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.health.HealthSyncManager
import com.haodong.yimalaile.domain.menstrual.MenstrualService
import com.haodong.yimalaile.domain.notifications.NotificationPrefs
import com.haodong.yimalaile.domain.settings.SettingsRepository
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.until

/** Visual position of each onboarding step (used for the indicator and back nav). */
internal enum class OnboardingStep {
    Welcome,
    HealthSync,
    Notifications,
    PeriodDate,
    CycleSettings,
    AllSet,
}

@Composable
fun OnboardingScreen(
    service: MenstrualService,
    settings: SettingsRepository,
    healthSyncManager: HealthSyncManager?,
    notificationPrefs: NotificationPrefs,
    notificationPermissionGranted: Boolean,
    onUpdateNotificationPrefs: (NotificationPrefs) -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onComplete: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var step by remember { mutableStateOf(OnboardingStep.Welcome) }
    var periodStart by remember { mutableStateOf<LocalDate?>(null) }
    var periodEnd by remember { mutableStateOf<LocalDate?>(null) }
    var cycleLength by remember { mutableIntStateOf(28) }
    var periodDuration by remember { mutableIntStateOf(5) }
    var importedCount by remember { mutableIntStateOf(0) }
    var existingRecordCount by remember { mutableIntStateOf(0) }

    // Steps the user actually sees, in order. We hide HealthSync when the
    // platform doesn't expose a health store, and PeriodDate once we already
    // have records on file (either pre-existing or freshly imported).
    val orderedSteps: List<OnboardingStep> = remember(healthSyncManager, existingRecordCount) {
        buildList {
            add(OnboardingStep.Welcome)
            if (healthSyncManager != null) add(OnboardingStep.HealthSync)
            add(OnboardingStep.Notifications)
            if (existingRecordCount == 0) add(OnboardingStep.PeriodDate)
            add(OnboardingStep.CycleSettings)
            add(OnboardingStep.AllSet)
        }
    }

    val totalProgressSteps = orderedSteps.count { it != OnboardingStep.Welcome && it != OnboardingStep.AllSet }
    val currentProgressIndex = orderedSteps
        .filter { it != OnboardingStep.Welcome && it != OnboardingStep.AllSet }
        .indexOf(step)
        .let { if (it < 0) 0 else it + 1 }

    fun goNext() {
        val idx = orderedSteps.indexOf(step)
        if (idx >= 0 && idx + 1 < orderedSteps.size) step = orderedSteps[idx + 1]
    }

    fun goBack() {
        val idx = orderedSteps.indexOf(step)
        if (idx > 0) step = orderedSteps[idx - 1]
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 12.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedVisibility(
                visible = step != OnboardingStep.Welcome && step != OnboardingStep.AllSet,
            ) {
                StepIndicator(
                    current = currentProgressIndex,
                    total = totalProgressSteps,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                )
            }

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    val forward = orderedSteps.indexOf(targetState) >= orderedSteps.indexOf(initialState)
                    val sign = if (forward) 1 else -1
                    (slideInHorizontally(tween(280)) { sign * it / 6 } + fadeIn(tween(280)))
                        .togetherWith(
                            slideOutHorizontally(tween(220)) { -sign * it / 6 } + fadeOut(tween(220))
                        )
                },
                modifier = Modifier.weight(1f).fillMaxWidth(),
                label = "onboarding-step",
            ) { current ->
                when (current) {
                    OnboardingStep.Welcome -> WelcomeStep(onNext = { goNext() })

                    OnboardingStep.HealthSync -> HealthSyncStep(
                        healthSyncManager = healthSyncManager ?: return@AnimatedContent,
                        onConnected = { imported ->
                            importedCount = imported
                            scope.launch {
                                settings.setHealthSyncEnabled(true)
                                existingRecordCount = service.getCycleState(cycleLength).records.size
                                goNext()
                            }
                        },
                        onSkip = { goNext() },
                        onBack = { goBack() },
                    )

                    OnboardingStep.Notifications -> NotificationsStep(
                        prefs = notificationPrefs,
                        hasPermission = notificationPermissionGranted,
                        onPrefsChange = onUpdateNotificationPrefs,
                        onRequestPermission = onRequestNotificationPermission,
                        onContinue = { goNext() },
                        onSkip = { goNext() },
                        onBack = { goBack() },
                    )

                    OnboardingStep.PeriodDate -> PeriodDateStep(
                        periodStart = periodStart,
                        periodEnd = periodEnd,
                        onSelectionChange = { s, e ->
                            periodStart = s
                            periodEnd = e
                        },
                        onNext = {
                            if (periodStart != null && periodEnd != null) {
                                periodDuration = periodStart!!.until(periodEnd!!, DateTimeUnit.DAY).toInt() + 1
                            }
                            goNext()
                        },
                        onBack = { goBack() },
                    )

                    OnboardingStep.CycleSettings -> CycleSettingsStep(
                        cycleLength = cycleLength,
                        periodDuration = periodDuration,
                        onCycleLengthChange = { cycleLength = it },
                        onPeriodDurationChange = { periodDuration = it },
                        showSyntheticHint = (existingRecordCount + (if (periodStart != null && periodEnd != null && existingRecordCount == 0) 1 else 0)) < ENOUGH_RECORDS_FOR_PREDICTION,
                        onSave = {
                            scope.launch {
                                commitOnboarding(
                                    service = service,
                                    settings = settings,
                                    cycleLength = cycleLength,
                                    periodDuration = periodDuration,
                                    manualStart = periodStart,
                                    manualEnd = periodEnd,
                                )
                                goNext()
                            }
                        },
                        onBack = { goBack() },
                    )

                    OnboardingStep.AllSet -> AllSetStep(
                        importedCount = importedCount,
                        onComplete = onComplete,
                    )
                }
            }
        }
    }
}
