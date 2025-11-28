package com.example.linkedinapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.linkedinapp.repository.UserRepository
import com.example.linkedinapp.util.SessionManager

class AuthViewModelFactory(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val context: Context? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(userRepository, sessionManager, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


