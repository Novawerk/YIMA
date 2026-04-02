package com.haodong.yimalaile.ui.record

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.haodong.yimalaile.domain.menstrual.DailyRecord
import com.haodong.yimalaile.domain.menstrual.Intensity
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.domain.menstrual.Mood
import com.haodong.yimalaile.ui.theme.AppColors
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.until

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecordDetailSheet(
    record: MenstrualRecord,
    onDismiss: () -> Unit,
) {
    val days = record.endDate?.let { record.startDate.until(it, DateTimeUnit.DAY) + 1 }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AppColors.SoftCream,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // Header
            Text("经期详情", style = MaterialTheme.typography.titleLarge, color = AppColors.DarkCoffee)
            Spacer(Modifier.height(20.dp))

            // Date range card
            Box(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppColors.BlushPink.copy(alpha = 0.35f))
                    .padding(20.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(
                            "${record.startDate.year}/${record.startDate.monthNumber}/${record.startDate.dayOfMonth}",
                            style = MaterialTheme.typography.titleMedium,
                            color = AppColors.DarkCoffee,
                        )
                        if (record.endDate != null) {
                            Text(
                                "→ ${record.endDate.year}/${record.endDate.monthNumber}/${record.endDate.dayOfMonth}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.DarkCoffee.copy(alpha = 0.5f),
                            )
                        } else {
                            Text("进行中", style = MaterialTheme.typography.bodyMedium, color = AppColors.DeepRose)
                        }
                    }
                    if (days != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$days", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = AppColors.DarkCoffee)
                            Text("天", style = MaterialTheme.typography.labelMedium, color = AppColors.DarkCoffee.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            // Duration dots
            if (days != null) {
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(days.coerceAtMost(14)) { i ->
                        val alpha = 0.3f + (0.5f * (1f - i.toFloat() / days.coerceAtMost(14)))
                        Box(Modifier.size(16.dp).clip(CircleShape).background(AppColors.DeepRose.copy(alpha = alpha)))
                    }
                }
            }

            // Daily records
            if (record.dailyRecords.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Text("每日记录", style = MaterialTheme.typography.titleMedium, color = AppColors.DarkCoffee)
                Spacer(Modifier.height(12.dp))
                record.dailyRecords.sortedBy { it.date }.forEach { day ->
                    DailyRecordRow(day)
                    Spacer(Modifier.height(8.dp))
                }
            }

            // No daily records hint
            if (record.dailyRecords.isEmpty()) {
                Spacer(Modifier.height(20.dp))
                Text(
                    "没有每日记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.DarkCoffee.copy(alpha = 0.4f),
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DailyRecordRow(day: DailyRecord) {
    Box(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.WarmPeach.copy(alpha = 0.2f))
            .padding(16.dp)
    ) {
        Column {
            Text(
                "${day.date.monthNumber}/${day.date.dayOfMonth}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.DarkCoffee,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (day.intensity != null) {
                    InfoChip(intensityLabel(day.intensity), AppColors.DeepRose.copy(alpha = 0.15f))
                }
                if (day.mood != null) {
                    InfoChip(moodLabel(day.mood), AppColors.WarmPeach.copy(alpha = 0.5f))
                }
            }
            if (day.symptoms.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    day.symptoms.forEach { symptom ->
                        InfoChip(symptom, AppColors.BlushPink.copy(alpha = 0.5f))
                    }
                }
            }
            if (!day.notes.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(day.notes, style = MaterialTheme.typography.bodySmall, color = AppColors.DarkCoffee.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
private fun InfoChip(text: String, bgColor: androidx.compose.ui.graphics.Color) {
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = AppColors.DarkCoffee)
    }
}

private fun intensityLabel(i: Intensity) = when (i) {
    Intensity.LIGHT -> "少量"
    Intensity.MEDIUM -> "中量"
    Intensity.HEAVY -> "多量"
}

private fun moodLabel(m: Mood) = when (m) {
    Mood.HAPPY -> "😊"
    Mood.NEUTRAL -> "😐"
    Mood.SAD -> "😔"
    Mood.VERY_SAD -> "😢"
}
