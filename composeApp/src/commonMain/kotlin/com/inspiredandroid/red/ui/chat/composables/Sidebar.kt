package com.inspiredandroid.red.ui.chat.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.inspiredandroid.red.platformHover
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import red.composeapp.generated.resources.Res
import red.composeapp.generated.resources.logo
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.PlatformFile
import org.koin.compose.koinInject
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inspiredandroid.red.data.ContextFile
import kotlinx.coroutines.launch
import com.inspiredandroid.red.ui.chat.ChatUiState
import com.inspiredandroid.red.ui.chat.ConversationSummary
import com.inspiredandroid.red.ui.handCursor

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun Sidebar(
    state: ChatUiState,
    currentConversationId: String?,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onToggleSidebar: () -> Unit,
    modifier: Modifier = Modifier,
    onNewChatClicked: () -> Unit = {},
    onConversationClicked: (String) -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    var showContextDialog by remember { mutableStateOf(false) }
    var showRenameDialogForId by remember { mutableStateOf<String?>(null) }
    var renameText by remember { mutableStateOf("") }
    var showDeleteConfirmationForId by remember { mutableStateOf<String?>(null) }
    
    // Left sidebar surface background
    val sidebarBg = if (scheme.background == Color.Black) {
        Color.Black
    } else {
        scheme.surfaceContainerLowest.copy(alpha = 0.95f)
    }

    Surface(
        modifier = modifier.border(
            width = 1.dp,
            color = scheme.outlineVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(0.dp)
        ),
        color = sidebarBg,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // App Header & Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.handCursor()
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.logo),
                        contentDescription = "Logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(
                    onClick = onToggleSidebar,
                    modifier = Modifier
                        .size(36.dp)
                        .handCursor()
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuOpen,
                        contentDescription = "Collapse sidebar",
                        tint = scheme.onSurfaceVariant
                    )
                }
            }

            // Quick Actions: New Chat, Agents, Context
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SidebarActionButton(
                    icon = Icons.Default.Add,
                    label = "New Chat",
                    onClick = {
                        state.actions.startNewChat()
                        onNewChatClicked()
                    }
                )
                SidebarActionButton(
                    icon = Icons.Default.Tune,
                    label = "Context",
                    onClick = { showContextDialog = true }
                )
            }

            Spacer(Modifier.height(16.dp))

            Spacer(Modifier.height(16.dp))

            // Conversations (Chats) Section Header
            Text(
                text = "Chats",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Conversations List
            val displayConversations = remember(state.savedConversations) {
                state.savedConversations.filter { !it.isHeartbeat }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(displayConversations, key = { it.id }) { conv ->
                    val isSelected = conv.id == currentConversationId
                    var isHovered by remember { mutableStateOf(false) }
                    var menuExpanded by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isSelected -> scheme.primaryContainer.copy(alpha = 0.5f)
                                    isHovered -> scheme.surfaceVariant.copy(alpha = 0.3f)
                                    else -> Color.Transparent
                                }
                            )
                            .platformHover(onEnter = { isHovered = true }, onExit = { isHovered = false })
                            .combinedClickable(
                                onClick = {
                                    state.actions.loadConversation(conv.id)
                                    onConversationClicked(conv.id)
                                },
                                onLongClick = { menuExpanded = true }
                            )
                            .handCursor()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (conv.isStarred) Icons.Default.Star else Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = if (conv.isStarred) Color(0xFFFFC107) else scheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = conv.title,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (isSelected) scheme.onPrimaryContainer else scheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        // 3-dot menu or star indicator
                        if (isHovered || isSelected || menuExpanded) {
                            Box {
                                IconButton(
                                    onClick = { menuExpanded = true },
                                    modifier = Modifier.size(24.dp).handCursor()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Options",
                                        tint = scheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Rename", fontSize = 13.sp) },
                                        leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp)) },
                                        onClick = {
                                            menuExpanded = false
                                            renameText = conv.title
                                            showRenameDialogForId = conv.id
                                        },
                                        modifier = Modifier.handCursor()
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (conv.isStarred) "Unstar" else "Star", fontSize = 13.sp) },
                                        leadingIcon = { Icon(if (conv.isStarred) Icons.Default.StarOutline else Icons.Default.Star, null, modifier = Modifier.size(16.dp)) },
                                        onClick = {
                                            menuExpanded = false
                                            state.actions.toggleStarConversation(conv.id)
                                        },
                                        modifier = Modifier.handCursor()
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete", fontSize = 13.sp, color = MaterialTheme.colorScheme.error) },
                                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp)) },
                                        onClick = {
                                            menuExpanded = false
                                            showDeleteConfirmationForId = conv.id
                                        },
                                        modifier = Modifier.handCursor()
                                    )
                                }
                            }
                        }
                    }
                }
            }



            // Notifications row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onNavigateToNotifications() }
                    .handCursor()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Notifications",
                    fontSize = 13.sp,
                    color = scheme.onSurface
                )
                val unreadCount = state.appNotifications.count { !it.isRead }
                if (unreadCount > 0) {
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF2196F3), RoundedCornerShape(10.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = unreadCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Settings row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onNavigateToSettings() }
                    .handCursor()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Settings",
                    fontSize = 13.sp,
                    color = scheme.onSurface
                )
            }
        }
    }

    // Rename Dialog
    if (showRenameDialogForId != null) {
        val targetId = showRenameDialogForId!!
        AlertDialog(
            onDismissRequest = { showRenameDialogForId = null },
            title = { Text("Rename Chat", style = MaterialTheme.typography.titleMedium) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp), 
                    onClick = {
                        state.actions.renameConversation(targetId, renameText)
                        showRenameDialogForId = null
                    },
                    modifier = Modifier.handCursor()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp), 
                    onClick = { showRenameDialogForId = null },
                    modifier = Modifier.handCursor()
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmationForId != null) {
        val targetId = showDeleteConfirmationForId!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationForId = null },
            title = { Text("Delete Chat", style = MaterialTheme.typography.titleMedium) },
            text = { Text("Are you sure you want to delete this chat? This action cannot be undone.") },
            confirmButton = {
                Button(shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp), 
                    onClick = {
                        state.actions.deleteConversation(targetId)
                        showDeleteConfirmationForId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.handCursor()
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                TextButton(shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp), 
                    onClick = { showDeleteConfirmationForId = null },
                    modifier = Modifier.handCursor()
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showContextDialog) {
        ContextSettingsDialog(
            onDismissRequest = { showContextDialog = false }
        )
    }
}

