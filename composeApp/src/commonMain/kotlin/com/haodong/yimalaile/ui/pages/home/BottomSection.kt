package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
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
    homeMode: HomeMode,
    onToggleMode: (HomeMode) -> Unit,
    onPeriodArrived: () -> Unit,
    onPeriodGone: () -> Unit,
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
                // Tab 1: Home overview
                IconButton(
                    onClick = { onToggleMode(HomeMode.CALENDAR) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (homeMode == HomeMode.CALENDAR) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = if (homeMode == HomeMode.CALENDAR) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Icon(
                        if (homeMode == HomeMode.CALENDAR) Icons.Filled.Home else Icons.Outlined.Home,
                        contentDescription = null,
                    )
                }
                // Tab 2: Detail calendar
                IconButton(
                    onClick = { onToggleMode(HomeMode.DETAIL) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (homeMode == HomeMode.DETAIL) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = if (homeMode == HomeMode.DETAIL) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Icon(
                        if (homeMode == HomeMode.DETAIL) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                    )
                }
                // Tab 3: Stats
                IconButton(
                    onClick = { onToggleMode(HomeMode.STATS) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (homeMode == HomeMode.STATS) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = if (homeMode == HomeMode.STATS) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Icon(
                        if (homeMode == HomeMode.STATS) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                        contentDescription = null,
                    )
                }
                SmallSpacer(8)
                if (inPeriod) {
                    Button(
                        onClick = onPeriodGone,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.height(40.dp),
                    ) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        SmallSpacer(4)
                        Text(stringResource(Res.string.home_end_period), style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    Button(
                        onClick = onPeriodArrived,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.height(40.dp),
                    ) {
                        Icon(Icons.Outlined.WaterDrop, contentDescription = null, modifier = Modifier.size(16.dp))
                        SmallSpacer(4)
                        Text(stringResource(Res.string.btn_record_period), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
