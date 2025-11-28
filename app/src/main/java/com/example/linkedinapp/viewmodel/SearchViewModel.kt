package com.example.linkedinapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linkedinapp.data.User
import com.example.linkedinapp.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    fun searchUsers(query: String) {
        _searchQuery.value = query
        
        // Убираем @ если он есть в начале запроса
        val cleanQuery = query.trim().removePrefix("@").trim()
        
        if (cleanQuery.isEmpty()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }
        
        viewModelScope.launch {
            _isSearching.value = true
            try {
                // Поиск по частичному совпадению username
                val results = userRepository.searchUsersByUsername(
                    query = "%$cleanQuery%",
                    limit = 20
                )
                _searchResults.value = results
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }
    
    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }
}


