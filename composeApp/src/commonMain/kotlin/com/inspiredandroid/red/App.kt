@file:OptIn(ExperimentalMaterial3Api::class)

package com.inspiredandroid.red

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Surface
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import com.inspiredandroid.red.ui.chat.composables.Sidebar
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.svg.SvgDecoder
import com.inspiredandroid.red.data.AppSettings
import com.inspiredandroid.red.data.ThemeMode
import com.inspiredandroid.red.data.AppColorScheme
import com.inspiredandroid.red.tools.CalendarPermissionController
import com.inspiredandroid.red.tools.ContactsPermissionController
import com.inspiredandroid.red.tools.NotificationPermissionController
import com.inspiredandroid.red.tools.SetupCalendarPermissionHandler
import com.inspiredandroid.red.tools.SetupContactsPermissionHandler
import com.inspiredandroid.red.tools.SetupNotificationPermissionHandler
import com.inspiredandroid.red.tools.SetupSmsPermissionHandler
import com.inspiredandroid.red.tools.SetupSmsSendPermissionHandler
import com.inspiredandroid.red.tools.SmsPermissionController
import com.inspiredandroid.red.tools.SmsSendPermissionController
import com.inspiredandroid.red.ui.PixelPlayerDarkColorScheme
import com.inspiredandroid.red.ui.DarkClaymorphismColorScheme
import com.inspiredandroid.red.ui.Theme
import com.inspiredandroid.red.PlatformBackHandler
import com.inspiredandroid.red.ui.chat.ChatScreen
import com.inspiredandroid.red.ui.chat.ChatViewModel
import com.inspiredandroid.red.ui.components.FullScreenImageHost
import com.inspiredandroid.red.ui.handCursor
import com.inspiredandroid.red.ui.rememberSandboxAwareUriHandler
import com.inspiredandroid.red.ui.settings.SettingsScreen
import com.inspiredandroid.red.ui.notifications.NotificationsScreen
import com.inspiredandroid.red.ui.withBlackBackground

import red.composeapp.generated.resources.Res
import red.composeapp.generated.resources.tab_chat
import red.composeapp.generated.resources.tab_settings
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.marc_apps.tts.TextToSpeechInstance
import nl.marc_apps.tts.experimental.ExperimentalVoiceApi
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinConfiguration

@Serializable
@SerialName("home")
object Home

@Serializable
@SerialName("settings")
object Settings

@Serializable
@SerialName("notifications")
object Notifications

@Composable
fun App(
    navController: NavHostController,
    lightColorScheme: ColorScheme = PixelPlayerDarkColorScheme,
    darkColorScheme: ColorScheme = PixelPlayerDarkColorScheme,
    textToSpeech: TextToSpeechInstance? = null,
    isKoinStarted: Boolean = false,
    onAppOpens: ((Int) -> Unit)? = null,
) {
    setSingletonImageLoaderFactory { context: PlatformContext ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
                add(SvgDecoder.Factory())
            }
            .build()
    }

    // Reuse global Koin if already started (Android Application class),
    // otherwise create a new instance (iOS, Desktop, Wasm).
    if (isKoinStarted) {
        AppContent(navController, lightColorScheme, darkColorScheme, textToSpeech, onAppOpens)
    } else {
        KoinApplication(
            configuration = koinConfiguration {
                modules(appModule)
            },
        ) {
            AppContent(navController, lightColorScheme, darkColorScheme, textToSpeech, onAppOpens)
        }
    }
}

