package com.example.linkedinapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.linkedinapp.R
import com.example.linkedinapp.data.Job
import com.example.linkedinapp.ui.components.ProfilePhoto
import com.example.linkedinapp.ui.components.TelegramTextField
import com.example.linkedinapp.viewmodel.JobsViewModel
import com.example.linkedinapp.viewmodel.JobsViewModelFactory
import com.example.linkedinapp.ui.theme.getHeadHunterBackground
import com.example.linkedinapp.ui.theme.getHeadHunterCardBackground
import com.example.linkedinapp.ui.theme.getHeadHunterCardBackgroundVariant
import com.example.linkedinapp.ui.theme.getHeadHunterTextPrimary
import com.example.linkedinapp.ui.theme.getHeadHunterTextSecondary
import com.example.linkedinapp.ui.theme.getHeadHunterPrimary
import com.example.linkedinapp.ui.theme.getHeadHunterBorder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
    userName: String? = null,
    profilePhotoId: Int = 0,
    profilePhotoUrl: String? = null,
    isGuest: Boolean = false,
    currentUserId: Long? = null,
    onNavigateToProfile: () -> Unit,
    onNavigateToMyJobs: () -> Unit = {},
    onNavigateToSettings: () -> Unit,
    onNavigateToMain: () -> Unit,
    onNavigateToMessages: () -> Unit = {},
    onNavigateToJobDetail: (Long) -> Unit = {},
    onNavigateToCreateJob: () -> Unit = {},
    onWriteClick: ((Long) -> Unit)? = null,
    jobsViewModelFactory: JobsViewModelFactory
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val jobsViewModel: JobsViewModel = viewModel(factory = jobsViewModelFactory)
    val jobs by jobsViewModel.jobs.collectAsState()
    val isLoading by jobsViewModel.isLoading.collectAsState()
    val searchQuery by jobsViewModel.searchQuery.collectAsState()
    val experienceFilter by jobsViewModel.experienceFilter.collectAsState()
    val cityFilter by jobsViewModel.cityFilter.collectAsState()
    val minSalaryFilter by jobsViewModel.minSalaryFilter.collectAsState()
    
    var searchText by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }
    
    // Debounce поиска
    LaunchedEffect(searchText) {
        delay(300)
        jobsViewModel.searchJobs(searchText)
    }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp)
            ) {
                // Заголовок drawer
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
                        text = if (isGuest) "Гость" else (userName ?: "Пользователь"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                HorizontalDivider()
                
                // Кнопка Профиль
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Профиль"
                        )
                    },
                    label = { Text("Профиль") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToProfile()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
                
                // Кнопка Мои вакансии
                if (!isGuest) {
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Мои вакансии"
                            )
                        },
                        label = { Text("Мои вакансии") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToMyJobs()
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                
                // Кнопка Настройки
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Настройки"
                        )
                    },
                    label = { Text("Настройки") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToSettings()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(getHeadHunterBackground())
        ) {
            // HeadHunter Dark Theme top bar
            Surface(
                color = getHeadHunterCardBackground(),
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
                            contentDescription = "Меню",
                            tint = getHeadHunterTextPrimary(),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = { onNavigateToMessages() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Сообщения",
                            tint = getHeadHunterTextPrimary(),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = { onNavigateToMain() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Лента",
                            tint = getHeadHunterTextPrimary(),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            HorizontalDivider(
                color = getHeadHunterBorder(),
                thickness = 1.dp
            )
            
            // Поиск и фильтры
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // HeadHunter-style search field
                TelegramTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = "Поиск вакансий...",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Поиск",
                            tint = getHeadHunterTextSecondary()
                        )
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                IconButton(
                    onClick = { showFilterDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Фильтры",
                        tint = if (experienceFilter != null) com.example.linkedinapp.ui.theme.HeadHunterOrange else getHeadHunterTextSecondary()
                    )
                }
                
                if (!isGuest && currentUserId != null) {
                    FloatingActionButton(
                        onClick = { onNavigateToCreateJob() },
                        modifier = Modifier.size(48.dp),
                        containerColor = com.example.linkedinapp.ui.theme.HeadHunterOrange,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Добавить вакансию"
                        )
                    }
                }
            }
            
            // Показ активных фильтров
            if (experienceFilter != null || cityFilter != null || minSalaryFilter != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (experienceFilter != null) {
                        FilterChip(
                            selected = true,
                            onClick = { jobsViewModel.setExperienceFilter(null) },
                            label = { Text("Опыт: $experienceFilter") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = com.example.linkedinapp.ui.theme.HeadHunterOrange.copy(alpha = 0.1f),
                                selectedLabelColor = com.example.linkedinapp.ui.theme.HeadHunterOrange
                            )
                        )
                    }
                    if (cityFilter != null) {
                        FilterChip(
                            selected = true,
                            onClick = { jobsViewModel.setCityFilter(null) },
                            label = { Text("Город: $cityFilter") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = com.example.linkedinapp.ui.theme.HeadHunterOrange.copy(alpha = 0.1f),
                                selectedLabelColor = com.example.linkedinapp.ui.theme.HeadHunterOrange
                            )
                        )
                    }
                    if (minSalaryFilter != null) {
                        FilterChip(
                            selected = true,
                            onClick = { jobsViewModel.setMinSalaryFilter(null) },
                            label = { Text("Зарплата: от ${minSalaryFilter}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = com.example.linkedinapp.ui.theme.HeadHunterOrange.copy(alpha = 0.1f),
                                selectedLabelColor = com.example.linkedinapp.ui.theme.HeadHunterOrange
                            )
                        )
                    }
                }
            }
            
            // Лента вакансий в стиле HeadHunter
            if (isLoading && jobs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = getHeadHunterPrimary()
                    )
                }
            } else if (jobs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty() || experienceFilter != null || cityFilter != null || minSalaryFilter != null) 
                                "Вакансии не найдены" 
                            else 
                                "Пока нет вакансий",
                            fontSize = 18.sp,
                            color = getHeadHunterTextSecondary()
                        )
                        if (searchQuery.isNotEmpty() || experienceFilter != null || cityFilter != null || minSalaryFilter != null) {
                            TextButton(
                                onClick = { jobsViewModel.clearFilters() },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = com.example.linkedinapp.ui.theme.HeadHunterOrange
                                )
                            ) {
                                Text("Сбросить фильтры")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(jobs) { job ->
                        JobCard(
                            job = job,
                            onClick = { onNavigateToJobDetail(job.id) },
                            onApplyClick = { onNavigateToJobDetail(job.id) },
                            onWriteClick = if (!isGuest && currentUserId != null && job.employerId != currentUserId) {
                                { onWriteClick?.invoke(job.employerId) }
                            } else null
                        )
                    }
                }
            }
        }
    }
    
    // Диалог фильтров
    if (showFilterDialog) {
        FilterDialog(
            currentExperience = experienceFilter,
            currentCity = cityFilter,
            currentMinSalary = minSalaryFilter,
            onDismiss = { showFilterDialog = false },
            onExperienceSelected = { experience ->
                jobsViewModel.setExperienceFilter(experience)
            },
            onCitySelected = { city ->
                jobsViewModel.setCityFilter(city)
            },
            onMinSalarySelected = { minSalary ->
                jobsViewModel.setMinSalaryFilter(minSalary)
            },
            onApply = { showFilterDialog = false },
            onClearFilters = { jobsViewModel.clearFilters() }
        )
    }
}

