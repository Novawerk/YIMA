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
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.compose_multiplatform
import yimalaile.composeapp.generated.resources.*

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

    // Capture snackbar strings in composable scope before passing to coroutine lambdas
    val msgSuccess = stringResource(Res.string.record_save_success)
    val msgDuplicate = stringResource(Res.string.record_duplicate_date)
    val msgTooClose = stringResource(Res.string.record_too_close)
    val msgError = stringResource(Res.string.record_save_error)

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
                                    snackbarHostState.showSnackbar(msgSuccess)
                                }
                                is AddRecordResult.DuplicateStartDate -> {
                                    snackbarHostState.showSnackbar(msgDuplicate)
                                }
                                is AddRecordResult.TooCloseToOtherRecord -> {
                                    snackbarHostState.showSnackbar(msgTooClose)
                                }
                                else -> {
                                    snackbarHostState.showSnackbar(msgError)
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

    val strStatusNotHere = stringResource(Res.string.status_not_here)
    val strStatusWaitingDesc = stringResource(Res.string.status_waiting_desc)
    val strStatusHere = stringResource(Res.string.status_here)
    val strStatusHereDesc = stringResource(Res.string.status_here_desc)
    val strStatusSoon = stringResource(Res.string.status_soon)
    val strStatusSoonDesc = stringResource(Res.string.status_soon_desc)
    val strStatusRelaxDesc = stringResource(Res.string.status_relax_desc)
    val strNoPrediction = stringResource(Res.string.status_no_prediction)

    var statusText = strStatusNotHere
    var statusDescription = strStatusWaitingDesc
    var predictionText = if (predictedNext != null) {
        stringResource(Res.string.status_predicted_date, predictedNext.month, predictedNext.day)
    } else {
        strNoPrediction
    }
    var statusColor = AppColors.Primary

    if (lastPeriodDate != null && predictedNext != null) {
        val days = daysBetween(today, predictedNext)
        if (days <= 0) {
            statusText = strStatusHere
            statusDescription = strStatusHereDesc
            statusColor = AppColors.Primary
        } else if (days <= 3) {
            statusText = strStatusSoon
            statusDescription = strStatusSoonDesc
            statusColor = AppColors.Warning
        } else {
            statusText = strStatusNotHere
            statusDescription = strStatusRelaxDesc
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
                    text = stringResource(Res.string.home_title),
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
                    title = stringResource(Res.string.stat_cycle_length),
                    value = avgCycle.toString(),
                    unit = stringResource(Res.string.unit_days),
                    backgroundColor = AppColors.CardBg1,
                    textColor = AppColors.Primary,
                    shape = AppShapes.LeftBlob,
                    modifier = Modifier.weight(1f)
                )
                StatCardNew(
                    title = stringResource(Res.string.stat_period_length),
                    value = state.averagePeriodLength?.toString() ?: "--",
                    unit = if (state.averagePeriodLength != null) stringResource(Res.string.unit_days) else "",
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
                        text = stringResource(Res.string.home_motivational_quote),
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
                    stringResource(Res.string.btn_record_period),
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
    val dayHeaders = listOf(
        stringResource(Res.string.calendar_day_sun),
        stringResource(Res.string.calendar_day_mon),
        stringResource(Res.string.calendar_day_tue),
        stringResource(Res.string.calendar_day_wed),
        stringResource(Res.string.calendar_day_thu),
        stringResource(Res.string.calendar_day_fri),
        stringResource(Res.string.calendar_day_sat)
    )

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

    val strNavBack = stringResource(Res.string.nav_back)
    val strCalendarTitle = stringResource(Res.string.calendar_title)
    val strPrevMonth = stringResource(Res.string.calendar_prev_month)
    val strNextMonth = stringResource(Res.string.calendar_next_month)
    val strMonthOverview = stringResource(Res.string.calendar_month_overview)
    val strPeriodDaysCount = if (periodDaysThisMonth > 0) {
        stringResource(Res.string.calendar_period_days_count, periodDaysThisMonth)
    } else {
        stringResource(Res.string.calendar_period_days_none)
    }
    val strCycleLengthStat = if (averageCycleLength != null) {
        stringResource(Res.string.calendar_cycle_length_count, averageCycleLength)
    } else {
        stringResource(Res.string.calendar_cycle_length_none)
    }
    val strHistoryTitle = stringResource(Res.string.calendar_history)
    val strNoRecords = stringResource(Res.string.calendar_no_records)
    val strStartOnly = stringResource(Res.string.calendar_record_start_only)

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
                Icon(Icons.Default.ArrowBack, contentDescription = strNavBack, tint = AppColors.Primary)
            }
            Text(
                text = strCalendarTitle,
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
                Icon(Icons.Default.ArrowBack, contentDescription = strPrevMonth, tint = AppColors.Primary)
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
                    text = stringResource(Res.string.calendar_year_month, displayYear, displayMonth),
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary)
                )
            }
            IconButton(
                onClick = {
                    if (displayMonth == 12) { displayMonth = 1; displayYear++ } else displayMonth++
                },
                modifier = Modifier.size(48.dp).background(AppColors.Accent.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = strNextMonth, tint = AppColors.Primary)
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

        // This month stats
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = AppColors.Primary.copy(alpha = 0.05f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = strMonthOverview,
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(AppColors.Primary.copy(alpha = 0.4f)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strPeriodDaysCount,
                        color = AppColors.Primary.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(AppColors.Success.copy(alpha = 0.4f)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strCycleLengthStat,
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
                    text = strHistoryTitle,
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary)
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (sortedRecords.isEmpty()) {
                    Text(
                        text = strNoRecords,
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
                            val startStr = stringResource(Res.string.date_month_day, record.startDate.month, record.startDate.day)
                            val label = if (record.endDate != null) {
                                val endStr = stringResource(Res.string.date_month_day, record.endDate.month, record.endDate.day)
                                val days = daysBetween(record.startDate, record.endDate) + 1
                                "$startStr — $endStr ${stringResource(Res.string.calendar_record_days, days)}"
                            } else {
                                "$startStr $strStartOnly"
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
