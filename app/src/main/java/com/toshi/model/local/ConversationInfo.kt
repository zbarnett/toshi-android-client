package com.toshi.model.local

data class ConversationInfo(
        val conversation: Conversation,
        val isMuted: Boolean,
        val isBlocked: Boolean
)