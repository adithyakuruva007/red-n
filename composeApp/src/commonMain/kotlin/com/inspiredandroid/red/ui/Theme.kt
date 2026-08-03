@file:Suppress("DEPRECATION")

package com.inspiredandroid.red.ui
 
import org.koin.compose.koinInject
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inspiredandroid.red.data.AppSettings
import com.inspiredandroid.red.data.AppColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

fun Modifier.handCursor() = pointerHoverIcon(PointerIcon.Hand, overrideDescendants = true)

fun ColorScheme.withBlackBackground(): ColorScheme = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceContainerLowest = Color.Black,
)

val ColorScheme.isOledFlavor: Boolean get() = background == Color.Black
val ColorScheme.isClaymorphism: Boolean get() = background == Color(0xFF1A1A24)
val ColorScheme.isPixelPlayer: Boolean get() = background == Color(0xFF1E1234)

@Composable
fun redAdaptiveCardColors(): CardColors {
    val scheme = MaterialTheme.colorScheme
    return CardDefaults.cardColors(
        containerColor = when {
            scheme.isPixelPlayer -> Color(0xFF2A1F40)
            scheme.isOledFlavor -> Color.Transparent
            scheme.isClaymorphism -> Color(0xFF242432)
            else -> scheme.surfaceVariant.copy(alpha = 0.5f)
        }
    )
}

@Composable
fun redAdaptiveCardBorder(): BorderStroke? {
    val scheme = MaterialTheme.colorScheme
    return when {
        scheme.isPixelPlayer -> BorderStroke(1.dp, Color(0xFF5E497A).copy(alpha = 0.4f))
        scheme.isOledFlavor -> BorderStroke(1.dp, scheme.outlineVariant)
        scheme.isClaymorphism -> BorderStroke(2.dp, Color.White.copy(alpha = 0.08f))
        else -> null
    }
}

@Composable
fun Modifier.redAdaptiveCardSurface(shape: Shape = CardDefaults.shape): Modifier {
    val scheme = MaterialTheme.colorScheme
    val finalShape = when {
        scheme.isPixelPlayer -> RoundedCornerShape(20.dp)
        scheme.isClaymorphism -> RoundedCornerShape(24.dp)
        else -> shape
    }
    return this
        .clip(finalShape)
        .background(
            when {
                scheme.isPixelPlayer -> Color(0xFF2A1F40)
                scheme.isOledFlavor -> Color.Transparent
                scheme.isClaymorphism -> Color(0xFF242432)
                else -> scheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
        .then(
            when {
                scheme.isPixelPlayer -> Modifier.border(1.dp, Color(0xFF5E497A).copy(alpha = 0.4f), finalShape)
                scheme.isOledFlavor -> Modifier.border(1.dp, scheme.outlineVariant, finalShape)
                scheme.isClaymorphism -> Modifier.border(2.dp, Color.White.copy(alpha = 0.08f), finalShape)
                else -> Modifier
            }
        )
}

@Composable
fun Modifier.redInputSurface(isFocused: Boolean, shape: Shape = RoundedCornerShape(20.dp)): Modifier {
    val scheme = MaterialTheme.colorScheme
    val finalBg = when {
        scheme.isPixelPlayer -> Color(0xFF2A1F40)
        scheme.isOledFlavor -> Color.Transparent
        scheme.isClaymorphism -> Color(0xFF242432)
        else -> scheme.surfaceContainerHigh
    }
    val finalBorder = when {
        isFocused -> BorderStroke(1.5.dp, scheme.primary)
        scheme.isPixelPlayer -> BorderStroke(1.dp, Color(0xFF5E497A).copy(alpha = 0.5f))
        scheme.isOledFlavor -> BorderStroke(1.dp, scheme.outlineVariant)
        scheme.isClaymorphism -> BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        else -> BorderStroke(1.dp, scheme.outline.copy(alpha = 0.5f))
    }
    return this
        .clip(shape)
        .background(finalBg)
        .border(finalBorder, shape)
}

@Composable
fun Modifier.redIconButtonSurface(shape: Shape = RoundedCornerShape(12.dp)): Modifier {
    val scheme = MaterialTheme.colorScheme
    val finalBg = when {
        scheme.isPixelPlayer -> Color(0xFF382954)
        scheme.isOledFlavor -> Color.Transparent
        scheme.isClaymorphism -> Color(0xFF242432)
        else -> scheme.surfaceVariant
    }
    val finalBorder = when {
        scheme.isPixelPlayer -> BorderStroke(1.dp, Color(0xFF5E497A).copy(alpha = 0.3f))
        scheme.isOledFlavor -> BorderStroke(1.dp, scheme.outlineVariant)
        scheme.isClaymorphism -> BorderStroke(2.dp, Color.White.copy(alpha = 0.08f))
        else -> null
    }
    val base = this.clip(shape).background(finalBg, shape)
    return if (finalBorder != null) {
        base.border(finalBorder, shape)
    } else {
        base
    }
}

@Composable
fun outlineTextFieldColors() = OutlinedTextFieldDefaults.colors()

@Composable
fun RedOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        label = label,
        placeholder = placeholder,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        shape = RoundedCornerShape(12.dp),
        colors = outlineTextFieldColors(),
    )
}

