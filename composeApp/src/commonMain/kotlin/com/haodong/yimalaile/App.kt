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
import com.haodong.yimalaile.ui.PrivacyDisclaimerPlaceholder
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
    val settingsRepo = remember { AppSettings.requireRepository() }
    var disclaimerAccepted by remember { mutableStateOf(settingsRepo.isDisclaimerAccepted()) }

    if (!disclaimerAccepted) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.BackgroundLight),
            contentAlignment = Alignment.Center
        ) {
            PrivacyDisclaimerPlaceholder(onAccept = {
                settingsRepo.setDisclaimerAccepted(true)
                disclaimerAccepted = true
            })
        }
        return
    }

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
                        records = state.records,
                        averageCycleLength = state.averageCycleLength,
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
                    value = state.averagePeriodLength?.toString() ?: "--",
                    unit = if (state.averagePeriodLength != null) "天" else "",
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
fun CalendarHistoryScreen(
    records: List<MenstrualRecord>,
    averageCycleLength: Int?,
    onClose: () -> Unit
) {
    val today = LocalDateKey.fromEpochMillis(currentEpochMillis())
    var displayYear by remember { mutableStateOf(today.year) }
    var displayMonth by remember { mutableStateOf(today.month) }

    val startDateSet = remember(records) { records.filter { !it.isDeleted }.map { it.startDate }.toSet() }
    val periodDaySet = remember(records) {
        val days = mutableSetOf<LocalDateKey>()
        for (record in records.filter { !it.isDeleted }) {
            val end = record.endDate ?: continue
            var d = record.startDate.nextDay()
            while (d <= end) { days.add(d); d = d.nextDay() }
        }
        days
    }

    val sortedRecords = remember(records) {
        records.filter { !it.isDeleted }.sortedByDescending { it.startDate }
    }

    val daysInCurrentMonth = daysInMonth(displayYear, displayMonth)
    val firstDayOffset = dayOfWeek(displayYear, displayMonth, 1)
    val dayHeaders = listOf("日", "一", "二", "三", "四", "五", "六")

    val periodDaysThisMonth = remember(records, displayYear, displayMonth) {
        val monthStart = LocalDateKey(displayYear, displayMonth, 1)
        val monthEnd = LocalDateKey(displayYear, displayMonth, daysInMonth(displayYear, displayMonth))
        var count = 0
        for (record in records.filter { !it.isDeleted }) {
            val end = record.endDate ?: record.startDate
            var d = record.startDate
            while (d <= end) { if (d >= monthStart && d <= monthEnd) count++; d = d.nextDay() }
        }
        count
    }

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
                style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary)
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Month Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (displayMonth == 1) { displayMonth = 12; displayYear-- } else displayMonth--
                },
                modifier = Modifier.size(48.dp).background(AppColors.Accent.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "上个月", tint = AppColors.Primary)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .clip(AppShapes.DateBlob)
                    .background(AppColors.Primary.copy(alpha = 0.1f))
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${displayYear}年 ${displayMonth}月",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary)
                )
            }
            IconButton(
                onClick = {
                    if (displayMonth == 12) { displayMonth = 1; displayYear++ } else displayMonth++
                },
                modifier = Modifier.size(48.dp).background(AppColors.Accent.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "下个月", tint = AppColors.Primary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calendar Grid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(AppColors.Accent.copy(alpha = 0.2f))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Day-of-week headers
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    dayHeaders.forEach { header ->
                        Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = header,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.Primary.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }

                val totalCells = firstDayOffset + daysInCurrentMonth
                val rows = (totalCells + 6) / 7
                repeat(rows) { rowIndex ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        repeat(7) { colIndex ->
                            val cellIndex = rowIndex * 7 + colIndex
                            val day = cellIndex - firstDayOffset + 1
                            if (day < 1 || day > daysInCurrentMonth) {
                                Spacer(modifier = Modifier.size(36.dp))
                            } else {
                                val date = LocalDateKey(displayYear, displayMonth, day)
                                val isStart = date in startDateSet
                                val isPeriodDay = date in periodDaySet
                                val isToday = date == today
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isStart -> AppColors.Primary.copy(alpha = 0.85f)
                                                isPeriodDay -> AppColors.Primary.copy(alpha = 0.25f)
                                                isToday -> AppColors.Accent.copy(alpha = 0.6f)
                                                else -> Color.Transparent
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$day",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = if (isStart) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isStart) Color.White else AppColors.Primary.copy(alpha = 0.75f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 本月概览 stats
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = AppColors.Primary.copy(alpha = 0.05f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "本月概览",
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(AppColors.Primary.copy(alpha = 0.4f)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "经期天数: ${if (periodDaysThisMonth > 0) "${periodDaysThisMonth}天" else "暂无记录"}",
                        color = AppColors.Primary.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(AppColors.Success.copy(alpha = 0.4f)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "周期长度: ${if (averageCycleLength != null) "${averageCycleLength}天" else "暂无数据"}",
                        color = AppColors.Primary.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Period history list
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = AppColors.Primary.copy(alpha = 0.05f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "历史记录",
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary)
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (sortedRecords.isEmpty()) {
                    Text(
                        text = "暂无记录",
                        style = TextStyle(fontSize = 14.sp, color = AppColors.Primary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    sortedRecords.forEachIndexed { index, record ->
                        if (index > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(AppColors.Primary.copy(alpha = 0.1f))
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.Primary.copy(alpha = 0.5f))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            val startStr = "${record.startDate.month}月${record.startDate.day}日"
                            val label = if (record.endDate != null) {
                                val endStr = "${record.endDate.month}月${record.endDate.day}日"
                                val days = daysBetween(record.startDate, record.endDate) + 1
                                "$startStr — $endStr (${days}天)"
                            } else {
                                "$startStr (仅开始日)"
                            }
                            Text(
                                text = label,
                                style = TextStyle(fontSize = 14.sp, color = AppColors.Primary.copy(alpha = 0.8f))
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
