package com.example.data.firebase

import com.example.data.admin.AdminConfig

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
    /** Either [AdminConfig.ROLE_USER] or [AdminConfig.ROLE_ADMIN]. Mirrors `users/{uid}.role`. */
    val role: String = AdminConfig.ROLE_USER,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * True when this account may open the admin console. The bootstrap allowlist covers the very
     * first owner, who has no admin around to promote them.
     */
    val isAdmin: Boolean
        get() = role.equals(AdminConfig.ROLE_ADMIN, ignoreCase = true) ||
                AdminConfig.isBootstrapAdmin(email)

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
            "role" to role,
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
                role = (map["role"] as? String)?.takeIf { it.isNotBlank() } ?: AdminConfig.ROLE_USER,
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
