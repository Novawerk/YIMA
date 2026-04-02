package com.haodong.yimalaile.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.app_version
import yimalaile.composeapp.generated.resources.disclaimer_view_again
import yimalaile.composeapp.generated.resources.nav_back
import yimalaile.composeapp.generated.resources.settings_language
import yimalaile.composeapp.generated.resources.settings_language_follow_system
import yimalaile.composeapp.generated.resources.settings_privacy
import yimalaile.composeapp.generated.resources.settings_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.nav_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            ListItem(
                headlineContent = { Text(stringResource(Res.string.settings_language)) },
                supportingContent = { Text(stringResource(Res.string.settings_language_follow_system)) },
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text(stringResource(Res.string.settings_privacy)) },
                modifier = Modifier.clickable { /* TODO: show disclaimer */ },
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text(stringResource(Res.string.app_version)) },
                supportingContent = { Text("1.0.0") },
            )
        }
    }
}
