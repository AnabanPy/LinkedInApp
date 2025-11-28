package com.example.linkedinapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linkedinapp.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.linkedinapp.data.Message
import com.example.linkedinapp.data.User
import com.example.linkedinapp.ui.components.ProfilePhoto
import com.example.linkedinapp.ui.theme.TelegramDarkMessageBubbleReceived
import com.example.linkedinapp.ui.theme.TelegramDarkMessageBubbleSent
import com.example.linkedinapp.ui.theme.TelegramLightMessageBubbleReceived
import com.example.linkedinapp.ui.theme.TelegramLightMessageBubbleSent
import com.example.linkedinapp.ui.theme.rememberThemeManager
import com.example.linkedinapp.util.ThemeManager
import com.example.linkedinapp.viewmodel.MessagesViewModel
import com.example.linkedinapp.viewmodel.MessagesViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    otherUser: User,
    currentUserId: Long,
    onBackClick: () -> Unit,
    onNavigateToProfile: (Long) -> Unit,
    messagesViewModelFactory: MessagesViewModelFactory
) {
    val messagesViewModel: MessagesViewModel = viewModel(factory = messagesViewModelFactory)
    val messages by messagesViewModel.messages.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    // Загружаем сообщения при открытии чата или смене пользователя
    LaunchedEffect(otherUser.id) {
        messagesViewModel.loadMessages(otherUser.id)
    }
    
    // Очищаем сообщения при выходе из экрана
    DisposableEffect(Unit) {
        onDispose {
            messagesViewModel.clearMessages()
        }
    }
    
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }
    
    Scaffold(
        topBar = {
            // Telegram-style TopBar для чата
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToProfile(otherUser.id) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProfilePhoto(
                            profilePhotoId = otherUser.profilePhotoId,
                            profilePhotoUrl = otherUser.profilePhotoUrl,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${otherUser.firstName} ${otherUser.lastName}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.online),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Список сообщений (Telegram style)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background),
                state = listState,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    items = messages,
                    key = { message -> message.id }
                ) { message ->
                    MessageBubble(
                        message = message,
                        isFromCurrentUser = message.senderId == currentUserId
                    )
                }
            }
            
            // Поле ввода сообщения в стиле Telegram
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 36.dp, max = 120.dp),
                        placeholder = { 
                            Text(
                                stringResource(R.string.message_placeholder), 
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontSize = 15.sp
                            ) 
                        },
                        singleLine = false,
                        maxLines = 4,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    FloatingActionButton(
                        onClick = {
                            val textToSend = messageText.trim()
                            if (textToSend.isNotBlank()) {
                                messagesViewModel.sendMessage(otherUser.id, textToSend)
                                messageText = ""
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .then(
                                if (messageText.trim().isBlank()) Modifier.alpha(0.5f)
                                else Modifier
                            ),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = stringResource(R.string.send),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isFromCurrentUser: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 1.5.dp),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isFromCurrentUser) 12.dp else 4.dp,
                        bottomEnd = if (isFromCurrentUser) 4.dp else 12.dp
                    )
                )
                .background(
                    run {
                        val themeManager = rememberThemeManager()
                        val isSystemDark = isSystemInDarkTheme()
                        val isDarkTheme = themeManager.isDarkTheme(isSystemDark)
                        
                        if (isFromCurrentUser) {
                            // Sent messages - use primary color for light, special color for dark
                            if (isDarkTheme) {
                                TelegramDarkMessageBubbleSent
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        } else {
                            // Received messages
                            if (isDarkTheme) {
                                TelegramDarkMessageBubbleReceived
                            } else {
                                TelegramLightMessageBubbleReceived
                            }
                        }
                    }
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = message.text,
                fontSize = 15.sp,
                color = if (isFromCurrentUser) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                lineHeight = 19.sp
            )
        }
    }
}

