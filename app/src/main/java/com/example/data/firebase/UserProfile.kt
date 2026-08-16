package com.example.data.firebase

/**
 * Data model for User Profile stored in Firestore (`users` collection).
 */
data class UserProfile(
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val coins: Int = 200,
    val isVip: Boolean = false,
    val vipExpiryTimestamp: Long = 0L,
    val checkinStreak: Int = 1,
    val lastCheckinDateEpochDay: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "email" to email,
            "displayName" to displayName,
            "photoUrl" to photoUrl,
            "coins" to coins,
            "isVip" to isVip,
            "vipExpiryTimestamp" to vipExpiryTimestamp,
            "checkinStreak" to checkinStreak,
            "lastCheckinDateEpochDay" to lastCheckinDateEpochDay,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): UserProfile {
            return UserProfile(
                userId = map["userId"] as? String ?: "",
                email = map["email"] as? String ?: "",
                displayName = map["displayName"] as? String ?: "ReelShort Viewer",
                photoUrl = map["photoUrl"] as? String ?: "",
                coins = (map["coins"] as? Number)?.toInt() ?: 200,
                isVip = map["isVip"] as? Boolean ?: false,
                vipExpiryTimestamp = (map["vipExpiryTimestamp"] as? Number)?.toLong() ?: 0L,
                checkinStreak = (map["checkinStreak"] as? Number)?.toInt() ?: 1,
                lastCheckinDateEpochDay = (map["lastCheckinDateEpochDay"] as? Number)?.toLong() ?: 0L,
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
