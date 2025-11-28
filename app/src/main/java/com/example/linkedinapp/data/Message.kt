package com.example.linkedinapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val senderId: Long,
    val receiverId: Long,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)


