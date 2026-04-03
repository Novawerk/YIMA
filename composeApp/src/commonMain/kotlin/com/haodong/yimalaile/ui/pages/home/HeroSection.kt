package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.CyclePhaseInfo
import com.haodong.yimalaile.ui.components.GrowSpacer
import com.haodong.yimalaile.ui.components.SmallSpacer
import com.haodong.yimalaile.ui.theme.expressiveShapes
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

/**
 * Hero section: title, big countdown number, progress indicator, and info cards.
 * Fills remaining vertical space via weight(1f).
 */
@Composable
internal fun ColumnScope.HeroSection(
    inPeriod: Boolean,
    heroNumber: Int,
    dayCount: Int?,
    phaseInfo: CyclePhaseInfo?,
    onPhaseClick: () -> Unit,
) {
    Column(
        Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        Text(
            if (inPeriod) stringResource(Res.string.home_take_care)
            else if (heroNumber <= 0) stringResource(Res.string.home_hero_overdue)
            else stringResource(Res.string.home_next_visit),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SmallSpacer(12)

        GrowSpacer()

        // Big countdown number
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                "${if (heroNumber <= 0 && !inPeriod) -heroNumber else heroNumber}",
                style = MaterialTheme.typography.displayLargeEmphasized,
                fontSize = 128.sp,
                fontWeight = FontWeight.Black,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (phaseInfo != null) {
                    CircularWavyProgressIndicator(
                        progress = { phaseInfo.progress },
                        modifier = Modifier.size(48.dp),
                        wavelength = 12.dp,
                        waveSpeed = 4.dp,
                    )
                }
                SmallSpacer(48)
                Text(
                    text = if (inPeriod && phaseInfo != null) {
                        stringResource(Res.string.home_estimated_remaining, (phaseInfo.periodLength - (dayCount ?: 0)).coerceAtLeast(0))
                    } else if (inPeriod) {
                        stringResource(Res.string.home_day_n, dayCount ?: 0)
                    } else {
                        stringResource(Res.string.unit_days)
                    },
                    style = MaterialTheme.typography.labelSmall,
                )
                SmallSpacer(24)
            }
        }
        SmallSpacer(24)

        // Info Cards
        if (phaseInfo != null) {
            // Current phase card
            Surface(
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.fillMaxWidth().clickable { onPhaseClick() },
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(Res.string.home_current_phase),
                        style = MaterialTheme.typography.labelSmallEmphasized,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Black,
                    )
                    GrowSpacer()
                    Text(
                        phaseDisplayName(phaseInfo.phase),
                        style = MaterialTheme.typography.bodyLargeEmphasized,
                    )
                    SmallSpacer(8)
                    Surface(
                        modifier = Modifier.size(24.dp),
                        color = phaseColor(phaseInfo.phase),
                        shape = phaseShape(phaseInfo.phase),
                    ) { }
                }
            }
            SmallSpacer(16)
            Surface(
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (inPeriod) stringResource(Res.string.home_estimated_remaining, (phaseInfo.periodLength - (dayCount ?: 0)).coerceAtLeast(0))
                        else stringResource(Res.string.home_next_period_starts),
                        style = MaterialTheme.typography.labelSmallEmphasized,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Black,
                    )
                    GrowSpacer()
                    if (!inPeriod && phaseInfo.nextPeriodStart != null) {
                        val d = phaseInfo.nextPeriodStart
                        Text(
                            "${d.monthNumber}/${d.dayOfMonth}",
                            style = MaterialTheme.typography.bodyLargeEmphasized,
                        )
                        SmallSpacer(8)
                        Surface(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.expressiveShapes.puffy,
                        ) { }
                    }
                }
            }
            SmallSpacer(16)
        }
    }
}
