package com.example.linkedinapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.linkedinapp.R
import com.example.linkedinapp.ui.components.ProfilePhoto
import com.example.linkedinapp.viewmodel.MessagesViewModel
import com.example.linkedinapp.viewmodel.MessagesViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    onBackClick: () -> Unit,
    onConversationClick: (Long) -> Unit,
    messagesViewModelFactory: MessagesViewModelFactory
) {
    val messagesViewModel: MessagesViewModel = viewModel(factory = messagesViewModelFactory)
    val conversations by messagesViewModel.conversations.collectAsState()
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            // Telegram-style TopBar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = stringResource(R.string.messages),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { paddingValues ->
        if (conversations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_conversations),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(conversations) { conversation ->
                    ConversationItem(
                        conversation = conversation,
                        onClick = { onConversationClick(conversation.otherUser.id) },
                        context = context
                    )
                    // Telegram-style divider
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 80.dp, end = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: com.example.linkedinapp.viewmodel.ConversationItem,
    onClick: () -> Unit,
    context: android.content.Context
) {
    // Telegram-style conversation item - максимально похоже на Telegram
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Фото профиля - как в Telegram
        ProfilePhoto(
            profilePhotoId = conversation.otherUser.profilePhotoId,
            profilePhotoUrl = conversation.otherUser.profilePhotoUrl,
            modifier = Modifier.size(56.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Информация о переписке
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${conversation.otherUser.firstName} ${conversation.otherUser.lastName}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                
                    conversation.lastMessage?.let { message ->
                    Text(
                        text = formatTimestamp(message.timestamp, context),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(3.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                conversation.lastMessage?.let { message ->
                    Text(
                        text = message.text,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                } ?: Text(
                    text = stringResource(R.string.no_messages),
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

fun formatTimestamp(timestamp: Long, context: android.content.Context): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> context.getString(R.string.just_now)
        diff < 3600000 -> context.getString(R.string.minutes_ago, diff / 60000)
        diff < 86400000 -> context.getString(R.string.hours_ago, diff / 3600000)
        diff < 604800000 -> context.getString(R.string.days_ago, diff / 86400000)
        else -> {
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

