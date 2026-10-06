package com.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatApiMessage(
    @field:Json(name = "role") val role: String,
    @field:Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class ChatApiRequest(
    @field:Json(name = "messages") val messages: List<ChatApiMessage>,
    @field:Json(name = "memories") val memories: List<String> = emptyList(),
    @field:Json(name = "isVoice") val isVoice: Boolean = false,
    @field:Json(name = "language") val language: String = "auto",
    @field:Json(name = "model") val model: String? = "gpt-4o-mini"
)

@JsonClass(generateAdapter = true)
data class ChatApiResponse(
    @field:Json(name = "reply") val reply: String,
    @field:Json(name = "assistant") val assistant: String? = "Nova",
    @field:Json(name = "model") val model: String? = null,
    @field:Json(name = "timestamp") val timestamp: Long? = null
)

@JsonClass(generateAdapter = true)
data class HealthApiResponse(
    @field:Json(name = "status") val status: String,
    @field:Json(name = "assistant") val assistant: String,
    @field:Json(name = "hasApiKey") val hasApiKey: Boolean,
    @field:Json(name = "timestamp") val timestamp: Long
)
