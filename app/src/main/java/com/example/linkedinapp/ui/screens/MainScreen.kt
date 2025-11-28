package com.example.linkedinapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linkedinapp.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.linkedinapp.data.User
import com.example.linkedinapp.ui.components.ProfilePhoto
import com.example.linkedinapp.ui.components.TelegramTextField
import com.example.linkedinapp.ui.components.TelegramTopBar
import com.example.linkedinapp.viewmodel.MessagesViewModel
import com.example.linkedinapp.viewmodel.MessagesViewModelFactory
import com.example.linkedinapp.viewmodel.SearchViewModel
import com.example.linkedinapp.viewmodel.SearchViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    userName: String? = null,
    profilePhotoId: Int = 0,
    profilePhotoUrl: String? = null,
    isGuest: Boolean = false,
    currentUserId: Long? = null,
    onNavigateToProfile: () -> Unit,
    onNavigateToMyJobs: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToUserProfile: (Long) -> Unit = {},
    onNavigateToChat: (Long) -> Unit = {},
    onNavigateToMessages: () -> Unit = {},
    onNavigateToFeed: () -> Unit = {},
    onNavigateToMain: () -> Unit = {},
    searchViewModelFactory: SearchViewModelFactory,
    messagesViewModelFactory: MessagesViewModelFactory?
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val searchViewModel: SearchViewModel = viewModel(factory = searchViewModelFactory)
    val searchResults by searchViewModel.searchResults.collectAsState()
    val isSearching by searchViewModel.isSearching.collectAsState()
    val searchQuery by searchViewModel.searchQuery.collectAsState()
    
    val messagesViewModel: MessagesViewModel? = messagesViewModelFactory?.let { 
        viewModel(factory = it) 
    }
    val conversations by messagesViewModel?.conversations?.collectAsState() ?: remember { 
        mutableStateOf(emptyList<com.example.linkedinapp.viewmodel.ConversationItem>()) 
    }
    
    var queryText by remember { mutableStateOf("") }
    
    // Debounce поиска
    LaunchedEffect(queryText) {
        delay(300) // Задержка 300мс перед поиском
        if (queryText.isNotEmpty()) {
            searchViewModel.searchUsers(queryText)
        } else {
            searchViewModel.clearSearch()
        }
    }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                // Telegram-style drawer header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfilePhoto(
                        profilePhotoId = profilePhotoId,
                        profilePhotoUrl = profilePhotoUrl,
                        modifier = Modifier
                            .size(80.dp)
                            .padding(bottom = 12.dp)
                    )
                    Text(
                        text = if (isGuest) stringResource(R.string.guest) else (userName ?: stringResource(R.string.user)),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    thickness = 0.5.dp
                )
                
                // Telegram-style menu items
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = stringResource(R.string.profile),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    label = { Text(stringResource(R.string.profile), fontSize = 17.sp) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToProfile()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                
                if (!isGuest) {
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.my_jobs),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        label = { Text(stringResource(R.string.my_jobs), fontSize = 17.sp) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToMyJobs()
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
                
                NavigationDrawerItem(
                    icon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.settings),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        label = { Text(stringResource(R.string.settings), fontSize = 17.sp) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToSettings()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Telegram-style top bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(R.string.menu),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = { onNavigateToMessages() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = stringResource(R.string.messages),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = { onNavigateToFeed() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = stringResource(R.string.feed),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                thickness = 0.5.dp
            )
            
            // Telegram-style search
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                TelegramTextField(
                    value = queryText,
                    onValueChange = { queryText = it },
                    placeholder = stringResource(R.string.search_users),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.search),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    singleLine = true
                )
                
                // Telegram-style content
                when {
                    isSearching -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    searchQuery.isNotEmpty() && searchResults.isNotEmpty() -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            items(searchResults) { user ->
                                UserSearchResultItem(
                                    user = user,
                                    onClick = { onNavigateToUserProfile(user.id) }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 80.dp, end = 16.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                    searchQuery.isNotEmpty() && searchResults.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_users_found),
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    else -> {
                        // Telegram-style conversations list
                        if (!isGuest && conversations.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                items(conversations) { conversation ->
                                    ConversationItem(
                                        conversation = conversation,
                                        onClick = { onNavigateToChat(conversation.otherUser.id) },
                                        context = context
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 80.dp, end = 16.dp),
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                        thickness = 0.5.dp
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (isGuest) {
                                        Text(
                                            text = stringResource(R.string.guest_welcome),
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    } else if (userName != null) {
                                        Text(
                                            text = stringResource(R.string.welcome_user, userName),
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    
                                    if (conversations.isEmpty() && !isGuest) {
                                        Text(
                                            text = stringResource(R.string.no_conversations),
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

