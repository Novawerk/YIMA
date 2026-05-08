package com.haodong.yimalaile.ui.pages.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.ui.components.CycleCalendarGrid
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.components.SmallSpacer
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.onboarding_back
import yimalaile.composeapp.generated.resources.onboarding_continue
import yimalaile.composeapp.generated.resources.onboarding_select_end
import yimalaile.composeapp.generated.resources.onboarding_select_period_hint
import yimalaile.composeapp.generated.resources.onboarding_select_period_range
import kotlin.time.Clock

@Composable
internal fun PeriodDateStep(
    periodStart: LocalDate?,
    periodEnd: LocalDate?,
    onSelectionChange: (LocalDate?, LocalDate?) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val emptyState = remember {
        CycleState(records = emptyList(), predictions = emptyList(), currentPeriod = null, inPredictedPeriod = false)
    }

    val selectionHint = when {
        periodStart != null && periodEnd != null ->
            "${periodStart.month.number}/${periodStart.day} — ${periodEnd.month.number}/${periodEnd.day}"
        periodStart != null ->
            "${periodStart.month.number}/${periodStart.day} — ${stringResource(Res.string.onboarding_select_end)}"
        else -> stringResource(Res.string.onboarding_select_period_hint)
    }

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(Res.string.onboarding_select_period_range),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        SmallSpacer(6)
        Text(
            selectionHint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        SmallSpacer(16)

        CycleCalendarGrid(
            state = emptyState,
            phaseInfo = null,
            selectedStart = periodStart,
            selectedEnd = periodEnd,
            onDateClick = { date ->
                if (date > today) return@CycleCalendarGrid
                if (periodStart == null) {
                    onSelectionChange(date, null)
                } else if (periodEnd == null) {
                    when {
                        date < periodStart -> onSelectionChange(date, periodStart)
                        date == periodStart -> onSelectionChange(null, null)
                        else -> onSelectionChange(periodStart, date)
                    }
                } else {
                    onSelectionChange(date, null)
                }
            },
            isDateEnabled = { it <= today },
            modifier = Modifier.weight(1f),
            monthRange = -3..0,
        )

        SmallSpacer(16)
        PrimaryCta(
            text = stringResource(Res.string.onboarding_continue),
            onClick = onNext,
            enabled = periodStart != null && periodEnd != null,
        )
        SmallSpacer(4)
        Row(Modifier.fillMaxWidth()) {
            TextButton(onClick = onBack) { Text(stringResource(Res.string.onboarding_back)) }
        }
    }
}
