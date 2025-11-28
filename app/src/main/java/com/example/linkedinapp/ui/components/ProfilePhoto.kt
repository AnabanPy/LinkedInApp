package com.example.linkedinapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import coil.compose.rememberAsyncImagePainter
import com.example.linkedinapp.R
import com.example.linkedinapp.util.getProfilePhotoResource

/**
 * Компонент для отображения фото профиля пользователя.
 * Поддерживает как предустановленные фото (по profilePhotoId),
 * так и загруженные фото (по profilePhotoUrl из Firebase Storage).
 */
@Composable
fun ProfilePhoto(
    profilePhotoId: Int = 0,
    profilePhotoUrl: String? = null,
    modifier: Modifier = Modifier,
    size: Dp? = null,
    contentDescription: String? = "Фото профиля"
) {
    if (profilePhotoUrl != null && profilePhotoUrl.isNotEmpty()) {
        // Отображаем загруженное фото из Firebase Storage
        Image(
            painter = rememberAsyncImagePainter(
                model = profilePhotoUrl
            ),
            contentDescription = contentDescription,
            modifier = modifier
                .then(size?.let { Modifier.size(it) } ?: Modifier)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        // Отображаем предустановленное фото
        Image(
            painter = painterResource(id = getProfilePhotoResource(profilePhotoId)),
            contentDescription = contentDescription,
            modifier = modifier
                .then(size?.let { Modifier.size(it) } ?: Modifier)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}