@Composable
private fun AppContent(
    navController: NavHostController,
    lightColorScheme: ColorScheme,
    darkColorScheme: ColorScheme,
    textToSpeech: TextToSpeechInstance?,
    onAppOpens: ((Int) -> Unit)?,
) {
    val appSettings = koinInject<AppSettings>()

    // Track app opens after Koin is initialized
    onAppOpens?.let { callback ->
        LaunchedEffect(Unit) {
            callback(appSettings.trackAppOpen())
        }
    }

    // Set up permission handlers
    val calendarPermissionController = koinInject<CalendarPermissionController>()
    SetupCalendarPermissionHandler(calendarPermissionController)

    val notificationPermissionController = koinInject<NotificationPermissionController>()
    SetupNotificationPermissionHandler(notificationPermissionController)

    val smsPermissionController = koinInject<SmsPermissionController>()
    SetupSmsPermissionHandler(smsPermissionController)

    val smsSendPermissionController = koinInject<SmsSendPermissionController>()
    SetupSmsSendPermissionHandler(smsSendPermissionController)

    val contactsPermissionController = koinInject<ContactsPermissionController>()
    SetupContactsPermissionHandler(contactsPermissionController)

    // Set TTS voice to match system language
    @OptIn(ExperimentalVoiceApi::class)
    LaunchedEffect(textToSpeech) {
        val tts = textToSpeech ?: return@LaunchedEffect
        val systemLanguage = Locale.current.language
        val matchingVoice = tts.voices
            .firstOrNull { it.languageTag.startsWith(systemLanguage) }
        if (matchingVoice != null) {
            tts.currentVoice = matchingVoice
        }
    }

    val uiScale by appSettings.uiScaleFlow.collectAsStateWithLifecycle()
    val defaultDensity = LocalDensity.current
    val scaledDensity = remember(defaultDensity, uiScale) {
        Density(defaultDensity.density * uiScale, defaultDensity.fontScale)
    }

    val colorSchemeType by appSettings.colorSchemeFlow.collectAsStateWithLifecycle()

    val effectiveColorScheme = when (colorSchemeType) {
        AppColorScheme.PixelPlayer -> PixelPlayerDarkColorScheme
        AppColorScheme.Claymorphism -> DarkClaymorphismColorScheme
    }

    val sandboxController = koinInject<SandboxController>()
    val sandboxAwareUriHandler = rememberSandboxAwareUriHandler(sandboxController)

    CompositionLocalProvider(
        LocalDensity provides scaledDensity,
        LocalUriHandler provides sandboxAwareUriHandler,
    ) {
        Theme(colorScheme = effectiveColorScheme) {
            FullScreenImageHost {
                val chatViewModel: ChatViewModel = koinViewModel()
                val showTabBar = currentPlatform !is Platform.Mobile
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val isHome = currentBackStackEntry?.destination?.route == "home"

                var sidebarExpanded by remember { mutableStateOf(currentPlatform !is Platform.Mobile) }
                var showSettingsInSidebar by remember { mutableStateOf(false) }
                var showNotificationsInSidebar by remember { mutableStateOf(false) }
                val chatUiState by chatViewModel.state.collectAsStateWithLifecycle()

                LaunchedEffect(chatViewModel) {
                    chatViewModel.navigateToNotificationsRequested.collect {
                        if (currentPlatform is Platform.Mobile) {
                            sidebarExpanded = true
                            showSettingsInSidebar = false
                            showNotificationsInSidebar = true
                        } else {
                            val currentDest = navController.currentBackStackEntry?.destination
                            val hasNotifications = currentDest?.route?.endsWith("Notifications") == true
                            if (!hasNotifications) {
                                navController.navigate(Notifications)
                            }
                        }
                    }
                }

                if (currentPlatform is Platform.Mobile) {
                    val keyboardController = LocalSoftwareKeyboardController.current
                    val focusManager = LocalFocusManager.current
                    val isKeyboardVisible = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp

                    PlatformBackHandler(enabled = isKeyboardVisible || showSettingsInSidebar || showNotificationsInSidebar || sidebarExpanded) {
                        if (isKeyboardVisible) {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        } else if (showSettingsInSidebar) {
                            chatViewModel.refreshSettings()
                            showSettingsInSidebar = false
                        } else if (showNotificationsInSidebar) {
                            showNotificationsInSidebar = false
                        } else if (sidebarExpanded) {
                            sidebarExpanded = false
                        }
                    }

                    CustomPixelSidebarDrawer(
                        sidebarExpanded = sidebarExpanded,
                        onCloseSidebar = { sidebarExpanded = false },
                        onOpenSidebar = { sidebarExpanded = true },
                        sidebarContent = {
                            Sidebar(
                                state = chatUiState,
                                currentConversationId = chatUiState.currentConversationId,
                                onNavigateToSettings = {
                                    sidebarExpanded = false
                                    showSettingsInSidebar = true
                                    showNotificationsInSidebar = false
                                },
                                onNavigateToNotifications = {
                                    sidebarExpanded = false
                                    showNotificationsInSidebar = true
                                    showSettingsInSidebar = false
                                },
                                onToggleSidebar = { sidebarExpanded = false },
                                modifier = Modifier.fillMaxSize(),
                                onNewChatClicked = {
                                    showSettingsInSidebar = false
                                    showNotificationsInSidebar = false
                                    sidebarExpanded = false
                                },
                                onConversationClicked = { _ ->
                                    showSettingsInSidebar = false
                                    showNotificationsInSidebar = false
                                    sidebarExpanded = false
                                }
                            )
                        },
                        content = {
                            NavHost(
                                navController,
                                startDestination = Home,
                                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                            ) {
                                composable<Home> {
                                    ChatScreen(
                                        viewModel = chatViewModel,
                                        textToSpeech = textToSpeech,
                                        onNavigateToSettings = {
                                            sidebarExpanded = false
                                            showSettingsInSidebar = true
                                            showNotificationsInSidebar = false
                                        },
                                        onNavigateToNotifications = {
                                            sidebarExpanded = false
                                            showNotificationsInSidebar = true
                                            showSettingsInSidebar = false
                                        },
                                        isSandboxAvailable = currentPlatform is Platform.Mobile.Android,
                                        navigationTabBar = null,
                                        onToggleSidebar = { sidebarExpanded = !sidebarExpanded },
                                        isSidebarExpanded = sidebarExpanded,
                                    )
                                }
                            }
                        }
                    )

                        if (showSettingsInSidebar) {
                            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                                SettingsScreen(
                                    onNavigateBack = {
                                        chatViewModel.refreshSettings()
                                        showSettingsInSidebar = false
                                    },
                                    navigationTabBar = null,
                                    onToggleSidebar = { sidebarExpanded = false },
                                    isSidebarExpanded = sidebarExpanded,
                                )
                            }
                        } else if (showNotificationsInSidebar) {
                            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                                NotificationsScreen(
                                    state = chatUiState,
                                    onNavigateBack = {
                                        showNotificationsInSidebar = false
                                    },
                                    onNotificationClicked = { _ ->
                                        showNotificationsInSidebar = false
                                        sidebarExpanded = false
                                    }
                                )
                            }
                        }
                } else {
                    Row(Modifier.fillMaxSize()) {
                        if (sidebarExpanded) {
                            Sidebar(
                                state = chatUiState,
                                currentConversationId = chatUiState.currentConversationId,
                                onNavigateToSettings = {
                                    val currentRoute = navController.currentBackStackEntry?.destination?.route ?: ""
                                    if (currentRoute.contains("settings", ignoreCase = true)) {
                                        navController.navigate(Home) {
                                            popUpTo(Home) { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate(Settings)
                                    }
                                },
                                onNavigateToNotifications = {
                                    val currentRoute = navController.currentBackStackEntry?.destination?.route ?: ""
                                    if (currentRoute.contains("notifications", ignoreCase = true)) {
                                        navController.navigate(Home) {
                                            popUpTo(Home) { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate(Notifications)
                                    }
                                },
                                onToggleSidebar = { sidebarExpanded = false },
                                modifier = Modifier.width(280.dp).fillMaxHeight(),
                                onNewChatClicked = {
                                    navController.navigate(Home) {
                                        popUpTo(Home) { inclusive = true }
                                    }
                                },
                                onConversationClicked = { _ ->
                                    navController.navigate(Home) {
                                        popUpTo(Home) { inclusive = true }
                                    }
                                }
                            )
                        }

                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            NavHost(
                                navController,
                                startDestination = Home,
                                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                            ) {
                                composable<Home> {
                                    ChatScreen(
                                        viewModel = chatViewModel,
                                        textToSpeech = textToSpeech,
                                        onNavigateToSettings = {
                                            val currentRoute = navController.currentBackStackEntry?.destination?.route ?: ""
                                            if (currentRoute.contains("settings", ignoreCase = true)) {
                                                navController.navigate(Home) {
                                                    popUpTo(Home) { inclusive = true }
                                                }
                                            } else {
                                                navController.navigate(Settings)
                                            }
                                        },
                                        onNavigateToNotifications = {
                                            val currentRoute = navController.currentBackStackEntry?.destination?.route ?: ""
                                            if (currentRoute.contains("notifications", ignoreCase = true)) {
                                                navController.navigate(Home) {
                                                    popUpTo(Home) { inclusive = true }
                                                }
                                            } else {
                                                navController.navigate(Notifications)
                                            }
                                        },
                                        isSandboxAvailable = currentPlatform is Platform.Mobile.Android,
                                        navigationTabBar = null,
                                        onToggleSidebar = { sidebarExpanded = !sidebarExpanded },
                                        isSidebarExpanded = sidebarExpanded,
                                    )
                                }
                                composable<Settings> {
                                    SettingsScreen(
                                        onNavigateBack = {
                                            chatViewModel.refreshSettings()
                                            navController.navigateUp()
                                        },
                                        navigationTabBar = null,
                                        onToggleSidebar = { sidebarExpanded = !sidebarExpanded },
                                        isSidebarExpanded = sidebarExpanded,
                                    )
                                }
                                composable<Notifications> {
                                    NotificationsScreen(
                                        state = chatUiState,
                                        onNavigateBack = {
                                            navController.navigateUp()
                                        },
                                        onNotificationClicked = { _ ->
                                            navController.popBackStack(Home, inclusive = false)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomPixelSidebarDrawer(
    sidebarExpanded: Boolean,
    onCloseSidebar: () -> Unit,
    onOpenSidebar: () -> Unit,
    sidebarContent: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val drawerWidthDp = 280.dp
    val drawerWidthPx = remember(density, drawerWidthDp) { with(density) { drawerWidthDp.toPx() } }

    val offsetX = remember { Animatable(if (sidebarExpanded) 0f else -drawerWidthPx) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(sidebarExpanded, drawerWidthPx) {
        val target = if (sidebarExpanded) 0f else -drawerWidthPx
        if (offsetX.value != target) {
            offsetX.animateTo(target, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(drawerWidthPx, sidebarExpanded) {
                awaitEachGesture {
                    val down = awaitFirstDown(pass = PointerEventPass.Initial)
                    val screenWidthPx = size.width.toFloat()
                    val isLeftHalfTouch = down.position.x <= screenWidthPx / 2f

                    var dragging = false
                    var lastX = down.position.x
                    var totalDragX = 0f

                    while (true) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                        val change = event.changes.firstOrNull() ?: break
                        if (!change.pressed) break

                        val currentX = change.position.x
                        val dragAmount = currentX - lastX
                        lastX = currentX
                        totalDragX += dragAmount

                        if (!dragging) {
                            val isRightwardSwipe = !sidebarExpanded && isLeftHalfTouch && totalDragX > 8f
                            val isLeftwardOrOpenSwipe = sidebarExpanded && kotlin.math.abs(totalDragX) > 8f
                            if (isRightwardSwipe || isLeftwardOrOpenSwipe) {
                                dragging = true
                                keyboardController?.hide()
                            }
                        }

                        if (dragging) {
                            change.consume()
                            scope.launch {
                                val newOffset = (offsetX.value + dragAmount).coerceIn(-drawerWidthPx, 0f)
                                offsetX.snapTo(newOffset)
                            }
                        }
                    }

                    if (dragging) {
                        scope.launch {
                            if (offsetX.value > -drawerWidthPx / 2f) {
                                offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                onOpenSidebar()
                            } else {
                                offsetX.animateTo(-drawerWidthPx, tween(200))
                                onCloseSidebar()
                            }
                        }
                    }
                }
            }
    ) {
        content()

        val progress = ((drawerWidthPx + offsetX.value) / drawerWidthPx).coerceIn(0f, 1f)

        if (progress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.55f * progress }
                    .background(Color.Black)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            scope.launch {
                                offsetX.animateTo(-drawerWidthPx, tween(200))
                                onCloseSidebar()
                            }
                        }
                    )
            )
        }

        Surface(
            modifier = Modifier
                .width(drawerWidthDp)
                .fillMaxHeight()
                .graphicsLayer {
                    translationX = offsetX.value
                },
            shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shadowElevation = (8 * progress).dp
        ) {
            sidebarContent()
        }
    }
}
