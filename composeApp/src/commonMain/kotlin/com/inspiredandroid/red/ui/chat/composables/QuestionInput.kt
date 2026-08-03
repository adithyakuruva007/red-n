package com.inspiredandroid.red.ui.chat.composables

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.height
import com.inspiredandroid.red.rememberPlatformCameraLauncher
import com.inspiredandroid.red.PlatformCameraLauncher
import com.inspiredandroid.red.rememberPlatformSpeechRecognizer
import com.inspiredandroid.red.Platform
import com.inspiredandroid.red.currentPlatform
import com.inspiredandroid.red.data.ServiceEntry
import com.inspiredandroid.red.data.imageExtensions
import com.inspiredandroid.red.skills.SkillManifest
import com.inspiredandroid.red.ui.rememberGradientBrush
import com.inspiredandroid.red.ui.handCursor
import com.inspiredandroid.red.ui.redInputSurface
import com.inspiredandroid.red.ui.redIconButtonSurface
import com.inspiredandroid.red.data.AppSettings
import com.inspiredandroid.red.data.AppColorScheme
import org.koin.compose.koinInject
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.focus.onFocusChanged
import com.inspiredandroid.red.ui.outlineTextFieldColors
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.name
import red.composeapp.generated.resources.Res
import red.composeapp.generated.resources.ic_attach
import red.composeapp.generated.resources.ic_file
import red.composeapp.generated.resources.ic_image
import red.composeapp.generated.resources.ic_stop
import red.composeapp.generated.resources.ic_up
import red.composeapp.generated.resources.prompt_ask_question
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestionInput(
    files: ImmutableList<PlatformFile>,
    addFile: (PlatformFile) -> Unit,
    removeFile: (PlatformFile) -> Unit,
    ask: (String) -> Unit,
    supportedFileExtensions: ImmutableList<String>,
    textState: TextFieldValue,
    onTextStateChange: (TextFieldValue) -> Unit,
    isLoading: Boolean = false,
    cancel: () -> Unit = {},
    availableServices: ImmutableList<ServiceEntry> = persistentListOf(),
    onSelectService: (String) -> Unit = {},
    installedSkills: ImmutableList<SkillManifest> = persistentListOf(),
    modifier: Modifier = Modifier,
) {
    val appSettings: AppSettings = koinInject()
    val gradientBrush = rememberGradientBrush()

    Column(modifier = modifier) {
        // Slash autocomplete: shown when the user is typing the first token and it starts
        // with `/`. Selecting an entry rewrites the first token to the canonical skill id
        // so the ViewModel can match it at send time.
        if (installedSkills.isNotEmpty()) {
            val slashQuery = remember(textState.text, textState.selection) {
                detectSlashQuery(textState.text, textState.selection.start)
            }
            if (slashQuery != null) {
                SkillAutocomplete(
                    skills = installedSkills,
                    query = slashQuery,
                    onSelect = { skill ->
                        val text = textState.text
                        val firstSpace = text.indexOfFirst { it.isWhitespace() }
                        val rest = if (firstSpace < 0) "" else text.substring(firstSpace)
                        val newText = "/${skill.id}$rest"
                        val cursor = ("/" + skill.id + " ").length
                        onTextStateChange(
                            TextFieldValue(
                                text = if (rest.isEmpty()) "/${skill.id} " else newText,
                                selection = TextRange(cursor.coerceAtMost(if (rest.isEmpty()) cursor else newText.length)),
                            ),
                        )
                    },
                )
                Spacer(Modifier.padding(top = 4.dp))
            }
        }

        fun submitQuestion() {
            val text = textState.text
            if (text.isNotBlank()) {
                ask(text.trim())
                onTextStateChange(TextFieldValue(""))
            }
        }

        val allowFileAttachment = supportedFileExtensions.isNotEmpty()
        val filePickerLauncher = if (allowFileAttachment) {
            rememberFilePickerLauncher(
                type = FileKitType.File(extensions = supportedFileExtensions),
            ) { file ->
                if (file != null) addFile(file)
            }
        } else {
            null
        }

        val photoPickerLauncher = rememberFilePickerLauncher(
            type = FileKitType.Image,
        ) { file ->
            if (file != null) addFile(file)
        }

        val cameraPickerLauncher = rememberPlatformCameraLauncher { file ->
            if (file != null) addFile(file)
        }

        var isListening by remember { mutableStateOf(false) }
        val speechRecognizer = rememberPlatformSpeechRecognizer(
            onResult = { result ->
                val currentText = textState.text
                val space = if (currentText.isEmpty() || currentText.endsWith(" ")) "" else " "
                onTextStateChange(TextFieldValue(currentText + space + result))
            },
            onListeningChange = { isListening = it }
        )

        val focusRequester = remember { FocusRequester() }
        var isFocused by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 8.dp)
                .redInputSurface(isFocused = isFocused, shape = RoundedCornerShape(24.dp))
        ) {
            // Prompt input text field (transparent)
            TextField(
                value = textState,
                onValueChange = onTextStateChange,
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .onFocusChanged { isFocused = it.isFocused }
                    .fillMaxWidth()
                    .heightIn(min = 36.dp, max = 100.dp)
                    .onPreviewKeyEvent { event ->
                        if (currentPlatform !is Platform.Mobile && event.key.keyCode == Key.Enter.keyCode && event.type == KeyEventType.KeyDown) {
                            if (event.isShiftPressed) {
                                val currentText = textState.text
                                val selection = textState.selection
                                val start = minOf(selection.start, selection.end).coerceIn(0, currentText.length)
                                val end = maxOf(selection.start, selection.end).coerceIn(0, currentText.length)

                                val newText = currentText.replaceRange(start, end, "\n")
                                onTextStateChange(
                                    TextFieldValue(
                                        text = newText,
                                        selection = TextRange(start + 1),
                                    ),
                                )
                                return@onPreviewKeyEvent true
                            } else {
                                submitQuestion()
                                return@onPreviewKeyEvent true
                            }
                        }
                        return@onPreviewKeyEvent false
                    },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                ),
                placeholder = {
                    Text(
                        "Ask Red anything...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                },
                keyboardActions = if (currentPlatform !is Platform.Mobile) {
                    KeyboardActions(onSend = { submitQuestion() })
                } else {
                    KeyboardActions()
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = if (currentPlatform is Platform.Mobile) ImeAction.Default else ImeAction.Send,
                ),
            )

            // FlowRow of attached files (inside the card)
            if (files.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    for (file in files) {
                        val icon = if (file.extension.lowercase() in imageExtensions) {
                            Res.drawable.ic_image
                        } else {
                            Res.drawable.ic_file
                        }
                        SuggestionChip(
                            modifier = Modifier.handCursor(),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            onClick = { removeFile(file) },
                            icon = {
                                Icon(
                                    modifier = Modifier.size(16.dp),
                                    painter = painterResource(icon),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onBackground,
                                )
                            },
                            label = {
                                DisableSelection {
                                    Text(
                                        modifier = Modifier.handCursor(),
                                        text = truncateFileName(file.name),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            },
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Plus button for attachments picker options
                var menuExpanded by remember { mutableStateOf(false) }
                Box {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .redIconButtonSurface(shape = CircleShape)
                            .clickable { menuExpanded = true }
                            .handCursor(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Attachment Options",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add files", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Description, null, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                menuExpanded = false
                                filePickerLauncher?.launch()
                            },
                            modifier = Modifier.handCursor()
                        )
                        DropdownMenuItem(
                            text = { Text("Add photos", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Image, null, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                menuExpanded = false
                                photoPickerLauncher.launch()
                            },
                            modifier = Modifier.handCursor()
                        )
                        DropdownMenuItem(
                            text = { Text("Click a picture", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                menuExpanded = false
                                cameraPickerLauncher.launch()
                            },
                            modifier = Modifier.handCursor()
                        )

                    }
                }

                Spacer(Modifier.width(8.dp))

                // Service model switcher
                if (availableServices.size > 1) {
                    ServiceSelector(
                        services = availableServices,
                        onSelectService = onSelectService,
                    )
                }

                Spacer(Modifier.weight(1f))

                // Microphone Voice Chat Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .redIconButtonSurface(shape = CircleShape)
                        .clickable {
                            if (isListening) {
                                speechRecognizer.stopListening()
                            } else {
                                speechRecognizer.startListening()
                            }
                        }
                        .handCursor(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (isListening) Color.Red else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Upward Send Button or Stop Button (PixelPlayer expressive gradient pill button)
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(brush = gradientBrush, shape = CircleShape)
                        .handCursor()
                        .clickable {
                            if (isLoading) {
                                cancel()
                            } else {
                                submitQuestion()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLoading) Icons.Default.Stop else Icons.Default.ArrowUpward,
                        contentDescription = "Send",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                }
            }
        }

        val inInspection = LocalInspectionMode.current
        LaunchedEffect(Unit) {
            if (!inInspection) focusRequester.requestFocus()
        }
    }
}

/**
 * Returns the slash-command query string the user is currently typing, or null if
 * the cursor isn't inside a leading `/<token>`. Examples:
 *  - `"/su"` cursor at 3 → `"su"`
 *  - `"/summarize https://…"` cursor at 4 → `"sum"`
 *  - `"hello /foo"` (slash not at start) → `null`
 *  - `"/foo bar"` cursor at 6 → `null` (cursor past first space)
 */
internal fun detectSlashQuery(text: String, cursor: Int): String? {
    if (!text.startsWith('/')) return null
    val firstSpace = text.indexOfFirst { it.isWhitespace() }
    val tokenEnd = if (firstSpace < 0) text.length else firstSpace
    if (cursor > tokenEnd) return null
    return text.substring(1, tokenEnd).lowercase()
}

/**
 * Shortens a filename that is too long to display in a chip. Returns the first [maxChars]
 * characters of the base name followed by `…` and the original extension, so the user still
 * recognizes the file type. Short names are returned unchanged.
 */
internal fun truncateFileName(name: String, maxChars: Int = 16): String {
    if (name.length <= maxChars) return name
    val dotIndex = name.lastIndexOf('.')
    return if (dotIndex > 0 && dotIndex < name.length - 1) {
        val base = name.substring(0, dotIndex)
        val ext = name.substring(dotIndex) // includes the dot
        val keep = (maxChars - ext.length - 1).coerceAtLeast(1)
        "${base.take(keep)}…$ext"
    } else {
        "${name.take(maxChars - 1)}…"
    }
}

@Composable
internal fun TrailingIcon(
    icon: org.jetbrains.compose.resources.DrawableResource = Res.drawable.ic_up,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPulsing: Boolean = false,
) {
    val gradientBrush = rememberGradientBrush()
    val pulseModifier = if (isPulsing) {
        val infiniteTransition = rememberInfiniteTransition()
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.92f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        )
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.7f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        )
        Modifier.graphicsLayer {
            scaleX = pulseScale
            scaleY = pulseScale
            alpha = pulseAlpha
        }
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color = MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
            .handCursor()
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            vectorResource(icon),
            modifier = Modifier.size(32.dp).then(pulseModifier),
            contentDescription = null,
            tint = Color.White,
        )
    }
}

@Composable
internal fun CircleIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .redIconButtonSurface(shape = RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .handCursor(),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            modifier = Modifier.size(24.dp),
            contentDescription = null,
            tint = tint,
        )
    }
}
