package com.example.linkedinapp.worker

import android.content.Context
import android.content.SharedPreferences
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.linkedinapp.data.AppDatabase
import com.example.linkedinapp.data.Message
import com.example.linkedinapp.repository.MessageRepository
import com.example.linkedinapp.repository.UserRepository
import com.example.linkedinapp.util.NotificationManager
import kotlinx.coroutines.tasks.await

class MessageCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val prefs = applicationContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val currentUserId = prefs.getLong("current_user_id", -1L)
            
            if (currentUserId == -1L) {
                return Result.success() // Пользователь не авторизован
            }

            val database = AppDatabase.getDatabase(applicationContext)
            val messageRepository = MessageRepository(database.messageDao(), applicationContext)
            val userRepository = UserRepository(database.userDao(), applicationContext)
            
            // Получаем последний известный timestamp сообщений
            // При первой проверке берем время минус 1 час, чтобы не пропустить недавние сообщения
            val defaultLastCheckTime = System.currentTimeMillis() - (60 * 60 * 1000) // 1 час назад
            val lastCheckTime = prefs.getLong("last_message_check_time", defaultLastCheckTime)
            val currentTime = System.currentTimeMillis()
            
            // Получаем все существующие сообщения для текущего пользователя
            val existingMessages = messageRepository.getAllMessagesForUser(currentUserId)
            val existingMessageIds = existingMessages.map { it.id }.toSet()
            val existingMessageKeys = existingMessages.map { 
                Triple(it.text, it.senderId, it.receiverId) 
            }.toSet()
            
            // Синхронизируем сообщения из Firestore
            // Получаем все сообщения где текущий пользователь - получатель
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val messagesCollection = firestore.collection("messages")
            
            // Получаем сообщения, которые пришли после последней проверки
            val query = try {
                messagesCollection
                    .whereEqualTo("receiverId", currentUserId.toString())
                    .whereGreaterThan("timestamp", lastCheckTime)
                    .get()
                    .await()
            } catch (e: Exception) {
                // Если запрос не удался (например, нет индекса), получаем все сообщения
                messagesCollection
                    .whereEqualTo("receiverId", currentUserId.toString())
                    .get()
                    .await()
            }
            
            val firestoreMessages = query.documents.mapNotNull { doc ->
                doc.data?.let { data ->
                    val timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    // Пропускаем старые сообщения
                    if (timestamp <= lastCheckTime) return@mapNotNull null
                    
                    Message(
                        id = 0L, // Будет сгенерирован локально
                        senderId = (data["senderId"] as? String)?.toLongOrNull() ?: 0L,
                        receiverId = (data["receiverId"] as? String)?.toLongOrNull() ?: 0L,
                        text = data["text"] as? String ?: "",
                        timestamp = timestamp
                    )
                }
            }
            
            // Фильтруем новые сообщения (не дубликаты)
            val uniqueNewMessages = firestoreMessages.filter { message ->
                val key = Triple(message.text, message.senderId, message.receiverId)
                !existingMessageKeys.contains(key) && 
                message.receiverId == currentUserId && 
                message.senderId != currentUserId
            }
            
            // Сохраняем новые сообщения и показываем уведомления
            uniqueNewMessages.forEach { message ->
                // Сохраняем в локальную БД
                messageRepository.insertMessage(message)
                
                // Показываем уведомление
                val sender = userRepository.getUserById(message.senderId)
                if (sender != null) {
                    val senderName = "${sender.firstName} ${sender.lastName}"
                    NotificationManager.showMessageNotification(
                        context = applicationContext,
                        senderName = senderName,
                        messageText = message.text,
                        senderId = message.senderId
                    )
                }
            }
            
            // Обновляем время последней проверки
            prefs.edit().putLong("last_message_check_time", currentTime).apply()
            
            Result.success()
        } catch (e: Exception) {
            // При ошибке возвращаем retry, чтобы WorkManager повторил попытку
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}

