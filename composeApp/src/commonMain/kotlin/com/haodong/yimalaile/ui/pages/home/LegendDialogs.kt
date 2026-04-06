package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.ui.components.DecorShape
import com.haodong.yimalaile.ui.components.SmallSpacer
import com.haodong.yimalaile.ui.theme.expressiveShapes
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

@Composable
internal fun ChartLegendDialog(onDismiss: () -> Unit) {
    val barColor = MaterialTheme.colorScheme.primary
    val predictedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(Res.string.stats_chart_title), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(16.dp).clip(RoundedCornerShape(4.dp)).background(barColor))
                    SmallSpacer(12)
                    Text(stringResource(Res.string.stats_legend_actual), style = MaterialTheme.typography.bodyMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(16.dp).clip(RoundedCornerShape(4.dp)).background(predictedColor))
                    SmallSpacer(12)
                    Text(stringResource(Res.string.stats_legend_predicted), style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
    )
}

@Composable
internal fun CalendarLegendDialog(onDismiss: () -> Unit) {
    val ovColor = Color(0xFF7C4DFF)
    val periodColor = MaterialTheme.colorScheme.error
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.dialog_confirm))
            }
        },
        title = { Text(stringResource(Res.string.home_cycle_calendar)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(24.dp).height(4.dp).clip(RoundedCornerShape(50)).background(periodColor))
                    SmallSpacer(12)
                    Text(stringResource(Res.string.legend_period), style = MaterialTheme.typography.bodyMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(24.dp).height(4.dp).clip(RoundedCornerShape(50)).background(periodColor.copy(alpha = 0.5f)))
                    SmallSpacer(12)
                    Text(stringResource(Res.string.legend_predicted), style = MaterialTheme.typography.bodyMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(24.dp).height(4.dp).clip(RoundedCornerShape(50)).background(ovColor))
                    SmallSpacer(12)
                    Text(stringResource(Res.string.detail_ovulation), style = MaterialTheme.typography.bodyMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(24.dp), contentAlignment = Alignment.Center) {
                        DecorShape(size = 14, shape = MaterialTheme.expressiveShapes.flower, color = ovColor)
                    }
                    SmallSpacer(12)
                    Text(stringResource(Res.string.detail_ovulation_day), style = MaterialTheme.typography.bodyMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(24.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = periodColor)
                    }
                    SmallSpacer(12)
                    Text(stringResource(Res.string.record_start_date), style = MaterialTheme.typography.bodyMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(24.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Pause, contentDescription = null, modifier = Modifier.size(16.dp), tint = periodColor)
                    }
                    SmallSpacer(12)
                    Text(stringResource(Res.string.record_end_date), style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
    )
}
