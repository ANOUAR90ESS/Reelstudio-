package com.example.data.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Helper object to manage Firebase Firestore operations (User Profiles and Chat History Storage)
 * and Firebase Authentication state.
 */
object FirebaseHelper {

    private const val TAG = "FirebaseHelper"
    private const val COLLECTION_USERS = "users"
    private const val COLLECTION_DRAMA_CHATS = "drama_chats"
    private const val COLLECTION_USER_CHATS = "user_chats"

    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    // ==========================================
    // USER PROFILES MANAGEMENT
    // ==========================================

    /**
     * Save or update user profile in Firestore
     */
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        return runCatching {
            val docRef = firestore.collection(COLLECTION_USERS).document(profile.userId)
            docRef.set(profile.toMap()).await()
            Log.d(TAG, "User profile saved successfully: ${profile.userId}")
            Unit
        }.onFailure { e ->
            Log.e(TAG, "Error saving user profile: ${e.message}", e)
        }
    }

    /**
     * Fetch user profile once from Firestore
     */
    suspend fun fetchUserProfile(userId: String): UserProfile? {
        if (userId.isBlank()) return null
        return try {
            val doc = firestore.collection(COLLECTION_USERS).document(userId).get().await()
            if (doc.exists() && doc.data != null) {
                UserProfile.fromMap(doc.data!!)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user profile: ${e.message}", e)
            null
        }
    }

    /**
     * Real-time Flow of User Profile from Firestore
     */
    fun getUserProfileFlow(userId: String): Flow<UserProfile?> = callbackFlow {
        if (userId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = firestore.collection(COLLECTION_USERS)
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for user profile", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists() && snapshot.data != null) {
                    trySend(UserProfile.fromMap(snapshot.data!!))
                } else {
                    trySend(null)
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Update user coin balance in Firestore
     */
    suspend fun updateUserCoins(userId: String, newCoins: Int): Result<Unit> {
        if (userId.isBlank()) return Result.failure(IllegalArgumentException("User ID is empty"))
        return runCatching {
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .update("coins", newCoins, "updatedAt", System.currentTimeMillis())
                .await()
            Unit
        }.onFailure { e ->
            Log.e(TAG, "Error updating user coins: ${e.message}", e)
        }
    }

    /**
     * Update VIP subscription status in Firestore
     */
    suspend fun updateUserVip(userId: String, isVip: Boolean, expiryTimestamp: Long): Result<Unit> {
        if (userId.isBlank()) return Result.failure(IllegalArgumentException("User ID is empty"))
        return runCatching {
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .update(
                    mapOf(
                        "isVip" to isVip,
                        "vipExpiryTimestamp" to expiryTimestamp,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            Unit
        }.onFailure { e ->
            Log.e(TAG, "Error updating VIP status: ${e.message}", e)
        }
    }

    // ==========================================
    // CHAT HISTORY & COMMENTS STORAGE
    // ==========================================

    /**
     * Save a chat/comment message for a drama episode into Firestore
     */
    suspend fun sendDramaChatMessage(
        dramaId: String,
        episodeNumber: Int,
        message: ChatMessage
    ): Result<String> {
        return runCatching {
            val collectionRef = firestore.collection(COLLECTION_DRAMA_CHATS)
            val docRef = if (message.id.isNotBlank()) {
                collectionRef.document(message.id)
            } else {
                collectionRef.document()
            }
            val finalMsg = message.copy(
                id = docRef.id,
                dramaId = dramaId,
                episodeNumber = episodeNumber,
                timestamp = if (message.timestamp > 0) message.timestamp else System.currentTimeMillis()
            )
            docRef.set(finalMsg.toMap()).await()
            docRef.id
        }.onFailure { e ->
            Log.e(TAG, "Error saving drama chat message: ${e.message}", e)
        }
    }

    /**
     * Real-time Flow of drama episode chat history / discussions
     */
    fun observeDramaChatHistory(dramaId: String, episodeNumber: Int): Flow<List<ChatMessage>> = callbackFlow {
        val query = firestore.collection(COLLECTION_DRAMA_CHATS)
            .whereEqualTo("dramaId", dramaId)
            .whereEqualTo("episodeNumber", episodeNumber)
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Listen failed for drama chat history: ${error.message}")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val messages = snapshot.documents.mapNotNull { doc ->
                    doc.data?.let { data -> ChatMessage.fromMap(doc.id, data) }
                }
                trySend(messages)
            }
        }

        awaitClose { listener.remove() }
    }

    /**
     * Save direct user chat message history (e.g. AI Drama Assistant or personalized user history)
     */
    suspend fun saveUserChatHistory(userId: String, message: ChatMessage): Result<String> {
        if (userId.isBlank()) return Result.failure(IllegalArgumentException("User ID is empty"))
        return runCatching {
            val userChatCol = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_USER_CHATS)

            val docRef = if (message.id.isNotBlank()) {
                userChatCol.document(message.id)
            } else {
                userChatCol.document()
            }

            val finalMsg = message.copy(
                id = docRef.id,
                timestamp = if (message.timestamp > 0) message.timestamp else System.currentTimeMillis()
            )
            docRef.set(finalMsg.toMap()).await()
            docRef.id
        }.onFailure { e ->
            Log.e(TAG, "Error saving user chat history: ${e.message}", e)
        }
    }

    /**
     * Real-time Flow of personalized user chat history
     */
    fun observeUserChatHistory(userId: String): Flow<List<ChatMessage>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val query = firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_USER_CHATS)
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Listen failed for user chat history: ${error.message}")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val messages = snapshot.documents.mapNotNull { doc ->
                    doc.data?.let { data -> ChatMessage.fromMap(doc.id, data) }
                }
                trySend(messages)
            }
        }

        awaitClose { listener.remove() }
    }
}
