package com.example.linkedinapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val middleName: String? = null,
    val email: String,
    val phone: String,
    val username: String,
    val password: String,
    val profilePhotoId: Int = 0, // 0 = default, 1-6 = custom photos
    val profilePhotoUrl: String? = null // URL загруженного фото из Firebase Storage
)

