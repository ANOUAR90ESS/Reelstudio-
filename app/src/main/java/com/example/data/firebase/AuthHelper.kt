package com.example.data.firebase

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

object AuthHelper {
    private const val TAG = "AuthHelper"

    val auth: FirebaseAuth
        get() = FirebaseHelper.auth

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * Sign in with Google using Jetpack Credential Manager
     */
    suspend fun signInWithGoogle(
        context: Context,
        serverClientId: String = ""
    ): Result<FirebaseUser> {
        return runCatching {
            val credentialManager = CredentialManager.create(context)

            // If a server client ID is configured, use GetSignInWithGoogleOption or GetGoogleIdOption
            val targetClientId = serverClientId.ifBlank {
                "reelshort-streaming.apps.googleusercontent.com"
            }

            val rawNonce = UUID.randomUUID().toString()
            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val googleIdOption = try {
                GetSignInWithGoogleOption.Builder(targetClientId)
                    .setNonce(hashedNonce)
                    .build()
            } catch (e: Exception) {
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(targetClientId)
                    .setNonce(hashedNonce)
                    .setAutoSelectEnabled(false)
                    .build()
            }

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response: GetCredentialResponse = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = response.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                val authResult: AuthResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user ?: throw IllegalStateException("Firebase user is null after sign in")

                // Sync profile to Firestore
                val profile = UserProfile(
                    userId = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: googleIdTokenCredential.displayName ?: "ReelShort Member",
                    photoUrl = user.photoUrl?.toString() ?: googleIdTokenCredential.profilePictureUri?.toString() ?: "",
                    updatedAt = System.currentTimeMillis()
                )
                FirebaseHelper.saveUserProfile(profile)
                user
            } else {
                throw IllegalStateException("Unexpected credential type: ${credential.type}")
            }
        }.recoverCatching { error ->
            if (error is GetCredentialCancellationException) {
                throw error
            }
            Log.e(TAG, "Google Sign-In failed: ${error.message}", error)
            throw error
        }
    }

    /**
     * Sign in with Email and Password
     */
    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser> {
        return runCatching {
            val result = auth.signInWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user ?: throw IllegalStateException("User is null")

            // Fetch or create profile
            val existing = FirebaseHelper.fetchUserProfile(user.uid)
            if (existing == null) {
                val newProfile = UserProfile(
                    userId = user.uid,
                    email = user.email ?: email,
                    displayName = user.displayName ?: email.substringBefore("@"),
                    updatedAt = System.currentTimeMillis()
                )
                FirebaseHelper.saveUserProfile(newProfile)
            }
            user
        }.onFailure { e ->
            Log.e(TAG, "Email sign in failed: ${e.message}", e)
        }
    }

    /**
     * Sign up with Email and Password
     */
    suspend fun signUpWithEmail(email: String, pass: String, displayName: String): Result<FirebaseUser> {
        return runCatching {
            val result = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user ?: throw IllegalStateException("User creation failed")

            if (displayName.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .build()
                user.updateProfile(profileUpdates).await()
            }

            val profile = UserProfile(
                userId = user.uid,
                email = email.trim(),
                displayName = displayName.ifBlank { email.substringBefore("@") },
                coins = 250, // Welcome bonus
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            FirebaseHelper.saveUserProfile(profile)
            user
        }.onFailure { e ->
            Log.e(TAG, "Email sign up failed: ${e.message}", e)
        }
    }

    /**
     * Instant Demo / VIP Tester Login for testing and offline environments
     */
    suspend fun signInDemoAccount(name: String = "VIP Premium Member"): Result<UserProfile> {
        return runCatching {
            val demoUserId = "demo_user_${UUID.randomUUID().toString().take(8)}"
            val profile = UserProfile(
                userId = demoUserId,
                email = "demo.vip@reelshort.stream",
                displayName = name,
                photoUrl = "",
                coins = 500,
                isVip = true,
                vipExpiryTimestamp = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
                checkinStreak = 3,
                updatedAt = System.currentTimeMillis()
            )
            FirebaseHelper.saveUserProfile(profile)
            profile
        }
    }

    /**
     * Sign out
     */
    fun signOut() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out: ${e.message}", e)
        }
    }
}
