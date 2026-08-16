package com.example.data.admin

/**
 * Central configuration for the administration console.
 *
 * IMPORTANT — this file only drives what the *client* is willing to show. It is a UX gate, not a
 * security boundary: anybody holding the APK can flip a boolean in a patched build. The real
 * enforcement lives in the Firestore Security Rules shipped in `firestore.rules`, which only allow
 * writes to the `dramas` / `episodes` collections when the caller's `users/{uid}.role` document
 * field equals "admin". Keep both sides in sync when you change the role model.
 */
object AdminConfig {

    const val ROLE_ADMIN = "admin"
    const val ROLE_USER = "user"

    /**
     * Bootstrap owners. The very first admin cannot be promoted by another admin (nobody exists
     * yet), so these addresses are recognised as admins as soon as they sign in, and their
     * Firestore profile is upgraded to `role = "admin"` on first launch.
     *
     * Add or remove the project owners here — comparison is case-insensitive.
     */
    val bootstrapAdminEmails: Set<String> = setOf(
        "anwarasbas2018@gmail.com"
    )

    /**
     * Offline / demo escape hatch. Firebase is optional in this project (there is no
     * google-services.json checked in), so without this the console would be unreachable on a
     * device that has never signed in. The passcode is deliberately *not* a secret — it only
     * unlocks the local editor, and anything it creates stays on-device until a real admin
     * account publishes it to Firestore.
     */
    const val LOCAL_ADMIN_PASSCODE = "reel-admin"

    fun isBootstrapAdmin(email: String?): Boolean {
        val normalized = email?.trim()?.lowercase() ?: return false
        return normalized.isNotEmpty() && bootstrapAdminEmails.any { it.lowercase() == normalized }
    }
}
