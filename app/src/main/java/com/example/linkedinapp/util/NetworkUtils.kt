package com.example.linkedinapp.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NetworkUtils {
    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    
    fun checkInternetConnection(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        // Проверяем активную сеть
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        // Проверяем наличие интернета
        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        
        // NET_CAPABILITY_VALIDATED доступен с API 23+, но minSdk = 24, так что это безопасно
        // Но добавим проверку на случай, если capability недоступен
        val isValidated = try {
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Exception) {
            // Если проверка не удалась, считаем что интернет есть если есть NET_CAPABILITY_INTERNET
            hasInternet
        }
        
        val isConnected = hasInternet && isValidated
        
        _isOnline.value = isConnected
        return isConnected
    }
    
    fun isOnline(): Boolean = _isOnline.value
}


