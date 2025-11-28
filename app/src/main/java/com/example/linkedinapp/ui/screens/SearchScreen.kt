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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.linkedinapp.viewmodel.SearchViewModel
import com.example.linkedinapp.viewmodel.SearchViewModelFactory
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onUserClick: (User) -> Unit = {},
    searchViewModelFactory: SearchViewModelFactory
) {
    val searchViewModel: SearchViewModel = viewModel(factory = searchViewModelFactory)
    val searchResults by searchViewModel.searchResults.collectAsState()
    val isSearching by searchViewModel.isSearching.collectAsState()
    val searchQuery by searchViewModel.searchQuery.collectAsState()
    
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
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TelegramTopBar(
            title = stringResource(R.string.search_users),
            onBackClick = onBackClick
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Telegram-style search field
            TelegramTextField(
                value = queryText,
                onValueChange = { queryText = it },
                placeholder = stringResource(R.string.search_placeholder),
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
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                thickness = 0.5.dp
            )
            
            // Результаты поиска
            when {
                isSearching -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                searchQuery.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.enter_search_query),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                searchResults.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        items(searchResults) { user ->
                            UserSearchResultItem(
                                user = user,
                                onClick = { onUserClick(user) }
                            )
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
    }
}

@Composable
fun UserSearchResultItem(
    user: User,
    onClick: () -> Unit
) {
    // Telegram-style user item
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfilePhoto(
            profilePhotoId = user.profilePhotoId,
            profilePhotoUrl = user.profilePhotoUrl,
            modifier = Modifier.size(56.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "${user.firstName} ${user.lastName}",
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(3.dp))
            
            Text(
                text = "@${user.username}",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

