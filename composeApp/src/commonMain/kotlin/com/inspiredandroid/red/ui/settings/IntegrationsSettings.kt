package com.inspiredandroid.red.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.inspiredandroid.red.getPlatformPath
import com.inspiredandroid.red.ui.handCursor
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher

@Composable
internal fun IntegrationsContent(
    state: SettingsUiState,
    actions: SettingsActions,
) {
    val directoryPicker = rememberDirectoryPickerLauncher { directory ->
        if (directory != null) {
            val path = directory.getPlatformPath()
            if (path != null) {
                actions.onChangeObsidianVaultPath(path)
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Obsidian Vault Sync
        SettingsCard {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Obsidian Vault",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Obsidian Integration",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "Sync a local Obsidian Vault directory to let the assistant read your notes, linking them as context for your questions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.obsidianVaultPath,
                        onValueChange = { actions.onChangeObsidianVaultPath(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("No vault path configured") },
                        label = { Text("Vault Directory Path") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                    )

                    Button(
                        shape = RoundedCornerShape(16.dp), 
                        onClick = { directoryPicker.launch() },
                        modifier = Modifier.handCursor()
                    ) {
                        Text("Browse")
                    }
                }
            }
        }

        // Extensible Skills Integration
        SettingsCard {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Skills and Tools",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Extensible Skills & Tools",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "Extend the AI's capabilities dynamically by enabling modular tools and installing custom skills (e.g. CLI operations, Web Search, calendar events, notifications).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    shape = RoundedCornerShape(16.dp), 
                    onClick = { actions.onSelectTab(SettingsTab.Tools) },
                    modifier = Modifier.handCursor()
                ) {
                    Text("Manage Skills & Tools")
                }
            }
        }
    }
}
