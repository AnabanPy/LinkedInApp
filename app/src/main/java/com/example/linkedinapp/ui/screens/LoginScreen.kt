package com.example.linkedinapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.linkedinapp.ui.components.TelegramTopBar
import com.example.linkedinapp.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onLoginError: () -> Unit,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val fillAllFieldsText = stringResource(R.string.fill_all_fields)
    val wrongCredentialsText = stringResource(R.string.wrong_credentials)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TelegramTopBar(
            title = stringResource(R.string.login_title_screen),
            onBackClick = onBackClick
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = ""
                },
                label = { Text(stringResource(R.string.email)) },
                placeholder = { Text(stringResource(R.string.email_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = ""
                },
                label = { Text(stringResource(R.string.password)) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
            
            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        errorMessage = fillAllFieldsText
                    } else {
                        coroutineScope.launch {
                            val success = authViewModel.loginUser(email, password)
                            if (success) {
                                onLoginSuccess()
                            } else {
                                errorMessage = wrongCredentialsText
                                onLoginError()
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
                    stringResource(R.string.login_button),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

