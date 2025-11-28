package com.example.linkedinapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.linkedinapp.repository.JobRepository

class JobsViewModelFactory(
    private val jobRepository: JobRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JobsViewModel::class.java)) {
            return JobsViewModel(jobRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

