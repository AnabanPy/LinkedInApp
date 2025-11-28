package com.example.linkedinapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long
    
    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) AND password = :password LIMIT 1")
    suspend fun getUserByEmailAndPassword(email: String, password: String): User?
    
    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): User?
    
    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun getUserByUsername(username: String): User?
    
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>
    
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): User?
    
    @Query("UPDATE users SET profilePhotoId = :photoId WHERE id = :userId")
    suspend fun updateProfilePhoto(userId: Long, photoId: Int)
    
    @Query("UPDATE users SET profilePhotoUrl = :photoUrl WHERE id = :userId")
    suspend fun updateProfilePhotoUrl(userId: Long, photoUrl: String?)
    
    @Query("SELECT * FROM users WHERE LOWER(username) LIKE LOWER(:query) OR LOWER(firstName) LIKE LOWER(:query) OR LOWER(lastName) LIKE LOWER(:query) OR LOWER(firstName || ' ' || lastName) LIKE LOWER(:query) ORDER BY username LIMIT :limit")
    suspend fun searchUsersByUsername(query: String, limit: Int = 20): List<User>
}

