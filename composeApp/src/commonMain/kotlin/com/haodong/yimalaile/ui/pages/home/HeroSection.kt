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

// ============================================================
// Hero display state — encapsulates all title/number logic
// ============================================================

/**
 * All possible hero display configurations.
 *
 * | State                        | title                                     | number | label |
 * |------------------------------|-------------------------------------------|--------|-------|
 * | In period, normal            | "经期造访中，这是第X天，还剩"                  | Y 天   | 天    |
 * | In period, overdue (> avg)   | "经期比平时长了，第X天"                       | —      | sub   |
 * | Due today (0 days)           | "今天姨妈该来了"                              | —      | sub   |
 * | Overdue (< 0 days)           | "你的月经已经推迟"                            | X 天   | 天    |
 * | Upcoming (> 0 days)          | "下次姨妈造访"                               | X 天   | 天    |
 */
private data class HeroDisplay(
    val title: String,
    val number: Int?,       // null = show subtitle text instead
    val subtitle: String?,  // shown when number is null
)

@Composable
private fun resolveHeroDisplay(
    inPeriod: Boolean,
    dayCount: Int?,
    heroNumber: Int,
    phaseInfo: CyclePhaseInfo?,
): HeroDisplay {
    val remainingDays = if (inPeriod && phaseInfo != null) {
        (phaseInfo.periodLength - (dayCount ?: 0)).coerceAtLeast(0)
    } else 0

    return when {
        // In period but exceeded average length
        inPeriod && remainingDays <= 0 -> HeroDisplay(
            title = stringResource(Res.string.home_period_overdue_title, dayCount ?: 0),
            number = null,
            subtitle = stringResource(Res.string.home_period_overdue_sub),
        )
        // In period, normal
        inPeriod -> HeroDisplay(
            title = stringResource(Res.string.home_period_day_title, dayCount ?: 0),
            number = remainingDays,
            subtitle = null,
        )
        // Due today
        heroNumber == 0 -> HeroDisplay(
            title = stringResource(Res.string.home_hero_due_today),
            number = null,
            subtitle = stringResource(Res.string.home_hero_due_today_sub),
        )
        // Overdue
        heroNumber < 0 -> HeroDisplay(
            title = stringResource(Res.string.home_hero_overdue),
            number = -heroNumber,
            subtitle = null,
        )
        // Upcoming
        else -> HeroDisplay(
            title = stringResource(Res.string.home_next_visit),
            number = heroNumber,
            subtitle = null,
        )
    }
}

// ============================================================
// Hero section composable
// ============================================================

@Composable
internal fun ColumnScope.HeroSection(
    inPeriod: Boolean,
    heroNumber: Int,
    dayCount: Int?,
    phaseInfo: CyclePhaseInfo?,
    onPhaseClick: () -> Unit,
) {
    val display = resolveHeroDisplay(inPeriod, dayCount, heroNumber, phaseInfo)

    Column(
        Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        // Title
        Text(
            display.title,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SmallSpacer(12)

        GrowSpacer()

        // Subtitle (when no number)
        if (display.number == null && display.subtitle != null) {
            Text(
                display.subtitle,
                style = MaterialTheme.typography.displayLargeEmphasized,
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Big number + progress
        if (display.number != null) {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    "${display.number}",
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
                        text = stringResource(Res.string.unit_days),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    SmallSpacer(24)
                }
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
                        style = MaterialTheme.typography.labelMediumEmphasized,
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
            // Next period card — only show when NOT in period
            if (!inPeriod) {
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
                            stringResource(Res.string.home_next_period_starts),
                            style = MaterialTheme.typography.labelMediumEmphasized,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Black,
                        )
                        GrowSpacer()
                        if (phaseInfo.nextPeriodStart != null) {
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
            }
            SmallSpacer(16)
        }
    }
}
