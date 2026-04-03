package com.haodong.yimalaile.ui.pages.record

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.PredictedCycle
import com.haodong.yimalaile.ui.components.DecorShape
import com.haodong.yimalaile.ui.components.SmallSpacer
import com.haodong.yimalaile.ui.theme.expressiveShapes
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

/**
 * Read-only detail sheet for a predicted period.
 * Shows predicted dates, estimated duration, and decorative prediction badge.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionDetailSheet(
    prediction: PredictedCycle,
    avgPeriodLength: Int,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Prediction badge
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(50),
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DecorShape(
                        size = 12,
                        shape = MaterialTheme.expressiveShapes.sunny,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                    SmallSpacer(6)
                    Text(
                        stringResource(Res.string.home_next_period_starts),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }
            SmallSpacer(24)

            // Predicted duration
            Text(
                "$avgPeriodLength",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                stringResource(Res.string.unit_days),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SmallSpacer(24)

            // Date range
            Surface(
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            stringResource(Res.string.record_start_date),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        SmallSpacer(4)
                        Text(
                            "${prediction.predictedStart.monthNumber}/${prediction.predictedStart.dayOfMonth}",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    if (prediction.predictedEnd != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                stringResource(Res.string.record_end_date),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            SmallSpacer(4)
                            Text(
                                "${prediction.predictedEnd.monthNumber}/${prediction.predictedEnd.dayOfMonth}",
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }
            }
            SmallSpacer(16)

            Text(
                stringResource(Res.string.home_backfill_to_predict),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
