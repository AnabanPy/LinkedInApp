package com.example.linkedinapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.linkedinapp.data.Job
import com.example.linkedinapp.viewmodel.JobsViewModel
import com.example.linkedinapp.viewmodel.JobsViewModelFactory
import com.example.linkedinapp.ui.theme.HeadHunterOrange
import com.example.linkedinapp.ui.theme.getHeadHunterBackground
import com.example.linkedinapp.ui.theme.getHeadHunterCardBackground
import com.example.linkedinapp.ui.theme.getHeadHunterCardBackgroundVariant
import com.example.linkedinapp.ui.theme.getHeadHunterTextPrimary
import com.example.linkedinapp.ui.theme.getHeadHunterTextSecondary
import com.example.linkedinapp.ui.theme.getHeadHunterPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyJobsScreen(
    currentUserId: Long,
    onBackClick: () -> Unit,
    onJobClick: (Long) -> Unit,
    onEditJob: (Long) -> Unit,
    onCreateJob: () -> Unit,
    jobsViewModelFactory: JobsViewModelFactory
) {
    val jobsViewModel: JobsViewModel = viewModel(factory = jobsViewModelFactory)
    val userJobs by jobsViewModel.userJobs.collectAsState()
    
    LaunchedEffect(currentUserId) {
        jobsViewModel.loadJobsByEmployer(currentUserId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Мои вакансии",
                        color = getHeadHunterTextPrimary()
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = getHeadHunterTextPrimary()
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onCreateJob) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Добавить вакансию",
                            tint = HeadHunterOrange
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = getHeadHunterCardBackground()
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateJob,
                containerColor = HeadHunterOrange
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить вакансию"
                )
            }
        },
        containerColor = getHeadHunterBackground()
    ) { paddingValues ->
        if (userJobs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "У вас пока нет вакансий",
                    fontSize = 16.sp,
                    color = getHeadHunterTextSecondary()
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = userJobs,
                    key = { job -> job.id }
                ) { job ->
                    MyJobCard(
                        job = job,
                        onJobClick = { onJobClick(job.id) },
                        onEditClick = { onEditJob(job.id) },
                        onDeleteClick = {
                            jobsViewModel.deleteJob(job) {
                                // После удаления список обновится автоматически через Flow
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MyJobCard(
    job: Job,
    onJobClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    // HeadHunter Dark Theme job card (упрощенная версия)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onJobClick),
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
            // Название вакансии
            Text(
                text = job.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = getHeadHunterTextPrimary(),
                lineHeight = 24.sp
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
            
            // Кнопки действий
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Кнопка "Изменить"
                Button(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = getHeadHunterPrimary()
                    )
                ) {
                    Text(
                        text = "Изменить",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                
                // Кнопка "Удалить"
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Удалить",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
    
    // Диалог подтверждения удаления
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Удалить вакансию?",
                    color = getHeadHunterTextPrimary()
                )
            },
            text = {
                Text(
                    text = "Вы уверены, что хотите удалить вакансию \"${job.title}\"? Это действие нельзя отменить.",
                    color = getHeadHunterTextSecondary()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Отмена")
                }
            },
            containerColor = getHeadHunterCardBackground()
        )
    }
}

