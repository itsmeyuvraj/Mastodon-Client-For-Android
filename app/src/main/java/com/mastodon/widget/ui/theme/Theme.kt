package com.mastodon.widget.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val MastodonPurple = Color(0xFF6040FF)
val MastodonPurpleLight = Color(0xFFE6E1FF)
val MastodonTeal = Color(0xFF00C5A8)
val ReblogGreen = Color(0xFF21CF6B)
val FavouriteRed = Color(0xFFE4405F)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5B40E8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8E3FF),
    onPrimaryContainer = Color(0xFF1A006E),
    secondary = Color(0xFF006B5F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFABEEE3),
    onSecondaryContainer = Color(0xFF00201C),
    tertiary = Color(0xFF8B2FC9),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF4D9FF),
    onTertiaryContainer = Color(0xFF33004F),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF6F4FF),
    onBackground = Color(0xFF1B1B25),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1B1B25),
    surfaceVariant = Color(0xFFE7E1F0),
    onSurfaceVariant = Color(0xFF49464F),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF3EFFE),
    surfaceContainer = Color(0xFFEDE8F8),
    surfaceContainerHigh = Color(0xFFE7E2F3),
    surfaceContainerHighest = Color(0xFFE2DDEE),
    outline = Color(0xFF7A7680),
    outlineVariant = Color(0xFFCAC5D2),
    inverseSurface = Color(0xFF30303A),
    inverseOnSurface = Color(0xFFF3EFF8),
    inversePrimary = Color(0xFFCDBEFF),
    scrim = Color(0xFF000000),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFCDBEFF),
    onPrimary = Color(0xFF2A0090),
    primaryContainer = Color(0xFF4320CB),
    onPrimaryContainer = Color(0xFFE8E3FF),
    secondary = Color(0xFF8FD1C6),
    onSecondary = Color(0xFF003732),
    secondaryContainer = Color(0xFF005048),
    onSecondaryContainer = Color(0xFFABEEE3),
    tertiary = Color(0xFFE3AAFF),
    onTertiary = Color(0xFF510075),
    tertiaryContainer = Color(0xFF70009F),
    onTertiaryContainer = Color(0xFFF4D9FF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF131218),
    onBackground = Color(0xFFE5E1EE),
    surface = Color(0xFF1A1920),
    onSurface = Color(0xFFE5E1EE),
    surfaceVariant = Color(0xFF49464F),
    onSurfaceVariant = Color(0xFFCBC5D2),
    surfaceContainerLowest = Color(0xFF0E0D14),
    surfaceContainerLow = Color(0xFF231F2C),
    surfaceContainer = Color(0xFF272330),
    surfaceContainerHigh = Color(0xFF322E3B),
    surfaceContainerHighest = Color(0xFF3D3946),
    outline = Color(0xFF948F9C),
    outlineVariant = Color(0xFF49464F),
    inverseSurface = Color(0xFFE5E1EE),
    inverseOnSurface = Color(0xFF30303A),
    inversePrimary = Color(0xFF5B40E8),
    scrim = Color(0xFF000000),
)

@Composable
fun MastodonWidgetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
