package com.example.linkedinapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linkedinapp.data.Job
import com.example.linkedinapp.repository.JobRepository
import kotlinx.coroutines.Job as CoroutineJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class JobsViewModel(
    private val jobRepository: JobRepository
) : ViewModel() {
    private val _jobs = MutableStateFlow<List<Job>>(emptyList())
    val jobs: StateFlow<List<Job>> = _jobs.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _experienceFilter = MutableStateFlow<String?>(null)
    val experienceFilter: StateFlow<String?> = _experienceFilter.asStateFlow()
    
    private val _cityFilter = MutableStateFlow<String?>(null)
    val cityFilter: StateFlow<String?> = _cityFilter.asStateFlow()
    
    private val _minSalaryFilter = MutableStateFlow<Int?>(null)
    val minSalaryFilter: StateFlow<Int?> = _minSalaryFilter.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private var currentJob: CoroutineJob? = null
    
    init {
        loadAllJobs()
    }
    
    private fun loadAllJobs() {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            _isLoading.value = true
            jobRepository.getAllJobs()
                .catch { e ->
                    _isLoading.value = false
                }
                .collect { jobList ->
                    // Удаляем дубликаты на основе комбинации employerId, title и createdAt
                    val uniqueByKey = jobList.distinctBy { 
                        "${it.employerId}_${it.title}_${it.createdAt}"
                    }
                    // Также удаляем дубликаты по ID на случай, если они есть
                    _jobs.value = uniqueByKey.distinctBy { it.id }
                    _isLoading.value = false
                }
        }
    }
    
    fun searchJobs(query: String) {
        _searchQuery.value = query
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                val experience = _experienceFilter.value
                val city = _cityFilter.value
                val minSalary = _minSalaryFilter.value
                
                val flow = if (query.isEmpty()) {
                    jobRepository.filterJobs(experience, city, minSalary)
                } else {
                    jobRepository.searchJobsWithFilters(query, experience, city, minSalary)
                }
                
                flow
                    .catch { e ->
                        _isLoading.value = false
                    }
                    .collect { jobList ->
                        // Удаляем дубликаты на основе комбинации employerId, title и createdAt
                        val uniqueByKey = jobList.distinctBy { 
                            "${it.employerId}_${it.title}_${it.createdAt}"
                        }
                        // Также удаляем дубликаты по ID на случай, если они есть
                        _jobs.value = uniqueByKey.distinctBy { it.id }
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }
    
    fun setExperienceFilter(experience: String?) {
        _experienceFilter.value = experience
        searchJobs(_searchQuery.value)
    }
    
    fun setCityFilter(city: String?) {
        _cityFilter.value = city
        searchJobs(_searchQuery.value)
    }
    
    fun setMinSalaryFilter(minSalary: Int?) {
        _minSalaryFilter.value = minSalary
        searchJobs(_searchQuery.value)
    }
    
    fun clearFilters() {
        _searchQuery.value = ""
        _experienceFilter.value = null
        _cityFilter.value = null
        _minSalaryFilter.value = null
        loadAllJobs()
    }
    
    fun addJob(job: Job, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                jobRepository.insertJob(job)
                onSuccess()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun getJobById(jobId: Long, onResult: (Job?) -> Unit) {
        viewModelScope.launch {
            try {
                val job = jobRepository.getJobById(jobId)
                onResult(job)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }
    
    private val _userJobs = MutableStateFlow<List<Job>>(emptyList())
    val userJobs: StateFlow<List<Job>> = _userJobs.asStateFlow()
    
    private var currentUserJobsJob: CoroutineJob? = null
    
    fun loadJobsByEmployer(employerId: Long) {
        currentUserJobsJob?.cancel()
        currentUserJobsJob = viewModelScope.launch {
            jobRepository.getJobsByEmployer(employerId).collect { jobs ->
                // Удаляем дубликаты на основе комбинации employerId, title и createdAt
                val uniqueByKey = jobs.distinctBy { 
                    "${it.employerId}_${it.title}_${it.createdAt}"
                }
                // Также удаляем дубликаты по ID на случай, если они есть
                _userJobs.value = uniqueByKey.distinctBy { it.id }
            }
        }
    }
    
    fun updateJob(job: Job, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                jobRepository.updateJob(job)
                onSuccess()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun deleteJob(job: Job, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                jobRepository.deleteJob(job)
                onSuccess()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

