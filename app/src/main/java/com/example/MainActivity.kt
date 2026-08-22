@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ads.AdManager
import com.example.ui.components.RewardedAdDialog
import com.example.ui.components.TopBarHeader
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.BluetoothLobbyScreen
import com.example.ui.screens.DailyChallengeScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.OnlineHubScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.QuizPlayScreen
import com.example.ui.screens.QuizResultScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CleanMinOutlineVariant
import com.example.ui.theme.CleanMinPrimary
import com.example.ui.theme.CleanMinSecondaryContainer
import com.example.ui.theme.CleanMinSurfaceVariant
import com.example.ui.theme.FullQuizzTheme
import com.example.ui.viewmodel.CurrentScreen
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        com.example.core.audio.AudioPlayerManager.getInstance().init(this)
        
        // Initialize AdMob
        AdManager.initialize(this) {
            if (!AdManager.isRunningInEmulator()) {
                AdManager.loadRewardedAd(this)
                AdManager.loadInterstitialAd(this)
            }
        }

        setContent {
            FullQuizzTheme(darkTheme = false) {
                FullQuizzApp(viewModel = viewModel)
            }
        }
    }
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun FullQuizzApp(viewModel: MainViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity
    val currentScreen by viewModel.currentScreen.collectAsState()
    val playerProfile by viewModel.playerProfile.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val showRewardedAdDialog by viewModel.showRewardedAdDialog.collectAsState()

    // Quiz Session States
    val activeQuestions by viewModel.activeQuestions.collectAsState()
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
    val currentScore by viewModel.currentScore.collectAsState()
    val currentCombo by viewModel.currentCombo.collectAsState()
    val selectedOption by viewModel.selectedOption.collectAsState()
    val isAnswerConfirmed by viewModel.isAnswerConfirmed.collectAsState()
    val eliminatedOptionIndices by viewModel.eliminatedOptionIndices.collectAsState()
    val timeRemainingSeconds by viewModel.timeRemainingSeconds.collectAsState()
    val activeCategoryTitle by viewModel.activeCategoryTitle.collectAsState()
    val quizSubmissionResult by viewModel.quizSubmissionResult.collectAsState()
    val hasWatchedDoubleAd by viewModel.hasWatchedDoubleAd.collectAsState()

    val isMusicEnabled by viewModel.isMusicEnabled.collectAsState()
    val isMenuMusicEnabled by viewModel.isMenuMusicEnabled.collectAsState()
    val isSfxEnabled by viewModel.isSfxEnabled.collectAsState()
    val bgmVolume by viewModel.bgmVolume.collectAsState()
    val sfxVolume by viewModel.sfxVolume.collectAsState()

    // Global Rewarded Ad Dialog
    RewardedAdDialog(
        isOpen = showRewardedAdDialog,
        onDismiss = { viewModel.closeRewardedAdDialog() },
        onWatchAdForLife = {
            activity?.let { act ->
                AdManager.showRewardedAd(
                    activity = act,
                    rewardType = com.example.core.ads.AdRewardType.EXTRA_LIFE,
                    defaultAmount = 1,
                    onRewardEarned = { viewModel.watchAdForLife() }
                )
            } ?: viewModel.watchAdForLife()
        },
        onBuyLifeWithCoins = { viewModel.buyLifeWithCoins() },
        onWatchAdForCoins = {
            activity?.let { act ->
                AdManager.showRewardedAd(
                    activity = act,
                    rewardType = com.example.core.ads.AdRewardType.COINS,
                    defaultAmount = 50,
                    onRewardEarned = { viewModel.watchAdForCoins() }
                )
            } ?: viewModel.watchAdForCoins()
        },
        currentCoins = playerProfile?.coins ?: 0,
        currentLives = playerProfile?.lives ?: 5
    )

    val isPlayingOrResult = currentScreen is CurrentScreen.QuizPlay || currentScreen is CurrentScreen.QuizResult
    val availableCloudPacks = viewModel.availableCloudPacks
    val activeOnlineRoom by viewModel.activeOnlineRoom.collectAsState()
    val isSyncingCloud by viewModel.isSyncingCloud.collectAsState()
    val cloudSyncMessage by viewModel.cloudSyncMessage.collectAsState()
    val firebaseUser by viewModel.currentFirebaseUser.collectAsState()
    val isAuthenticatingFirebase by viewModel.isAuthenticatingFirebase.collectAsState()

    // Bluetooth States
    val bluetoothConnectionState by viewModel.bluetoothConnectionState.collectAsState()
    val bluetoothDiscoveredDevices by viewModel.bluetoothDiscoveredDevices.collectAsState()
    val bluetoothPairedDevices by viewModel.bluetoothPairedDevices.collectAsState()
    val isBluetoothEnabled by viewModel.isBluetoothEnabled.collectAsState()
    val lastReceivedBtMessage by viewModel.lastReceivedBtMessage.collectAsState()

    // 1v1 Duel States
    val isDuelMode by viewModel.isDuelMode.collectAsState()
    val opponentName by viewModel.opponentName.collectAsState()
    val opponentScore by viewModel.opponentScore.collectAsState()
    val opponentQuestionIndex by viewModel.opponentQuestionIndex.collectAsState()
    val opponentLastAnswerCorrect by viewModel.opponentLastAnswerCorrect.collectAsState()
    val opponentFinished by viewModel.opponentFinished.collectAsState()
    val duelWinnerMessage by viewModel.duelWinnerMessage.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            if (!isPlayingOrResult && currentScreen !is CurrentScreen.Settings && currentScreen !is CurrentScreen.DailyChallenge && currentScreen !is CurrentScreen.OnlineHub && currentScreen !is CurrentScreen.BluetoothLobby) {
                TopBarHeader(
                    player = playerProfile,
                    onAvatarClick = { viewModel.navigateTo(CurrentScreen.Profile) },
                    onLivesClick = { viewModel.openRewardedAdDialog() },
                    onCoinsClick = { viewModel.openRewardedAdDialog() }
                )
            }
        },
        bottomBar = {
            if (!isPlayingOrResult && currentScreen !is CurrentScreen.Settings && currentScreen !is CurrentScreen.DailyChallenge && currentScreen !is CurrentScreen.OnlineHub && currentScreen !is CurrentScreen.BluetoothLobby) {
                CleanMinBottomNavigation(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                is CurrentScreen.Home -> {
                    HomeScreen(
                        player = playerProfile,
                        categories = categories,
                        onStartQuiz = { catId, mode -> viewModel.startQuiz(catId, mode) },
                        onOpenDailyChallenge = { viewModel.navigateTo(CurrentScreen.DailyChallenge) },
                        onOpenLeaderboard = { viewModel.navigateTo(CurrentScreen.Leaderboard) },
                        onOpenOnlineHub = { viewModel.navigateTo(CurrentScreen.OnlineHub) },
                        onOpenBluetoothDuel = { viewModel.navigateTo(CurrentScreen.BluetoothLobby) }
                    )
                }

                is CurrentScreen.BluetoothLobby -> {
                    BluetoothLobbyScreen(
                        connectionState = bluetoothConnectionState,
                        discoveredDevices = bluetoothDiscoveredDevices,
                        pairedDevices = bluetoothPairedDevices,
                        isBluetoothEnabled = isBluetoothEnabled,
                        lastReceivedMessage = lastReceivedBtMessage,
                        onBack = { viewModel.navigateTo(CurrentScreen.Home) },
                        onStartScan = { viewModel.startBluetoothDiscovery() },
                        onStopScan = { viewModel.stopBluetoothDiscovery() },
                        onStartHosting = { viewModel.startBluetoothHosting() },
                        onConnectToDevice = { viewModel.connectToBluetoothDevice(it) },
                        onDisconnect = { viewModel.disconnectBluetooth() },
                        onSendMessage = { viewModel.sendBluetoothMessage(it) },
                        onStartGame = { viewModel.startBluetoothDuel("all") }
                    )
                }

                is CurrentScreen.OnlineHub -> {
                    OnlineHubScreen(
                        playerProfile = playerProfile,
                        categories = categories,
                        availablePacks = availableCloudPacks,
                        activeRoom = activeOnlineRoom,
                        isSyncing = isSyncingCloud,
                        syncMessage = cloudSyncMessage,
                        onBack = { viewModel.navigateTo(CurrentScreen.Home) },
                        onJoinMatchmaking = { code -> viewModel.joinOnlineMatchmaking(code) },
                        onStartOnlineDuel = { catId -> viewModel.startQuiz(catId, "STANDARD") },
                        onInstallPack = { pack -> viewModel.installCloudPack(pack) },
                        onCreateCustomQuestion = { q -> viewModel.createCustomQuestion(q) }
                    )
                }

                is CurrentScreen.QuizPlay -> {
                    QuizPlayScreen(
                        questions = activeQuestions,
                        currentIndex = currentQuestionIndex,
                        currentScore = currentScore,
                        currentCombo = currentCombo,
                        selectedOption = selectedOption,
                        isAnswerConfirmed = isAnswerConfirmed,
                        eliminatedOptionIndices = eliminatedOptionIndices,
                        timeRemainingSeconds = timeRemainingSeconds,
                        playerCoins = playerProfile?.coins ?: 0,
                        categoryTitle = activeCategoryTitle,
                        onSelectOption = { viewModel.selectOption(it) },
                        onNextQuestion = { viewModel.nextQuestion() },
                        onUse5050Hint = { viewModel.use5050Hint() },
                        onUseSkipHint = { viewModel.useSkipHint() },
                        onWatchAdForHint = {
                            activity?.let { act ->
                                viewModel.watchAdForRewardedHint(act, com.example.core.ads.AdRewardType.HINT_5050)
                            }
                        },
                        onReportQuestion = { reason, comment -> viewModel.reportQuestion(reason, comment) },
                        onQuitQuiz = { viewModel.navigateTo(CurrentScreen.Home) },
                        isMusicEnabled = isMusicEnabled,
                        onToggleMusic = { viewModel.toggleMusic() },
                        isDuelMode = isDuelMode,
                        opponentName = opponentName,
                        opponentScore = opponentScore,
                        opponentQuestionIndex = opponentQuestionIndex,
                        opponentFinished = opponentFinished,
                        opponentLastAnswerCorrect = opponentLastAnswerCorrect
                    )
                }

                is CurrentScreen.QuizResult -> {
                    quizSubmissionResult?.let { result ->
                        QuizResultScreen(
                            result = result,
                            categoryName = activeCategoryTitle,
                            onReplay = { viewModel.startQuiz("all", "STANDARD") },
                            onGoHome = {
                                activity?.let { act ->
                                    viewModel.showInterstitialOnExitQuiz(act) {
                                        viewModel.navigateTo(CurrentScreen.Home)
                                    }
                                } ?: viewModel.navigateTo(CurrentScreen.Home)
                            },
                            onWatchDoubleRewardAd = { viewModel.watchDoubleRewardAd(activity) },
                            hasWatchedDoubleAd = hasWatchedDoubleAd,
                            isDuelMode = isDuelMode,
                            opponentName = opponentName,
                            opponentScore = opponentScore,
                            duelWinnerMessage = duelWinnerMessage,
                            onDuelRematch = { viewModel.requestDuelRematch() }
                        )
                    }
                }

                is CurrentScreen.Leaderboard -> {
                    LeaderboardScreen(
                        player = playerProfile
                    )
                }

                is CurrentScreen.Achievements -> {
                    AchievementsScreen(
                        achievements = achievements,
                        onClaimReward = { viewModel.claimAchievement(it) }
                    )
                }

                is CurrentScreen.Profile -> {
                    ProfileScreen(
                        player = playerProfile,
                        pendingSyncCount = pendingSyncCount,
                        syncStatus = syncStatus,
                        firebaseUser = firebaseUser,
                        isAuthenticatingFirebase = isAuthenticatingFirebase,
                        onUpdateProfile = { nick, avatar -> viewModel.updateProfile(nick, avatar) },
                        onForceSync = { viewModel.forceSync() },
                        onOpenSettings = { viewModel.navigateTo(CurrentScreen.Settings) },
                        onSignInGoogle = { viewModel.signInWithGoogle(context) },
                        onSignOutGoogle = { viewModel.signOutFromFirebase(context) },
                        onSyncFirebase = { viewModel.syncWithFirebaseCloud() }
                    )
                }

                is CurrentScreen.Settings -> {
                    SettingsScreen(
                        onBack = { viewModel.navigateTo(CurrentScreen.Profile) },
                        onResetProgress = { viewModel.resetAllProgress() },
                        isMusicEnabled = isMusicEnabled,
                        isMenuMusicEnabled = isMenuMusicEnabled,
                        isSfxEnabled = isSfxEnabled,
                        bgmVolume = bgmVolume,
                        sfxVolume = sfxVolume,
                        onToggleMusic = { viewModel.setMusicEnabled(it) },
                        onToggleMenuMusic = { viewModel.setMenuMusicEnabled(it) },
                        onToggleSfx = { viewModel.setSfxEnabled(it) },
                        onBgmVolumeChange = { viewModel.setBgmVolume(it) },
                        onSfxVolumeChange = { viewModel.setSfxVolume(it) }
                    )
                }

                is CurrentScreen.DailyChallenge -> {
                    DailyChallengeScreen(
                        onBack = { viewModel.navigateTo(CurrentScreen.Home) },
                        onStartDailyChallenge = { viewModel.startQuiz("daily", "STANDARD") }
                    )
                }
            }
        }
    }
}