@Composable
fun JobCard(
    job: Job,
    onClick: () -> Unit,
    onApplyClick: () -> Unit = onClick,
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
            // Название вакансии (кликабельное)
            Text(
                text = job.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = getHeadHunterTextPrimary(),
                lineHeight = 24.sp,
                modifier = Modifier.clickable(onClick = onClick)
            )
            
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
            
            // Кнопка "Откликнуться"
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

@Composable
fun FilterDialog(
    currentExperience: String?,
    currentCity: String?,
    currentMinSalary: Int?,
    onDismiss: () -> Unit,
    onExperienceSelected: (String?) -> Unit,
    onCitySelected: (String?) -> Unit,
    onMinSalarySelected: (Int?) -> Unit,
    onApply: () -> Unit,
    onClearFilters: () -> Unit
) {
    val experienceOptions = listOf(
        "Без опыта",
        "1-3 года",
        "3-5 лет",
        "5+ лет"
    )
    
    var cityText by remember { mutableStateOf(currentCity ?: "") }
    var minSalaryText by remember { mutableStateOf(currentMinSalary?.toString() ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Фильтры") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Опыт работы
                Column {
                    Text(
                        text = "Опыт работы",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    experienceOptions.forEach { experience ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onExperienceSelected(experience) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentExperience == experience,
                                onClick = { onExperienceSelected(experience) }
                            )
                            Text(
                                text = experience,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onExperienceSelected(null) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentExperience == null,
                            onClick = { onExperienceSelected(null) }
                        )
                        Text(
                            text = "Любой",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                
                Divider()
                
                // Город
                Column {
                    Text(
                        text = "Город",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = cityText,
                        onValueChange = { cityText = it },
                        label = { Text("Введите город") },
                        placeholder = { Text("Например: Москва") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            if (cityText.isNotEmpty()) {
                                IconButton(onClick = { 
                                    cityText = ""
                                    onCitySelected(null)
                                }) {
                                    Text("✕")
                                }
                            }
                        }
                    )
                }
                
                Divider()
                
                // Минимальная зарплата
                Column {
                    Text(
                        text = "Минимальная зарплата",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = minSalaryText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) minSalaryText = it },
                        label = { Text("От") },
                        placeholder = { Text("0") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = {
                            if (minSalaryText.isNotEmpty()) {
                                IconButton(onClick = { 
                                    minSalaryText = ""
                                    onMinSalarySelected(null)
                                }) {
                                    Text("✕")
                                }
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = { 
                    cityText = ""
                    minSalaryText = ""
                    onClearFilters()
                    onDismiss()
                }) {
                    Text("Сбросить")
                }
                Button(onClick = {
                    onCitySelected(cityText.ifEmpty { null })
                    onMinSalarySelected(minSalaryText.toIntOrNull())
                    onApply()
                }) {
                    Text("Применить")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

