package com.example.linkedinapp.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linkedinapp.data.Job
import com.example.linkedinapp.viewmodel.JobsViewModel
import com.example.linkedinapp.viewmodel.JobsViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditJobScreen(
    jobId: Long,
    employerId: Long,
    onBackClick: () -> Unit,
    onJobUpdated: () -> Unit,
    jobsViewModelFactory: JobsViewModelFactory
) {
    val jobsViewModel: JobsViewModel = viewModel(factory = jobsViewModelFactory)
    
    val experienceOptions = listOf(
        "Без опыта",
        "1-3 года",
        "3-5 лет",
        "5+ лет"
    )
    
    val currencyOptions = listOf("руб.", "USD", "EUR", "KZT")
    
    var job by remember { mutableStateOf<Job?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    var title by remember { mutableStateOf("") }
    var salaryFrom by remember { mutableStateOf("") }
    var salaryTo by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("руб.") }
    var experienceExpanded by remember { mutableStateOf(false) }
    var selectedExperience by remember { mutableStateOf<String?>(null) }
    var resume by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var aboutUs by remember { mutableStateOf("") }
    var requiredQualities by remember { mutableStateOf("") }
    var weOffer by remember { mutableStateOf("") }
    var keySkills by remember { mutableStateOf("") }
    
    var isUpdating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Загружаем данные вакансии
    LaunchedEffect(jobId) {
        jobsViewModel.getJobById(jobId) { foundJob ->
            if (foundJob != null && foundJob.employerId == employerId) {
                job = foundJob
                title = foundJob.title
                salaryFrom = foundJob.salaryFrom?.toString() ?: ""
                salaryTo = foundJob.salaryTo?.toString() ?: ""
                selectedCurrency = foundJob.salaryCurrency
                selectedExperience = foundJob.experience
                resume = foundJob.resume
                city = foundJob.city
                aboutUs = foundJob.aboutUs
                requiredQualities = foundJob.requiredQualities
                weOffer = foundJob.weOffer
                keySkills = foundJob.keySkills
            }
            isLoading = false
        }
    }
    
    fun validateAndUpdate() {
        errorMessage = null
        
        if (title.isEmpty()) {
            errorMessage = "Название вакансии обязательно"
            return
        }
        if (selectedExperience == null) {
            errorMessage = "Опыт работы обязателен"
            return
        }
        if (resume.isEmpty()) {
            errorMessage = "Описание обязательно"
            return
        }
        if (city.isEmpty()) {
            errorMessage = "Город обязателен"
            return
        }
        if (aboutUs.isEmpty()) {
            errorMessage = "Поле 'О нас' обязательно"
            return
        }
        if (requiredQualities.isEmpty()) {
            errorMessage = "Поле 'Нужные качества' обязательно"
            return
        }
        if (weOffer.isEmpty()) {
            errorMessage = "Поле 'Мы предлагаем' обязательно"
            return
        }
        if (keySkills.isEmpty()) {
            errorMessage = "Поле 'Ключевые навыки' обязательно"
            return
        }
        
        val salaryFromInt = salaryFrom.toIntOrNull()
        val salaryToInt = salaryTo.toIntOrNull()
        
        if (salaryFromInt != null && salaryToInt != null && salaryFromInt > salaryToInt) {
            errorMessage = "Зарплата 'от' не может быть больше 'до'"
            return
        }
        
        val currentJob = job ?: return
        
        errorMessage = null
        isUpdating = true
        
        val updatedJob = currentJob.copy(
            title = title,
            salaryFrom = salaryFromInt,
            salaryTo = salaryToInt,
            salaryCurrency = selectedCurrency,
            experience = selectedExperience!!,
            resume = resume,
            city = city,
            aboutUs = aboutUs,
            requiredQualities = requiredQualities,
            weOffer = weOffer,
            keySkills = keySkills
        )
        
        jobsViewModel.updateJob(updatedJob) {
            isUpdating = false
            onJobUpdated()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактировать вакансию") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = { validateAndUpdate() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = !isUpdating && !isLoading,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (isUpdating) "Сохранение..." else "Сохранить изменения",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
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
                    Text("Вакансия не найдена или у вас нет прав на редактирование")
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (errorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                
                // Название вакансии (обязательно)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название вакансии *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Зарплата
                Text(
                    text = "Зарплата *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = salaryFrom,
                        onValueChange = { if (it.all { char -> char.isDigit() }) salaryFrom = it },
                        label = { Text("От") },
                        placeholder = { Text("0") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = salaryTo,
                        onValueChange = { if (it.all { char -> char.isDigit() }) salaryTo = it },
                        label = { Text("До") },
                        placeholder = { Text("0") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    var currencyExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        ExposedDropdownMenuBox(
                            expanded = currencyExpanded,
                            onExpandedChange = { currencyExpanded = !currencyExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedCurrency,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Валюта") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = currencyExpanded,
                                onDismissRequest = { currencyExpanded = false }
                            ) {
                                currencyOptions.forEach { currency ->
                                    DropdownMenuItem(
                                        text = { Text(currency) },
                                        onClick = {
                                            selectedCurrency = currency
                                            currencyExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Опыт работы
                Box(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = experienceExpanded,
                        onExpandedChange = { experienceExpanded = !experienceExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedExperience ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Опыт работы *") },
                            placeholder = { Text("Выберите опыт работы") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = experienceExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = experienceExpanded,
                            onDismissRequest = { experienceExpanded = false }
                        ) {
                            experienceOptions.forEach { experience ->
                                DropdownMenuItem(
                                    text = { Text(experience) },
                                    onClick = {
                                        selectedExperience = experience
                                        experienceExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Город (обязательно)
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Город *") },
                    placeholder = { Text("Введите город") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Описание (обязательно)
                OutlinedTextField(
                    value = resume,
                    onValueChange = { resume = it },
                    label = { Text("Описание *") },
                    placeholder = { Text("Описание вакансии") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )
                
                Divider()
                
                Text(
                    text = "Дополнительная информация",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // О нас (обязательно)
                OutlinedTextField(
                    value = aboutUs,
                    onValueChange = { aboutUs = it },
                    label = { Text("О нас *") },
                    placeholder = { Text("Расскажите о компании") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )
                
                // Нужные качества (обязательно)
                OutlinedTextField(
                    value = requiredQualities,
                    onValueChange = { requiredQualities = it },
                    label = { Text("Нужные качества *") },
                    placeholder = { Text("Какие качества вы ищете в кандидате") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )
                
                // Мы предлагаем (обязательно)
                OutlinedTextField(
                    value = weOffer,
                    onValueChange = { weOffer = it },
                    label = { Text("Мы предлагаем *") },
                    placeholder = { Text("Что вы предлагаете кандидату") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )
                
                // Ключевые навыки (обязательно)
                OutlinedTextField(
                    value = keySkills,
                    onValueChange = { keySkills = it },
                    label = { Text("Ключевые навыки *") },
                    placeholder = { Text("Например: Java, Kotlin, Android SDK") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )
            }
        }
    }
}

