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

    // 5. Global Loading Overlay
    LoadingOverlay(
        isLoading = isLoading,
        message = loadingMessage,
        subMessage = loadingSubMessage,
        onCancel = { viewModel.hideLoading() }
    )
}
