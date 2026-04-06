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
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.ui.components.SmallSpacer
import kotlinx.datetime.DateTimeUnit
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
    onClick: () -> Unit,
) {
    val periodDays = record.endDate?.let {
        record.startDate.until(it, DateTimeUnit.DAY).toInt() + 1
    }
    val ascIdx = sortedAsc.indexOf(record)
    val cycleLen = if (ascIdx > 0) {
        sortedAsc[ascIdx - 1].startDate.until(record.startDate, DateTimeUnit.DAY).toInt()
    } else null
    val dateStr = "${record.startDate.monthNumber}/${record.startDate.dayOfMonth}"

    Surface(
        onClick = onClick,
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Date
            Text(
                "$dateStr ${record.startDate.year}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )

            // Period days: label + number
            if (periodDays != null) {
                Text(
                    stringResource(Res.string.stats_col_period),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SmallSpacer(4)
                Text(
                    "$periodDays",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            SmallSpacer(16)

            // Cycle length: label + number, or "Current" badge
            if (isCurrent) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(50),
                ) {
                    Text(
                        stringResource(Res.string.stats_current_cycle),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            } else {
                Text(
                    stringResource(Res.string.stats_col_cycle),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SmallSpacer(4)
                Text(
                    if (cycleLen != null) "$cycleLen" else "-",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
