package com.example.linkedinapp.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "linkedin_app_session"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_IS_GUEST = "is_guest"
    }
    
    fun saveSession(userId: Long) {
        prefs.edit().apply {
            putLong(KEY_USER_ID, userId)
            putBoolean(KEY_IS_GUEST, false)
            apply()
        }
    }
    
    fun saveGuestSession() {
        prefs.edit().apply {
            putBoolean(KEY_IS_GUEST, true)
            remove(KEY_USER_ID)
            apply()
        }
    }
    
    fun getUserId(): Long? {
        val userId = prefs.getLong(KEY_USER_ID, -1L)
        return if (userId != -1L) userId else null
    }
    
    fun isGuest(): Boolean {
        return prefs.getBoolean(KEY_IS_GUEST, false)
    }
    
    fun isLoggedIn(): Boolean {
        return getUserId() != null || isGuest()
    }
    
    fun clearSession() {
        prefs.edit().apply {
            remove(KEY_USER_ID)
            remove(KEY_IS_GUEST)
            apply()
        }
    }
}

