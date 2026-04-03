package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.CyclePhase
import com.haodong.yimalaile.domain.menstrual.CyclePhaseInfo
import com.haodong.yimalaile.ui.components.SmallSpacer
import com.haodong.yimalaile.ui.theme.expressiveShapes
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

/**
 * Bottom sheet showing all cycle phases with timing info.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PhaseExplanationSheet(
    phaseInfo: CyclePhaseInfo,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(Res.string.home_current_phase),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                stringResource(Res.string.home_day_in_cycle, phaseInfo.dayInCycle, phaseInfo.cycleLength),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SmallSpacer(8)

            CyclePhase.entries.forEach { phase ->
                val isCurrent = phase == phaseInfo.phase
                val daysUntil = phaseInfo.daysUntilPhase(phase)
                val timingLabel = if (isCurrent) stringResource(Res.string.phase_now)
                                  else stringResource(Res.string.phase_in_days, daysUntil)

                Surface(
                    tonalElevation = if (isCurrent) 3.dp else 0.dp,
                    shape = MaterialTheme.shapes.large,
                    border = if (isCurrent) BorderStroke(2.dp, phaseColor(phase)) else null,
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            modifier = Modifier.size(24.dp),
                            color = phaseColor(phase),
                            shape = phaseShape(phase),
                        ) { }
                        SmallSpacer(12)
                        Column(Modifier.weight(1f)) {
                            Text(
                                phaseDisplayName(phase),
                                style = MaterialTheme.typography.bodyLargeEmphasized,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            )
                            SmallSpacer(2)
                            Text(
                                phaseDescription(phase),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        SmallSpacer(8)
                        Text(
                            timingLabel,
                            style = MaterialTheme.typography.labelMediumEmphasized,
                            color = if (isCurrent) phaseColor(phase)
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