@Composable
fun CleanMinBottomNavigation(
    currentScreen: CurrentScreen,
    onNavigate: (CurrentScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CleanMinSurfaceVariant,
        tonalElevation = 3.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("bottom_nav_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CleanMinNavItem(
                icon = Icons.Default.Home,
                label = "Accueil",
                isSelected = currentScreen is CurrentScreen.Home,
                onClick = { onNavigate(CurrentScreen.Home) },
                testTag = "nav_home"
            )

            CleanMinNavItem(
                icon = Icons.Default.MilitaryTech,
                label = "Succès",
                isSelected = currentScreen is CurrentScreen.Achievements,
                onClick = { onNavigate(CurrentScreen.Achievements) },
                testTag = "nav_achievements"
            )

            CleanMinNavItem(
                icon = Icons.Default.Leaderboard,
                label = "Classement",
                isSelected = currentScreen is CurrentScreen.Leaderboard,
                onClick = { onNavigate(CurrentScreen.Leaderboard) },
                testTag = "nav_leaderboard"
            )

            CleanMinNavItem(
                icon = Icons.Default.Person,
                label = "Profil",
                isSelected = currentScreen is CurrentScreen.Profile,
                onClick = { onNavigate(CurrentScreen.Profile) },
                testTag = "nav_profile"
            )
        }
    }
}

@Composable
fun CleanMinNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) CleanMinSecondaryContainer else Color.Transparent)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) CleanMinPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        )
    }
}
