package com.brahma.jarvis.ai

enum class Sender { USER, ASSISTANT, SYSTEM }

data class ChatMessage(
    val sender: Sender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
