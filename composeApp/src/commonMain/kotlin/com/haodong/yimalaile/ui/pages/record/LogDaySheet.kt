package com.haodong.yimalaile.ui.pages.record

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.domain.menstrual.Intensity
import com.haodong.yimalaile.domain.menstrual.Mood
import com.haodong.yimalaile.ui.components.PrimaryCta
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LogDaySheet(
    targetDate: LocalDate? = null,
    onDismiss: () -> Unit,
    onSave: (Intensity?, Mood?, List<String>, String?) -> Unit,
) {
    var selectedIntensity by remember { mutableStateOf<Intensity?>(null) }
    var selectedMood by remember { mutableStateOf<Mood?>(null) }
    var selectedSymptoms by remember { mutableStateOf(setOf<String>()) }
    var notes by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(Modifier.padding(24.dp)) {
            val title = if (targetDate != null) {
                stringResource(Res.string.record_date_title, targetDate.monthNumber, targetDate.dayOfMonth)
            } else {
                stringResource(Res.string.record_dialog_title)
            }
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(Modifier.height(24.dp))

            // Intensity
            Text(stringResource(Res.string.record_flow_intensity), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf(
                    Triple(Intensity.LIGHT, stringResource(Res.string.intensity_light), 10.dp),
                    Triple(Intensity.MEDIUM, stringResource(Res.string.intensity_medium), 18.dp),
                    Triple(Intensity.HEAVY, stringResource(Res.string.intensity_heavy), 26.dp),
                ).forEach { (value, label, dotSize) ->
                    DotOption(
                        dotSize = dotSize,
                        label = label,
                        selected = selectedIntensity == value,
                        onClick = { selectedIntensity = if (selectedIntensity == value) null else value },
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Mood
            Text(stringResource(Res.string.record_mood), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    Triple(Mood.HAPPY, stringResource(Res.string.mood_happy), "😊"),
                    Triple(Mood.NEUTRAL, stringResource(Res.string.mood_neutral), "😐"),
                    Triple(Mood.SAD, stringResource(Res.string.mood_sad), "😔"),
                    Triple(Mood.VERY_SAD, stringResource(Res.string.mood_very_sad), "😢"),
                ).forEach { (value, label, icon) ->
                    IconOption(
                        icon = icon,
                        label = label,
                        selected = selectedMood == value,
                        onClick = { selectedMood = if (selectedMood == value) null else value },
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Symptoms
            Text(stringResource(Res.string.record_symptoms), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "cramps" to stringResource(Res.string.symptom_cramps),
                    "back_pain" to stringResource(Res.string.symptom_back_pain),
                    "headache" to stringResource(Res.string.symptom_headache),
                    "breast_pain" to stringResource(Res.string.symptom_breast_pain),
                    "fatigue" to stringResource(Res.string.symptom_fatigue),
                ).forEach { (key, label) ->
                    PillChip(
                        label = label,
                        selected = key in selectedSymptoms,
                        onClick = {
                            selectedSymptoms = if (key in selectedSymptoms) selectedSymptoms - key else selectedSymptoms + key
                        },
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(Res.string.record_notes_hint)) },
                minLines = 2,
                shape = RoundedCornerShape(16.dp),
            )

            Spacer(Modifier.height(24.dp))

            PrimaryCta(
                text = stringResource(Res.string.record_save_btn),
                onClick = {
                    onSave(selectedIntensity, selectedMood, selectedSymptoms.toList(), notes.takeIf { it.isNotBlank() })
                },
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DotOption(dotSize: Dp, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = if (selected) MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    else MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun IconOption(icon: String, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(icon, fontSize = 24.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = if (selected) MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    else MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PillChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground)
    }
}
