package com.example.linkedinapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.linkedinapp.util.ThemeManager

// Telegram Light Color Scheme
private val TelegramLightColorScheme = lightColorScheme(
    primary = TelegramLightPrimary,
    onPrimary = TelegramLightOnPrimary,
    secondary = TelegramLightSecondary,
    background = TelegramLightBackground,
    onBackground = TelegramLightOnBackground,
    surface = TelegramLightSurface,
    onSurface = TelegramLightOnSurface,
    surfaceVariant = TelegramLightSurfaceVariant,
    onSurfaceVariant = TelegramLightOnSurfaceVariant,
    outline = TelegramLightOutline,
    error = TelegramLightError,
    primaryContainer = TelegramLightPrimary.copy(alpha = 0.1f),
    secondaryContainer = TelegramLightSecondary.copy(alpha = 0.1f)
)

// Telegram Dark Color Scheme
private val TelegramDarkColorScheme = darkColorScheme(
    primary = TelegramDarkPrimary,
    onPrimary = TelegramDarkOnPrimary,
    secondary = TelegramDarkSecondary,
    background = TelegramDarkBackground,
    onBackground = TelegramDarkOnBackground,
    surface = TelegramDarkSurface,
    onSurface = TelegramDarkOnSurface,
    surfaceVariant = TelegramDarkSurfaceVariant,
    onSurfaceVariant = TelegramDarkOnSurfaceVariant,
    outline = TelegramDarkOutline,
    error = TelegramDarkError,
    primaryContainer = TelegramDarkPrimary.copy(alpha = 0.2f),
    secondaryContainer = TelegramDarkSecondary.copy(alpha = 0.2f)
)

// Local ThemeManager for accessing in composables
val LocalThemeManager = compositionLocalOf<ThemeManager> {
    error("No ThemeManager provided")
}

@Composable
fun LinkJobTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    
    // Read theme mode from preferences - will be updated when activity recreates
    val themeMode = remember { themeManager.getThemeMode() }
    
    // Determine if dark theme should be used
    val isDarkTheme = remember(themeMode, darkTheme) {
        when (themeMode) {
            ThemeManager.THEME_DARK -> true
            ThemeManager.THEME_LIGHT -> false
            else -> darkTheme
        }
    }
    
    val colorScheme = if (isDarkTheme) {
        TelegramDarkColorScheme
    } else {
        TelegramLightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkTheme
        }
    }

    CompositionLocalProvider(LocalThemeManager provides themeManager) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun rememberThemeManager(): ThemeManager {
    return LocalThemeManager.current
}
