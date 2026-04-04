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

/**
 * Bottom docked toolbar — M3 Expressive style.
 * Contains mode toggle icons + primary action FAB.
 */
@Composable
fun BottomSection(
    inPeriod: Boolean,
    calendarMode: Boolean,
    onToggleMode: (Boolean) -> Unit,
    onPeriodArrived: () -> Unit,
    onPeriodGone: () -> Unit,
) {
    // Docked toolbar
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {


        Surface(
            tonalElevation = 2.dp,
            shape = RoundedCornerShape(50),
        ) {
            Row(
                Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Mode toggle icons
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
                // Mutually exclusive: end period OR record period
                SmallSpacer(8)
                if (inPeriod) {
                    FloatingActionButton(
                        onClick = onPeriodGone,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.height(48.dp),
                    ) {
                        Text(
                            stringResource(Res.string.home_end_period),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                } else {
                    FloatingActionButton(
                        onClick = onPeriodArrived,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.height(48.dp),
                    ) {
                        Row(
                            Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            SmallSpacer(6)
                            Text(
                                stringResource(Res.string.btn_record_period),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}