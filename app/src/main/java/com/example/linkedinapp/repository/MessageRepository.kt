package com.example.linkedinapp.repository

import android.content.Context
import com.example.linkedinapp.data.Message
import com.example.linkedinapp.data.MessageDao
import com.example.linkedinapp.util.NetworkUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class MessageRepository(
    private val messageDao: MessageDao,
    private val context: Context
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val messagesCollection = firestore.collection("messages")
    
    private fun isOnline(): Boolean = NetworkUtils.checkInternetConnection(context)
    
    suspend fun insertMessage(message: Message): Long {
        // Проверяем, нет ли уже такого сообщения в БД
        // (проверяем по тексту, отправителю, получателю и времени в пределах 3 секунд)
        val existingMessages = if (message.id == 0L) {
            // Если ID не задан, проверяем дубликаты по другим полям
            val allMessages = messageDao.getMessagesBetweenUsersSync(
                message.senderId,
                message.receiverId
            )
            allMessages.filter { existing ->
                existing.text == message.text &&
                existing.senderId == message.senderId &&
                existing.receiverId == message.receiverId &&
                kotlin.math.abs(existing.timestamp - message.timestamp) < 3000
            }
        } else {
            emptyList()
        }
        
        // Если дубликат найден, возвращаем его ID
        if (existingMessages.isNotEmpty()) {
            return existingMessages.first().id
        }
        
        // Сохраняем локально (REPLACE стратегия предотвратит дубликаты по ID)
        val localId = messageDao.insertMessage(message)
        
        // Если онлайн, пытаемся синхронизировать с Firestore (в фоне)
        // Примечание: локальный ID и Firestore ID могут отличаться, это нормально
        // Синхронизация работает по содержимому сообщения, а не по ID
        if (isOnline()) {
            try {
                val messageMap = messageToMap(message.copy(id = localId))
                messagesCollection.add(messageMap).await()
            } catch (e: Exception) {
                // При ошибке оставляем локальное сообщение как есть
            }
        }
        
        return localId
    }
    
    suspend fun syncMessagesFromFirestore(userId: Long, otherUserId: Long) {
        if (!isOnline()) return
        
        try {
            // Получаем сообщения где текущий пользователь - отправитель
            // Убираем orderBy чтобы избежать необходимости в индексе, сортируем в коде
            val sentQuery = try {
                messagesCollection
                    .whereEqualTo("senderId", userId.toString())
                    .whereEqualTo("receiverId", otherUserId.toString())
                    .orderBy("timestamp")
                    .get()
                    .await()
            } catch (e: Exception) {
                // Если orderBy не работает (нет индекса), получаем без сортировки
                messagesCollection
                    .whereEqualTo("senderId", userId.toString())
                    .whereEqualTo("receiverId", otherUserId.toString())
                    .get()
                    .await()
            }
            
            // Получаем сообщения где текущий пользователь - получатель
            val receivedQuery = try {
                messagesCollection
                    .whereEqualTo("senderId", otherUserId.toString())
                    .whereEqualTo("receiverId", userId.toString())
                    .orderBy("timestamp")
                    .get()
                    .await()
            } catch (e: Exception) {
                // Если orderBy не работает (нет индекса), получаем без сортировки
                messagesCollection
                    .whereEqualTo("senderId", otherUserId.toString())
                    .whereEqualTo("receiverId", userId.toString())
                    .get()
                    .await()
            }
            
            val allMessages = (sentQuery.documents + receivedQuery.documents)
                .mapNotNull { doc ->
                    doc.data?.let { mapToMessage(doc.id, it) }
                }
                .sortedBy { it.timestamp }
            
            // Получаем существующие локальные сообщения для проверки дубликатов
            val localMessagesList = messageDao.getMessagesBetweenUsersSync(userId, otherUserId)
            
            // Сохраняем только новые сообщения (проверяем по содержимому, отправителю, получателю и времени)
            allMessages.forEach { firestoreMessage ->
                val isDuplicate = localMessagesList.any { local ->
                    // Проверяем дубликат по содержимому, отправителю, получателю и времени (в пределах 5 секунд)
                    local.text == firestoreMessage.text &&
                    local.senderId == firestoreMessage.senderId &&
                    local.receiverId == firestoreMessage.receiverId &&
                    kotlin.math.abs(local.timestamp - firestoreMessage.timestamp) < 5000
                }
                if (!isDuplicate) {
                    messageDao.insertMessage(firestoreMessage)
                }
            }
        } catch (e: Exception) {
            // При ошибке просто игнорируем
        }
    }
    
    fun getMessagesBetweenUsers(userId: Long, otherUserId: Long): Flow<List<Message>> {
        // Просто возвращаем flow из локальной БД
        // Синхронизация должна вызываться отдельно при необходимости
        return messageDao.getMessagesBetweenUsers(userId, otherUserId)
    }
    
    suspend fun getAllMessagesForUser(userId: Long): List<Message> {
        // Всегда используем локальную БД, чтобы избежать дублирования
        // Синхронизация должна происходить отдельно при необходимости
        return messageDao.getAllMessagesForUser(userId)
    }
    
    suspend fun getMessageById(messageId: Long): Message? {
        return if (isOnline()) {
            try {
                val doc = messagesCollection.document(messageId.toString()).get().await()
                if (doc.exists()) {
                    val message = mapToMessage(doc.id, doc.data!!)
                    messageDao.insertMessage(message)
                    message
                } else {
                    messageDao.getMessageById(messageId)
                }
            } catch (e: Exception) {
                messageDao.getMessageById(messageId)
            }
        } else {
            messageDao.getMessageById(messageId)
        }
    }
    
    private fun messageToMap(message: Message): Map<String, Any> {
        return mapOf(
            "senderId" to message.senderId.toString(),
            "receiverId" to message.receiverId.toString(),
            "text" to message.text,
            "timestamp" to message.timestamp
        )
    }
    
    private fun mapToMessage(id: String, data: Map<String, Any>): Message {
        return Message(
            id = id.toLongOrNull() ?: 0L,
            senderId = (data["senderId"] as? String)?.toLongOrNull() ?: 0L,
            receiverId = (data["receiverId"] as? String)?.toLongOrNull() ?: 0L,
            text = data["text"] as? String ?: "",
            timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}