@Composable
fun RedClearableTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
) {
    var focused by remember { mutableStateOf(false) }
    RedOutlinedTextField(
        modifier = modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused },
        value = value,
        onValueChange = onValueChange,
        label = label,
        singleLine = singleLine,
        trailingIcon = {
            IconButton(
                onClick = { onValueChange("") },
                modifier = Modifier.handCursor()
                    .alpha(if (focused && value.isNotEmpty()) 1f else 0f),
                enabled = value.isNotEmpty(),
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}



val SansSerifFontFamily = FontFamily.SansSerif

val AppTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = SansSerifFontFamily),
        displayMedium = displayMedium.copy(fontFamily = SansSerifFontFamily),
        displaySmall = displaySmall.copy(fontFamily = SansSerifFontFamily),
        headlineLarge = headlineLarge.copy(fontFamily = SansSerifFontFamily),
        headlineMedium = headlineMedium.copy(fontFamily = SansSerifFontFamily),
        headlineSmall = headlineSmall.copy(fontFamily = SansSerifFontFamily),
        titleLarge = titleLarge.copy(fontFamily = SansSerifFontFamily),
        titleMedium = titleMedium.copy(fontFamily = SansSerifFontFamily),
        titleSmall = titleSmall.copy(fontFamily = SansSerifFontFamily),
        bodyLarge = bodyLarge.copy(fontFamily = SansSerifFontFamily),
        bodyMedium = bodyMedium.copy(fontFamily = SansSerifFontFamily),
        bodySmall = bodySmall.copy(fontFamily = SansSerifFontFamily),
        labelLarge = labelLarge.copy(fontFamily = SansSerifFontFamily),
        labelMedium = labelMedium.copy(fontFamily = SansSerifFontFamily),
        labelSmall = labelSmall.copy(fontFamily = SansSerifFontFamily)
    )
}

@Composable
@Preview
fun Theme(
    colorScheme: ColorScheme,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
    ) {
        content()
    }
}



val DarkAdwaitaBlackColorScheme = darkColorScheme(
    primary = Color(0xFF3584E4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF1C71D8),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFB0B0B0),
    onSecondary = Color(0xFF212121),
    secondaryContainer = Color(0xFF242424),
    onSecondaryContainer = Color(0xFFEEEEEE),
    surface = Color(0xFF101010),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF242424),
    onSurfaceVariant = Color(0xFFCCCCCC),
    outline = Color(0xFF555555),
    outlineVariant = Color(0xFF303030),
)

val DarkAdwaitaBlackLightBlueColorScheme = darkColorScheme(
    primary = Color(0xFF62A0EA),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF3584E4),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFB0B0B0),
    onSecondary = Color(0xFF212121),
    secondaryContainer = Color(0xFF242424),
    onSecondaryContainer = Color(0xFFEEEEEE),
    surface = Color(0xFF101010),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF242424),
    onSurfaceVariant = Color(0xFFCCCCCC),
    outline = Color(0xFF555555),
    outlineVariant = Color(0xFF303030),
)

val LightAdwaitaColorScheme = lightColorScheme(
    primary = Color(0xFF3584E4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDAECFF),
    onPrimaryContainer = Color(0xFF002244),
    secondary = Color(0xFF777777),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF0f0f0),
    onSecondaryContainer = Color(0xFF212121),
    surface = Color(0xFFF6F6F6),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFE4E4E4),
    onSurfaceVariant = Color(0xFF404040),
    outline = Color(0xFF777777),
    outlineVariant = Color(0xFFCCCCCC),
)

val DarkClaymorphismColorScheme = darkColorScheme(
    primary = Color(0xFF3584E4), // Adwaita blue
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF1C71D8),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF8E8E9F),
    onSecondary = Color(0xFF1A1A24),
    secondaryContainer = Color(0xFF282836),
    onSecondaryContainer = Color(0xFFE5E5EA),
    surface = Color(0xFF242432),
    background = Color(0xFF1A1A24), // Slate dark
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF2D2D3E),
    onSurfaceVariant = Color(0xFFD1D1D6),
    outline = Color(0xFF48485C),
    outlineVariant = Color(0xFF333346),
)



val PixelPlayerDarkColorScheme = darkColorScheme(
    primary = Color(0xFFAB47BC), // PixelPlayPurplePrimary
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF4A148C),
    onPrimaryContainer = Color(0xFFE1BEE7),
    secondary = Color(0xFFF06292), // PixelPlayPink
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF880E4F),
    onSecondaryContainer = Color(0xFFF8BBD0),
    tertiary = Color(0xFFFF8A65), // PixelPlayOrange
    onTertiary = Color(0xFFFFFFFF),
    surface = Color(0xFF2A1F40), // PixelPlaySurface
    surfaceContainer = Color(0xFF2A1F40),
    surfaceContainerHigh = Color(0xFF352750),
    surfaceContainerHighest = Color(0xFF403060),
    background = Color(0xFF1E1234), // PixelPlayPurpleDark
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFE1BEE7), // PixelPlayLightPurple
    surfaceVariant = Color(0xFF382954),
    onSurfaceVariant = Color(0xFFD1C4E9),
    outline = Color(0xFF5E497A),
    outlineVariant = Color(0xFF423458),
)

@Composable
fun rememberGradientBrush(): Brush {
    val appSettings = koinInject<AppSettings>()
    val colorSchemeType by appSettings.colorSchemeFlow.collectAsStateWithLifecycle()
    return remember(colorSchemeType) {
        when (colorSchemeType) {
            AppColorScheme.PixelPlayer -> {
                Brush.horizontalGradient(listOf(Color(0xFFAB47BC), Color(0xFFF06292)))
            }
            AppColorScheme.AdwaitaBlack -> {
                Brush.horizontalGradient(listOf(Color(0xFF3584E4), Color(0xFF1C71D8)))
            }
            AppColorScheme.AdwaitaBlackLightBlue -> {
                Brush.horizontalGradient(listOf(Color(0xFF62A0EA), Color(0xFF3584E4)))
            }
            AppColorScheme.Claymorphism -> {
                Brush.horizontalGradient(listOf(Color(0xFF3584E4), Color(0xFF1C71D8)))
            }
        }
    }
}
