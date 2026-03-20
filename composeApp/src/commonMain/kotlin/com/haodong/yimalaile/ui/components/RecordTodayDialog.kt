package com.haodong.yimalaile.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.haodong.yimalaile.data.*
import com.haodong.yimalaile.ui.theme.AppShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordTodayDialog(
    colorPrimary: Color,
    colorAccent: Color,
    colorBackgroundLight: Color,
    onDismiss: () -> Unit,
    onSave: (MenstrualRecord) -> Unit
) {
    val today = remember { LocalDateKey.fromEpochMillis(currentEpochMillis()) }
    var selectedDate by remember { mutableStateOf(today) }
    var selectedIntensity by remember { mutableStateOf<Intensity?>(null) }
    var selectedMood by remember { mutableStateOf<Mood?>(null) }
    var selectedSymptoms by remember { mutableStateOf(setOf<String>()) }
    var noteText by remember { mutableStateOf("") }
    
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toEpochMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = LocalDateKey.fromEpochMillis(it)
                    }
                    showDatePicker = false
                }) {
                    Text("确定", color = colorPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消", color = colorPrimary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp)
                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)),
            color = colorBackgroundLight
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(colorPrimary.copy(alpha = 0.2f))
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "记录今天",
                    style = TextStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorPrimary
                    )
                )

                Surface(
                    shape = CircleShape,
                    color = colorAccent.copy(alpha = 0.2f),
                    modifier = Modifier.padding(top = 8.dp),
                    onClick = { showDatePicker = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val dateText = if (selectedDate == today) {
                            "Today, ${selectedDate.getMonthName()} ${selectedDate.day}"
                        } else {
                            "${selectedDate.getMonthName()} ${selectedDate.day}, ${selectedDate.year}"
                        }
                        Text(
                            dateText,
                            style = TextStyle(color = colorPrimary.copy(alpha = 0.5f), fontSize = 14.sp)
                        )
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = colorPrimary.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Date selector (last 30 days)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val last30Days = (0 downTo -29).map { today.plusDays(it) }.reversed()
                    last30Days.forEach { date ->
                        val isSelected = date == selectedDate
                        val isActuallyToday = date == today
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .size(width = 64.dp, height = 80.dp)
                                .clip(AppShapes.DateBlob)
                                .background(if (isSelected) colorPrimary.copy(alpha = 0.2f) else colorAccent.copy(alpha = 0.1f))
                                .clickable { selectedDate = date }
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                date.getMonthName(),
                                style = TextStyle(
                                    fontSize = 10.sp,
                                    color = colorPrimary.copy(alpha = 0.4f),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                date.day.toString().padStart(2, '0'),
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    color = colorPrimary,
                                    fontWeight = FontWeight.Black
                                )
                            )
                            if (isActuallyToday) {
                                Text(
                                    "TODAY",
                                    style = TextStyle(
                                        fontSize = 8.sp,
                                        color = colorPrimary.copy(alpha = 0.4f),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Section: Flow Intensity
                RecordSectionTitle("经量强度", Icons.Default.WaterDrop, colorPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IntensityItem("少量", Intensity.LIGHT, selectedIntensity == Intensity.LIGHT, colorPrimary, colorAccent, AppShapes.Blob1) {
                        selectedIntensity = Intensity.LIGHT
                    }
                    IntensityItem("中量", Intensity.MEDIUM, selectedIntensity == Intensity.MEDIUM, colorPrimary, colorAccent, AppShapes.Blob2) {
                        selectedIntensity = Intensity.MEDIUM
                    }
                    IntensityItem("多量", Intensity.HEAVY, selectedIntensity == Intensity.HEAVY, colorPrimary, colorAccent, AppShapes.Blob3) {
                        selectedIntensity = Intensity.HEAVY
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section: Mood
                RecordSectionTitle("今日心情", Icons.Default.Mood, colorPrimary)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(100.dp))
                            .background(colorAccent.copy(alpha = 0.1f))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MoodItem("开心", Mood.HAPPY, Icons.Default.SentimentSatisfied, selectedMood == Mood.HAPPY, colorPrimary) {
                            selectedMood = Mood.HAPPY
                        }
                        MoodItem("一般", Mood.NEUTRAL, Icons.Default.SentimentNeutral, selectedMood == Mood.NEUTRAL, colorPrimary) {
                            selectedMood = Mood.NEUTRAL
                        }
                        MoodItem("低落", Mood.SAD, Icons.Default.SentimentDissatisfied, selectedMood == Mood.SAD, colorPrimary) {
                            selectedMood = Mood.SAD
                        }
                        MoodItem("难受", Mood.VERY_SAD, Icons.Default.MoodBad, selectedMood == Mood.VERY_SAD, colorPrimary) {
                            selectedMood = Mood.VERY_SAD
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section: Symptoms
                RecordSectionTitle("身体症状", Icons.Default.MedicalServices, colorPrimary)
                val symptoms = listOf("腹痛", "腰酸", "头痛", "胸部胀痛", "疲倦")
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 8.dp
                ) {
                    symptoms.forEach { symptom ->
                        val isSelected = selectedSymptoms.contains(symptom)
                        SymptomChip(symptom, isSelected, colorPrimary, colorAccent) {
                            selectedSymptoms = if (isSelected) {
                                selectedSymptoms - symptom
                            } else {
                                selectedSymptoms + symptom
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section: Notes
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .drawBehind {
                            val stroke = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                            drawRoundRect(
                                color = colorPrimary.copy(alpha = 0.2f),
                                cornerRadius = CornerRadius(16.dp.toPx()),
                                style = stroke
                            )
                        }
                        .padding(16.dp)
                ) {
                    if (noteText.isEmpty()) {
                        Text(
                            "写下今天的小心情...",
                            style = TextStyle(color = colorPrimary.copy(alpha = 0.3f), fontSize = 16.sp)
                        )
                    }
                    BasicTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        modifier = Modifier.fillMaxSize(),
                        textStyle = TextStyle(color = colorPrimary, fontSize = 16.sp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Save Button
                Button(
                    onClick = {
                        val now = selectedDate.toEpochMillis()
                        onSave(
                            MenstrualRecord(
                                id = Ids.newId("rec"),
                                startDate = selectedDate,
                                intensity = selectedIntensity,
                                mood = selectedMood,
                                symptoms = selectedSymptoms.toList(),
                                notes = noteText,
                                createdAtEpochMillis = now,
                                updatedAtEpochMillis = now
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(80.dp).padding(vertical = 8.dp),
                    shape = RoundedCornerShape(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorPrimary)
                ) {
                    Text("保存记录", style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Black))
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("取消", style = TextStyle(color = colorPrimary.copy(alpha = 0.5f), fontSize = 16.sp))
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun RecordSectionTitle(title: String, icon: ImageVector, colorPrimary: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = colorPrimary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            title,
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorPrimary.copy(alpha = 0.7f)
            )
        )
    }
}

@Composable
fun IntensityItem(
    label: String,
    intensity: Intensity,
    isSelected: Boolean,
    colorPrimary: Color,
    colorAccent: Color,
    shape: Shape,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(shape)
                .background(if (isSelected) colorAccent.copy(alpha = 0.6f) else colorAccent.copy(alpha = 0.1f))
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) colorPrimary.copy(alpha = 0.3f) else Color.Transparent,
                    shape = shape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.WaterDrop,
                contentDescription = null,
                tint = colorPrimary.copy(alpha = if (isSelected) 0.8f else 0.4f),
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label,
            style = TextStyle(
                color = colorPrimary.copy(alpha = if (isSelected) 0.8f else 0.4f),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        )
    }
}

@Composable
fun MoodItem(
    label: String,
    mood: Mood,
    icon: ImageVector,
    isSelected: Boolean,
    colorPrimary: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(if (isSelected) colorPrimary.copy(alpha = 0.3f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = colorPrimary.copy(alpha = if (isSelected) 1f else 0.3f),
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label,
            style = TextStyle(
                color = colorPrimary.copy(alpha = if (isSelected) 1f else 0.3f),
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        )
    }
}

@Composable
fun SymptomChip(
    label: String,
    isSelected: Boolean,
    colorPrimary: Color,
    colorAccent: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = AppShapes.SymptomChipShape,
        color = if (isSelected) colorPrimary.copy(alpha = 0.7f) else colorAccent.copy(alpha = 0.2f),
        interactionSource = remember { MutableInteractionSource() },
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = TextStyle(
                    color = if (isSelected) Color.White else colorPrimary.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: Dp = 0.dp,
    crossAxisSpacing: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(mainAxisSpacing),
        verticalArrangement = Arrangement.spacedBy(crossAxisSpacing),
        content = { content() }
    )
}
