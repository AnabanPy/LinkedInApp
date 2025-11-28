package com.example.linkedinapp.util

import com.example.linkedinapp.R

fun getProfilePhotoResource(photoId: Int): Int {
    return when (photoId) {
        1 -> R.drawable.profile_photo_1
        2 -> R.drawable.profile_photo_2
        3 -> R.drawable.profile_photo_3
        4 -> R.drawable.profile_photo_4
        5 -> R.drawable.profile_photo_5
        6 -> R.drawable.profile_photo_6
        else -> R.drawable.profile_photo_default
    }
}


