package com.gemini.multiverse.domain.chat

enum class MessageSender {
    USER,
    LIA
}

enum class MessageStatus {
    SENDING,
    SENT,
    ERROR
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENT
)
