package com.example.ui.viewmodel

import com.example.data.admin.DramaFormState

/**
 * Where the admin console currently is.
 *
 * The console deliberately sits outside [MainTab]: it is not a tab a viewer can reach, and the
 * bottom navigation is hidden while it is open. [None] means the app is showing the normal viewer
 * experience.
 */
sealed interface AdminRoute {

    data object None : AdminRoute

    data object Dashboard : AdminRoute

    /** Create (blank form) or edit (pre-filled form) a film. */
    data class FilmEditor(val form: DramaFormState) : AdminRoute

    /**
     * Episode list + editor for one film. Only the id is carried so the screen always renders the
     * current database state rather than a snapshot taken when the route was pushed.
     */
    data class EpisodeManager(val dramaId: String) : AdminRoute
}
