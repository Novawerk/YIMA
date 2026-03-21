package com.haodong.yimalaile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.data.MenstrualRecord
import com.haodong.yimalaile.ui.theme.AppColors
import com.haodong.yimalaile.ui.theme.AppShapes
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.*

@Composable
fun StatisticsScreen(
    records: List<MenstrualRecord>,
    averageCycleLength: Int?,
    averagePeriodLength: Int?,
    cycleLengths: List<Int>,
    periodLengths: List<Int>,
    onClose: () -> Unit,
    onGoRecord: () -> Unit
) {
    val hasEnoughData = cycleLengths.size >= 2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BackgroundLight)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(48.dp)
                    .background(AppColors.Accent.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = stringResource(Res.string.nav_back),
                    tint = AppColors.Primary
                )
            }
            Text(
                text = stringResource(Res.string.stats_title),
                style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary)
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        if (!hasEnoughData) {
            StatisticsEmptyState(
                completeCycles = cycleLengths.size,
                onGoRecord = onGoRecord
            )
        } else {
            StatisticsLoadedContent(
                averageCycleLength = averageCycleLength ?: 28,
                averagePeriodLength = averagePeriodLength,
                cycleLengths = cycleLengths,
                periodLengths = periodLengths
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun StatisticsEmptyState(
    completeCycles: Int,
    onGoRecord: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp)
    ) {
        // Blob illustration
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(AppShapes.HeroBlob)
                .background(AppColors.Accent.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "📊", fontSize = 52.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(Res.string.stats_empty_message),
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.Primary.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            shape = CircleShape,
            color = AppColors.Accent.copy(alpha = 0.3f),
            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Accent.copy(alpha = 0.4f))
        ) {
            Text(
                text = stringResource(Res.string.stats_empty_progress, completeCycles),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Primary.copy(alpha = 0.7f)
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onGoRecord,
            shape = RoundedCornerShape(36.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
            modifier = Modifier.height(56.dp).padding(horizontal = 8.dp)
        ) {
            Text(
                text = stringResource(Res.string.btn_record_period),
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun StatisticsLoadedContent(
    averageCycleLength: Int,
    averagePeriodLength: Int?,
    cycleLengths: List<Int>,
    periodLengths: List<Int>
) {
    val displayCycles = cycleLengths.takeLast(6)
    val maxCycleLength = displayCycles.maxOrNull()?.toFloat() ?: 1f

    // Prediction confidence based on number of cycles
    val cycleCount = cycleLengths.size
    val (confidenceLabel, confidenceFraction) = when {
        cycleCount >= 6 -> Pair(stringResource(Res.string.stats_confidence_high), 0.85f)
        cycleCount >= 3 -> Pair(stringResource(Res.string.stats_confidence_medium), 0.55f)
        else -> Pair(stringResource(Res.string.stats_confidence_low), 0.25f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Stat cards row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avg Cycle card
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = AppColors.Accent.copy(alpha = 0.4f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(Res.string.stats_avg_cycle),
                        style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.Primary.copy(alpha = 0.8f))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = averageCycleLength.toString(),
                            style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(Res.string.unit_days),
                            style = TextStyle(fontSize = 14.sp, color = AppColors.Primary.copy(alpha = 0.7f))
                        )
                    }
                }
            }
            // Avg Period card
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = AppColors.Primary.copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(Res.string.stats_avg_period),
                        style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.Primary.copy(alpha = 0.8f))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = averagePeriodLength?.toString() ?: "--",
                            style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary)
                        )
                        if (averagePeriodLength != null) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(Res.string.unit_days),
                                style = TextStyle(fontSize = 14.sp, color = AppColors.Primary.copy(alpha = 0.7f))
                            )
                        }
                    }
                }
            }
        }

        // Cycle Trend Chart
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = AppColors.Primary.copy(alpha = 0.04f),
            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Primary.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = stringResource(Res.string.stats_cycle_trend),
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary)
                    )
                    Text(
                        text = stringResource(Res.string.stats_last_n_cycles, displayCycles.size),
                        style = TextStyle(fontSize = 11.sp, color = AppColors.Primary.copy(alpha = 0.5f))
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    displayCycles.forEach { length ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = length.toString(),
                                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary.copy(alpha = 0.7f)),
                                modifier = Modifier.width(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 60.dp, bottomEnd = 20.dp, bottomStart = 50.dp))
                                    .background(AppColors.Primary.copy(alpha = 0.06f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = length / maxCycleLength)
                                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 60.dp, bottomEnd = 20.dp, bottomStart = 50.dp))
                                        .background(AppColors.Primary.copy(alpha = 0.7f))
                                )
                            }
                        }
                    }
                }
            }
        }

        // Period Duration Dots (only if we have period length data)
        if (periodLengths.isNotEmpty()) {
            val displayPeriods = periodLengths.takeLast(6)
            val maxPeriodLength = displayPeriods.maxOrNull()?.toFloat() ?: 1f

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = AppColors.Primary.copy(alpha = 0.04f),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Primary.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(Res.string.stats_period_duration),
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        displayPeriods.forEachIndexed { index, length ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                val dotFraction = length / maxPeriodLength
                                val bottomPadding = ((1f - dotFraction) * 48).dp
                                Spacer(modifier = Modifier.weight(1f))
                                Box(
                                    modifier = Modifier
                                        .padding(bottom = bottomPadding)
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(AppColors.Accent)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "P${index + 1}",
                                    style = TextStyle(fontSize = 9.sp, color = AppColors.Primary.copy(alpha = 0.4f))
                                )
                            }
                        }
                    }
                }
            }
        }

        // Prediction Confidence
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            color = AppColors.Primary.copy(alpha = 0.05f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stringResource(Res.string.stats_prediction_confidence)}: $confidenceLabel",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary)
                )
                // Small circular progress ring
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AppColors.Accent.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${(confidenceFraction * 100).toInt()}%",
                        style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary)
                    )
                }
            }
        }

        // Encouragement Note
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AppShapes.Blob1)
                .background(AppColors.Accent.copy(alpha = 0.2f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "\" ${stringResource(Res.string.stats_encouragement)} \"",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.Primary.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            )
        }
    }
}
