package com.example.data.firebase

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Uploads admin media (episode videos, posters, voiceover tracks) to Firebase Storage.
 *
 * Progress is exposed as a [Flow] of [UploadState] rather than a suspend function returning a URL,
 * because a phone uploading a video needs a visible progress bar and a working cancel — a call that
 * simply blocks for two minutes is not something an admin can use.
 *
 * Who may write here is enforced by `storage.rules`, not by this class.
 */
object MediaUploader {

    private const val TAG = "MediaUploader"

    /** Folder layout inside the bucket: `catalog/{dramaId}/{kind}/{file}`. */
    enum class MediaKind(val folder: String) {
        VIDEO("videos"),
        POSTER("posters"),
        THUMBNAIL("thumbnails"),
        VOICEOVER("voiceovers")
    }

    sealed interface UploadState {
        /** 0f..1f, or null when the total size is unknown. */
        data class InProgress(val fraction: Float?, val bytesTransferred: Long, val totalBytes: Long) : UploadState

        data class Success(val downloadUrl: String, val storagePath: String) : UploadState

        data class Failed(val error: Throwable) : UploadState
    }

    private val storage: FirebaseStorage get() = FirebaseStorage.getInstance()

    /**
     * Uploads [uri] and emits progress until the download URL is available.
     *
     * The flow completes after a terminal state. Cancelling collection cancels the upload, so
     * navigating away from the editor does not leave a background transfer burning the admin's data.
     */
    fun upload(
        uri: Uri,
        dramaId: String,
        kind: MediaKind,
        fileName: String? = null
    ): Flow<UploadState> = callbackFlow {
        val safeName = buildFileName(fileName, kind)
        val ref: StorageReference = storage.reference
            .child("catalog")
            .child(dramaId.ifBlank { "unassigned" })
            .child(kind.folder)
            .child(safeName)

        val task = ref.putFile(uri)

        task.addOnProgressListener { snapshot ->
            val total = snapshot.totalByteCount
            trySend(
                UploadState.InProgress(
                    fraction = if (total > 0) snapshot.bytesTransferred.toFloat() / total else null,
                    bytesTransferred = snapshot.bytesTransferred,
                    totalBytes = total
                )
            )
        }

        task.addOnSuccessListener {
            // The download URL is a second round trip; without it the app has a storage path it
            // cannot hand to the player or to Coil.
            ref.downloadUrl
                .addOnSuccessListener { downloadUri ->
                    trySend(UploadState.Success(downloadUri.toString(), ref.path))
                    close()
                }
                .addOnFailureListener { error ->
                    Log.e(TAG, "Upload succeeded but the download URL failed: ${error.message}", error)
                    trySend(UploadState.Failed(error))
                    close()
                }
        }

        task.addOnFailureListener { error ->
            Log.e(TAG, "Upload failed for ${ref.path}: ${error.message}", error)
            trySend(UploadState.Failed(error))
            close()
        }

        awaitClose {
            if (!task.isComplete) {
                task.cancel()
            }
        }
    }

    /** Deletes a previously uploaded file. Best-effort: a missing object is not an error worth surfacing. */
    suspend fun delete(downloadUrl: String): Result<Unit> = runCatching {
        if (downloadUrl.isBlank()) return@runCatching Unit
        storage.getReferenceFromUrl(downloadUrl).delete().await()
        Unit
    }.onFailure { e ->
        Log.w(TAG, "Could not delete $downloadUrl: ${e.message}")
    }

    /**
     * A collision-proof, filesystem-safe name. The original name is kept as a readable prefix so an
     * admin browsing the bucket can still tell what a file is.
     */
    private fun buildFileName(original: String?, kind: MediaKind): String {
        val extension = original?.substringAfterLast('.', "")?.takeIf { it.isNotBlank() && it.length <= 5 }
            ?: kind.defaultExtension()

        val stem = original
            ?.substringBeforeLast('.')
            ?.replace(Regex("[^A-Za-z0-9._-]"), "_")
            ?.take(40)
            ?.trim('_')
            ?.takeIf { it.isNotBlank() }
            ?: kind.folder

        return "${stem}_${UUID.randomUUID().toString().take(8)}.$extension"
    }

    private fun MediaKind.defaultExtension(): String = when (this) {
        MediaKind.VIDEO -> "mp4"
        MediaKind.POSTER, MediaKind.THUMBNAIL -> "jpg"
        MediaKind.VOICEOVER -> "m4a"
    }
}
