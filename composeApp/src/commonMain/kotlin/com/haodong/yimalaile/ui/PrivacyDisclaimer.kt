package com.haodong.yimalaile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.disclaimer_accept
import yimalaile.composeapp.generated.resources.disclaimer_body
import yimalaile.composeapp.generated.resources.disclaimer_title

/**
 * Phase 0 placeholder: simple, local-only disclaimer UI.
 * Not wired into App() yet to avoid behavior changes. Persistence will be added in storage phase.
 */
@Composable
fun PrivacyDisclaimerPlaceholder(
    onAccept: (() -> Unit)? = null,
) {
    val acceptedState = remember { mutableStateOf(false) }
    if (acceptedState.value) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(Res.string.disclaimer_title),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = stringResource(Res.string.disclaimer_body),
            style = MaterialTheme.typography.bodyMedium
        )
        Button(onClick = {
            acceptedState.value = true
            onAccept?.invoke()
        }) {
            Text(text = stringResource(Res.string.disclaimer_accept))
        }
    }
}
