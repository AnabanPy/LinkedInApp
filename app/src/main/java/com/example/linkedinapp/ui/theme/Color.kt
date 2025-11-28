package com.example.linkedinapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Telegram Light Theme Colors (точные цвета Telegram)
val TelegramLightBackground = Color(0xFFFFFFFF)
val TelegramLightSurface = Color(0xFFF0F0F0)
val TelegramLightSurfaceVariant = Color(0xFFE5E5E5)
val TelegramLightPrimary = Color(0xFF3390EC) // Более точный синий Telegram
val TelegramLightOnPrimary = Color(0xFFFFFFFF)
val TelegramLightSecondary = Color(0xFF6D6D72)
val TelegramLightOnBackground = Color(0xFF000000)
val TelegramLightOnSurface = Color(0xFF000000)
val TelegramLightOnSurfaceVariant = Color(0xFF6D6D72)
val TelegramLightOutline = Color(0xFFC7C7CC)
val TelegramLightError = Color(0xFFFF3B30)
val TelegramLightMessageBubbleSent = Color(0xFFDCF8C6) // Зеленый для отправленных
val TelegramLightMessageBubbleReceived = Color(0xFFFFFFFF) // Белый для полученных
val TelegramLightDivider = Color(0xFFC6C6C8)
val TelegramLightInputBackground = Color(0xFFF0F0F0)

// Telegram Dark Theme Colors (точные цвета Telegram)
val TelegramDarkBackground = Color(0xFF0E1621)
val TelegramDarkSurface = Color(0xFF17212B)
val TelegramDarkSurfaceVariant = Color(0xFF242F3D)
val TelegramDarkPrimary = Color(0xFF5B9BD1) // Синий для темной темы
val TelegramDarkOnPrimary = Color(0xFFFFFFFF)
val TelegramDarkSecondary = Color(0xFF708499)
val TelegramDarkOnBackground = Color(0xFFE4ECF0)
val TelegramDarkOnSurface = Color(0xFFE4ECF0)
val TelegramDarkOnSurfaceVariant = Color(0xFF8C98A8)
val TelegramDarkOutline = Color(0xFF3D4851)
val TelegramDarkError = Color(0xFFFF3B30)
val TelegramDarkMessageBubbleSent = Color(0xFF2B5278) // Синий для отправленных
val TelegramDarkMessageBubbleReceived = Color(0xFF182533) // Темный для полученных
val TelegramDarkDivider = Color(0xFF242F3D)
val TelegramDarkInputBackground = Color(0xFF1E2732)

// HeadHunter Color Palette (точные цвета как на сайте hh.ru)
val HeadHunterOrange = Color(0xFFFF6738) // Основной оранжевый HeadHunter
val HeadHunterOrangeDark = Color(0xFFE55A2B) // Темный оранжевый для hover
val HeadHunterOrangeLight = Color(0xFFFF7A52) // Светлый оранжевый
val HeadHunterOrangeAccent = Color(0xFFFF3D00) // Яркий акцентный оранжевый
val HeadHunterBackground = Color(0xFFF5F5F5) // Светло-серый фон как на hh.ru
val HeadHunterCardBackground = Color(0xFFFFFFFF) // Белый фон карточек
val HeadHunterTextPrimary = Color(0xFF1A1A1A) // Основной текст (почти черный)
val HeadHunterTextSecondary = Color(0xFF767676) // Вторичный текст (серый)
val HeadHunterTextTertiary = Color(0xFF999999) // Третичный текст (светло-серый)
val HeadHunterBorder = Color(0xFFE8E8E8) // Границы карточек
val HeadHunterDivider = Color(0xFFE0E0E0) // Разделители
val HeadHunterBadge = Color(0xFFFFD700) // Золотой для бейджей
val HeadHunterLink = Color(0xFF0066CC) // Синий для ссылок
val HeadHunterSuccess = Color(0xFF4CAF50) // Зеленый для успешных действий

