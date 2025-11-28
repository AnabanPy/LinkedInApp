package com.example.linkedinapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.linkedinapp.R
import com.example.linkedinapp.data.User
import com.example.linkedinapp.ui.components.TelegramTopBar
import com.example.linkedinapp.util.Validation
import com.example.linkedinapp.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun RegistrationScreen(
    onBackClick: () -> Unit,
    onRegisterSuccess: () -> Unit,
    authViewModel: AuthViewModel
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("@") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var firstNameError by remember { mutableStateOf("") }
    var lastNameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }
    var registrationError by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    
    // Получаем все строки заранее для использования в лямбдах
    val fieldRequiredText = stringResource(R.string.field_required)
    val emailErrorText = stringResource(R.string.email_error)
    val phoneErrorText = stringResource(R.string.phone_error)
    val passwordErrorText = stringResource(R.string.password_error)
    val passwordConfirmationErrorText = stringResource(R.string.password_confirmation_error)
    val userAlreadyExistsText = stringResource(R.string.user_already_exists)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TelegramTopBar(
            title = stringResource(R.string.registration_title),
            onBackClick = onBackClick
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = lastName,
            onValueChange = {
                lastName = it
                lastNameError = if (it.isEmpty()) fieldRequiredText else ""
                registrationError = ""
            },
            label = { Text(stringResource(R.string.last_name_label)) },
            modifier = Modifier.fillMaxWidth(),
            isError = lastNameError.isNotEmpty(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        if (lastNameError.isNotEmpty()) {
            Text(
                text = lastNameError,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = firstName,
            onValueChange = {
                firstName = it
                    firstNameError = if (it.isEmpty()) fieldRequiredText else ""
                registrationError = ""
            },
            label = { Text(stringResource(R.string.first_name_label)) },
            modifier = Modifier.fillMaxWidth(),
            isError = firstNameError.isNotEmpty(),
            singleLine = true
        )
        if (firstNameError.isNotEmpty()) {
            Text(
                text = firstNameError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = middleName,
            onValueChange = { middleName = it },
            label = { Text(stringResource(R.string.middle_name_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = when {
                    it.isEmpty() -> "Это поле обязательно"
                    !Validation.isValidEmail(it) -> emailErrorText
                    else -> ""
                }
                registrationError = ""
            },
            label = { Text(stringResource(R.string.email_label)) },
            modifier = Modifier.fillMaxWidth(),
            isError = emailError.isNotEmpty(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )
        if (emailError.isNotEmpty()) {
            Text(
                text = emailError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = phone,
            onValueChange = {
                phone = it
                phoneError = when {
                    it.isEmpty() -> "Это поле обязательно"
                    !Validation.isValidPhone(it) -> phoneErrorText
                    else -> ""
                }
            },
            label = { Text(stringResource(R.string.phone_label_full)) },
            modifier = Modifier.fillMaxWidth(),
            isError = phoneError.isNotEmpty(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true
        )
        if (phoneError.isNotEmpty()) {
            Text(
                text = phoneError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = username,
            onValueChange = {
                // Автоматически добавляем @ в начале, если его нет
                val newValue = if (it.isEmpty() || !it.startsWith("@")) {
                    "@" + it.removePrefix("@")
                } else {
                    it
                }
                username = newValue
                val validation = Validation.isValidUsername(newValue)
                usernameError = if (!validation.isValid) validation.errorMessage else ""
                registrationError = ""
            },
            label = { Text(stringResource(R.string.username_label_full)) },
            placeholder = { Text(stringResource(R.string.username_hint)) },
            modifier = Modifier.fillMaxWidth(),
            isError = usernameError.isNotEmpty(),
            singleLine = true
        )
        if (usernameError.isNotEmpty()) {
            Text(
                text = usernameError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = when {
                    it.isEmpty() -> "Это поле обязательно"
                    !Validation.isValidPassword(it) -> passwordErrorText
                    else -> ""
                }
                // Проверяем совпадение паролей при изменении
                    if (confirmPassword.isNotEmpty() && it != confirmPassword) {
                    confirmPasswordError = passwordConfirmationErrorText
                } else if (confirmPassword.isNotEmpty()) {
                    confirmPasswordError = ""
                }
            },
            label = { Text(stringResource(R.string.password_label)) },
            modifier = Modifier.fillMaxWidth(),
            isError = passwordError.isNotEmpty(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )
        if (passwordError.isNotEmpty()) {
            Text(
                text = passwordError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                confirmPasswordError = when {
                    it.isEmpty() -> "Это поле обязательно"
                    it != password -> passwordConfirmationErrorText
                    else -> ""
                }
            },
            label = { Text(stringResource(R.string.confirm_password)) },
            modifier = Modifier.fillMaxWidth(),
            isError = confirmPasswordError.isNotEmpty(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )
        if (confirmPasswordError.isNotEmpty()) {
            Text(
                text = confirmPasswordError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
        
        if (registrationError.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = registrationError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = {
                // Валидация всех полей
                var hasErrors = false
                
                if (firstName.isEmpty()) {
                    firstNameError = fieldRequiredText
                    hasErrors = true
                }
                if (lastName.isEmpty()) {
                    lastNameError = fieldRequiredText
                    hasErrors = true
                }
                if (email.isEmpty()) {
                    emailError = fieldRequiredText
                    hasErrors = true
                } else if (!Validation.isValidEmail(email)) {
                    emailError = emailErrorText
                    hasErrors = true
                }
                if (phone.isEmpty()) {
                    phoneError = fieldRequiredText
                    hasErrors = true
                } else if (!Validation.isValidPhone(phone)) {
                    phoneError = phoneErrorText
                    hasErrors = true
                }
                val usernameValidation = Validation.isValidUsername(username)
                if (!usernameValidation.isValid) {
                    usernameError = usernameValidation.errorMessage
                    hasErrors = true
                }
                if (password.isEmpty()) {
                    passwordError = fieldRequiredText
                    hasErrors = true
                } else if (!Validation.isValidPassword(password)) {
                    passwordError = passwordErrorText
                    hasErrors = true
                }
                if (confirmPassword.isEmpty()) {
                    confirmPasswordError = fieldRequiredText
                    hasErrors = true
                } else if (confirmPassword != password) {
                    confirmPasswordError = passwordConfirmationErrorText
                    hasErrors = true
                }
                
                if (!hasErrors) {
                    val user = User(
                        firstName = firstName,
                        lastName = lastName,
                        middleName = if (middleName.isNotEmpty()) middleName else null,
                        email = email,
                        phone = phone,
                        username = username.removePrefix("@"),
                        password = password
                    )
                    coroutineScope.launch {
                        val success = authViewModel.registerUser(user)
                        if (success) {
                            onRegisterSuccess()
                        } else {
                            registrationError = userAlreadyExistsText
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                stringResource(R.string.register_button),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        }
    }
}
