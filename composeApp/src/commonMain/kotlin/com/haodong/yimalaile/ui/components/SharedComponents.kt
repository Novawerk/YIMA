package com.haodong.yimalaile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
            containerColor = MaterialTheme.colorScheme.primary,
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
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Large stat card with big number. */
@Composable
fun BigStatCard(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
            .padding(20.dp),
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.width(4.dp))
                Text(unit, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}

/** Period duration dots chart — shows up to 3 records, with "view all" link. */
@Composable
fun PeriodDurationChart(
    records: List<MenstrualRecord>,
    daysStr: String,
    onViewAll: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val shown = records.takeLast(3)
    val hasMore = records.size > 3

    Box(
        modifier.fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            shown.forEach { record ->
                val days = record.startDate.until(record.endDate!!, DateTimeUnit.DAY) + 1
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${record.startDate.monthNumber}/${record.startDate.dayOfMonth}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.width(40.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(days.coerceAtMost(10)) { i ->
                            val alpha = 0.3f + (0.5f * (1f - i.toFloat() / days.coerceAtMost(10)))
                            Box(Modifier.size(14.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = alpha)))
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Text("$days$daysStr", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (hasMore && onViewAll != null) {
                Box(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onViewAll)
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "查看全部 ${records.size} 条记录 →",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    )
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
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        val secondary = MaterialTheme.colorScheme.secondary
        val primary = MaterialTheme.colorScheme.primary
        Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
            listOf(
                secondary.copy(alpha = 0.6f),
                primary.copy(alpha = 0.25f),
                secondary.copy(alpha = 0.4f),
                primary.copy(alpha = 0.15f),
                secondary.copy(alpha = 0.5f),
            ).forEach { color ->
                Box(Modifier.size(40.dp).clip(CircleShape).background(color))
            }
        }
    }
}

/** Small heart decoration. */
@Composable
fun HeartDecoration(modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    val resolvedColor = if (color == Color.Unspecified) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else color
    Text("♥", color = resolvedColor, style = MaterialTheme.typography.titleLarge, modifier = modifier)
}
