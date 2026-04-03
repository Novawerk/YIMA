package com.haodong.yimalaile.ui.pages.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.CycleState
import com.haodong.yimalaile.ui.components.GrowSpacer
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.components.SmallSpacer
import com.haodong.yimalaile.ui.theme.expressiveShapes
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.until
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

@Composable
internal fun ColumnScope.NotInPeriodContent(
    state: CycleState,
    today: LocalDate,
    onStartPeriod: () -> Unit,
    onBackfill: () -> Unit,
) {
    val prediction = state.predictions.firstOrNull()
    val daysUntil = prediction?.let { today.until(it.predictedStart, DateTimeUnit.DAY).toInt() }

    val (subtitle, heroText) = when {
        daysUntil == null -> "" to stringResource(Res.string.status_no_prediction)
        daysUntil < 0 -> stringResource(Res.string.home_hero_overdue_sub) to stringResource(Res.string.home_hero_overdue)
        daysUntil <= 3 -> stringResource(Res.string.home_hero_soon_sub) to stringResource(Res.string.home_hero_soon)
        else -> stringResource(Res.string.home_hero_early_sub) to stringResource(Res.string.home_hero_early)
    }

    val daysStr = stringResource(Res.string.unit_days)
    val records = state.recentPeriods

    val avgCycle = if (records.size >= 2) {
        val sorted = records.sortedBy { it.startDate }
        val gaps = sorted.zipWithNext().map { (a, b) -> a.startDate.until(b.startDate, DateTimeUnit.DAY).toInt() }
        gaps.sum() / gaps.size
    } else null

    val avgPeriod = records.filter { it.endDate != null }.let { list ->
        if (list.isEmpty()) null
        else list.sumOf { it.startDate.until(it.endDate!!, DateTimeUnit.DAY).toInt() + 1 } / list.size
    }


    // ── Hero (fills remaining space, centered) ──
    Column(
        Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
    ) {

        if (subtitle.isNotEmpty()) {
            Text(
                "下次姨妈造访",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
        }
        GrowSpacer()
        if (prediction != null) {
            Spacer(Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    "24", style = MaterialTheme.typography.displayLargeEmphasized,
                    fontSize = 128.sp, fontWeight = FontWeight.Black
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier) {
                    CircularWavyProgressIndicator(
                        { 0.25f }, modifier = Modifier.size(48.dp),
                        wavelength = 12.dp, waveSpeed = 4.dp
                    )
                    SmallSpacer(48)
                    Text(text = "Days", style = MaterialTheme.typography.labelSmall)
                    SmallSpacer(24)
                }

            }
            Spacer(Modifier.height(24.dp))

            Surface(
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "当前阶段", //TODO： add a help icon/dialog to  explain different 周期阶段，并且展示本周期的时间线
                        style = MaterialTheme.typography.labelSmallEmphasized,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Black
                    )
                    GrowSpacer()
                    Text(
                        "黄体期", //TODO: 加入不同的阶段计算
                        style = MaterialTheme.typography.bodyLargeEmphasized,
                    )
                    SmallSpacer(8)
                    Surface(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.tertiary,
                        shape = MaterialTheme.expressiveShapes.bun //TODO: 根据当前阶段提供不同的形状和颜色
                    ) { }

                }
            }
            SmallSpacer(16)
            Surface(
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "预计下个经期开始",
                        style = MaterialTheme.typography.labelSmallEmphasized,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Black
                    )
                    GrowSpacer()
                    Text(
                        "04/23(星期二)",
                        style = MaterialTheme.typography.bodyLargeEmphasized,
                    )
                    SmallSpacer(8)
                    Surface(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.tertiary,
                        shape = MaterialTheme.expressiveShapes.sunny
                    ) { }

                }
            }

            SmallSpacer(24)

        }

    }

    // ── Bottom: stats + actions ──

    Surface(tonalElevation = 1.dp, shape = MaterialTheme.shapes.extraLarge) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("过去的记录 // 经期情况", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Light)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                items(4) {
                    Surface(tonalElevation = 3.dp, shape = MaterialTheme.shapes.small) {
                        Box(modifier = Modifier.width(78.dp).height(110.dp)) {
                            Text(
                                "4月12日",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.align(Alignment.Center)
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).height(24.dp),
                                //TODO: 此处高度应该是月经来的天数的长度的统计
                            ) { }
                        }
                    }
                }
                //TODO: 如果没有记录/记录太少的情况，还需要显示未来几次预测的情况，最多显示三次，预测的卡片要有所不同
            }

            if (avgCycle == null) {
                OutlinedButton(
                    onClick = onBackfill,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Text(
                        stringResource(Res.string.home_backfill_to_predict),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            PrimaryCta(
                text = "✦ ${stringResource(Res.string.btn_record_period)}",
                onClick = onStartPeriod,
            )
        }

    }

}
