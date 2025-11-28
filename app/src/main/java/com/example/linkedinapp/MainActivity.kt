package com.example.linkedinapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.linkedinapp.navigation.NavGraph
import com.example.linkedinapp.ui.theme.LinkJobTheme
import com.example.linkedinapp.util.NotificationManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Создаем канал уведомлений
        NotificationManager.createNotificationChannel(this)
        
        enableEdgeToEdge()
        setContent {
            LinkJobTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    NavGraph(
                        navController = navController,
                        initialChatUserId = intent.getLongExtra("other_user_id", -1L).takeIf { it != -1L }
                    )
                }
            }
        }
    }
    
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}


