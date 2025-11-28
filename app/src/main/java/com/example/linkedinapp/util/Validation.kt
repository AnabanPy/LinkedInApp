package com.example.linkedinapp.util

object Validation {
    fun isValidUsername(username: String): ValidationResult {
        if (username.isEmpty()) {
            return ValidationResult(false, "Имя пользователя не может быть пустым")
        }
        
        // Убираем @ если есть
        val cleanUsername = username.removePrefix("@")
        
        // Проверка: только буквы, цифры и подчеркивания
        if (!cleanUsername.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            return ValidationResult(false, "Имя пользователя может содержать только буквы, цифры и подчеркивания")
        }
        
        // Проверка: не может начинаться с подчеркивания или цифры
        if (cleanUsername.startsWith("_") || cleanUsername.firstOrNull()?.isDigit() == true) {
            return ValidationResult(false, "Имя пользователя не может начинаться с подчеркивания или цифры")
        }
        
        // Проверка: обязательно должна быть хотя бы одна буква
        if (!cleanUsername.any { it.isLetter() }) {
            return ValidationResult(false, "Имя пользователя должно содержать хотя бы одну букву")
        }
        
        return ValidationResult(true, "")
    }
    
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    fun isValidPhone(phone: String): Boolean {
        // Простая проверка: минимум 10 цифр
        val digitsOnly = phone.filter { it.isDigit() }
        return digitsOnly.length >= 10
    }
    
    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String
)


