package com.example.linkedinapp.repository

import android.content.Context
import com.example.linkedinapp.data.User
import com.example.linkedinapp.data.UserDao
import com.example.linkedinapp.util.NetworkUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val userDao: UserDao,
    private val context: Context
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    
    private fun isOnline(): Boolean = NetworkUtils.checkInternetConnection(context)
    
    suspend fun insertUser(user: User): Long {
        val userId = if (isOnline()) {
            // Сохраняем в Firestore
            try {
                val userMap = userToMap(user)
                val docRef = usersCollection.add(userMap).await()
                // Используем хеш строки ID как Long для совместимости
                docRef.id.hashCode().toLong()
            } catch (e: Exception) {
                // При ошибке генерируем ID на основе email для консистентности
                (user.email.lowercase().trim() + user.username.lowercase().trim()).hashCode().toLong()
            }
        } else {
            // Для офлайн пользователей генерируем ID на основе email и username для консистентности
            (user.email.lowercase().trim() + user.username.lowercase().trim()).hashCode().toLong()
        }
        
        // Проверяем, существует ли уже пользователь с таким email или username
        // Если да, обновляем его ID, чтобы избежать дубликатов
        val existingUserByEmail = userDao.getUserByEmail(user.email.lowercase().trim())
        val existingUserByUsername = userDao.getUserByUsername(user.username.lowercase().trim())
        
        val existingUser = existingUserByEmail ?: existingUserByUsername
        if (existingUser != null && existingUser.id != userId) {
            // Если пользователь существует с другим ID, обновляем его
            val updatedUser = existingUser.copy(
                id = userId,
                firstName = user.firstName,
                lastName = user.lastName,
                middleName = user.middleName,
                email = user.email.lowercase().trim(),
                phone = user.phone,
                username = user.username.lowercase().trim(),
                password = user.password,
                profilePhotoId = user.profilePhotoId,
                profilePhotoUrl = user.profilePhotoUrl
            )
            userDao.insertUser(updatedUser)
            return userId
        }
        
        // Всегда сохраняем локально с правильным ID
        // Если ID уже установлен и не равен 0, Room будет использовать его
        val userWithId = user.copy(
            id = userId,
            email = user.email.lowercase().trim(),
            username = user.username.lowercase().trim()
        )
        val localId = userDao.insertUser(userWithId)
        // Если Room вернул другой ID (автогенерированный), используем его только если userId был 0
        return if (userId != 0L) userId else localId
    }
    
    suspend fun getUserByEmailAndPassword(email: String, password: String): User? {
        return if (isOnline()) {
            // Ищем в Firestore
            try {
                val querySnapshot = usersCollection
                    .whereEqualTo("email", email.lowercase().trim())
                    .whereEqualTo("password", password)
                    .limit(1)
                    .get()
                    .await()
                
                if (!querySnapshot.isEmpty) {
                    val doc = querySnapshot.documents.first()
                    val user = mapToUser(doc.id, doc.data!!)
                    // Сохраняем локально с правильным ID (хеш Firestore ID)
                    userDao.insertUser(user)
                    user
                } else {
                    // Если не найдено в Firestore, проверяем локально
                    userDao.getUserByEmailAndPassword(email, password)
                }
            } catch (e: Exception) {
                // При ошибке используем локальную базу
                userDao.getUserByEmailAndPassword(email, password)
            }
        } else {
            // Используем только локальную базу
            userDao.getUserByEmailAndPassword(email, password)
        }
    }
    
    suspend fun getUserByEmail(email: String): User? {
        return if (isOnline()) {
            try {
                val querySnapshot = usersCollection
                    .whereEqualTo("email", email.lowercase().trim())
                    .limit(1)
                    .get()
                    .await()
                
                if (!querySnapshot.isEmpty) {
                    val doc = querySnapshot.documents.first()
                    val user = mapToUser(doc.id, doc.data!!)
                    // Сохраняем локально с правильным ID (хеш Firestore ID)
                    userDao.insertUser(user)
                    user
                } else {
                    userDao.getUserByEmail(email)
                }
            } catch (e: Exception) {
                userDao.getUserByEmail(email)
            }
        } else {
            userDao.getUserByEmail(email)
        }
    }
    
    suspend fun getUserByUsername(username: String): User? {
        return if (isOnline()) {
            try {
                val querySnapshot = usersCollection
                    .whereEqualTo("username", username.lowercase().trim())
                    .limit(1)
                    .get()
                    .await()
                
                if (!querySnapshot.isEmpty) {
                    val doc = querySnapshot.documents.first()
                    val user = mapToUser(doc.id, doc.data!!)
                    // Сохраняем локально с правильным ID (хеш Firestore ID)
                    userDao.insertUser(user)
                    user
                } else {
                    userDao.getUserByUsername(username)
                }
            } catch (e: Exception) {
                userDao.getUserByUsername(username)
            }
        } else {
            userDao.getUserByUsername(username)
        }
    }
    
    fun getAllUsers(): Flow<List<User>> = flow {
        if (isOnline()) {
            try {
                val snapshot = usersCollection.get().await()
                val users = snapshot.documents.map { doc ->
                    mapToUser(doc.id, doc.data!!)
                }
                // Сохраняем все пользователи локально
                users.forEach { userDao.insertUser(it) }
                emit(users)
            } catch (e: Exception) {
                // При ошибке используем локальную базу
                userDao.getAllUsers().collect { emit(it) }
            }
        } else {
            userDao.getAllUsers().collect { emit(it) }
        }
    }
    
    suspend fun getUserById(id: Long): User? {
        // Сначала проверяем локально
        val localUser = userDao.getUserById(id)
        if (localUser != null) {
            return localUser
        }
        
        // Если не найден локально, пытаемся найти в Firestore
        // Но для этого нужно знать email или username, что мы не знаем по ID
        // Поэтому просто возвращаем null, если не найден локально
        // Проблема в том, что ID из Firestore не совпадает с локальным ID
        // Нужно сохранять соответствие между Firestore ID и локальным ID
        
        // Попробуем найти по всем пользователям и сравнить ID
        if (isOnline()) {
            try {
                val snapshot = usersCollection.get().await()
                for (doc in snapshot.documents) {
                    val firestoreId = doc.id.hashCode().toLong()
                    if (firestoreId == id || doc.id == id.toString()) {
                        val user = mapToUser(doc.id, doc.data!!)
                        // Сохраняем с правильным ID
                        val savedId = userDao.insertUser(user.copy(id = id))
                        return user.copy(id = id)
                    }
                }
            } catch (e: Exception) {
                // Игнорируем ошибку
            }
        }
        
        return null
    }
    
    suspend fun updateProfilePhoto(userId: Long, photoId: Int) {
        // Обновляем локально
        userDao.updateProfilePhoto(userId, photoId)
        
        if (isOnline()) {
            try {
                // Находим пользователя локально, чтобы получить email
                val user = userDao.getUserById(userId)
                if (user != null) {
                    // Ищем документ в Firestore по email
                    val querySnapshot = usersCollection
                        .whereEqualTo("email", user.email.lowercase().trim())
                        .limit(1)
                        .get()
                        .await()
                    
                    if (!querySnapshot.isEmpty) {
                        val doc = querySnapshot.documents.first()
                        doc.reference.update("profilePhotoId", photoId).await()
                    }
                }
            } catch (e: Exception) {
                // Игнорируем ошибку, локальное обновление уже выполнено
            }
        }
    }
    
    suspend fun updateProfilePhotoUrl(userId: Long, photoUrl: String?) {
        // Обновляем локально
        userDao.updateProfilePhotoUrl(userId, photoUrl)
        
        if (isOnline()) {
            try {
                // Находим пользователя локально, чтобы получить email
                val user = userDao.getUserById(userId)
                if (user != null) {
                    // Ищем документ в Firestore по email
                    val querySnapshot = usersCollection
                        .whereEqualTo("email", user.email.lowercase().trim())
                        .limit(1)
                        .get()
                        .await()
                    
                    if (!querySnapshot.isEmpty) {
                        val doc = querySnapshot.documents.first()
                        doc.reference.update("profilePhotoUrl", photoUrl ?: "").await()
                    }
                }
            } catch (e: Exception) {
                // Игнорируем ошибку, локальное обновление уже выполнено
            }
        }
    }
    
    suspend fun searchUsersByUsername(query: String, limit: Int = 20): List<User> {
        return if (isOnline()) {
            try {
                val searchQuery = query.removePrefix("%").removeSuffix("%").lowercase()
                val allUsers = mutableSetOf<User>()
                
                // Поиск по username
                try {
                    val usernameQuery = usersCollection
                        .whereGreaterThanOrEqualTo("username", searchQuery)
                        .whereLessThanOrEqualTo("username", searchQuery + "\uf8ff")
                        .limit(limit.toLong())
                        .get()
                        .await()
                    usernameQuery.documents.forEach { doc ->
                        val firestoreId = doc.id.hashCode().toLong()
                        val user = mapToUser(doc.id, doc.data!!)
                        userDao.insertUser(user.copy(id = firestoreId))
                        allUsers.add(user.copy(id = firestoreId))
                    }
                } catch (e: Exception) {
                    // Игнорируем ошибку, продолжаем поиск
                }
                
                // Поиск по firstName
                try {
                    val firstNameQuery = usersCollection
                        .whereGreaterThanOrEqualTo("firstName", searchQuery)
                        .whereLessThanOrEqualTo("firstName", searchQuery + "\uf8ff")
                        .limit(limit.toLong())
                        .get()
                        .await()
                    firstNameQuery.documents.forEach { doc ->
                        val firestoreId = doc.id.hashCode().toLong()
                        val user = mapToUser(doc.id, doc.data!!)
                        userDao.insertUser(user.copy(id = firestoreId))
                        allUsers.add(user.copy(id = firestoreId))
                    }
                } catch (e: Exception) {
                    // Игнорируем ошибку, продолжаем поиск
                }
                
                // Поиск по lastName
                try {
                    val lastNameQuery = usersCollection
                        .whereGreaterThanOrEqualTo("lastName", searchQuery)
                        .whereLessThanOrEqualTo("lastName", searchQuery + "\uf8ff")
                        .limit(limit.toLong())
                        .get()
                        .await()
                    lastNameQuery.documents.forEach { doc ->
                        val firestoreId = doc.id.hashCode().toLong()
                        val user = mapToUser(doc.id, doc.data!!)
                        userDao.insertUser(user.copy(id = firestoreId))
                        allUsers.add(user.copy(id = firestoreId))
                    }
                } catch (e: Exception) {
                    // Игнорируем ошибку, продолжаем поиск
                }
                
                // Возвращаем уникальных пользователей, ограниченных лимитом
                allUsers.take(limit).toList()
            } catch (e: Exception) {
                // При ошибке используем локальную базу
                userDao.searchUsersByUsername(query, limit)
            }
        } else {
            // Используем только локальную базу
            userDao.searchUsersByUsername(query, limit)
        }
    }
    
    private fun userToMap(user: User): Map<String, Any> {
        return mapOf(
            "firstName" to user.firstName,
            "lastName" to user.lastName,
            "middleName" to (user.middleName ?: ""),
            "email" to user.email.lowercase().trim(),
            "phone" to user.phone,
            "username" to user.username.lowercase().trim(),
            "password" to user.password,
            "profilePhotoId" to user.profilePhotoId,
            "profilePhotoUrl" to (user.profilePhotoUrl ?: "")
        )
    }
    
    private fun mapToUser(id: String, data: Map<String, Any>): User {
        // Используем хеш строки ID как Long для совместимости с Room
        val userId = id.hashCode().toLong()
        return User(
            id = userId,
            firstName = data["firstName"] as? String ?: "",
            lastName = data["lastName"] as? String ?: "",
            middleName = data["middleName"] as? String,
            email = data["email"] as? String ?: "",
            phone = data["phone"] as? String ?: "",
            username = data["username"] as? String ?: "",
            password = data["password"] as? String ?: "",
            profilePhotoId = (data["profilePhotoId"] as? Number)?.toInt() ?: 0,
            profilePhotoUrl = data["profilePhotoUrl"] as? String
        )
    }
}


