package com.example.linkedinapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.linkedinapp.R
import com.example.linkedinapp.data.Job
import com.example.linkedinapp.data.User
import com.example.linkedinapp.ui.components.ProfilePhoto
import com.example.linkedinapp.ui.components.TelegramTopBar
import com.example.linkedinapp.ui.theme.HeadHunterOrange
import com.example.linkedinapp.ui.theme.getHeadHunterCardBackground
import com.example.linkedinapp.ui.theme.getHeadHunterCardBackgroundVariant
import com.example.linkedinapp.ui.theme.getHeadHunterTextPrimary
import com.example.linkedinapp.ui.theme.getHeadHunterTextSecondary
import com.example.linkedinapp.ui.theme.getHeadHunterPrimary
import com.example.linkedinapp.viewmodel.JobsViewModel
import com.example.linkedinapp.viewmodel.JobsViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User? = null,
    isGuest: Boolean = false,
    isOwnProfile: Boolean = true,
    onBackClick: () -> Unit,
    onPhotoSelected: suspend (Int) -> Unit = {},
    onWriteMessage: (() -> Unit)? = null,
    onJobClick: ((Long) -> Unit)? = null,
    onEditJob: ((Long) -> Unit)? = null,
    jobsViewModelFactory: JobsViewModelFactory? = null
) {
    var showPhotoSelector by remember { mutableStateOf(false) }
    var selectedPhotoId by remember { mutableStateOf(user?.profilePhotoId ?: 0) }
    val scope = rememberCoroutineScope()
    
    val jobsViewModel: JobsViewModel? = jobsViewModelFactory?.let { viewModel(factory = it) }
    val userJobsState = jobsViewModel?.userJobs?.collectAsState() ?: remember { mutableStateOf(emptyList<Job>()) }
    val userJobs = userJobsState.value
    
    LaunchedEffect(user?.profilePhotoId) {
        selectedPhotoId = user?.profilePhotoId ?: 0
    }
    
    // Загружаем вакансии пользователя
    LaunchedEffect(user?.id, jobsViewModel) {
        if (user != null && jobsViewModel != null) {
            jobsViewModel.loadJobsByEmployer(user.id)
        }
    }
    
    val profilePhotos = listOf(
        0 to R.drawable.profile_photo_default,
        1 to R.drawable.profile_photo_1,
        2 to R.drawable.profile_photo_2,
        3 to R.drawable.profile_photo_3,
        4 to R.drawable.profile_photo_4,
        5 to R.drawable.profile_photo_5,
        6 to R.drawable.profile_photo_6
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TelegramTopBar(
            title = "Профиль",
            onBackClick = onBackClick
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (isGuest) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Вы вошли как гость",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                }
            } else if (user != null) {
                // Фото профиля сверху
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .then(
                                if (isOwnProfile) {
                                    Modifier
                                        .clickable { showPhotoSelector = true }
                                        .border(
                                            width = 2.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        )
                                } else {
                                    Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        ProfilePhoto(
                            profilePhotoId = selectedPhotoId,
                            profilePhotoUrl = user.profilePhotoUrl,
                            modifier = Modifier.size(if (isOwnProfile) 116.dp else 120.dp)
                        )
                    }
                    
                    if (isOwnProfile) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        TextButton(onClick = { showPhotoSelector = true }) {
                            Text("Изменить фото")
                        }
                    } else if (onWriteMessage != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { onWriteMessage() },
                            modifier = Modifier.fillMaxWidth(0.6f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                "Написать",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    thickness = 0.5.dp
                )
                
                // Telegram-style user data
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Личная информация",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    ProfileInfoItem(
                        label = "Фамилия",
                        value = user.lastName
                    )
                    
                    ProfileInfoItem(
                        label = "Имя",
                        value = user.firstName
                    )
                    
                    if (!user.middleName.isNullOrEmpty()) {
                        ProfileInfoItem(
                            label = "Отчество",
                            value = user.middleName
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Контактная информация",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    ProfileInfoItem(
                        label = "Почта",
                        value = user.email
                    )
                    
                    ProfileInfoItem(
                        label = "Телефон",
                        value = user.phone
                    )
                    
                    ProfileInfoItem(
                        label = "Имя пользователя",
                        value = "@${user.username}"
                    )
                }
                
                // Вакансии пользователя (только для других пользователей)
                if (!isOwnProfile && (userJobs.isNotEmpty() || jobsViewModel != null)) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        thickness = 0.5.dp
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = "Вакансии",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        if (userJobs.isEmpty()) {
                            Text(
                                text = "У пользователя пока нет вакансий",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                userJobs.forEach { job ->
                                    ProfileJobCard(
                                        job = job,
                                        isOwnProfile = false,
                                        onJobClick = { onJobClick?.invoke(job.id) },
                                        onEditClick = { onEditJob?.invoke(job.id) },
                                        onApplyClick = { onJobClick?.invoke(job.id) },
                                        onWriteClick = if (onWriteMessage != null) {
                                            { onWriteMessage() }
                                        } else null
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Диалог выбора фото (только для собственного профиля)
        if (showPhotoSelector && isOwnProfile) {
            AlertDialog(
                onDismissRequest = { showPhotoSelector = false },
                title = { Text("Выберите фото профиля") },
                text = {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.height(280.dp)
                    ) {
                        items(profilePhotos) { (photoId, drawableRes) ->
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (selectedPhotoId == photoId) 4.dp else 0.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedPhotoId = photoId
                                        scope.launch {
                                            onPhotoSelected(photoId)
                                            showPhotoSelector = false
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = drawableRes),
                                    contentDescription = "Фото профиля $photoId",
                                    modifier = Modifier
                                        .size(if (selectedPhotoId == photoId) 76.dp else 80.dp)
                                        .clip(CircleShape)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPhotoSelector = false }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileInfoItem(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ProfileJobCard(
    job: Job,
    isOwnProfile: Boolean,
    onJobClick: () -> Unit,
    onEditClick: () -> Unit,
    onApplyClick: () -> Unit = onJobClick,
    onWriteClick: (() -> Unit)? = null
) {
    // HeadHunter Dark Theme job card (упрощенная версия)
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = getHeadHunterCardBackground()
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Название вакансии (кликабельное)
                Text(
                    text = job.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onJobClick),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = getHeadHunterTextPrimary(),
                    lineHeight = 24.sp
                )
                
                // Кнопка редактирования (только для собственного профиля)
                if (isOwnProfile) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Редактировать",
                            modifier = Modifier.size(20.dp),
                            tint = HeadHunterOrange
                        )
                    }
                }
            }
            
            // Зарплата
            val salaryText = job.getSalaryString()
            if (salaryText.isNotEmpty()) {
                Text(
                    text = "$salaryText за месяц",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = getHeadHunterTextPrimary(),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            // Местоположение
            if (job.city.isNotEmpty()) {
                Text(
                    text = job.city,
                    fontSize = 14.sp,
                    color = getHeadHunterTextSecondary(),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            // Пил-образная кнопка с опытом работы
            if (job.experience.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = getHeadHunterCardBackgroundVariant(),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .wrapContentWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = getHeadHunterTextSecondary()
                        )
                        Text(
                            text = job.experience,
                            fontSize = 13.sp,
                            color = getHeadHunterTextSecondary()
                        )
                    }
                }
            }
            
            // Кнопка "Откликнуться" (только если не свой профиль)
            if (!isOwnProfile) {
                Button(
                    onClick = onApplyClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = getHeadHunterPrimary()
                    )
                ) {
                    Text(
                        text = "Откликнуться",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

