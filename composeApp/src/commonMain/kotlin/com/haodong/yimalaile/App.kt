package com.haodong.yimalaile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.data.*
import com.haodong.yimalaile.ui.components.RecordTodayDialog
import kotlinx.coroutines.launch
import com.haodong.yimalaile.ui.components.StatCardNew
import com.haodong.yimalaile.ui.SettingsScreen
import com.haodong.yimalaile.ui.theme.AppColors
import com.haodong.yimalaile.ui.theme.AppShapes
import org.jetbrains.compose.resources.painterResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.compose_multiplatform

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val viewModel = remember { HomeViewModel(AppDatabase.requireRepository()) }
    DisposableEffect(Unit) { onDispose { viewModel.clear() } }

    val state by viewModel.state.collectAsState()
    var showRecordDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf("home") }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.BackgroundLight)
                .padding(padding)
        ) {
            when (currentScreen) {
                "home" -> {
                    HomeScreen(
                        state = state,
                        onOpenSettings = { currentScreen = "settings" },
                        onOpenCalendar = { currentScreen = "calendar" },
                        onShowRecordDialog = { showRecordDialog = true }
                    )
                }
                "settings" -> {
                    SettingsScreen(
                        onClose = { currentScreen = "home" }
                    )
                }
                "calendar" -> {
                    CalendarHistoryScreen(
                        onClose = { currentScreen = "home" }
                    )
                }
            }

            if (showRecordDialog) {
                RecordTodayDialog(
                    colorPrimary = AppColors.Primary,
                    colorAccent = AppColors.Accent,
                    colorBackgroundLight = AppColors.BackgroundLight,
                    onDismiss = { showRecordDialog = false },
                    onSave = { record ->
                        scope.launch {
                            val result = viewModel.addRecord(record)
                            when (result) {
                                is AddRecordResult.Success -> {
                                    showRecordDialog = false
                                    snackbarHostState.showSnackbar("记录成功")
                                }
                                is AddRecordResult.DuplicateStartDate -> {
                                    snackbarHostState.showSnackbar("该日期已有记录")
                                }
                                is AddRecordResult.TooCloseToOtherRecord -> {
                                    snackbarHostState.showSnackbar("两次月经记录之间至少需要间隔 15 天")
                                }
                                else -> {
                                    snackbarHostState.showSnackbar("保存失败")
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onOpenSettings: () -> Unit,
    onOpenCalendar: () -> Unit,
    onShowRecordDialog: () -> Unit
) {
    val today = remember { LocalDateKey.fromEpochMillis(currentEpochMillis()) }
    val lastPeriodDate = state.lastPeriodDate
    val predictedNext = state.predictedNextPeriod ?: lastPeriodDate?.plusDays(28)
    val avgCycle = state.averageCycleLength ?: 28

    var statusText = "没来"
    var statusDescription = "正在等待你的好消息"
    var predictionText = if (predictedNext != null) "预计 ${predictedNext.month}月${predictedNext.day}日" else "暂无预测"
    var statusColor = AppColors.Primary

    if (lastPeriodDate != null && predictedNext != null) {
        val days = daysBetween(today, predictedNext)
        if (days <= 0) {
            statusText = "来了"
            statusDescription = "多喝热水，注意保暖"
            statusColor = AppColors.Primary
        } else if (days <= 3) {
            statusText = "快了"
            statusDescription = "大概就在这两天了"
            statusColor = AppColors.Warning
        } else {
            statusText = "没来"
            statusDescription = "享受轻松的时光吧"
            statusColor = AppColors.Success
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onOpenCalendar,
                    modifier = Modifier
                        .size(52.dp)
                        .background(AppColors.Accent.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = AppColors.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "月经期预测",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Primary.copy(alpha = 0.8f)
                    )
                )

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(52.dp)
                        .background(AppColors.Accent.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        tint = AppColors.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Hero Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clip(AppShapes.HeroBlob)
                        .background(statusColor.copy(alpha = 0.1f))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box {
                        Text(
                            text = statusText,
                            style = TextStyle(
                                fontSize = 110.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 110.sp,
                                color = statusColor
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = statusColor.copy(alpha = 0.4f),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 32.dp, y = (-8).dp)
                                .size(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = statusDescription,
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = statusColor.copy(alpha = 0.6f)
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        shape = CircleShape,
                        color = AppColors.Accent.copy(alpha = 0.3f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Accent.copy(alpha = 0.4f)),
                    ) {
                        Text(
                            text = predictionText,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.Primary.copy(alpha = 0.8f)
                            )
                        )
                    }
                }
            }

            // Stats Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCardNew(
                    title = "周期长度",
                    value = avgCycle.toString(),
                    unit = "天",
                    backgroundColor = AppColors.CardBg1,
                    textColor = AppColors.Primary,
                    shape = AppShapes.LeftBlob,
                    modifier = Modifier.weight(1f)
                )
                StatCardNew(
                    title = "经期时长",
                    value = "5",
                    unit = "天",
                    backgroundColor = AppColors.CardBg2,
                    textColor = AppColors.Primary,
                    shape = AppShapes.RightBlob,
                    modifier = Modifier.weight(1f)
                )
            }

            // Illustration Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .height(200.dp)
                    .clip(RoundedCornerShape(32.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppColors.Accent.copy(alpha = 0.4f))
                )

                Image(
                    painter = painterResource(Res.drawable.compose_multiplatform),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .alpha(0.2f),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(AppColors.Primary)
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "\"照顾好自己，多喝温水。\"",
                        style = TextStyle(
                            color = AppColors.Primary,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Italic,
                            fontSize = 18.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }

        // Primary Action (Fixed at bottom)
        Button(
            onClick = onShowRecordDialog,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .height(72.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(36.dp), spotColor = AppColors.Primary.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(36.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Primary
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "记录月经",
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun CalendarHistoryScreen(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BackgroundLight)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(48.dp)
                    .background(AppColors.Accent.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppColors.Primary)
            }
            Text(
                text = "日历记录",
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Primary
                )
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Hand-drawn Month Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(width = 200.dp, height = 60.dp)
                    .clip(AppShapes.DateBlob)
                    .background(AppColors.Primary.copy(alpha = 0.1f))
            )
            Text(
                text = "2026年 3月",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Primary
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calendar Placeholder (Blob style)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(32.dp))
                .background(AppColors.Accent.copy(alpha = 0.2f))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.SpaceEvenly) {
                // Simplified calendar grid representation
                repeat(5) { rowIndex ->
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(7) { colIndex ->
                            val day = rowIndex * 7 + colIndex + 1
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (day == 14) AppColors.Primary.copy(alpha = 0.3f) else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                if (day <= 31) {
                                    Text(
                                        text = "$day",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            color = AppColors.Primary.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Legend/Info
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = AppColors.Primary.copy(alpha = 0.05f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "本月概览",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Primary
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(AppColors.Primary.copy(alpha = 0.4f)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("经期天数: 5天", color = AppColors.Primary.copy(alpha = 0.7f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(AppColors.Success.copy(alpha = 0.4f)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("周期长度: 28天", color = AppColors.Primary.copy(alpha = 0.7f))
                }
            }
        }
    }
}