// HeadHunter Dark Theme Colors (для темной темы как в приложении hh работа)
val HeadHunterDarkBackground = Color(0xFF0E1621) // Темный фон
val HeadHunterDarkCardBackground = Color(0xFF242F3D) // Темно-серая карточка
val HeadHunterDarkCardBackgroundVariant = Color(0xFF1E2732) // Вариант темной карточки
val HeadHunterDarkTextPrimary = Color(0xFFE4ECF0) // Основной текст на темном фоне
val HeadHunterDarkTextSecondary = Color(0xFF8C98A8) // Вторичный текст на темном фоне
val HeadHunterDarkTextTertiary = Color(0xFF708499) // Третичный текст на темном фоне
val HeadHunterDarkPrimary = Color(0xFF5B9BD1) // Синий для кнопок в темной теме
val HeadHunterDarkViewingText = Color(0xFF4CAF50) // Зеленый для "Сейчас смотрит X человек"
val HeadHunterDarkBorder = Color(0xFF3D4851) // Границы в темной теме

// Legacy colors (kept for compatibility)
val LinkedInBlue = TelegramLightPrimary
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Helper functions for theme-aware HeadHunter colors
@Composable
fun getHeadHunterBackground(): Color {
    val isDark = isDarkTheme()
    return if (isDark) HeadHunterDarkBackground else HeadHunterBackground
}

@Composable
fun getHeadHunterCardBackground(): Color {
    val isDark = isDarkTheme()
    return if (isDark) HeadHunterDarkCardBackground else HeadHunterCardBackground
}

@Composable
fun getHeadHunterCardBackgroundVariant(): Color {
    val isDark = isDarkTheme()
    return if (isDark) HeadHunterDarkCardBackgroundVariant else HeadHunterCardBackground
}

@Composable
fun getHeadHunterTextPrimary(): Color {
    val isDark = isDarkTheme()
    return if (isDark) HeadHunterDarkTextPrimary else HeadHunterTextPrimary
}

@Composable
fun getHeadHunterTextSecondary(): Color {
    val isDark = isDarkTheme()
    return if (isDark) HeadHunterDarkTextSecondary else HeadHunterTextSecondary
}

@Composable
fun getHeadHunterTextTertiary(): Color {
    val isDark = isDarkTheme()
    return if (isDark) HeadHunterDarkTextTertiary else HeadHunterTextTertiary
}

@Composable
fun getHeadHunterPrimary(): Color {
    val isDark = isDarkTheme()
    return if (isDark) HeadHunterDarkPrimary else HeadHunterOrange
}

@Composable
fun getHeadHunterBorder(): Color {
    val isDark = isDarkTheme()
    return if (isDark) HeadHunterDarkBorder else HeadHunterBorder
}

@Composable
private fun isDarkTheme(): Boolean {
    // Use MaterialTheme to determine if current theme is dark
    // Check if background color matches dark theme (TelegramDarkBackground = 0xFF0E1621)
    val colorScheme = MaterialTheme.colorScheme
    val background = colorScheme.background
    // Telegram dark background has very low RGB values, light is white (1.0, 1.0, 1.0)
    // Check if background is dark (RGB values are low)
    val isDarkByColor = background.red < 0.2f && background.green < 0.2f && background.blue < 0.3f
    
    // Also check via ThemeManager for consistency
    val context = LocalContext.current
    val themeManager = remember { com.example.linkedinapp.util.ThemeManager(context) }
    val isSystemDark = isSystemInDarkTheme()
    // Read theme mode - this will be updated when activity recreates
    val themeMode = remember { themeManager.getThemeMode() }
    
    return remember(themeMode, isSystemDark, isDarkByColor) {
        when (themeMode) {
            com.example.linkedinapp.util.ThemeManager.THEME_DARK -> true
            com.example.linkedinapp.util.ThemeManager.THEME_LIGHT -> false
            else -> isDarkByColor || isSystemDark
        }
    }
}
