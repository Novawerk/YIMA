package com.haodong.yimalaile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.ui.theme.AppColors
import com.haodong.yimalaile.ui.theme.AppShapes

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

/** Two stat cards side by side (周期长度 / 经期时长). */
@Composable
fun StatCardsRow(
    label1: String, value1: String, unit1: String,
    label2: String, value2: String, unit2: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(label1, value1, unit1, Modifier.weight(1f))
        StatCard(label2, value2, unit2, Modifier.weight(1f))
    }
}

@Composable
fun StatCard(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.BlushPink.copy(alpha = 0.5f))
            .padding(16.dp),
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = AppColors.DarkCoffee.copy(alpha = 0.6f))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, style = MaterialTheme.typography.headlineMedium, color = AppColors.DarkCoffee)
                Text(" $unit", style = MaterialTheme.typography.bodyLarge, color = AppColors.DarkCoffee.copy(alpha = 0.6f))
            }
        }
    }
}

/** Decorative illustration placeholder — a gentle floral-like pattern using circles. */
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
        // Decorative circles as abstract floral
        Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
            listOf(
                AppColors.WarmPeach.copy(alpha = 0.6f),
                AppColors.DeepRose.copy(alpha = 0.25f),
                AppColors.WarmPeach.copy(alpha = 0.4f),
                AppColors.DeepRose.copy(alpha = 0.15f),
                AppColors.WarmPeach.copy(alpha = 0.5f),
            ).forEach { color ->
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

/** Small heart decoration used next to hero text. */
@Composable
fun HeartDecoration(modifier: Modifier = Modifier, color: Color = AppColors.DeepRose.copy(alpha = 0.4f)) {
    // Simple "♥" text as decoration
    Text("♥", color = color, style = MaterialTheme.typography.titleLarge, modifier = modifier)
}
