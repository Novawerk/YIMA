package com.haodong.yimalaile.ui.pages.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.notifications.NotificationPrefs
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

@Composable
fun NotificationSettingsScreen(
    prefs: NotificationPrefs,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onPrefsChange: (NotificationPrefs) -> Unit,
    onBack: () -> Unit,
) {
    var showPeriodDialog by remember { mutableStateOf(false) }
    var showOvulationDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 32.dp),
    ) {
        // ── Top bar ──
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(Res.string.notifications_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(40.dp))
        }

        Spacer(Modifier.height(32.dp))

        // ── Permission banner ──
        if (!hasPermission) {
            PermissionBanner(onRequestPermission = onRequestPermission)
            Spacer(Modifier.height(20.dp))
        }

        // ── Reminder cards ──
        ReminderCard(
            icon = Icons.Outlined.WaterDrop,
            accent = MaterialTheme.colorScheme.primary,
            title = stringResource(Res.string.notifications_period_title),
            valueLabel = if (prefs.periodReminderEnabled)
                daysBeforeLabel(prefs.periodReminderDaysBefore)
            else
                stringResource(Res.string.notifications_off),
            enabled = prefs.periodReminderEnabled,
            onEnabledChange = { onPrefsChange(prefs.copy(periodReminderEnabled = it)) },
            onTap = { showPeriodDialog = true },
        )

        Spacer(Modifier.height(12.dp))

        ReminderCard(
            icon = Icons.Outlined.AutoAwesome,
            accent = MaterialTheme.colorScheme.tertiary,
            title = stringResource(Res.string.notifications_ovulation_title),
            valueLabel = if (prefs.ovulationReminderEnabled)
                daysBeforeLabel(prefs.ovulationReminderDaysBefore)
            else
                stringResource(Res.string.notifications_off),
            enabled = prefs.ovulationReminderEnabled,
            onEnabledChange = { onPrefsChange(prefs.copy(ovulationReminderEnabled = it)) },
            onTap = { showOvulationDialog = true },
        )

        Spacer(Modifier.height(12.dp))

        ReminderCard(
            icon = Icons.Outlined.WbSunny,
            accent = MaterialTheme.colorScheme.secondary,
            title = stringResource(Res.string.notifications_daily_title),
            valueLabel = if (prefs.dailyReportEnabled)
                formatTime(prefs.dailyReportHour, prefs.dailyReportMinute)
            else
                stringResource(Res.string.notifications_off),
            enabled = prefs.dailyReportEnabled,
            onEnabledChange = { onPrefsChange(prefs.copy(dailyReportEnabled = it)) },
            onTap = { showTimeDialog = true },
        )

        Spacer(Modifier.height(24.dp))

        // ── Footer hint ──
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                Icons.Outlined.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(Res.string.notifications_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            )
        }
    }

    if (showPeriodDialog) {
        SliderDialog(
            title = stringResource(Res.string.notifications_period_title),
            currentValue = prefs.periodReminderDaysBefore,
            valueRange = NotificationPrefs.MIN_DAYS_BEFORE.toFloat()..NotificationPrefs.MAX_DAYS_BEFORE.toFloat(),
            steps = NotificationPrefs.MAX_DAYS_BEFORE - NotificationPrefs.MIN_DAYS_BEFORE - 1,
            minLabel = NotificationPrefs.MIN_DAYS_BEFORE.toString(),
            maxLabel = NotificationPrefs.MAX_DAYS_BEFORE.toString(),
            onConfirm = {
                onPrefsChange(prefs.copy(periodReminderDaysBefore = it, periodReminderEnabled = true))
                showPeriodDialog = false
            },
            onDismiss = { showPeriodDialog = false },
        )
    }

    if (showOvulationDialog) {
        SliderDialog(
            title = stringResource(Res.string.notifications_ovulation_title),
            currentValue = prefs.ovulationReminderDaysBefore,
            valueRange = NotificationPrefs.MIN_DAYS_BEFORE.toFloat()..NotificationPrefs.MAX_DAYS_BEFORE.toFloat(),
            steps = NotificationPrefs.MAX_DAYS_BEFORE - NotificationPrefs.MIN_DAYS_BEFORE - 1,
            minLabel = NotificationPrefs.MIN_DAYS_BEFORE.toString(),
            maxLabel = NotificationPrefs.MAX_DAYS_BEFORE.toString(),
            onConfirm = {
                onPrefsChange(prefs.copy(ovulationReminderDaysBefore = it, ovulationReminderEnabled = true))
                showOvulationDialog = false
            },
            onDismiss = { showOvulationDialog = false },
        )
    }

    if (showTimeDialog) {
        TimePickerDialog(
            initialHour = prefs.dailyReportHour,
            initialMinute = prefs.dailyReportMinute,
            onConfirm = { h, m ->
                onPrefsChange(prefs.copy(dailyReportHour = h, dailyReportMinute = m, dailyReportEnabled = true))
                showTimeDialog = false
            },
            onDismiss = { showTimeDialog = false },
        )
    }
}

// ═════════════════════════════════════════════════════════════════
// Permission banner — compact, colored, with icon
// ═════════════════════════════════════════════════════════════════

@Composable
private fun PermissionBanner(onRequestPermission: () -> Unit) {
    Surface(
        onClick = onRequestPermission,
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.NotificationsOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(Res.string.notifications_permission_required),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    stringResource(Res.string.notifications_permission_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                )
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// Reminder card — icon badge, title, value pill, switch
// Whole card is tappable when enabled (opens adjust dialog).
// ═════════════════════════════════════════════════════════════════

@Composable
private fun ReminderCard(
    icon: ImageVector,
    accent: Color,
    title: String,
    valueLabel: String,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onTap: () -> Unit,
) {
    val containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val iconBgColor = if (enabled) accent.copy(alpha = 0.15f)
    else MaterialTheme.colorScheme.surfaceVariant
    val iconTint = if (enabled) accent
    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    val titleColor = if (enabled) MaterialTheme.colorScheme.onSurface
    else MaterialTheme.colorScheme.onSurfaceVariant
    val valueColor = if (enabled) accent
    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(containerColor)
            .then(if (enabled) Modifier.clickable(onClick = onTap) else Modifier)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                valueLabel,
                style = MaterialTheme.typography.bodySmall,
                color = valueColor,
                fontWeight = if (enabled) FontWeight.Medium else FontWeight.Normal,
            )
        }
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = enabled,
            onCheckedChange = onEnabledChange,
        )
    }
}

// ═════════════════════════════════════════════════════════════════
// Time picker dialog
// ═════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.notifications_daily_time)) },
        text = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(state = state)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text(stringResource(Res.string.dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.dialog_cancel))
            }
        },
    )
}

// ═════════════════════════════════════════════════════════════════
// Helpers
// ═════════════════════════════════════════════════════════════════

/** Picks the right singular/plural resource. */
@Composable
private fun daysBeforeLabel(days: Int): String = if (days == 1) {
    stringResource(Res.string.notifications_day_before)
} else {
    stringResource(Res.string.notifications_days_before, days)
}

private fun formatTime(hour: Int, minute: Int): String {
    val h = hour.toString().padStart(2, '0')
    val m = minute.toString().padStart(2, '0')
    return "$h:$m"
}
