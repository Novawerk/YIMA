package com.haodong.yimalaile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.ui.theme.AppColors
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.until

/** Full-width pill CTA button matching design (Deep Rose bg, white text). */
@Composable
fun PrimaryCta(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.DeepRose,
            contentColor = Color.White,
        ),
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

/** Pill-shaped status badge (peach bg, muted text). */
@Composable
fun StatusPill(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(AppColors.WarmPeach.copy(alpha = 0.4f))
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = AppColors.DarkCoffee.copy(alpha = 0.7f),
        )
    }
}

/** Large stat card with big number. */
@Composable
fun BigStatCard(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(24.dp))
            .background(AppColors.BlushPink.copy(alpha = 0.45f))
            .padding(20.dp),
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = AppColors.DarkCoffee.copy(alpha = 0.5f))
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = AppColors.DarkCoffee)
                Spacer(Modifier.width(4.dp))
                Text(unit, style = MaterialTheme.typography.bodyLarge, color = AppColors.DarkCoffee.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}

/** Period duration dots chart. */
@Composable
fun PeriodDurationChart(records: List<MenstrualRecord>, daysStr: String, modifier: Modifier = Modifier) {
    Box(
        modifier.fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.BlushPink.copy(alpha = 0.25f))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            records.takeLast(6).forEach { record ->
                val days = record.startDate.until(record.endDate!!, DateTimeUnit.DAY) + 1
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${record.startDate.monthNumber}/${record.startDate.dayOfMonth}",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.DarkCoffee.copy(alpha = 0.4f),
                        modifier = Modifier.width(40.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(days.coerceAtMost(10)) { i ->
                            val alpha = 0.3f + (0.5f * (1f - i.toFloat() / days.coerceAtMost(10)))
                            Box(Modifier.size(14.dp).clip(CircleShape).background(AppColors.DeepRose.copy(alpha = alpha)))
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Text("$days$daysStr", style = MaterialTheme.typography.labelMedium, color = AppColors.DarkCoffee.copy(alpha = 0.5f))
                }
            }
        }
    }
}

/** Decorative illustration placeholder. */
@Composable
fun IllustrationPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AppColors.BlushPink.copy(alpha = 0.3f))
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
            listOf(
                AppColors.WarmPeach.copy(alpha = 0.6f),
                AppColors.DeepRose.copy(alpha = 0.25f),
                AppColors.WarmPeach.copy(alpha = 0.4f),
                AppColors.DeepRose.copy(alpha = 0.15f),
                AppColors.WarmPeach.copy(alpha = 0.5f),
            ).forEach { color ->
                Box(Modifier.size(40.dp).clip(CircleShape).background(color))
            }
        }
    }
}

/** Small heart decoration. */
@Composable
fun HeartDecoration(modifier: Modifier = Modifier, color: Color = AppColors.DeepRose.copy(alpha = 0.4f)) {
    Text("♥", color = color, style = MaterialTheme.typography.titleLarge, modifier = modifier)
}
