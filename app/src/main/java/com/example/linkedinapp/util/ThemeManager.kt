package com.example.linkedinapp.util

import android.content.Context
import android.content.SharedPreferences

open class ThemeManager(protected val context: Context) {
    protected val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "app_prefs"
        private const val KEY_THEME_MODE = "theme_mode"
        
        const val THEME_SYSTEM = 0
        const val THEME_LIGHT = 1
        const val THEME_DARK = 2
    }
    
    open fun getThemeMode(): Int {
        return prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM)
    }
    
    open fun setThemeMode(mode: Int) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply()
    }
    
    fun isDarkTheme(isSystemDark: Boolean): Boolean {
        return when (getThemeMode()) {
            THEME_DARK -> true
            THEME_LIGHT -> false
            else -> isSystemDark
        }
    }
}
