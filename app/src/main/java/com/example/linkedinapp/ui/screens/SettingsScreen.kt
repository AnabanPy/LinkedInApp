package com.example.linkedinapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linkedinapp.R
import com.example.linkedinapp.ui.components.TelegramTopBar
import com.example.linkedinapp.ui.theme.rememberThemeManager
import com.example.linkedinapp.util.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val themeManager = rememberThemeManager()
    val isSystemDark = isSystemInDarkTheme()
    var currentThemeMode by remember { mutableIntStateOf(themeManager.getThemeMode()) }
    val context = LocalContext.current
    
    // Update when returning to this screen
    LaunchedEffect(Unit) {
        currentThemeMode = themeManager.getThemeMode()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TelegramTopBar(
            title = stringResource(R.string.settings),
            onBackClick = onBackClick
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Telegram-style theme switcher
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.theme),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (currentThemeMode) {
                                ThemeManager.THEME_LIGHT -> stringResource(R.string.theme_light)
                                ThemeManager.THEME_DARK -> stringResource(R.string.theme_dark)
                                else -> stringResource(R.string.theme_system)
                            },
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Переключатель темной темы
                    Switch(
                        checked = currentThemeMode == ThemeManager.THEME_DARK,
                        onCheckedChange = { isDark ->
                            currentThemeMode = if (isDark) ThemeManager.THEME_DARK else ThemeManager.THEME_LIGHT
                            themeManager.setThemeMode(currentThemeMode)
                            // Recreate activity to apply theme change immediately
                            (context as? androidx.activity.ComponentActivity)?.recreate()
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Telegram-style logout button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.logout),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
    
    // Диалог подтверждения выхода
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.logout_dialog_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(stringResource(R.string.logout_dialog_message))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.logout_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

