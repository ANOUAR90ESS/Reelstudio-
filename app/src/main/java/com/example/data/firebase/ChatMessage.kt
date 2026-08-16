package com.example.data.firebase

/**
 * Data model for Chat history and Episode Discussion messages stored in Firestore (`drama_chats` and `user_chats` collections).
 */
data class ChatMessage(
    val id: String = "",
    val dramaId: String = "",
    val episodeNumber: Int = 1,
    val senderId: String = "",
    val senderName: String = "Viewer",
    val senderAvatarColor: Long = 0xFFE50914,
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isAiResponse: Boolean = false,
    val likes: Int = 0
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "dramaId" to dramaId,
            "episodeNumber" to episodeNumber,
            "senderId" to senderId,
            "senderName" to senderName,
            "senderAvatarColor" to senderAvatarColor,
            "text" to text,
            "timestamp" to timestamp,
            "isAiResponse" to isAiResponse,
            "likes" to likes
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): ChatMessage {
            return ChatMessage(
                id = id,
                dramaId = map["dramaId"] as? String ?: "",
                episodeNumber = (map["episodeNumber"] as? Number)?.toInt() ?: 1,
                senderId = map["senderId"] as? String ?: "",
                senderName = map["senderName"] as? String ?: "Viewer",
                senderAvatarColor = (map["senderAvatarColor"] as? Number)?.toLong() ?: 0xFFE50914,
                text = map["text"] as? String ?: "",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                isAiResponse = map["isAiResponse"] as? Boolean ?: false,
                likes = (map["likes"] as? Number)?.toInt() ?: 0
            )
        }
    }
}
