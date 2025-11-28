package com.example.linkedinapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message): Long
    
    @Query("SELECT * FROM messages WHERE (senderId = :userId AND receiverId = :otherUserId) OR (senderId = :otherUserId AND receiverId = :userId) ORDER BY timestamp ASC")
    fun getMessagesBetweenUsers(userId: Long, otherUserId: Long): Flow<List<Message>>
    
    @Query("SELECT * FROM messages WHERE senderId = :userId OR receiverId = :userId ORDER BY timestamp DESC")
    suspend fun getAllMessagesForUser(userId: Long): List<Message>
    
    @Query("SELECT * FROM messages WHERE id = :messageId LIMIT 1")
    suspend fun getMessageById(messageId: Long): Message?
    
    @Query("SELECT * FROM messages WHERE (senderId = :userId AND receiverId = :otherUserId) OR (senderId = :otherUserId AND receiverId = :userId) ORDER BY timestamp ASC")
    suspend fun getMessagesBetweenUsersSync(userId: Long, otherUserId: Long): List<Message>
}

