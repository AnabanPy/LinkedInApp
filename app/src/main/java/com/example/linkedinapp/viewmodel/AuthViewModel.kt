package com.example.linkedinapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linkedinapp.data.User
import com.example.linkedinapp.repository.UserRepository
import com.example.linkedinapp.util.MessageNotificationScheduler
import com.example.linkedinapp.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val context: Context? = null
) : ViewModel() {
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    private val _isGuest = MutableStateFlow(false)
    val isGuest: StateFlow<Boolean> = _isGuest.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    private val _isLoadingSession = MutableStateFlow(true)
    val isLoadingSession: StateFlow<Boolean> = _isLoadingSession.asStateFlow()
    
    init {
        loadSession()
    }
    
    private fun loadSession() {
        viewModelScope.launch {
            try {
                if (sessionManager.isGuest()) {
                    _isGuest.value = true
                    _isLoggedIn.value = true
                    _currentUser.value = null
                } else {
                    val userId = sessionManager.getUserId()
                    if (userId != null) {
                        val user = userRepository.getUserById(userId)
                        if (user != null) {
                            _currentUser.value = user
                            _isLoggedIn.value = true
                            _isGuest.value = false
                            // Запускаем фоновую проверку сообщений
                            context?.let {
                                MessageNotificationScheduler.startPeriodicCheck(it, user.id)
                            }
                        } else {
                            // Пользователь не найден, очищаем сессию
                            sessionManager.clearSession()
                        }
                    }
                }
            } catch (e: Exception) {
                // При ошибке очищаем сессию
                sessionManager.clearSession()
            } finally {
                _isLoadingSession.value = false
            }
        }
    }
    
    suspend fun registerUser(user: User): Boolean {
        // Проверяем, не существует ли уже пользователь с таким email или username
        val existingUserByEmail = userRepository.getUserByEmail(user.email.lowercase().trim())
        val existingUserByUsername = userRepository.getUserByUsername(user.username.lowercase().trim())
        
        return if (existingUserByEmail != null || existingUserByUsername != null) {
            false // Пользователь с таким email или username уже существует
        } else {
            // Сохраняем пользователя в базу данных (Firebase или локально)
            userRepository.insertUser(user.copy(
                email = user.email.lowercase().trim(),
                username = user.username.lowercase().trim()
            ))
            true // Регистрация успешна
        }
    }
    
    suspend fun loginUser(email: String, password: String): Boolean {
        val user = userRepository.getUserByEmailAndPassword(
            email.lowercase().trim(),
            password
        )
        return if (user != null) {
            _currentUser.value = user
            _isLoggedIn.value = true
            _isGuest.value = false
            sessionManager.saveSession(user.id)
            // Запускаем фоновую проверку сообщений
            context?.let {
                MessageNotificationScheduler.startPeriodicCheck(it, user.id)
            }
            true
        } else {
            false
        }
    }
    
    fun loginAsGuest() {
        viewModelScope.launch {
            _isGuest.value = true
            _isLoggedIn.value = true
            _currentUser.value = null
            sessionManager.saveGuestSession()
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            _isLoggedIn.value = false
            _isGuest.value = false
            _currentUser.value = null
            sessionManager.clearSession()
            // Останавливаем фоновую проверку сообщений
            context?.let {
                MessageNotificationScheduler.stopPeriodicCheck(it)
            }
        }
    }
    
    suspend fun updateProfilePhoto(photoId: Int) {
        val user = _currentUser.value
        if (user != null) {
            userRepository.updateProfilePhoto(user.id, photoId)
            _currentUser.value = user.copy(profilePhotoId = photoId)
        }
    }
    
    suspend fun updateProfilePhotoUrl(photoUrl: String?) {
        val user = _currentUser.value
        if (user != null) {
            userRepository.updateProfilePhotoUrl(user.id, photoUrl)
            _currentUser.value = user.copy(profilePhotoUrl = photoUrl)
        }
    }
}

