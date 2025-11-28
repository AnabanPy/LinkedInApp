package com.example.linkedinapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linkedinapp.data.Message
import com.example.linkedinapp.data.User
import com.example.linkedinapp.repository.MessageRepository
import com.example.linkedinapp.repository.UserRepository
import com.example.linkedinapp.util.NotificationManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class ConversationItem(
    val otherUser: User,
    val lastMessage: Message?,
    val unreadCount: Int = 0
)

class MessagesViewModel(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val currentUserId: Long,
    private val context: Context? = null
) : ViewModel() {
    
    private val _conversations = MutableStateFlow<List<ConversationItem>>(emptyList())
    val conversations: StateFlow<List<ConversationItem>> = _conversations.asStateFlow()
    
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    private var messagesJob: Job? = null
    private var conversationsJob: Job? = null
    
    // Отслеживаем ID пользователя, с которым открыт чат (чтобы не показывать уведомления)
    private var currentChatUserId: Long? = null
    private var lastMessageIds = mutableSetOf<Long>()
    
    init {
        loadConversations()
        startMonitoringNewMessages()
    }
    
    fun loadConversations() {
        viewModelScope.launch {
            val allMessages = messageRepository.getAllMessagesForUser(currentUserId)
            val conversationMap = mutableMapOf<Long, Message>()
            
            // Инициализируем lastMessageIds всеми существующими сообщениями
            // чтобы не показывать уведомления для старых сообщений
            lastMessageIds = allMessages.map { it.id }.toMutableSet()
            
            // Находим последнее сообщение для каждого собеседника
            allMessages.forEach { message ->
                val otherUserId = if (message.senderId == currentUserId) {
                    message.receiverId
                } else {
                    message.senderId
                }
                
                val existingLastMessage = conversationMap[otherUserId]
                if (existingLastMessage == null || message.timestamp > existingLastMessage.timestamp) {
                    conversationMap[otherUserId] = message
                }
            }
            
            // Получаем информацию о пользователях и создаем список переписок
            val conversationItems = conversationMap.map { (otherUserId, lastMessage) ->
                val otherUser = userRepository.getUserById(otherUserId)
                if (otherUser != null) {
                    ConversationItem(
                        otherUser = otherUser,
                        lastMessage = lastMessage
                    )
                } else {
                    null
                }
            }.filterNotNull()
            
            // Сортируем по времени последнего сообщения
            _conversations.value = conversationItems.sortedByDescending { it.lastMessage?.timestamp ?: 0L }
        }
    }
    
    private var currentOtherUserId: Long? = null
    
    fun loadMessages(otherUserId: Long) {
        // Если уже загружаем сообщения для этого пользователя, не делаем ничего
        if (currentOtherUserId == otherUserId && messagesJob?.isActive == true) {
            return
        }
        
        // Отменяем предыдущий job и очищаем сообщения
        messagesJob?.cancel()
        _messages.value = emptyList()
        currentOtherUserId = otherUserId
        currentChatUserId = otherUserId // Устанавливаем текущий открытый чат
        
        messagesJob = viewModelScope.launch {
            // Синхронизируем с Firestore только один раз при загрузке
            messageRepository.syncMessagesFromFirestore(currentUserId, otherUserId)
            
            // Затем собираем flow из локальной БД
            messageRepository.getMessagesBetweenUsers(currentUserId, otherUserId)
                .collect { messageList ->
                    _messages.value = messageList
                    // Обновляем последние известные ID сообщений для этого чата
                    lastMessageIds = messageList.map { it.id }.toMutableSet()
                }
        }
    }
    
    fun clearMessages() {
        messagesJob?.cancel()
        _messages.value = emptyList()
        currentOtherUserId = null
        currentChatUserId = null // Сбрасываем текущий открытый чат
    }
    
    // Мониторинг новых сообщений для показа уведомлений
    private fun startMonitoringNewMessages() {
        conversationsJob = viewModelScope.launch {
            // Периодически проверяем новые сообщения
            while (true) {
                delay(2000) // Проверяем каждые 2 секунды
                
                if (context == null) continue
                
                // Получаем все сообщения для текущего пользователя
                val allMessages = messageRepository.getAllMessagesForUser(currentUserId)
                
                // Находим новые входящие сообщения
                allMessages.forEach { message ->
                    // Проверяем, что это входящее сообщение (не от нас)
                    if (message.receiverId == currentUserId && 
                        message.senderId != currentUserId &&
                        !lastMessageIds.contains(message.id)) {
                        
                        // Не показываем уведомление, если открыт чат с этим пользователем
                        if (currentChatUserId != message.senderId) {
                            // Получаем информацию об отправителе
                            val sender = userRepository.getUserById(message.senderId)
                            if (sender != null) {
                                val senderName = "${sender.firstName} ${sender.lastName}"
                                NotificationManager.showMessageNotification(
                                    context = context,
                                    senderName = senderName,
                                    messageText = message.text,
                                    senderId = message.senderId
                                )
                            }
                        }
                        
                        // Добавляем ID в список известных сообщений
                        lastMessageIds.add(message.id)
                    }
                }
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        conversationsJob?.cancel()
        messagesJob?.cancel()
    }
    
    private val sendMutex = Mutex()
    
    fun sendMessage(receiverId: Long, text: String) {
        if (text.isBlank()) return
        
        viewModelScope.launch {
            // Используем Mutex для предотвращения одновременной отправки
            if (!sendMutex.tryLock()) {
                return@launch // Уже идет отправка
            }
            
            try {
                val message = Message(
                    senderId = currentUserId,
                    receiverId = receiverId,
                    text = text.trim()
                )
                // Вставляем сообщение - flow автоматически обновится
                messageRepository.insertMessage(message)
                
                // НЕ вызываем loadMessages здесь, так как flow уже собирает сообщения
                // и автоматически обновится при вставке нового сообщения
                
                // Сразу обновляем переписку в списке, чтобы она появилась немедленно
                updateConversationAfterMessage(receiverId, message)
                
                // НЕ вызываем loadConversations() здесь, чтобы избежать дублирования
                // Переписки обновятся автоматически через updateConversationAfterMessage
            } finally {
                sendMutex.unlock()
            }
        }
    }
    
    private fun updateConversationAfterMessage(otherUserId: Long, newMessage: Message) {
        viewModelScope.launch {
            val otherUser = userRepository.getUserById(otherUserId)
            if (otherUser != null) {
                val currentConversations = _conversations.value.toMutableList()
                
                // Ищем существующую переписку
                val existingIndex = currentConversations.indexOfFirst { 
                    it.otherUser.id == otherUserId 
                }
                
                val updatedConversation = ConversationItem(
                    otherUser = otherUser,
                    lastMessage = newMessage
                )
                
                if (existingIndex >= 0) {
                    // Обновляем существующую переписку и перемещаем в начало
                    currentConversations.removeAt(existingIndex)
                    currentConversations.add(0, updatedConversation)
                } else {
                    // Добавляем новую переписку в начало списка
                    currentConversations.add(0, updatedConversation)
                }
                
                // Обновляем список переписок
                _conversations.value = currentConversations
            }
        }
    }
}

