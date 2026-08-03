package com.inspiredandroid.red.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.inspiredandroid.red.data.ThemeMode
import com.inspiredandroid.red.data.AppSettings
import com.inspiredandroid.red.data.AppColorScheme
import org.koin.compose.koinInject
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inspiredandroid.red.ui.RedOutlinedTextField
import com.inspiredandroid.red.ui.components.RedSlider
import com.inspiredandroid.red.ui.handCursor
import red.composeapp.generated.resources.Res
import red.composeapp.generated.resources.ic_arrow_drop_down
import red.composeapp.generated.resources.settings_daemon_mode
import red.composeapp.generated.resources.settings_daemon_mode_description
import red.composeapp.generated.resources.settings_dynamic_ui
import red.composeapp.generated.resources.settings_dynamic_ui_description
import red.composeapp.generated.resources.settings_theme
import red.composeapp.generated.resources.settings_theme_dark
import red.composeapp.generated.resources.settings_theme_description
import red.composeapp.generated.resources.settings_theme_light
import red.composeapp.generated.resources.settings_theme_oled
import red.composeapp.generated.resources.settings_theme_system
import red.composeapp.generated.resources.settings_ui_scale
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import kotlin.math.roundToInt

@Composable
internal fun GeneralContent(uiState: SettingsUiState, actions: SettingsActions) {
    val appSettings: AppSettings = koinInject()
    val colorScheme by appSettings.colorSchemeFlow.collectAsStateWithLifecycle()

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val useStaggered = maxWidth >= 600.dp
        if (useStaggered) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (uiState.showDaemonToggle) {
                        SettingsCard {
                            DaemonModeToggle(
                                isDaemonEnabled = uiState.isDaemonEnabled,
                                onToggleDaemon = actions.onToggleDaemon,
                            )
                        }
                    }
                    SettingsCard {
                        DynamicUiToggle(
                            isDynamicUiEnabled = uiState.isDynamicUiEnabled,
                            onToggleDynamicUi = actions.onToggleDynamicUi,
                        )
                    }

                    SettingsCard {
                        ColorSchemePicker(
                            colorScheme = colorScheme,
                            onChangeColorScheme = { appSettings.setColorScheme(it) },
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (uiState.showUiScale) {
                        SettingsCard {
                            UiScaleSection(
                                uiScale = uiState.uiScale,
                                onChangeUiScale = actions.onChangeUiScale,
                            )
                        }
                    }
                    SettingsCard {
                        ExportImportSection(
                            onExportSettings = actions.onExportSettings,
                            onPrepareExport = actions.onPrepareExport,
                            onImportSettings = actions.onImportSettings,
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (uiState.showDaemonToggle) {
                    SettingsCard {
                        DaemonModeToggle(
                            isDaemonEnabled = uiState.isDaemonEnabled,
                            onToggleDaemon = actions.onToggleDaemon,
                        )
                    }
                }
                SettingsCard {
                    DynamicUiToggle(
                        isDynamicUiEnabled = uiState.isDynamicUiEnabled,
                        onToggleDynamicUi = actions.onToggleDynamicUi,
                    )
                }

                SettingsCard {
                    ColorSchemePicker(
                        colorScheme = colorScheme,
                        onChangeColorScheme = { appSettings.setColorScheme(it) },
                    )
                }
                if (uiState.showUiScale) {
                    SettingsCard {
                        UiScaleSection(
                            uiScale = uiState.uiScale,
                            onChangeUiScale = actions.onChangeUiScale,
                        )
                    }
                }
                SettingsCard {
                    ExportImportSection(
                        onExportSettings = actions.onExportSettings,
                        onPrepareExport = actions.onPrepareExport,
                        onImportSettings = actions.onImportSettings,
                    )
                }
            }
        }
    }
}

@Composable
private fun DaemonModeToggle(
    isDaemonEnabled: Boolean,
    onToggleDaemon: (Boolean) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ToggleableHeadline(
            title = stringResource(Res.string.settings_daemon_mode),
            description = stringResource(Res.string.settings_daemon_mode_description),
            checked = isDaemonEnabled,
            onCheckedChange = onToggleDaemon,
        )
    }
}

@Composable
private fun DynamicUiToggle(
    isDynamicUiEnabled: Boolean,
    onToggleDynamicUi: (Boolean) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ToggleableHeadline(
            title = stringResource(Res.string.settings_dynamic_ui),
            description = stringResource(Res.string.settings_dynamic_ui_description),
            checked = isDynamicUiEnabled,
            onCheckedChange = onToggleDynamicUi,
        )
    }
}


@Composable
private fun UiScaleSection(
    uiScale: Float,
    onChangeUiScale: (Float) -> Unit,
) {
    var sliderValue by remember(uiScale) { mutableStateOf(uiScale) }
    val steps = 14 // 16 snap points from 50% to 200% in 10% increments (14 intermediate)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.settings_ui_scale),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "${(sliderValue * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        RedSlider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onChangeUiScale(sliderValue) },
            valueRange = 0.5f..2.0f,
            steps = steps,
        )
    }
}

@Composable
private fun ColorSchemePicker(
    colorScheme: AppColorScheme,
    onChangeColorScheme: (AppColorScheme) -> Unit,
) {
    val options = listOf(
        AppColorScheme.PixelPlayer to "PixelPlayer (Purple & Pink)",
        AppColorScheme.AdwaitaBlack to "Adwaita Black",
        AppColorScheme.AdwaitaBlackLightBlue to "Adwaita Black (Light Blue)",
        AppColorScheme.Claymorphism to "Claymorphism (Dark Adwaita)",
    )
    val selectedLabel = options.firstOrNull { it.first == colorScheme }?.second ?: "PixelPlayer (Purple & Pink)"
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Color Scheme",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Choose the application color palette",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            RedOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(
                        modifier = Modifier.handCursor(),
                        imageVector = vectorResource(Res.drawable.ic_arrow_drop_down),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                },
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .handCursor()
                    .clickable { expanded = true },
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = RoundedCornerShape(16.dp),
            ) {
                options.forEach { (scheme, label) ->
                    val isSelected = scheme == colorScheme
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                        },
                        onClick = {
                            expanded = false
                            onChangeColorScheme(scheme)
                        },
                        modifier = Modifier
                            .handCursor()
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .padding(horizontal = 4.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            RoundedCornerShape(8.dp),
                                        )
                                } else {
                                    Modifier
                                },
                            ),
                    )
                }
            }
        }
    }
}

