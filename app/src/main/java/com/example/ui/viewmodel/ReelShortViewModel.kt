package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.BookmarkEntity
import com.example.data.local.UnlockedEpisodeEntity
import com.example.data.local.UserAccountEntity
import com.example.data.local.WatchHistoryEntity
import com.example.data.model.CoinPackage
import com.example.data.model.DailyTask
import com.example.data.model.Drama
import com.example.data.model.DramaComment
import com.example.data.model.DramaGenre
import com.example.data.model.Episode
import com.example.data.admin.AdminConfig
import com.example.data.admin.AdminStats
import com.example.data.admin.DramaFormState
import com.example.data.admin.EpisodeFormState
import com.example.data.ai.StoryGenerator
import com.example.data.firebase.MediaUploader
import com.example.data.repository.DramaRepository
import com.example.data.firebase.AdminFirestore
import com.example.data.firebase.AuthHelper
import com.example.data.firebase.ChatMessage
import com.example.data.firebase.FirebaseHelper
import com.example.data.firebase.UserProfile
import com.google.firebase.auth.FirebaseUser
import android.content.Context
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab(val title: String) {
    FOR_YOU("For You"),
    HOME("Home"),
    DISCOVER("Discover"),
    REWARDS("Rewards"),
    LIBRARY("Library")
}

data class PlayerUiState(
    val currentDrama: Drama? = null,
    val currentEpisode: Episode? = null,
    val isPlaying: Boolean = true,
    val progressSeconds: Int = 0,
    val speed: Float = 1.0f,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val isUnlocked: Boolean = true
)

class ReelShortViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DramaRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = DramaRepository(database.reelShortDao(), viewModelScope)
    }

    val dramas: StateFlow<List<Drama>> = repository.dramas
    val dailyTasks: StateFlow<List<DailyTask>> = repository.dailyTasks

    val userAccount: StateFlow<UserAccountEntity> = repository.userAccount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserAccountEntity(id = 1, coins = 200, isVip = false, checkinStreak = 1)
    )

    val watchHistory: StateFlow<List<WatchHistoryEntity>> = repository.watchHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.bookmarks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val unlockedEpisodes: StateFlow<List<UnlockedEpisodeEntity>> = repository.allUnlockedEpisodes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentTab = MutableStateFlow(MainTab.FOR_YOU)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _selectedDrama = MutableStateFlow<Drama?>(repository.dramas.value.firstOrNull())
    val selectedDrama: StateFlow<Drama?> = _selectedDrama.asStateFlow()

    private val _currentEpisodeIndex = MutableStateFlow(0)
    val currentEpisodeIndex: StateFlow<Int> = _currentEpisodeIndex.asStateFlow()

    private val _isPlaying = MutableStateFlow(true)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0)
    val playbackProgress: StateFlow<Int> = _playbackProgress.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGenre = MutableStateFlow(DramaGenre.ALL)
    val selectedGenre: StateFlow<DramaGenre> = _selectedGenre.asStateFlow()

    // Modals & BottomSheets
    private val _showUnlockDialog = MutableStateFlow(false)
    val showUnlockDialog: StateFlow<Boolean> = _showUnlockDialog.asStateFlow()

    private val _unlockTarget = MutableStateFlow<Pair<Drama, Episode>?>(null)
    val unlockTarget: StateFlow<Pair<Drama, Episode>?> = _unlockTarget.asStateFlow()

    private val _showCommentsSheet = MutableStateFlow(false)
    val showCommentsSheet: StateFlow<Boolean> = _showCommentsSheet.asStateFlow()

    private val _showEpisodePickerSheet = MutableStateFlow(false)
    val showEpisodePickerSheet: StateFlow<Boolean> = _showEpisodePickerSheet.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Global Loading Overlay State
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadingMessage = MutableStateFlow<String?>("Loading...")
    val loadingMessage: StateFlow<String?> = _loadingMessage.asStateFlow()

    private val _loadingSubMessage = MutableStateFlow<String?>(null)
    val loadingSubMessage: StateFlow<String?> = _loadingSubMessage.asStateFlow()

    // Firebase Auth & Firestore Profile State
    private val _currentUser = MutableStateFlow<FirebaseUser?>(AuthHelper.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _showAuthDialog = MutableStateFlow(false)
    val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    private var playbackTickerJob: Job? = null

    init {
        startPlaybackTicker()
        observeAuthState()
    }

    private fun observeAuthState() {
        AuthHelper.auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _currentUser.value = user
            if (user != null) {
                viewModelScope.launch {
                    // A bootstrap owner may predate the role field; upgrade them once so the
                    // Firestore rules recognise their writes too.
                    if (AdminConfig.isBootstrapAdmin(user.email)) {
                        AdminFirestore.ensureAdminRole(user.uid, user.email.orEmpty())
                    }
                    val profile = FirebaseHelper.fetchUserProfile(user.uid)
                    if (profile != null) {
                        _userProfile.value = profile
                        // Sync with local account
                        if (profile.isVip) {
                            repository.activateVip(30)
                        }
                    }
                }
            } else {
                _userProfile.value = null
            }
        }
    }

    fun showLoading(message: String = "Loading...", subMessage: String? = null) {
        _loadingMessage.value = message
        _loadingSubMessage.value = subMessage
        _isLoading.value = true
    }

    fun hideLoading() {
        _isLoading.value = false
        _loadingMessage.value = null
        _loadingSubMessage.value = null
    }

    fun openAuthDialog() {
        _authErrorMessage.value = null
        _showAuthDialog.value = true
    }

    fun closeAuthDialog() {
        _showAuthDialog.value = false
        _authErrorMessage.value = null
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            showLoading(message = "Connecting to Google...", subMessage = "Verifying credentials with Google Identity Services")
            _authErrorMessage.value = null
            val result = AuthHelper.signInWithGoogle(context)
            hideLoading()
            result.onSuccess { user ->
                _currentUser.value = user
                closeAuthDialog()
                showToast("Welcome, ${user.displayName ?: "Viewer"}! 🎉")
                syncAccountWithFirestore(user.uid)
            }.onFailure { error ->
                _authErrorMessage.value = error.localizedMessage ?: "Google Sign-In failed"
            }
        }
    }

    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            showLoading(message = "Signing in...", subMessage = "Authenticating with Firebase Auth")
            _authErrorMessage.value = null
            val result = AuthHelper.signInWithEmail(email, pass)
            hideLoading()
            result.onSuccess { user ->
                _currentUser.value = user
                closeAuthDialog()
                showToast("Signed in as ${user.email} ✨")
                syncAccountWithFirestore(user.uid)
            }.onFailure { error ->
                _authErrorMessage.value = error.localizedMessage ?: "Sign in failed. Check your email & password."
            }
        }
    }

    fun signUpWithEmail(email: String, pass: String, name: String) {
        viewModelScope.launch {
            showLoading(message = "Creating account...", subMessage = "Setting up your ReelShort cloud profile")
            _authErrorMessage.value = null
            val result = AuthHelper.signUpWithEmail(email, pass, name)
            hideLoading()
            result.onSuccess { user ->
                _currentUser.value = user
                closeAuthDialog()
                showToast("Account created! Bonus +250 Coins added! 🪙")
                repository.purchaseCoins(250, 0)
                syncAccountWithFirestore(user.uid)
            }.onFailure { error ->
                _authErrorMessage.value = error.localizedMessage ?: "Registration failed."
            }
        }
    }

    fun signInDemoAccount() {
        viewModelScope.launch {
            showLoading(message = "Activating Demo Account...", subMessage = "Configuring VIP profile with bonus coins")
            delay(500)
            val result = AuthHelper.signInDemoAccount("VIP Premium Member")
            hideLoading()
            result.onSuccess { profile ->
                _userProfile.value = profile
                closeAuthDialog()
                repository.activateVip(30)
                repository.purchaseCoins(300, 0)
                showToast("Demo VIP Account Active! 👑 Unlimited pass granted.")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            showLoading(message = "Signing out...")
            AuthHelper.signOut()
            _currentUser.value = null
            _userProfile.value = null
            delay(300)
            hideLoading()
            showToast("Signed out successfully")
        }
    }

    private fun syncAccountWithFirestore(userId: String) {
        viewModelScope.launch {
            val local = userAccount.value
            val profile = FirebaseHelper.fetchUserProfile(userId) ?: UserProfile(
                userId = userId,
                email = _currentUser.value?.email ?: "",
                displayName = _currentUser.value?.displayName ?: "ReelShort Member",
                coins = local.coins,
                isVip = local.isVip,
                checkinStreak = local.checkinStreak
            )
            _userProfile.value = profile
            FirebaseHelper.saveUserProfile(profile)
        }
    }

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun selectDrama(drama: Drama, startEpisodeNumber: Int = 1) {
        _selectedDrama.value = drama
        val epIdx = (startEpisodeNumber - 1).coerceIn(0, (drama.episodes.size - 1).coerceAtLeast(0))
        _currentEpisodeIndex.value = epIdx
        _playbackProgress.value = 0
        _isPlaying.value = true

        val episode = drama.episodes.getOrNull(epIdx)
        if (episode != null) {
            checkAndPromptUnlockIfNeeded(drama, episode)
        }
    }

    private fun checkAndPromptUnlockIfNeeded(drama: Drama, episode: Episode) {
        val isVip = userAccount.value.isVip
        val isUnlocked = episode.isFree || isVip || unlockedEpisodes.value.any {
            it.dramaId == drama.id && it.episodeNumber == episode.episodeNumber
        }

        if (!isUnlocked) {
            _isPlaying.value = false
            _unlockTarget.value = Pair(drama, episode)
            _showUnlockDialog.value = true
        }
    }

    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun seekTo(seconds: Int) {
        val drama = _selectedDrama.value ?: return
        val episode = drama.episodes.getOrNull(_currentEpisodeIndex.value) ?: return
        _playbackProgress.value = seconds.coerceIn(0, episode.durationSeconds)
    }

    fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed
        showToast("Speed set to ${speed}x")
    }

    fun playNextEpisode() {
        val drama = _selectedDrama.value ?: return
        if (_currentEpisodeIndex.value < drama.episodes.size - 1) {
            val nextIdx = _currentEpisodeIndex.value + 1
            _currentEpisodeIndex.value = nextIdx
            _playbackProgress.value = 0
            _isPlaying.value = true
            drama.episodes.getOrNull(nextIdx)?.let { ep ->
                checkAndPromptUnlockIfNeeded(drama, ep)
            }
        } else {
            showToast("You've reached the latest episode!")
        }
    }

    fun playPreviousEpisode() {
        val drama = _selectedDrama.value ?: return
        if (_currentEpisodeIndex.value > 0) {
            val prevIdx = _currentEpisodeIndex.value - 1
            _currentEpisodeIndex.value = prevIdx
            _playbackProgress.value = 0
            _isPlaying.value = true
        }
    }

    fun switchForYouDrama(next: Boolean) {
        val all = repository.dramas.value
        val currentIndex = all.indexOfFirst { it.id == _selectedDrama.value?.id }
        if (currentIndex != -1) {
            val newIdx = if (next) {
                (currentIndex + 1) % all.size
            } else {
                if (currentIndex - 1 < 0) all.size - 1 else currentIndex - 1
            }
            selectDrama(all[newIdx], startEpisodeNumber = 1)
        }
    }

    fun handleVerticalSwipe(next: Boolean) {
        val drama = _selectedDrama.value ?: return
        if (next) {
            if (_currentEpisodeIndex.value < drama.episodes.size - 1) {
                playNextEpisode()
            } else {
                switchForYouDrama(next = true)
                showToast("Now Playing: ${selectedDrama.value?.title}")
            }
        } else {
            if (_currentEpisodeIndex.value > 0) {
                playPreviousEpisode()
            } else {
                switchForYouDrama(next = false)
                showToast("Now Playing: ${selectedDrama.value?.title}")
            }
        }
    }

    fun toggleBookmark(dramaId: String) {
        viewModelScope.launch {
            val isCurrentBookmarked = bookmarks.value.any { it.dramaId == dramaId }
            repository.toggleBookmark(dramaId, isCurrentBookmarked)
            showToast(if (isCurrentBookmarked) "Removed from My List" else "Saved to My List")
        }
    }

    fun toggleLike(dramaId: String, episodeNumber: Int) {
        viewModelScope.launch {
            repository.toggleLike(dramaId, episodeNumber, isLiked = false)
            showToast("Liked Episode $episodeNumber ❤️")
        }
    }

    fun unlockTargetEpisodeWithCoins() {
        val target = _unlockTarget.value ?: return
        val drama = target.first
        val episode = target.second

        viewModelScope.launch {
            val success = repository.unlockEpisodeWithCoins(
                drama.id,
                episode.episodeNumber,
                episode.coinCost
            )
            if (success) {
                _showUnlockDialog.value = false
                _isPlaying.value = true
                showToast("Unlocked Episode ${episode.episodeNumber}! 🎉")
            } else {
                showToast("Not enough coins! Earn free coins in Rewards.")
            }
        }
    }

    fun unlockTargetEpisodeWithAd() {
        val target = _unlockTarget.value ?: return
        val drama = target.first
        val episode = target.second

        viewModelScope.launch {
            delay(1200) // Simulated ad reward
            repository.unlockEpisodeFreeReward(drama.id, episode.episodeNumber)
            _showUnlockDialog.value = false
            _isPlaying.value = true
            showToast("Ad watched! Episode ${episode.episodeNumber} Unlocked! 🎁")
        }
    }

    fun dismissUnlockDialog() {
        _showUnlockDialog.value = false
    }

    fun openUnlockDialog(drama: Drama, episode: Episode) {
        _unlockTarget.value = Pair(drama, episode)
        _showUnlockDialog.value = true
    }

    fun setCommentsSheetVisible(visible: Boolean) {
        _showCommentsSheet.value = visible
    }

    fun setEpisodePickerVisible(visible: Boolean) {
        _showEpisodePickerSheet.value = visible
    }

    fun postComment(content: String) {
        if (content.isBlank()) return
        val drama = _selectedDrama.value ?: return
        val episode = drama.episodes.getOrNull(_currentEpisodeIndex.value) ?: return

        viewModelScope.launch {
            val user = _currentUser.value
            val userName = _userProfile.value?.displayName ?: user?.displayName ?: user?.email?.substringBefore("@") ?: "You"
            repository.postComment(drama.id, episode.episodeNumber, content, userName = userName)

            // Save to Firestore drama chat storage
            val chatMsg = ChatMessage(
                dramaId = drama.id,
                episodeNumber = episode.episodeNumber,
                senderId = user?.uid ?: "local_user",
                senderName = userName,
                text = content,
                timestamp = System.currentTimeMillis()
            )
            FirebaseHelper.sendDramaChatMessage(drama.id, episode.episodeNumber, chatMsg)

            showToast("Comment posted!")
        }
    }

    fun getCommentsForCurrentEpisode(): StateFlow<List<DramaComment>> {
        val dramaId = _selectedDrama.value?.id ?: "drama_billionaire_husband"
        val epNum = (_currentEpisodeIndex.value + 1)
        return repository.getComments(dramaId, epNum).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun performDailyCheckIn() {
        viewModelScope.launch {
            val coins = repository.performDailyCheckIn(userAccount.value)
            if (coins > 0) {
                showToast("Claimed Day's Check-in: +$coins Coins! 🪙")
            } else {
                showToast("Already checked in today! Come back tomorrow.")
            }
        }
    }

    fun claimTaskReward(taskId: String) {
        viewModelScope.launch {
            val reward = repository.claimTask(taskId)
            if (reward > 0) {
                showToast("Task reward claimed: +$reward Coins! 🪙")
            }
        }
    }

    fun purchaseCoins(pack: CoinPackage) {
        viewModelScope.launch {
            repository.purchaseCoins(pack.coins, pack.bonusCoins)
            showToast("Purchased ${pack.coins + pack.bonusCoins} Coins! 🪙")
        }
    }

    fun subscribeVip() {
        viewModelScope.launch {
            repository.activateVip(30)
            showToast("VIP Subscription Activated! Enjoy unlimited streaming! 👑")
        }
    }

    fun clearWatchHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
            showToast("Watch history cleared")
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectGenre(genre: DramaGenre) {
        _selectedGenre.value = genre
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }


    // ==========================================
    // ADMIN CONSOLE
    // ==========================================

    /** Films authored in the console, drafts included. Only reachable while [isAdmin] is true. */
    val adminDramas: StateFlow<List<Drama>> = repository.adminDramas

    private val _adminRoute = MutableStateFlow<AdminRoute>(AdminRoute.None)
    val adminRoute: StateFlow<AdminRoute> = _adminRoute.asStateFlow()

    private val _showAdminAccessDialog = MutableStateFlow(false)
    val showAdminAccessDialog: StateFlow<Boolean> = _showAdminAccessDialog.asStateFlow()

    private val _adminAccessError = MutableStateFlow<String?>(null)
    val adminAccessError: StateFlow<String?> = _adminAccessError.asStateFlow()

    private val _isSavingAdminContent = MutableStateFlow(false)
    val isSavingAdminContent: StateFlow<Boolean> = _isSavingAdminContent.asStateFlow()

    /**
     * Set once the offline passcode has been accepted on this device. It is deliberately *not*
     * persisted: content authored this way stays local until a real admin account publishes it, and
     * a restart returns the app to viewer mode.
     */
    private val _localAdminUnlocked = MutableStateFlow(false)

    /**
     * Whether the admin entry point is offered at all: a signed-in account carrying the admin role,
     * a bootstrap owner, or a device unlocked with the console passcode.
     *
     * Started eagerly, and every guard goes through [resolveIsAdmin] against the live sources —
     * a derived flow that only recomputes while something collects it must never be what decides
     * whether a write is allowed.
     */
    val isAdmin: StateFlow<Boolean> = combine(
        _userProfile,
        _currentUser,
        _localAdminUnlocked
    ) { profile, user, localUnlocked ->
        resolveIsAdmin(profile, user?.email, localUnlocked)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = resolveIsAdmin(
            _userProfile.value,
            _currentUser.value?.email,
            _localAdminUnlocked.value
        )
    )

    private fun resolveIsAdmin(
        profile: UserProfile?,
        email: String?,
        localUnlocked: Boolean
    ): Boolean = profile?.isAdmin == true ||
            AdminConfig.isBootstrapAdmin(email) ||
            localUnlocked

    /** Reads the sources directly, so a guard can never act on a stale derived value. */
    private fun currentlyAdmin(): Boolean = resolveIsAdmin(
        _userProfile.value,
        _currentUser.value?.email,
        _localAdminUnlocked.value
    )

    val adminStats: StateFlow<AdminStats> = repository.adminDramas
        .map { AdminStats.of(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AdminStats()
        )

    fun openAdminAccessDialog() {
        _adminAccessError.value = null
        _showAdminAccessDialog.value = true
    }

    fun closeAdminAccessDialog() {
        _showAdminAccessDialog.value = false
        _adminAccessError.value = null
    }

    /** Entry point for an account that already holds the admin role. */
    fun enterAdminConsole() {
        if (!currentlyAdmin()) {
            showToast("Admin access is required to open the console")
            return
        }
        _showAdminAccessDialog.value = false
        _adminRoute.value = AdminRoute.Dashboard
    }

    /** Offline path: unlock the console on this device only. */
    fun submitAdminPasscode(passcode: String) {
        if (passcode.trim() == AdminConfig.LOCAL_ADMIN_PASSCODE) {
            _localAdminUnlocked.value = true
            _showAdminAccessDialog.value = false
            _adminAccessError.value = null
            _adminRoute.value = AdminRoute.Dashboard
            showToast("Local admin console unlocked")
        } else {
            _adminAccessError.value = "Incorrect passcode"
        }
    }

    fun exitAdminConsole() {
        _adminRoute.value = AdminRoute.None
    }

    fun openAdminDashboard() {
        _adminRoute.value = AdminRoute.Dashboard
    }

    fun openFilmEditor(drama: Drama?) {
        val uid = _currentUser.value?.uid ?: _userProfile.value?.userId.orEmpty()
        _adminRoute.value = AdminRoute.FilmEditor(
            form = if (drama == null) {
                DramaFormState(createdBy = uid)
            } else {
                DramaFormState.from(drama)
            }
        )
    }

    fun openEpisodeManager(dramaId: String) {
        _adminRoute.value = AdminRoute.EpisodeManager(dramaId)
    }

    fun saveFilm(form: DramaFormState) {
        if (!requireAdmin()) return
        val errors = form.validate()
        if (errors.isNotEmpty()) {
            showToast(errors.values.first())
            return
        }

        viewModelScope.launch {
            _isSavingAdminContent.value = true
            val result = repository.saveAdminDrama(form.toDrama())
            _isSavingAdminContent.value = false
            result.onSuccess { saved ->
                showToast(
                    if (form.editingExisting) "Saved \"${saved.title}\"" else "Created \"${saved.title}\""
                )
                // A brand new film has no episodes yet, so drop the admin straight into the
                // episode manager instead of back to a dashboard row they would have to find.
                _adminRoute.value = if (form.editingExisting) {
                    AdminRoute.Dashboard
                } else {
                    AdminRoute.EpisodeManager(saved.id)
                }
            }.onFailure { error ->
                showToast(error.localizedMessage ?: "Could not save the film")
            }
        }
    }

    fun deleteFilm(dramaId: String) {
        if (!requireAdmin()) return
        viewModelScope.launch {
            repository.deleteAdminDrama(dramaId)
                .onSuccess {
                    // The viewer may have been watching the film that just disappeared.
                    if (_selectedDrama.value?.id == dramaId) {
                        _selectedDrama.value = repository.dramas.value.firstOrNull()
                        _currentEpisodeIndex.value = 0
                        _playbackProgress.value = 0
                    }
                    showToast("Film deleted")
                }
                .onFailure { showToast(it.localizedMessage ?: "Could not delete the film") }
        }
    }

    fun toggleFilmPublished(dramaId: String, published: Boolean) {
        if (!requireAdmin()) return
        viewModelScope.launch {
            repository.setAdminDramaPublished(dramaId, published)
                .onSuccess {
                    showToast(if (published) "Film is live for viewers" else "Film moved back to drafts")
                }
                .onFailure { showToast(it.localizedMessage ?: "Could not change visibility") }
        }
    }

    fun saveEpisode(form: EpisodeFormState) {
        if (!requireAdmin()) return
        val drama = repository.getAdminDrama(form.dramaId)
        val taken = drama?.episodes.orEmpty()
            .filter { it.id != form.id }
            .map { it.episodeNumber }
            .toSet()

        val errors = form.validate(taken)
        if (errors.isNotEmpty()) {
            showToast(errors.values.first())
            return
        }

        viewModelScope.launch {
            _isSavingAdminContent.value = true
            val result = repository.saveAdminEpisode(form.toEpisode())
            _isSavingAdminContent.value = false
            result.onSuccess { episode ->
                showToast("Episode ${episode.episodeNumber} saved")
            }.onFailure { error ->
                showToast(error.localizedMessage ?: "Could not save the episode")
            }
        }
    }

    fun deleteEpisode(dramaId: String, episodeId: String) {
        if (!requireAdmin()) return
        viewModelScope.launch {
            repository.deleteAdminEpisode(dramaId, episodeId)
                .onSuccess { showToast("Episode deleted") }
                .onFailure { showToast(it.localizedMessage ?: "Could not delete the episode") }
        }
    }

    /**
     * Last line of defence inside the app process. The console is already unreachable from the UI
     * for a viewer; this stops a stale route or a race from writing content anyway.
     */
    private fun requireAdmin(): Boolean {
        if (currentlyAdmin()) return true
        _adminRoute.value = AdminRoute.None
        showToast("Admin access is required for this action")
        return false
    }


    // ==========================================
    // ADMIN CONSOLE: AI WRITING + MEDIA UPLOADS
    // ==========================================

    private val _showStoryGenerator = MutableStateFlow(false)
    val showStoryGenerator: StateFlow<Boolean> = _showStoryGenerator.asStateFlow()

    private val _isGeneratingStory = MutableStateFlow(false)
    val isGeneratingStory: StateFlow<Boolean> = _isGeneratingStory.asStateFlow()

    private val _generatedStory = MutableStateFlow<StoryGenerator.GeneratedStory?>(null)
    val generatedStory: StateFlow<StoryGenerator.GeneratedStory?> = _generatedStory.asStateFlow()

    private val _aiErrorMessage = MutableStateFlow<String?>(null)
    val aiErrorMessage: StateFlow<String?> = _aiErrorMessage.asStateFlow()

    /**
     * Episode outlines from the last accepted generation, kept so the episode manager can offer to
     * create them in one go after the film itself has been saved.
     */
    private val _pendingEpisodeOutlines = MutableStateFlow<List<StoryGenerator.GeneratedEpisode>>(emptyList())
    val pendingEpisodeOutlines: StateFlow<List<StoryGenerator.GeneratedEpisode>> =
        _pendingEpisodeOutlines.asStateFlow()

    /** Upload progress per media slot, keyed by [MediaUploader.MediaKind]. */
    private val _uploadStates = MutableStateFlow<Map<MediaUploader.MediaKind, MediaUploader.UploadState>>(emptyMap())
    val uploadStates: StateFlow<Map<MediaUploader.MediaKind, MediaUploader.UploadState>> =
        _uploadStates.asStateFlow()

    private var uploadJobs = mutableMapOf<MediaUploader.MediaKind, Job>()

    fun openStoryGenerator() {
        _aiErrorMessage.value = null
        _generatedStory.value = null
        _showStoryGenerator.value = true
    }

    fun closeStoryGenerator() {
        _showStoryGenerator.value = false
        _aiErrorMessage.value = null
        _generatedStory.value = null
    }

    fun generateStory(idea: String, genre: DramaGenre, episodeCount: Int) {
        if (!requireAdmin()) return
        if (idea.isBlank()) {
            _aiErrorMessage.value = "Describe your idea first"
            return
        }

        viewModelScope.launch {
            _isGeneratingStory.value = true
            _aiErrorMessage.value = null
            StoryGenerator.generateStory(idea, genre, episodeCount)
                .onSuccess { _generatedStory.value = it }
                .onFailure { error ->
                    _aiErrorMessage.value = error.localizedMessage
                        ?: "Could not reach the story writer. Check that Firebase AI Logic is enabled for this project."
                }
            _isGeneratingStory.value = false
        }
    }

    /**
     * Folds a generated concept into the open film editor. The admin still reviews and saves — the
     * generator never writes to the catalog on its own.
     */
    fun applyGeneratedStory(story: StoryGenerator.GeneratedStory) {
        val route = _adminRoute.value
        val currentForm = (route as? AdminRoute.FilmEditor)?.form ?: DramaFormState(
            createdBy = _currentUser.value?.uid.orEmpty()
        )

        _adminRoute.value = AdminRoute.FilmEditor(
            form = currentForm.copy(
                title = story.title,
                description = story.synopsis,
                tags = story.tags,
                cast = story.cast,
                director = story.director.ifBlank { currentForm.director },
                totalEpisodes = story.episodes.size.coerceAtLeast(
                    currentForm.totalEpisodes.toIntOrNull() ?: 0
                ).toString()
            )
        )
        _pendingEpisodeOutlines.value = story.episodes
        closeStoryGenerator()
        showToast("Story applied — review it, then save")
    }

    /**
     * Creates one draft episode per generated outline. Episodes that would collide with a number
     * the film already uses are skipped rather than overwriting existing work.
     */
    fun createEpisodesFromOutlines(dramaId: String) {
        if (!requireAdmin()) return
        val outlines = _pendingEpisodeOutlines.value
        if (outlines.isEmpty()) return

        viewModelScope.launch {
            _isSavingAdminContent.value = true
            val existing = repository.getAdminDrama(dramaId)?.episodes.orEmpty()
            val taken = existing.map { it.episodeNumber }.toMutableSet()
            var created = 0

            outlines.forEach { outline ->
                if (outline.episodeNumber in taken) return@forEach
                val form = EpisodeFormState(
                    dramaId = dramaId,
                    episodeNumber = outline.episodeNumber.toString(),
                    title = outline.title,
                    previewSubtitle = outline.hook,
                    isFree = outline.episodeNumber <= 3,
                    coinCost = if (outline.episodeNumber <= 3) "0" else "20"
                )
                repository.saveAdminEpisode(form.toEpisode()).onSuccess {
                    taken += outline.episodeNumber
                    created++
                }
            }

            _isSavingAdminContent.value = false
            _pendingEpisodeOutlines.value = emptyList()
            showToast(
                if (created > 0) "Created $created episode(s) from the outline"
                else "Those episode numbers already exist"
            )
        }
    }

    fun clearEpisodeOutlines() {
        _pendingEpisodeOutlines.value = emptyList()
    }

    /**
     * Writes the dialogue for an episode that is already open in the editor. Returns the generated
     * script through [onScriptReady] so the form can merge it without a round trip to the database.
     */
    fun generateEpisodeScript(
        form: EpisodeFormState,
        seriesTitle: String,
        synopsis: String,
        cast: List<String>,
        onScriptReady: (StoryGenerator.GeneratedScript) -> Unit
    ) {
        if (!requireAdmin()) return

        viewModelScope.launch {
            _isGeneratingStory.value = true
            _aiErrorMessage.value = null
            StoryGenerator.generateScript(
                seriesTitle = seriesTitle,
                synopsis = synopsis,
                episodeTitle = form.title.ifBlank { "Episode ${form.episodeNumber}" },
                episodeNumber = form.episodeNumber.toIntOrNull() ?: 1,
                durationSeconds = form.durationSeconds.toIntOrNull() ?: 85,
                cast = cast
            ).onSuccess { script ->
                onScriptReady(script)
                showToast("Script written — ${script.lines.size} lines")
            }.onFailure { error ->
                _aiErrorMessage.value = error.localizedMessage ?: "Could not write the script"
                showToast(_aiErrorMessage.value ?: "Script generation failed")
            }
            _isGeneratingStory.value = false
        }
    }

    /**
     * Uploads a picked file and hands the resulting download URL back through [onUploaded].
     *
     * Only one upload per media kind runs at a time: picking a second video replaces the first
     * rather than racing it, so the field cannot end up pointing at the losing upload.
     */
    fun uploadMedia(
        uri: android.net.Uri,
        dramaId: String,
        kind: MediaUploader.MediaKind,
        fileName: String?,
        onUploaded: (String) -> Unit
    ) {
        if (!requireAdmin()) return

        uploadJobs[kind]?.cancel()
        uploadJobs[kind] = viewModelScope.launch {
            MediaUploader.upload(uri, dramaId, kind, fileName).collect { state ->
                _uploadStates.value = _uploadStates.value + (kind to state)
                when (state) {
                    is MediaUploader.UploadState.Success -> {
                        onUploaded(state.downloadUrl)
                        showToast("Upload complete")
                        _uploadStates.value = _uploadStates.value - kind
                    }

                    is MediaUploader.UploadState.Failed -> {
                        showToast(state.error.localizedMessage ?: "Upload failed")
                    }

                    is MediaUploader.UploadState.InProgress -> Unit
                }
            }
        }
    }

    private fun startPlaybackTicker() {
        playbackTickerJob?.cancel()
        playbackTickerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_isPlaying.value && !_showUnlockDialog.value) {
                    val drama = _selectedDrama.value
                    if (drama != null) {
                        val episode = drama.episodes.getOrNull(_currentEpisodeIndex.value)
                        if (episode != null) {
                            val newProgress = _playbackProgress.value + (1 * _playbackSpeed.value).toInt()
                            if (newProgress >= episode.durationSeconds) {
                                // Auto next
                                repository.saveProgress(drama.id, episode.episodeNumber, episode.durationSeconds)
                                playNextEpisode()
                            } else {
                                _playbackProgress.value = newProgress
                                if (newProgress % 10 == 0) {
                                    repository.saveProgress(drama.id, episode.episodeNumber, newProgress)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playbackTickerJob?.cancel()
    }
}
