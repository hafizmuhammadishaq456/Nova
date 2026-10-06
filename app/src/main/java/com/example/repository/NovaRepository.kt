package com.example.repository

import com.example.database.NovaDatabase
import com.example.model.ChatApiMessage
import com.example.model.ChatApiRequest
import com.example.model.ConversationEntity
import com.example.model.MessageEntity
import com.example.model.TodoEntity
import com.example.model.UserMemoryEntity
import com.example.model.VideoProjectEntity
import com.example.network.ApiClient
import com.example.network.NetworkMonitor
import com.example.utils.LanguageDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class NovaRepository(
    private val database: NovaDatabase,
    private val networkMonitor: NetworkMonitor
) {
    private val conversationDao = database.conversationDao()
    private val messageDao = database.messageDao()
    private val userMemoryDao = database.userMemoryDao()
    private val todoDao = database.todoDao()
    private val videoProjectDao = database.videoProjectDao()

    val conversations: Flow<List<ConversationEntity>> = conversationDao.getAllConversations()
    val memories: Flow<List<UserMemoryEntity>> = userMemoryDao.getAllMemories()
    val todos: Flow<List<TodoEntity>> = todoDao.getAllTodos()
    val videoProjects: Flow<List<VideoProjectEntity>> = videoProjectDao.getAllProjects()
    val isOnline: Flow<Boolean> = networkMonitor.isOnline

    fun getMessages(conversationId: Long): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForConversation(conversationId)
    }

    suspend fun createConversation(title: String = "New Conversation"): Long {
        return conversationDao.insertConversation(ConversationEntity(title = title))
    }

    suspend fun deleteConversation(id: Long) {
        conversationDao.deleteConversationById(id)
    }

    suspend fun clearAllConversations() {
        conversationDao.clearAll()
        messageDao.clearAll()
    }

    /**
     * Send message to Nova backend with memory retrieval and offline safety check
     */
    suspend fun sendMessage(
        conversationId: Long,
        text: String,
        isVoice: Boolean = false,
        backendUrl: String? = null
    ): Result<MessageEntity> = withContext(Dispatchers.IO) {
        val detectedLang = LanguageDetector.detectLanguage(text)

        // 1. Save user message locally
        val userMsg = MessageEntity(
            conversationId = conversationId,
            role = "user",
            content = text.trim(),
            language = detectedLang,
            isVoice = isVoice
        )
        messageDao.insertMessage(userMsg)

        // Auto-update conversation title if it's the beginning
        val existingMessages = messageDao.getMessagesList(conversationId)
        if (existingMessages.size <= 2) {
            val conv = conversationDao.getConversationById(conversationId)
            if (conv != null) {
                val shortTitle = if (text.length > 28) text.take(28) + "..." else text
                conversationDao.updateConversation(
                    conv.copy(title = shortTitle, updatedAt = System.currentTimeMillis())
                )
            }
        }

        // 2. Check offline status
        if (!networkMonitor.isConnected()) {
            val offlineMsg = MessageEntity(
                conversationId = conversationId,
                role = "assistant",
                content = "Offline mode: Internet is currently unavailable. Your messages, memories, and to-do list remain accessible offline, but AI generation via OpenAI requires an active connection.",
                language = "en",
                isVoice = isVoice
            )
            messageDao.insertMessage(offlineMsg)
            return@withContext Result.failure(
                IllegalStateException("Offline mode: Cannot reach backend server without internet.")
            )
        }

        // 3. Retrieve relevant saved memories to provide context to Nova
        val allMemoriesList = userMemoryDao.getMemoriesList()
        val relevantMemories = allMemoriesList.map { "${it.key}: ${it.value}" }

        // 4. Build message history for the backend
        val apiMessages = existingMessages.takeLast(10).map {
            ChatApiMessage(role = it.role, content = it.content)
        } + ChatApiMessage(role = "user", content = text)

        val request = ChatApiRequest(
            messages = apiMessages,
            memories = relevantMemories,
            isVoice = isVoice,
            language = detectedLang
        )

        try {
            val service = ApiClient.getService(backendUrl)
            val response = service.sendChatMessage(request)

            val replyLang = LanguageDetector.detectLanguage(response.reply)
            val assistantMsg = MessageEntity(
                conversationId = conversationId,
                role = "assistant",
                content = response.reply,
                language = replyLang,
                isVoice = isVoice
            )
            val insertedId = messageDao.insertMessage(assistantMsg)
            Result.success(assistantMsg.copy(id = insertedId))
        } catch (e: Exception) {
            val errorMsg = MessageEntity(
                conversationId = conversationId,
                role = "assistant",
                content = "Unable to connect to Nova backend: ${e.localizedMessage ?: "Connection error"}. Ensure the backend server is running and accessible.",
                language = "en",
                isVoice = isVoice
            )
            messageDao.insertMessage(errorMsg)
            Result.failure(e)
        }
    }

    /**
     * Send direct prompt for Studio tools (YouTube, Code, etc.)
     */
    suspend fun executeStudioPrompt(
        prompt: String,
        backendUrl: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!networkMonitor.isConnected()) {
            return@withContext Result.failure(
                IllegalStateException("Offline mode: AI studio generation requires internet access.")
            )
        }

        val request = ChatApiRequest(
            messages = listOf(ChatApiMessage(role = "user", content = prompt)),
            isVoice = false
        )

        try {
            val service = ApiClient.getService(backendUrl)
            val response = service.sendChatMessage(request)
            Result.success(response.reply)
        } catch (e: Exception) {
            val fallback = com.example.utils.LocalGenerator.generateStudioFallback(prompt)
            Result.success(fallback)
        }
    }

    suspend fun checkBackendHealth(backendUrl: String? = null): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val service = ApiClient.getService(backendUrl)
            val health = service.checkHealth()
            Result.success(health.status == "ok")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Video Project operations
    suspend fun saveVideoProject(project: VideoProjectEntity): Long = withContext(Dispatchers.IO) {
        videoProjectDao.insertProject(project)
    }

    suspend fun deleteVideoProject(id: Long) = withContext(Dispatchers.IO) {
        videoProjectDao.deleteProjectById(id)
    }

    suspend fun duplicateVideoProject(id: Long): Long? = withContext(Dispatchers.IO) {
        val existing = videoProjectDao.getProjectById(id) ?: return@withContext null
        val duplicated = existing.copy(
            id = 0,
            title = "${existing.title} (Copy)",
            createdAt = System.currentTimeMillis()
        )
        videoProjectDao.insertProject(duplicated)
    }

    suspend fun updateVideoProject(project: VideoProjectEntity) = withContext(Dispatchers.IO) {
        videoProjectDao.updateProject(project)
    }

    // Memory operations
    suspend fun addMemory(key: String, value: String, category: String = "preference") {
        userMemoryDao.insertMemory(
            UserMemoryEntity(key = key.trim(), value = value.trim(), category = category)
        )
    }

    suspend fun deleteMemory(id: Long) {
        userMemoryDao.deleteMemoryById(id)
    }

    suspend fun clearAllMemories() {
        userMemoryDao.clearAll()
    }

    // Todo operations
    suspend fun addTodo(title: String, notes: String = "", priority: String = "normal") {
        todoDao.insertTodo(
            TodoEntity(title = title.trim(), notes = notes.trim(), priority = priority)
        )
    }

    suspend fun setTodoCompleted(id: Long, isCompleted: Boolean) {
        todoDao.setTodoCompleted(id, isCompleted)
    }

    suspend fun deleteTodo(id: Long) {
        todoDao.deleteTodoById(id)
    }

    suspend fun clearCompletedTodos() {
        todoDao.clearCompletedTodos()
    }
}
