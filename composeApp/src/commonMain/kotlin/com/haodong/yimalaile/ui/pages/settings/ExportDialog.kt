package com.haodong.yimalaile.ui.pages.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.ExportStatus
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.dialog_cancel
import yimalaile.composeapp.generated.resources.dialog_confirm
import yimalaile.composeapp.generated.resources.export_button
import yimalaile.composeapp.generated.resources.export_dialog_body
import yimalaile.composeapp.generated.resources.export_dialog_title
import yimalaile.composeapp.generated.resources.export_failure_body
import yimalaile.composeapp.generated.resources.export_failure_title
import yimalaile.composeapp.generated.resources.export_in_progress
import yimalaile.composeapp.generated.resources.export_lang_chinese
import yimalaile.composeapp.generated.resources.export_lang_english
import yimalaile.composeapp.generated.resources.export_success_body
import yimalaile.composeapp.generated.resources.export_success_title

/**
 * Dialog that asks the user to pick a language for the PDF export.
 * Also surfaces in-progress / success / failure states from [ExportStatus]
 * in place, so the user never has to chase a toast.
 */
@Composable
internal fun ExportDialog(
    status: ExportStatus,
    onExport: (language: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedLanguage by remember { mutableStateOf("en") }
    val busy = status is ExportStatus.InProgress

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = {
            val title = when (status) {
                is ExportStatus.Success -> stringResource(Res.string.export_success_title)
                is ExportStatus.Failure -> stringResource(Res.string.export_failure_title)
                else -> stringResource(Res.string.export_dialog_title)
            }
            Text(title)
        },
        text = {
            when (status) {
                is ExportStatus.Success -> {
                    Text(stringResource(Res.string.export_success_body))
                }
                is ExportStatus.Failure -> {
                    Text(stringResource(Res.string.export_failure_body, status.message))
                }
                is ExportStatus.InProgress -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(Res.string.export_in_progress))
                    }
                }
                ExportStatus.Idle -> {
                    Column {
                        Text(stringResource(Res.string.export_dialog_body))
                        Spacer(Modifier.height(16.dp))
                        LanguageOption(
                            label = stringResource(Res.string.export_lang_english),
                            selected = selectedLanguage == "en",
                            onClick = { selectedLanguage = "en" },
                        )
                        Spacer(Modifier.height(8.dp))
                        LanguageOption(
                            label = stringResource(Res.string.export_lang_chinese),
                            selected = selectedLanguage == "zh",
                            onClick = { selectedLanguage = "zh" },
                        )
                    }
                }
            }
        },
        confirmButton = {
            when (status) {
                ExportStatus.Idle -> {
                    TextButton(onClick = { onExport(selectedLanguage) }) {
                        Text(stringResource(Res.string.export_button))
                    }
                }
                is ExportStatus.Success, is ExportStatus.Failure -> {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(Res.string.dialog_confirm))
                    }
                }
                is ExportStatus.InProgress -> {
                    // No action while busy.
                }
            }
        },
        dismissButton = if (status is ExportStatus.Idle) {
            {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.dialog_cancel))
                }
            }
        } else null,
    )
}

@Composable
private fun LanguageOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        color = if (selected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .selectable(selected = selected, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected, onClick = null)
            Spacer(Modifier.width(12.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