@Composable
private fun ContextSettingsDialog(
    onDismissRequest: () -> Unit
) {
    val dataRepository = koinInject<com.inspiredandroid.red.data.DataRepository>()
    val contextFiles by dataRepository.contextFiles.collectAsStateWithLifecycle()
    val referencedIds by dataRepository.referencedConversationIds.collectAsStateWithLifecycle()
    val savedConversations by dataRepository.savedConversations.collectAsStateWithLifecycle()
    val currentConversationId by dataRepository.currentConversationId.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val contextFilePicker = rememberFilePickerLauncher(
        type = FileKitType.File(),
    ) { file ->
        if (file != null) {
            scope.launch {
                try {
                    val content = file.readBytes().decodeToString()
                    dataRepository.addContextFile(file.name, content)
                } catch (_: Exception) {}
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = "Context Settings",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Custom/External Context Files
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "External Context Files & Info",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Button(shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp), 
                            onClick = { contextFilePicker.launch() },
                            modifier = Modifier.handCursor(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add File", fontSize = 12.sp)
                        }
                    }

                    if (contextFiles.isEmpty()) {
                        Text(
                            text = "No custom context files added.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 120.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(contextFiles) { file ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = file.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { dataRepository.removeContextFile(file.name) },
                                        modifier = Modifier.size(24.dp).handCursor()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove file",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // Section 2: Reference Other Chats
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Referenced Chats in Context",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    val otherConversations = remember(savedConversations, currentConversationId) {
                        savedConversations.filter { it.id != currentConversationId && it.type != "heartbeat" }
                    }

                    if (otherConversations.isEmpty()) {
                        Text(
                            text = "No other active conversations available.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 160.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(otherConversations) { conv ->
                                val isChecked = referencedIds.contains(conv.id)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { dataRepository.toggleReferencedConversation(conv.id) }
                                        .handCursor()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { dataRepository.toggleReferencedConversation(conv.id) },
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = conv.title.ifEmpty { "Untitled Chat" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp), 
                onClick = onDismissRequest,
                modifier = Modifier.handCursor()
            ) {
                Text("Done")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun SidebarActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .handCursor()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = scheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = scheme.onSurface
        )
    }
}
