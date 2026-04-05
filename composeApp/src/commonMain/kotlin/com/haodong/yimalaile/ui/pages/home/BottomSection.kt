package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.ui.components.SmallSpacer
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.btn_record_period
import yimalaile.composeapp.generated.resources.home_end_period
import yimalaile.composeapp.generated.resources.history_backfill

@Composable
fun BottomSection(
    inPeriod: Boolean,
    calendarMode: Boolean,
    onToggleMode: (Boolean) -> Unit,
    onPeriodArrived: () -> Unit,
    onPeriodGone: () -> Unit,
    onBackfill: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(50),
        ) {
            Row(
                Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { onToggleMode(true) },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (calendarMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                }
                IconButton(
                    onClick = { onToggleMode(false) },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (!calendarMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Icon(Icons.Outlined.BarChart, contentDescription = null)
                }
                SmallSpacer(8)
                if (calendarMode) {
                    // Calendar mode: show period actions
                    if (inPeriod) {
                        Button(
                            onClick = onPeriodGone,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.height(40.dp),
                        ) {
                            Text(
                                stringResource(Res.string.home_end_period),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    } else {
                        Button(
                            onClick = onPeriodArrived,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.height(40.dp),
                        ) {
                            Text(
                                stringResource(Res.string.btn_record_period),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                } else {
                    // Stats mode: show backfill action
                    Button(
                        onClick = onBackfill,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.height(40.dp),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        SmallSpacer(4)
                        Text(
                            stringResource(Res.string.history_backfill),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}
