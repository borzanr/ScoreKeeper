package com.scorekeeper.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.scorekeeper.app.BuildConfig
import com.scorekeeper.app.R
import com.scorekeeper.app.ads.ConsentManager
import com.scorekeeper.app.ui.AppLanguage
import com.scorekeeper.app.ui.LUCKYDRAW_PACKAGE
import com.scorekeeper.app.ui.components.BackTopBar
import com.scorekeeper.app.ui.findActivity
import com.scorekeeper.app.ui.openPlayStore
import com.scorekeeper.app.ui.shareText

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val shareTitle = stringResource(R.string.settings_share_app)
    val systemLanguage = stringResource(R.string.settings_language_system)
    val currentLanguage = remember { AppLanguage.current(context) }
    var choosingLanguage by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BackTopBar(title = stringResource(R.string.settings_title), onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsItem(
                icon = Icons.Filled.Language,
                title = stringResource(R.string.settings_language),
                subtitle = AppLanguage.nativeName(currentLanguage) ?: systemLanguage,
                onClick = { choosingLanguage = true },
            )
            // A UE exige que o usuário consiga rever o consentimento a qualquer momento.
            SettingsItem(
                icon = Icons.Filled.PrivacyTip,
                title = stringResource(R.string.settings_privacy),
                subtitle = stringResource(R.string.settings_privacy_desc),
                onClick = { context.findActivity()?.let { ConsentManager.showPrivacyOptions(it) } },
            )
            SettingsItem(
                icon = Icons.Filled.Star,
                title = stringResource(R.string.settings_rate),
                onClick = { context.openPlayStore(context.packageName) },
            )
            SettingsItem(
                icon = Icons.Filled.Share,
                title = shareTitle,
                onClick = {
                    val storeUrl = context.getString(R.string.play_store_url, context.packageName)
                    context.shareText(context.getString(R.string.share_app_body, storeUrl), shareTitle)
                },
            )
            SettingsItem(
                icon = Icons.Filled.Casino,
                title = stringResource(R.string.settings_luckydraw),
                subtitle = stringResource(R.string.settings_luckydraw_desc),
                onClick = { context.openPlayStore(LUCKYDRAW_PACKAGE) },
            )
            Text(
                text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    if (choosingLanguage) {
        AlertDialog(
            onDismissRequest = { choosingLanguage = false },
            title = { Text(stringResource(R.string.settings_language)) },
            text = {
                Column {
                    AppLanguage.OPTIONS.forEach { tag ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.small)
                                .clickable {
                                    choosingLanguage = false
                                    if (tag != currentLanguage) {
                                        AppLanguage.set(context, tag)
                                        // Recria a tela para os textos saírem no idioma novo;
                                        // a navegação volta para Ajustes.
                                        context.findActivity()?.recreate()
                                    }
                                }
                                .padding(vertical = 4.dp),
                        ) {
                            RadioButton(
                                selected = tag == currentLanguage,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary),
                            )
                            Text(
                                text = AppLanguage.nativeName(tag) ?: systemLanguage,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(start = 12.dp),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { choosingLanguage = false }) { Text(stringResource(R.string.action_cancel)) }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    subtitle: String? = null,
) {
    ListItem(
        headlineContent = { Text(title, style = MaterialTheme.typography.titleMedium) },
        supportingContent = subtitle?.let { { Text(it, style = MaterialTheme.typography.bodyMedium) } },
        leadingContent = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background,
            headlineColor = MaterialTheme.colorScheme.onBackground,
            supportingColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = Modifier.clickable(onClick = onClick),
    )
}
