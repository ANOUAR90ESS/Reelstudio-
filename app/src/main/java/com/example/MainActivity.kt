package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Drama
import com.example.ui.components.AdminAccessDialog
import com.example.ui.components.AuthDialog
import com.example.ui.components.CommentsBottomSheet
import com.example.ui.components.EpisodePickerBottomSheet
import com.example.ui.components.EpisodeUnlockDialog
import com.example.ui.components.LoadingOverlay
import com.example.ui.components.ReelShortBottomNav
import com.example.ui.components.ReelShortTopBar
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.DramaDetailScreen
import com.example.ui.screens.ForYouScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.RewardsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ReelBackgroundDark
import com.example.ui.theme.ReelRedPrimary
import com.example.data.admin.AdminStats
import com.example.data.ai.StoryGenerator
import com.example.data.firebase.MediaUploader
import com.example.data.model.DramaGenre
import com.example.ui.screens.admin.AiStoryGeneratorSheet
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.admin.AdminDramaEditorScreen
import com.example.ui.screens.admin.AdminEpisodeEditorScreen
import com.example.ui.viewmodel.AdminRoute
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.ReelShortViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ReelShortViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ReelShortApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ReelShortApp(viewModel: ReelShortViewModel) {
    val context = LocalContext.current

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val dramas by viewModel.dramas.collectAsStateWithLifecycle()
    val userAccount by viewModel.userAccount.collectAsStateWithLifecycle()
    val selectedDrama by viewModel.selectedDrama.collectAsStateWithLifecycle()
    val currentEpisodeIndex by viewModel.currentEpisodeIndex.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val playbackProgress by viewModel.playbackProgress.collectAsStateWithLifecycle()
    val playbackSpeed by viewModel.playbackSpeed.collectAsStateWithLifecycle()

    val watchHistory by viewModel.watchHistory.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val unlockedList by viewModel.unlockedEpisodes.collectAsStateWithLifecycle()
    val dailyTasks by viewModel.dailyTasks.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedGenre by viewModel.selectedGenre.collectAsStateWithLifecycle()

    val showUnlockDialog by viewModel.showUnlockDialog.collectAsStateWithLifecycle()
    val unlockTarget by viewModel.unlockTarget.collectAsStateWithLifecycle()
    val showCommentsSheet by viewModel.showCommentsSheet.collectAsStateWithLifecycle()
    val showEpisodePickerSheet by viewModel.showEpisodePickerSheet.collectAsStateWithLifecycle()

    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val comments by viewModel.getCommentsForCurrentEpisode().collectAsStateWithLifecycle()

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val loadingMessage by viewModel.loadingMessage.collectAsStateWithLifecycle()
    val loadingSubMessage by viewModel.loadingSubMessage.collectAsStateWithLifecycle()

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val showAuthDialog by viewModel.showAuthDialog.collectAsStateWithLifecycle()
    val authErrorMessage by viewModel.authErrorMessage.collectAsStateWithLifecycle()

    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val adminRoute by viewModel.adminRoute.collectAsStateWithLifecycle()
    val adminDramas by viewModel.adminDramas.collectAsStateWithLifecycle()
    val adminStats by viewModel.adminStats.collectAsStateWithLifecycle()
    val showAdminAccessDialog by viewModel.showAdminAccessDialog.collectAsStateWithLifecycle()
    val adminAccessError by viewModel.adminAccessError.collectAsStateWithLifecycle()
    val isSavingAdminContent by viewModel.isSavingAdminContent.collectAsStateWithLifecycle()
    val showStoryGenerator by viewModel.showStoryGenerator.collectAsStateWithLifecycle()
    val isGeneratingStory by viewModel.isGeneratingStory.collectAsStateWithLifecycle()
    val generatedStory by viewModel.generatedStory.collectAsStateWithLifecycle()
    val aiErrorMessage by viewModel.aiErrorMessage.collectAsStateWithLifecycle()
    val pendingOutlines by viewModel.pendingEpisodeOutlines.collectAsStateWithLifecycle()
    val uploadStates by viewModel.uploadStates.collectAsStateWithLifecycle()

    // Nav state for Drama Detail
    var detailDrama by remember { mutableStateOf<Drama?>(null) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    BackHandler(enabled = detailDrama != null) {
        detailDrama = null
    }

    // The console owns the whole window: no viewer chrome, and Back walks the admin routes.
    if (adminRoute != AdminRoute.None) {
        AdminConsole(
            route = adminRoute,
            dramas = adminDramas,
            stats = adminStats,
            adminName = userProfile?.displayName ?: currentUser?.displayName ?: "Admin",
            adminEmail = userProfile?.email ?: currentUser?.email.orEmpty(),
            isSaving = isSavingAdminContent,
            uploadStates = uploadStates,
            isGeneratingStory = isGeneratingStory,
            pendingOutlines = pendingOutlines,
            viewModel = viewModel
        )

        // The AI writer floats above whichever console screen is open.
        AiStoryGeneratorSheet(
            isOpen = showStoryGenerator,
            genre = (adminRoute as? AdminRoute.FilmEditor)?.form?.genre ?: DramaGenre.BILLIONAIRE,
            isGenerating = isGeneratingStory,
            result = generatedStory,
            errorMessage = aiErrorMessage,
            onGenerate = { idea, genre, count -> viewModel.generateStory(idea, genre, count) },
            onApply = { viewModel.applyGeneratedStory(it) },
            onDismiss = { viewModel.closeStoryGenerator() }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ReelBackgroundDark,
        topBar = {
            if (detailDrama == null && currentTab != MainTab.FOR_YOU) {
                ReelShortTopBar(
                    coins = userAccount.coins,
                    isVip = userAccount.isVip,
                    onCoinsClick = { viewModel.selectTab(MainTab.REWARDS) },
                    onVipClick = { viewModel.selectTab(MainTab.REWARDS) },
                    onSearchClick = { viewModel.selectTab(MainTab.DISCOVER) }
                )
            }
        },
        bottomBar = {
            if (detailDrama == null) {
                ReelShortBottomNav(
                    currentTab = currentTab,
                    onTabSelected = { tab ->
                        viewModel.selectTab(tab)
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (detailDrama != null) {
                val drama = detailDrama!!
                val isBookmarked = bookmarks.any { it.dramaId == drama.id }
                DramaDetailScreen(
                    drama = drama,
                    allDramas = dramas,
                    isBookmarked = isBookmarked,
                    isVip = userAccount.isVip,
                    unlockedList = unlockedList,
                    onBack = { detailDrama = null },
                    onPlayEpisode = { epNum ->
                        viewModel.selectDrama(drama, epNum)
                        detailDrama = null
                        viewModel.selectTab(MainTab.FOR_YOU)
                    },
                    onToggleBookmark = { viewModel.toggleBookmark(drama.id) },
                    onShare = { viewModel.showToast("Shared ${drama.title} link!") },
                    onSelectRecommendedDrama = { rec -> detailDrama = rec }
                )
            } else {
                when (currentTab) {
                    MainTab.FOR_YOU -> {
                        ForYouScreen(
                            drama = selectedDrama ?: dramas.firstOrNull(),
                            currentEpisodeIndex = currentEpisodeIndex,
                            isPlaying = isPlaying,
                            progressSeconds = playbackProgress,
                            playbackSpeed = playbackSpeed,
                            isVip = userAccount.isVip,
                            coins = userAccount.coins,
                            bookmarks = bookmarks,
                            unlockedList = unlockedList,
                            onTogglePlayPause = { viewModel.togglePlayPause() },
                            onSeek = { viewModel.seekTo(it) },
                            onSetSpeed = { viewModel.setSpeed(it) },
                            onToggleBookmark = { viewModel.toggleBookmark(it) },
                            onToggleLike = { dId, epNum -> viewModel.toggleLike(dId, epNum) },
                            onOpenComments = { viewModel.setCommentsSheetVisible(true) },
                            onOpenEpisodePicker = { viewModel.setEpisodePickerVisible(true) },
                            onNextEpisode = { viewModel.playNextEpisode() },
                            onPrevEpisode = { viewModel.playPreviousEpisode() },
                            onSwipeVertical = { next -> viewModel.handleVerticalSwipe(next) },
                            onPromptUnlock = { d ->
                                val ep = d.episodes.getOrNull(currentEpisodeIndex) ?: return@ForYouScreen
                                viewModel.openUnlockDialog(d, ep)
                            },
                            onCoinsClick = { viewModel.selectTab(MainTab.REWARDS) },
                            onVipClick = { viewModel.selectTab(MainTab.REWARDS) },
                            onShare = { viewModel.showToast("Shared series link with friends!") }
                        )
                    }

                    MainTab.HOME -> {
                        HomeScreen(
                            dramas = dramas,
                            watchHistory = watchHistory,
                            bookmarks = bookmarks,
                            selectedGenre = selectedGenre,
                            onSelectGenre = { viewModel.selectGenre(it) },
                            onSelectDrama = { drama, epNum ->
                                viewModel.selectDrama(drama, epNum)
                                viewModel.selectTab(MainTab.FOR_YOU)
                            },
                            onOpenDramaDetail = { drama -> detailDrama = drama },
                            onToggleBookmark = { viewModel.toggleBookmark(it) }
                        )
                    }

                    MainTab.DISCOVER -> {
                        DiscoverScreen(
                            dramas = dramas,
                            searchQuery = searchQuery,
                            selectedGenre = selectedGenre,
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onSelectGenre = { viewModel.selectGenre(it) },
                            onSelectDrama = { drama -> detailDrama = drama }
                        )
                    }

                    MainTab.REWARDS -> {
                        RewardsScreen(
                            userAccount = userAccount,
                            dailyTasks = dailyTasks,
                            onCheckIn = { viewModel.performDailyCheckIn() },
                            onClaimTask = { viewModel.claimTaskReward(it) },
                            onPurchasePack = { viewModel.purchaseCoins(it) },
                            onSubscribeVip = { viewModel.subscribeVip() }
                        )
                    }

                    MainTab.LIBRARY -> {
                        LibraryScreen(
                            userAccount = userAccount,
                            allDramas = dramas,
                            bookmarks = bookmarks,
                            watchHistory = watchHistory,
                            unlockedList = unlockedList,
                            firebaseUser = currentUser,
                            userProfile = userProfile,
                            isAdmin = isAdmin,
                            onOpenAdminConsole = { viewModel.openAdminAccessDialog() },
                            onOpenAuth = { viewModel.openAuthDialog() },
                            onSignOut = { viewModel.signOut() },
                            onSelectDrama = { drama, epNum ->
                                viewModel.selectDrama(drama, epNum)
                                viewModel.selectTab(MainTab.FOR_YOU)
                            },
                            onOpenDramaDetail = { drama -> detailDrama = drama },
                            onClearHistory = { viewModel.clearWatchHistory() },
                            onNavigateToRewards = { viewModel.selectTab(MainTab.REWARDS) }
                        )
                    }
                }
            }
        }
    }

    // --- Modals & Overlays ---

    // 1. Episode Unlock Dialog
    if (showUnlockDialog && unlockTarget != null) {
        val (drama, episode) = unlockTarget!!
        EpisodeUnlockDialog(
            drama = drama,
            episode = episode,
            currentCoins = userAccount.coins,
            onUnlockWithCoins = { viewModel.unlockTargetEpisodeWithCoins() },
            onUnlockWithAd = { viewModel.unlockTargetEpisodeWithAd() },
            onNavigateToVip = {
                viewModel.dismissUnlockDialog()
                viewModel.selectTab(MainTab.REWARDS)
            },
            onDismiss = { viewModel.dismissUnlockDialog() }
        )
    }

    // 2. Comments Bottom Sheet
    if (showCommentsSheet) {
        CommentsBottomSheet(
            comments = comments,
            episodeNumber = currentEpisodeIndex + 1,
            onDismiss = { viewModel.setCommentsSheetVisible(false) },
            onSendComment = { viewModel.postComment(it) }
        )
    }

    // 3. Episode Picker Bottom Sheet
    if (showEpisodePickerSheet && selectedDrama != null) {
        EpisodePickerBottomSheet(
            drama = selectedDrama!!,
            currentEpisodeIndex = currentEpisodeIndex,
            unlockedList = unlockedList,
            isVip = userAccount.isVip,
            onSelectEpisode = { epNum ->
                viewModel.selectDrama(selectedDrama!!, epNum)
                viewModel.setEpisodePickerVisible(false)
            },
            onDismiss = { viewModel.setEpisodePickerVisible(false) }
        )
    }

    // 4. Authentication Dialog View (Google Sign-In, Email/Password, Demo VIP)
    AuthDialog(
        isOpen = showAuthDialog,
        onDismiss = { viewModel.closeAuthDialog() },
        onGoogleSignIn = { viewModel.signInWithGoogle(context) },
        onEmailSignIn = { email, pass -> viewModel.signInWithEmail(email, pass) },
        onEmailSignUp = { email, pass, name -> viewModel.signUpWithEmail(email, pass, name) },
        onDemoSignIn = { viewModel.signInDemoAccount() },
        errorMessage = authErrorMessage
    )

    // 5. Admin Console Access Gate
    AdminAccessDialog(
        isOpen = showAdminAccessDialog,
        isSignedInAdmin = isAdmin,
        onDismiss = { viewModel.closeAdminAccessDialog() },
        onEnterConsole = { viewModel.enterAdminConsole() },
        onSubmitPasscode = { viewModel.submitAdminPasscode(it) },
        errorMessage = adminAccessError
    )

    // 6. Global Loading Overlay
    LoadingOverlay(
        isLoading = isLoading,
        message = loadingMessage,
        subMessage = loadingSubMessage,
        onCancel = { viewModel.hideLoading() }
    )
}

/**
 * Routes the admin console. Back always walks one step towards the dashboard and finally out of the
 * console, so an admin can never get stranded on an editor with no way home.
 */
@Composable
private fun AdminConsole(
    route: AdminRoute,
    dramas: List<Drama>,
    stats: AdminStats,
    adminName: String,
    adminEmail: String,
    isSaving: Boolean,
    uploadStates: Map<MediaUploader.MediaKind, MediaUploader.UploadState>,
    isGeneratingStory: Boolean,
    pendingOutlines: List<StoryGenerator.GeneratedEpisode>,
    viewModel: ReelShortViewModel
) {
    BackHandler {
        if (route is AdminRoute.Dashboard) {
            viewModel.exitAdminConsole()
        } else {
            viewModel.openAdminDashboard()
        }
    }

    when (route) {
        AdminRoute.None -> Unit

        AdminRoute.Dashboard -> {
            AdminDashboardScreen(
                adminName = adminName,
                adminEmail = adminEmail,
                stats = stats,
                dramas = dramas,
                onCreateFilm = { viewModel.openFilmEditor(null) },
                onEditFilm = { viewModel.openFilmEditor(it) },
                onManageEpisodes = { viewModel.openEpisodeManager(it.id) },
                onTogglePublish = { viewModel.toggleFilmPublished(it.id, !it.isPublished) },
                onDeleteFilm = { viewModel.deleteFilm(it.id) },
                onExitConsole = { viewModel.exitAdminConsole() }
            )
        }

        is AdminRoute.FilmEditor -> {
            AdminDramaEditorScreen(
                initialForm = route.form,
                isSaving = isSaving,
                uploadStates = uploadStates,
                onUploadMedia = { uri, kind, name, onUploaded ->
                    viewModel.uploadMedia(uri, route.form.id, kind, name, onUploaded)
                },
                onOpenAiWriter = { viewModel.openStoryGenerator() },
                onSave = { viewModel.saveFilm(it) },
                onCancel = { viewModel.openAdminDashboard() }
            )
        }

        is AdminRoute.EpisodeManager -> {
            // Read the film out of the live list so a save is reflected without re-navigating.
            val drama = dramas.find { it.id == route.dramaId }
            if (drama == null) {
                // Reached right after creating a film, before the database flow has emitted it.
                // Waiting is correct here — bouncing to the dashboard would throw the admin out of
                // the editor they just asked for.
                LoadingOverlay(
                    isLoading = true,
                    message = "Opening episodes...",
                    onCancel = { viewModel.openAdminDashboard() }
                )
            } else {
                AdminEpisodeEditorScreen(
                    drama = drama,
                    isSaving = isSaving,
                    uploadStates = uploadStates,
                    onUploadMedia = { uri, kind, name, onUploaded ->
                        viewModel.uploadMedia(uri, drama.id, kind, name, onUploaded)
                    },
                    isGeneratingScript = isGeneratingStory,
                    onGenerateScript = { form, onReady ->
                        viewModel.generateEpisodeScript(
                            form = form,
                            seriesTitle = drama.title,
                            synopsis = drama.description,
                            cast = drama.cast,
                            onScriptReady = onReady
                        )
                    },
                    pendingOutlines = pendingOutlines,
                    onCreateFromOutlines = { viewModel.createEpisodesFromOutlines(drama.id) },
                    onDismissOutlines = { viewModel.clearEpisodeOutlines() },
                    onSaveEpisode = { viewModel.saveEpisode(it) },
                    onDeleteEpisode = { viewModel.deleteEpisode(drama.id, it.id) },
                    onBack = { viewModel.openAdminDashboard() }
                )
            }
        }
    }
}
