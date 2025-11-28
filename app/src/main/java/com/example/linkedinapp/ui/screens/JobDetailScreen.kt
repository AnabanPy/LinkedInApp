package com.example.linkedinapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linkedinapp.data.Job
import com.example.linkedinapp.viewmodel.JobsViewModel
import com.example.linkedinapp.viewmodel.JobsViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.linkedinapp.ui.theme.HeadHunterOrange
import com.example.linkedinapp.ui.theme.getHeadHunterBackground
import com.example.linkedinapp.ui.theme.getHeadHunterCardBackground
import com.example.linkedinapp.ui.theme.getHeadHunterTextPrimary
import com.example.linkedinapp.ui.theme.getHeadHunterTextSecondary
import com.example.linkedinapp.ui.theme.getHeadHunterPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    jobId: Long,
    currentUserId: Long?,
    isGuest: Boolean,
    onBackClick: () -> Unit,
    onWriteClick: (Long) -> Unit,
    jobsViewModelFactory: JobsViewModelFactory
) {
    val jobsViewModel: JobsViewModel = viewModel(factory = jobsViewModelFactory)
    var job by remember { mutableStateOf<Job?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(jobId) {
        jobsViewModel.getJobById(jobId) { foundJob ->
            job = foundJob
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            // HeadHunter-style TopBar
            Surface(
                color = getHeadHunterCardBackground(),
                shadowElevation = 1.dp
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
                            contentDescription = "Назад",
                            tint = getHeadHunterTextPrimary()
                        )
                    }
                    Text(
                        text = "Вакансия",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = getHeadHunterTextPrimary()
                    )
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (job == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Вакансия не найдена")
                    Button(onClick = onBackClick) {
                        Text("Назад")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(getHeadHunterBackground())
                    .verticalScroll(rememberScrollState())
            ) {
                // Основная информация в стиле HeadHunter Dark Theme
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = getHeadHunterCardBackground()
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Заголовок вакансии
                        Text(
                            text = job!!.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = getHeadHunterTextPrimary()
                        )
                        
                        // Зарплата
                        val salaryText = job!!.getSalaryString()
                        if (salaryText.isNotEmpty()) {
                            Text(
                                text = "$salaryText за месяц",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = getHeadHunterTextPrimary()
                            )
                        }
                        
                        // Город и опыт работы
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            if (job!!.city.isNotEmpty()) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📍",
                                        fontSize = 16.sp
                                    )
                                    Column {
                                        Text(
                                            text = "Город",
                                            fontSize = 12.sp,
                                            color = getHeadHunterTextSecondary()
                                        )
                                        Text(
                                            text = job!!.city,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = getHeadHunterTextPrimary()
                                        )
                                    }
                                }
                            }
                            
                            if (job!!.experience.isNotEmpty()) {
                                Column {
                                    Text(
                                        text = "Опыт работы",
                                        fontSize = 12.sp,
                                        color = getHeadHunterTextSecondary()
                                    )
                                    Text(
                                        text = job!!.experience,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = getHeadHunterTextPrimary()
                                    )
                                }
                            }
                        }
                        
                        if (job!!.resume.isNotEmpty()) {
                            Divider()
                            Column {
                                Text(
                                    text = "Описание",
                                    fontSize = 12.sp,
                                    color = getHeadHunterTextSecondary(),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = job!!.resume,
                                    fontSize = 15.sp,
                                    color = getHeadHunterTextPrimary(),
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
                
                // О нас
                if (job!!.aboutUs.isNotEmpty()) {
                    SectionCard(
                        title = "О нас",
                        content = job!!.aboutUs
                    )
                }
                
                // Нужные качества
                if (job!!.requiredQualities.isNotEmpty()) {
                    SectionCard(
                        title = "Нужные качества",
                        content = job!!.requiredQualities
                    )
                }
                
                // Мы предлагаем
                if (job!!.weOffer.isNotEmpty()) {
                    SectionCard(
                        title = "Мы предлагаем",
                        content = job!!.weOffer
                    )
                }
                
                // Ключевые навыки
                if (job!!.keySkills.isNotEmpty()) {
                    SectionCard(
                        title = "Ключевые навыки",
                        content = job!!.keySkills
                    )
                }
                
                // Кнопка "Связаться" в стиле HeadHunter
                if (!isGuest && currentUserId != null && job!!.employerId != currentUserId) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        Button(
                            onClick = { onWriteClick(job!!.employerId) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = getHeadHunterPrimary()
                            )
                        ) {
                            Text(
                                text = "Связаться",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = getHeadHunterCardBackground()
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = getHeadHunterTextPrimary()
            )
            Text(
                text = content,
                fontSize = 15.sp,
                color = getHeadHunterTextSecondary(),
                lineHeight = 22.sp
            )
        }
    }
}

