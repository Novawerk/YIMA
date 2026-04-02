package com.haodong.yimalaile.ui.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.menstrual.Intensity
import com.haodong.yimalaile.domain.menstrual.Mood
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.intensity_heavy
import yimalaile.composeapp.generated.resources.intensity_light
import yimalaile.composeapp.generated.resources.intensity_medium
import yimalaile.composeapp.generated.resources.mood_happy
import yimalaile.composeapp.generated.resources.mood_neutral
import yimalaile.composeapp.generated.resources.mood_sad
import yimalaile.composeapp.generated.resources.mood_very_sad
import yimalaile.composeapp.generated.resources.record_flow_intensity
import yimalaile.composeapp.generated.resources.record_mood
import yimalaile.composeapp.generated.resources.record_notes_hint
import yimalaile.composeapp.generated.resources.record_save_btn
import yimalaile.composeapp.generated.resources.record_symptoms
import yimalaile.composeapp.generated.resources.symptom_back_pain
import yimalaile.composeapp.generated.resources.symptom_breast_pain
import yimalaile.composeapp.generated.resources.symptom_cramps
import yimalaile.composeapp.generated.resources.symptom_fatigue
import yimalaile.composeapp.generated.resources.symptom_headache

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LogDaySheet(
    onDismiss: () -> Unit,
    onSave: (Intensity?, Mood?, List<String>, String?) -> Unit,
) {
    var selectedIntensity by remember { mutableStateOf<Intensity?>(null) }
    var selectedMood by remember { mutableStateOf<Mood?>(null) }
    var selectedSymptoms by remember { mutableStateOf(setOf<String>()) }
    var notes by remember { mutableStateOf("") }

    val intensityLabels = listOf(
        Intensity.LIGHT to Res.string.intensity_light,
        Intensity.MEDIUM to Res.string.intensity_medium,
        Intensity.HEAVY to Res.string.intensity_heavy,
    )
    val moodLabels = listOf(
        Mood.HAPPY to Res.string.mood_happy,
        Mood.NEUTRAL to Res.string.mood_neutral,
        Mood.SAD to Res.string.mood_sad,
        Mood.VERY_SAD to Res.string.mood_very_sad,
    )
    val symptomLabels = listOf(
        "cramps" to Res.string.symptom_cramps,
        "back_pain" to Res.string.symptom_back_pain,
        "headache" to Res.string.symptom_headache,
        "breast_pain" to Res.string.symptom_breast_pain,
        "fatigue" to Res.string.symptom_fatigue,
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(Modifier.padding(16.dp)) {
            // Intensity
            Text(stringResource(Res.string.record_flow_intensity), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                intensityLabels.forEach { (value, labelRes) ->
                    FilterChip(
                        selected = selectedIntensity == value,
                        onClick = { selectedIntensity = if (selectedIntensity == value) null else value },
                        label = { Text(stringResource(labelRes)) },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Mood
            Text(stringResource(Res.string.record_mood), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                moodLabels.forEach { (value, labelRes) ->
                    FilterChip(
                        selected = selectedMood == value,
                        onClick = { selectedMood = if (selectedMood == value) null else value },
                        label = { Text(stringResource(labelRes)) },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Symptoms
            Text(stringResource(Res.string.record_symptoms), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                symptomLabels.forEach { (key, labelRes) ->
                    FilterChip(
                        selected = key in selectedSymptoms,
                        onClick = {
                            selectedSymptoms = if (key in selectedSymptoms)
                                selectedSymptoms - key else selectedSymptoms + key
                        },
                        label = { Text(stringResource(labelRes)) },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(Res.string.record_notes_hint)) },
                minLines = 2,
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    onSave(
                        selectedIntensity,
                        selectedMood,
                        selectedSymptoms.toList(),
                        notes.takeIf { it.isNotBlank() },
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.record_save_btn))
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
