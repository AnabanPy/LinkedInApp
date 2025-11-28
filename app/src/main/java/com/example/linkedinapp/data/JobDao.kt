package com.example.linkedinapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Query("SELECT * FROM jobs ORDER BY createdAt DESC")
    fun getAllJobs(): Flow<List<Job>>
    
    @Query("SELECT * FROM jobs WHERE id = :jobId")
    suspend fun getJobById(jobId: Long): Job?
    
    @Query("SELECT * FROM jobs WHERE employerId = :employerId ORDER BY createdAt DESC")
    fun getJobsByEmployer(employerId: Long): Flow<List<Job>>
    
    @Query("SELECT * FROM jobs WHERE title LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchJobsByTitle(query: String): Flow<List<Job>>
    
    @Query("""
        SELECT * FROM jobs 
        WHERE title LIKE '%' || :query || '%' 
        AND (:experience IS NULL OR experience = :experience)
        AND (:city IS NULL OR city LIKE '%' || :city || '%')
        AND (:minSalary IS NULL OR salaryFrom >= :minSalary)
        ORDER BY createdAt DESC
    """)
    fun searchJobsWithFilters(
        query: String, 
        experience: String?, 
        city: String?,
        minSalary: Int?
    ): Flow<List<Job>>
    
    @Query("""
        SELECT * FROM jobs 
        WHERE (:experience IS NULL OR experience = :experience)
        AND (:city IS NULL OR city LIKE '%' || :city || '%')
        AND (:minSalary IS NULL OR salaryFrom >= :minSalary)
        ORDER BY createdAt DESC
    """)
    fun filterJobs(
        experience: String?,
        city: String?,
        minSalary: Int?
    ): Flow<List<Job>>
    
    @Query("SELECT * FROM jobs WHERE experience LIKE '%' || :experience || '%' ORDER BY createdAt DESC")
    fun filterJobsByExperience(experience: String): Flow<List<Job>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: Job): Long
    
    @Update
    suspend fun updateJob(job: Job)
    
    @Delete
    suspend fun deleteJob(job: Job)
    
    @Query("DELETE FROM jobs WHERE id = :jobId")
    suspend fun deleteJobById(jobId: Long)
    
    @Query("DELETE FROM jobs WHERE employerId = :employerId AND title = :title AND createdAt = :createdAt AND id != :excludeId")
    suspend fun deleteDuplicateJobs(employerId: Long, title: String, createdAt: Long, excludeId: Long)
    
    @Query("SELECT * FROM jobs WHERE employerId = :employerId AND title = :title AND createdAt = :createdAt")
    suspend fun findJobsByEmployerTitleAndDate(employerId: Long, title: String, createdAt: Long): List<Job>
    
    @Query("""
        DELETE FROM jobs 
        WHERE id NOT IN (
            SELECT MIN(id) 
            FROM jobs 
            GROUP BY employerId, title, createdAt
        )
    """)
    suspend fun deleteAllDuplicateJobs()
}

