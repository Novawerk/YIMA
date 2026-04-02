package com.haodong.yimalaile.ui.record

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.ui.theme.AppColors
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * Shows all days within a period as tappable circles.
 * Days that already have a daily record are dimmed.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DayPickerSheet(
    record: MenstrualRecord,
    onDismiss: () -> Unit,
    onDaySelected: (LocalDate) -> Unit,
) {
    val start = record.startDate
    val end = record.endDate ?: start
    val existingDates = record.dailyRecords.map { it.date }.toSet()

    // Build list of days in range
    val days = buildList {
        var d = start
        while (d <= end) {
            add(d)
            d = d.plus(1, DateTimeUnit.DAY)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = AppColors.SoftCream,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(Modifier.padding(24.dp)) {
            Text("选择日期", style = MaterialTheme.typography.titleLarge, color = AppColors.DarkCoffee)
            Spacer(Modifier.height(4.dp))
            Text(
                "选择要补充记录的日期",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.DarkCoffee.copy(alpha = 0.5f),
            )
            Spacer(Modifier.height(20.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                days.forEach { date ->
                    val hasRecord = date in existingDates
                    DayChip(
                        day = date.dayOfMonth,
                        month = date.monthNumber,
                        hasRecord = hasRecord,
                        onClick = { onDaySelected(date) },
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DayChip(day: Int, month: Int, hasRecord: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (hasRecord) AppColors.DeepRose.copy(alpha = 0.2f)
                    else AppColors.WarmPeach.copy(alpha = 0.4f)
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "$day",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (hasRecord) AppColors.DarkCoffee.copy(alpha = 0.4f) else AppColors.DarkCoffee,
            )
        }
        Text(
            "${month}月",
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.DarkCoffee.copy(alpha = 0.4f),
        )
        if (hasRecord) {
            Text("已记录", style = MaterialTheme.typography.labelSmall, color = AppColors.DeepRose.copy(alpha = 0.5f))
        }
    }
}
