package com.example.linkedinapp.repository

import android.content.Context
import com.example.linkedinapp.data.Job
import com.example.linkedinapp.data.JobDao
import com.example.linkedinapp.util.NetworkUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class JobRepository(
    private val jobDao: JobDao,
    private val context: Context
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val jobsCollection = firestore.collection("jobs")
    
    private fun isOnline(): Boolean = NetworkUtils.checkInternetConnection(context)
    
    fun getAllJobs(): Flow<List<Job>> = flow {
        if (isOnline()) {
            try {
                val snapshot = jobsCollection
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()
                
                val jobs = snapshot.documents.mapNotNull { doc ->
                    doc.data?.let { 
                        val job = mapToJob(doc.id, it)
                        // Используем хеш ID Firestore как локальный ID для совместимости
                        val firestoreId = doc.id.hashCode().toLong()
                        job.copy(id = firestoreId)
                    }
                }
                
                // Удаляем дубликаты перед сохранением
                // Группируем по employerId, title и createdAt для поиска дубликатов
                val uniqueJobs = mutableListOf<Job>()
                val seenKeys = mutableSetOf<String>()
                
                for (job in jobs) {
                    // Создаем уникальный ключ для проверки дубликатов
                    val key = "${job.employerId}_${job.title}_${job.createdAt}"
                    if (!seenKeys.contains(key)) {
                        seenKeys.add(key)
                        uniqueJobs.add(job)
                        
                        // Удаляем старые дубликаты с другими ID
                        val duplicates = jobDao.findJobsByEmployerTitleAndDate(
                            job.employerId, 
                            job.title, 
                            job.createdAt
                        )
                        for (duplicate in duplicates) {
                            if (duplicate.id != job.id) {
                                jobDao.deleteJobById(duplicate.id)
                            }
                        }
                    }
                }
                
                // Сохраняем уникальные вакансии локально с правильными ID
                uniqueJobs.forEach { jobDao.insertJob(it) }
                // Удаляем все оставшиеся дубликаты из БД
                jobDao.deleteAllDuplicateJobs()
                emit(uniqueJobs)
            } catch (e: Exception) {
                // При ошибке читаем из БД и удаляем дубликаты
                jobDao.deleteAllDuplicateJobs()
                jobDao.getAllJobs().collect { dbJobs ->
                    // Удаляем дубликаты по комбинации employerId, title и createdAt
                    val uniqueDbJobs = dbJobs.distinctBy { 
                        "${it.employerId}_${it.title}_${it.createdAt}"
                    }
                    // Также удаляем дубликаты по ID
                    val finalUniqueJobs = uniqueDbJobs.distinctBy { it.id }
                    emit(finalUniqueJobs)
                }
            }
        } else {
            // Удаляем все дубликаты перед чтением для гарантии
            try {
                jobDao.deleteAllDuplicateJobs()
            } catch (e: Exception) {
                // Игнорируем ошибку
            }
            // Читаем из БД и удаляем дубликаты
            jobDao.getAllJobs().collect { dbJobs ->
                // Удаляем дубликаты по комбинации employerId, title и createdAt
                val uniqueDbJobs = dbJobs.distinctBy { 
                    "${it.employerId}_${it.title}_${it.createdAt}"
                }
                // Также удаляем дубликаты по ID
                val finalUniqueJobs = uniqueDbJobs.distinctBy { it.id }
                emit(finalUniqueJobs)
            }
        }
    }
    
    suspend fun getJobById(jobId: Long): Job? {
        // Сначала проверяем локально
        val localJob = jobDao.getJobById(jobId)
        if (localJob != null) {
            return localJob
        }
        
        // Если не найден локально, пытаемся найти в Firestore
        // Ищем по всем вакансиям и сравниваем ID
        if (isOnline()) {
            try {
                val snapshot = jobsCollection.get().await()
                for (doc in snapshot.documents) {
                    val firestoreId = doc.id.hashCode().toLong()
                    if (firestoreId == jobId || doc.id == jobId.toString()) {
                        val job = mapToJob(doc.id, doc.data!!)
                        // Сохраняем с правильным ID
                        jobDao.insertJob(job.copy(id = jobId))
                        return job.copy(id = jobId)
                    }
                }
            } catch (e: Exception) {
                // Игнорируем ошибку
            }
        }
        
        return null
    }
    
    fun getJobsByEmployer(employerId: Long): Flow<List<Job>> = flow {
        if (isOnline()) {
            try {
                val querySnapshot = jobsCollection
                    .whereEqualTo("employerId", employerId.toString())
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()
                
                val jobs = querySnapshot.documents.mapNotNull { doc ->
                    doc.data?.let { 
                        val job = mapToJob(doc.id, it)
                        // Используем хеш ID Firestore как локальный ID для совместимости
                        val firestoreId = doc.id.hashCode().toLong()
                        job.copy(id = firestoreId)
                    }
                }
                
                // Удаляем дубликаты перед сохранением
                // Группируем по employerId, title и createdAt для поиска дубликатов
                val uniqueJobs = mutableListOf<Job>()
                val seenKeys = mutableSetOf<String>()
                
                for (job in jobs) {
                    // Создаем уникальный ключ для проверки дубликатов
                    val key = "${job.employerId}_${job.title}_${job.createdAt}"
                    if (!seenKeys.contains(key)) {
                        seenKeys.add(key)
                        uniqueJobs.add(job)
                        
                        // Удаляем все дубликаты из БД перед сохранением
                        val duplicates = jobDao.findJobsByEmployerTitleAndDate(
                            job.employerId, 
                            job.title, 
                            job.createdAt
                        )
                        // Удаляем все дубликаты, оставляем только один (с ID из Firestore)
                        for (duplicate in duplicates) {
                            if (duplicate.id != job.id) {
                                jobDao.deleteJobById(duplicate.id)
                            }
                        }
                    }
                }
                
                // Сохраняем уникальные вакансии локально с правильными ID
                uniqueJobs.forEach { jobDao.insertJob(it) }
                
                // Удаляем все оставшиеся дубликаты из БД
                jobDao.deleteAllDuplicateJobs()
            } catch (e: Exception) {
                // Игнорируем ошибку и используем данные из БД
            }
        }
        
        // Удаляем все дубликаты перед чтением для гарантии
        try {
            jobDao.deleteAllDuplicateJobs()
        } catch (e: Exception) {
            // Игнорируем ошибку
        }
        
        // Всегда читаем из БД и удаляем дубликаты
        jobDao.getJobsByEmployer(employerId).collect { dbJobs ->
            // Удаляем дубликаты по комбинации employerId, title и createdAt
            val uniqueDbJobs = dbJobs.distinctBy { 
                "${it.employerId}_${it.title}_${it.createdAt}"
            }
            // Также удаляем дубликаты по ID на случай, если они есть
            val finalUniqueJobs = uniqueDbJobs.distinctBy { it.id }
            emit(finalUniqueJobs)
        }
    }
    
    fun searchJobsByTitle(query: String): Flow<List<Job>> = flow {
        if (isOnline()) {
            try {
                val querySnapshot = jobsCollection
                    .whereGreaterThanOrEqualTo("title", query)
                    .whereLessThanOrEqualTo("title", query + "\uf8ff")
                    .orderBy("title")
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()
                
                val jobs = querySnapshot.documents.mapNotNull { doc ->
                    doc.data?.let { 
                        val job = mapToJob(doc.id, it)
                        // Используем хеш ID Firestore как локальный ID для совместимости
                        val firestoreId = doc.id.hashCode().toLong()
                        job.copy(id = firestoreId)
                    }
                }
                
                jobs.forEach { jobDao.insertJob(it) }
                jobDao.deleteAllDuplicateJobs()
                emit(jobs)
            } catch (e: Exception) {
                jobDao.deleteAllDuplicateJobs()
                jobDao.searchJobsByTitle(query).collect { dbJobs ->
                    val uniqueDbJobs = dbJobs.distinctBy { 
                        "${it.employerId}_${it.title}_${it.createdAt}"
                    }
                    emit(uniqueDbJobs.distinctBy { it.id })
                }
            }
        } else {
            jobDao.deleteAllDuplicateJobs()
            jobDao.searchJobsByTitle(query).collect { dbJobs ->
                val uniqueDbJobs = dbJobs.distinctBy { 
                    "${it.employerId}_${it.title}_${it.createdAt}"
                }
                emit(uniqueDbJobs.distinctBy { it.id })
            }
        }
    }
    
    fun searchJobsWithFilters(
        query: String,
        experience: String?,
        city: String?,
        minSalary: Int?
    ): Flow<List<Job>> = flow {
        if (isOnline()) {
            try {
                var firestoreQuery = jobsCollection as com.google.firebase.firestore.Query
                
                if (query.isNotEmpty()) {
                    firestoreQuery = firestoreQuery
                        .whereGreaterThanOrEqualTo("title", query)
                        .whereLessThanOrEqualTo("title", query + "\uf8ff")
                }
                
                if (experience != null) {
                    firestoreQuery = firestoreQuery.whereEqualTo("experience", experience)
                }
                
                if (city != null) {
                    firestoreQuery = firestoreQuery.whereGreaterThanOrEqualTo("city", city)
                        .whereLessThanOrEqualTo("city", city + "\uf8ff")
                }
                
                if (minSalary != null) {
                    firestoreQuery = firestoreQuery.whereGreaterThanOrEqualTo("salaryFrom", minSalary)
                }
                
                val snapshot = firestoreQuery
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()
                
                val jobs = snapshot.documents.mapNotNull { doc ->
                    doc.data?.let { 
                        val job = mapToJob(doc.id, it)
                        // Используем хеш ID Firestore как локальный ID для совместимости
                        val firestoreId = doc.id.hashCode().toLong()
                        job.copy(id = firestoreId)
                    }
                }
                
                jobs.forEach { jobDao.insertJob(it) }
                jobDao.deleteAllDuplicateJobs()
                emit(jobs)
            } catch (e: Exception) {
                jobDao.deleteAllDuplicateJobs()
                jobDao.searchJobsWithFilters(query, experience, city, minSalary).collect { dbJobs ->
                    val uniqueDbJobs = dbJobs.distinctBy { 
                        "${it.employerId}_${it.title}_${it.createdAt}"
                    }
                    emit(uniqueDbJobs.distinctBy { it.id })
                }
            }
        } else {
            jobDao.deleteAllDuplicateJobs()
            jobDao.searchJobsWithFilters(query, experience, city, minSalary).collect { dbJobs ->
                val uniqueDbJobs = dbJobs.distinctBy { 
                    "${it.employerId}_${it.title}_${it.createdAt}"
                }
                emit(uniqueDbJobs.distinctBy { it.id })
            }
        }
    }
    
    fun filterJobs(
        experience: String?,
        city: String?,
        minSalary: Int?
    ): Flow<List<Job>> = flow {
        if (isOnline()) {
            try {
                var firestoreQuery = jobsCollection as com.google.firebase.firestore.Query
                
                if (experience != null) {
                    firestoreQuery = firestoreQuery.whereEqualTo("experience", experience)
                }
                
                if (city != null) {
                    firestoreQuery = firestoreQuery.whereGreaterThanOrEqualTo("city", city)
                        .whereLessThanOrEqualTo("city", city + "\uf8ff")
                }
                
                if (minSalary != null) {
                    firestoreQuery = firestoreQuery.whereGreaterThanOrEqualTo("salaryFrom", minSalary)
                }
                
                val snapshot = firestoreQuery
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()
                
                val jobs = snapshot.documents.mapNotNull { doc ->
                    doc.data?.let { 
                        val job = mapToJob(doc.id, it)
                        // Используем хеш ID Firestore как локальный ID для совместимости
                        val firestoreId = doc.id.hashCode().toLong()
                        job.copy(id = firestoreId)
                    }
                }
                
                jobs.forEach { jobDao.insertJob(it) }
                jobDao.deleteAllDuplicateJobs()
                emit(jobs)
            } catch (e: Exception) {
                jobDao.deleteAllDuplicateJobs()
                jobDao.filterJobs(experience, city, minSalary).collect { dbJobs ->
                    val uniqueDbJobs = dbJobs.distinctBy { 
                        "${it.employerId}_${it.title}_${it.createdAt}"
                    }
                    emit(uniqueDbJobs.distinctBy { it.id })
                }
            }
        } else {
            jobDao.deleteAllDuplicateJobs()
            jobDao.filterJobs(experience, city, minSalary).collect { dbJobs ->
                val uniqueDbJobs = dbJobs.distinctBy { 
                    "${it.employerId}_${it.title}_${it.createdAt}"
                }
                emit(uniqueDbJobs.distinctBy { it.id })
            }
        }
    }
    
    fun filterJobsByExperience(experience: String): Flow<List<Job>> = flow {
        if (isOnline()) {
            try {
                val querySnapshot = jobsCollection
                    .whereEqualTo("experience", experience)
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()
                
                val jobs = querySnapshot.documents.mapNotNull { doc ->
                    doc.data?.let { 
                        val job = mapToJob(doc.id, it)
                        // Используем хеш ID Firestore как локальный ID для совместимости
                        val firestoreId = doc.id.hashCode().toLong()
                        job.copy(id = firestoreId)
                    }
                }
                
                jobs.forEach { jobDao.insertJob(it) }
                jobDao.deleteAllDuplicateJobs()
                emit(jobs)
            } catch (e: Exception) {
                jobDao.deleteAllDuplicateJobs()
                jobDao.filterJobsByExperience(experience).collect { dbJobs ->
                    val uniqueDbJobs = dbJobs.distinctBy { 
                        "${it.employerId}_${it.title}_${it.createdAt}"
                    }
                    emit(uniqueDbJobs.distinctBy { it.id })
                }
            }
        } else {
            jobDao.deleteAllDuplicateJobs()
            jobDao.filterJobsByExperience(experience).collect { dbJobs ->
                val uniqueDbJobs = dbJobs.distinctBy { 
                    "${it.employerId}_${it.title}_${it.createdAt}"
                }
                emit(uniqueDbJobs.distinctBy { it.id })
            }
        }
    }
    
    suspend fun insertJob(job: Job): Long {
        val jobId = if (isOnline()) {
            try {
                val jobMap = jobToMap(job)
                val docRef = jobsCollection.add(jobMap).await()
                // Используем хеш строки ID как Long для совместимости
                docRef.id.hashCode().toLong()
            } catch (e: Exception) {
                // При ошибке генерируем ID на основе employerId и title для консистентности
                (job.employerId.toString() + job.title + job.createdAt.toString()).hashCode().toLong()
            }
        } else {
            // Для офлайн вакансий генерируем ID на основе employerId, title и createdAt для консистентности
            (job.employerId.toString() + job.title + job.createdAt.toString()).hashCode().toLong()
        }
        
        // Проверяем, существует ли уже вакансия с таким employerId, title и createdAt
        // Если да, удаляем старые дубликаты
        val existingJobs = jobDao.findJobsByEmployerTitleAndDate(
            job.employerId,
            job.title,
            job.createdAt
        )
        
        // Удаляем все существующие дубликаты с другими ID
        for (existingJob in existingJobs) {
            if (existingJob.id != jobId) {
                jobDao.deleteJobById(existingJob.id)
            }
        }
        
        // Всегда сохраняем локально с правильным ID
        // Если ID уже установлен и не равен 0, Room будет использовать его
        val jobWithId = job.copy(id = jobId)
        val localId = jobDao.insertJob(jobWithId)
        // Если Room вернул другой ID (автогенерированный), используем его только если jobId был 0
        return if (jobId != 0L) jobId else localId
    }
    
    suspend fun updateJob(job: Job) {
        // Обновляем локально
        jobDao.updateJob(job)
        
        if (isOnline()) {
            try {
                // Ищем документ в Firestore по employerId, title и createdAt
                // так как job.id - это хеш от Firestore ID, а не сам Firestore ID
                val querySnapshot = jobsCollection
                    .whereEqualTo("employerId", job.employerId.toString())
                    .whereEqualTo("title", job.title)
                    .whereEqualTo("createdAt", job.createdAt)
                    .limit(1)
                    .get()
                    .await()
                
                if (!querySnapshot.isEmpty) {
                    val doc = querySnapshot.documents.first()
                    doc.reference.set(jobToMap(job)).await()
                }
            } catch (e: Exception) {
                // Игнорируем ошибку, локальное обновление уже выполнено
            }
        }
    }
    
    suspend fun deleteJob(job: Job) {
        if (isOnline()) {
            try {
                // Ищем документ в Firestore по employerId, title и createdAt
                // так как job.id - это хеш от Firestore ID, а не сам Firestore ID
                val querySnapshot = jobsCollection
                    .whereEqualTo("employerId", job.employerId.toString())
                    .whereEqualTo("title", job.title)
                    .whereEqualTo("createdAt", job.createdAt)
                    .limit(1)
                    .get()
                    .await()
                
                if (!querySnapshot.isEmpty) {
                    // Удаляем найденный документ из Firestore
                    val doc = querySnapshot.documents.first()
                    jobsCollection.document(doc.id).delete().await()
                }
            } catch (e: Exception) {
                // Игнорируем ошибку, удалим локально
            }
        }
        // Всегда удаляем из локальной базы
        jobDao.deleteJob(job)
    }
    
    private fun jobToMap(job: Job): Map<String, Any> {
        return mapOf(
            "title" to job.title,
            "salaryFrom" to (job.salaryFrom ?: 0),
            "salaryTo" to (job.salaryTo ?: 0),
            "salaryCurrency" to job.salaryCurrency,
            "experience" to job.experience,
            "resume" to job.resume,
            "city" to job.city,
            "aboutUs" to job.aboutUs,
            "requiredQualities" to job.requiredQualities,
            "weOffer" to job.weOffer,
            "keySkills" to job.keySkills,
            "employerId" to job.employerId.toString(),
            "createdAt" to job.createdAt
        )
    }
    
    private fun mapToJob(id: String, data: Map<String, Any>): Job {
        // Используем хеш строки ID как Long для совместимости с Room
        val jobId = id.hashCode().toLong()
        return Job(
            id = jobId,
            title = data["title"] as? String ?: "",
            salaryFrom = (data["salaryFrom"] as? Number)?.toInt(),
            salaryTo = (data["salaryTo"] as? Number)?.toInt(),
            salaryCurrency = data["salaryCurrency"] as? String ?: "руб.",
            experience = data["experience"] as? String ?: "",
            resume = data["resume"] as? String ?: "",
            city = data["city"] as? String ?: "",
            aboutUs = data["aboutUs"] as? String ?: "",
            requiredQualities = data["requiredQualities"] as? String ?: "",
            weOffer = data["weOffer"] as? String ?: "",
            keySkills = data["keySkills"] as? String ?: "",
            employerId = (data["employerId"] as? String)?.toLongOrNull() ?: 0L,
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}

