package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.ui.components.SmallSpacer
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.number
import kotlinx.datetime.until
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

/**
 * A single period record card showing date, period length, and cycle length.
 * Shared between HomeStatistics and DetailCalendarView.
 */
@Composable
internal fun RecordCard(
    record: MenstrualRecord,
    sortedAsc: List<MenstrualRecord>,
    isCurrent: Boolean,
    defaultCycleLength: Int = 28,
    onClick: () -> Unit,
) {
    val periodDays = record.endDate?.let {
        record.startDate.until(it, DateTimeUnit.DAY).toInt() + 1
    }
    val ascIdx = sortedAsc.indexOf(record)
    val cycleLen = if (ascIdx > 0) {
        sortedAsc[ascIdx - 1].startDate.until(record.startDate, DateTimeUnit.DAY).toInt()
    } else null
    
    // 如果是最新的一条记录，周期长度显示为默认长度
    val displayCycleLen = if (ascIdx == sortedAsc.size - 1) {
        defaultCycleLength.toString()
    } else if (cycleLen != null) {
        cycleLen.toString()
    } else {
        "-"
    }
    val dateStr = "${record.startDate.month.number}/${record.startDate.day}"

    Surface(
        onClick = onClick,
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Date range
                Column {
                    Text(
                        if (record.startDate.year == 2026) "${record.startDate.month.number}/${record.startDate.day}" else dateStr,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "${record.startDate.year}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Period days
                    if (periodDays != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "$periodDays",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                stringResource(Res.string.stats_period_days),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    SmallSpacer(24)

                    // Cycle days
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            displayCycleLen,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            stringResource(Res.string.stats_cycle_days),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
