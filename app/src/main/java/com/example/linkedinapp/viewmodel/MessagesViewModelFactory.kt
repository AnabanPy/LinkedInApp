package com.example.linkedinapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.linkedinapp.repository.MessageRepository
import com.example.linkedinapp.repository.UserRepository

class MessagesViewModelFactory(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val currentUserId: Long,
    private val context: Context? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MessagesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MessagesViewModel(messageRepository, userRepository, currentUserId, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

