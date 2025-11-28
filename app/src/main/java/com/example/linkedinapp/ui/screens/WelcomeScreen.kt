package com.example.linkedinapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linkedinapp.R

@Composable
fun WelcomeScreen(
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
    onGuestClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.welcome_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 48.dp)
        )
        
        // Telegram-style primary button
        Button(
            onClick = onRegisterClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                stringResource(R.string.register),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Telegram-style outlined button
        OutlinedButton(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                stringResource(R.string.login),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Telegram-style text button
        TextButton(
            onClick = onGuestClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(R.string.guest_login),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
