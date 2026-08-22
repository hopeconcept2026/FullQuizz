package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ads.AdManager
import com.example.core.ads.AdRewardResult
import com.example.core.ads.AdRewardType
import com.example.core.audio.GameShowAudioEngine
import com.example.core.constants.QuizConstants
import com.example.core.firebase.FirebaseFirestoreService
import com.example.core.firebase.FirebaseManager
import com.example.core.sync.SyncEngine
import com.example.core.sync.SyncStatus
import com.example.data.local.AppDatabase
import com.example.data.local.SeedData
import com.example.data.local.entity.AchievementEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.PlayerProfileEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.remote.CloudQuestionPack
import com.example.data.remote.OnlineLeaderboardEntry
import com.example.data.remote.OnlineMatchRoom
import com.example.data.remote.OnlinePlayer
import com.example.data.remote.QuizCloudDataSource
import com.example.data.repository.QuizRepository
import com.example.data.repository.QuizSubmissionResult
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import org.json.JSONArray
import org.json.JSONObject

sealed class CurrentScreen {
    object Home : CurrentScreen()
    object QuizPlay : CurrentScreen()
    object QuizResult : CurrentScreen()
    object Leaderboard : CurrentScreen()
    object Achievements : CurrentScreen()
    object Profile : CurrentScreen()
    object Settings : CurrentScreen()
    object DailyChallenge : CurrentScreen()
    object OnlineHub : CurrentScreen()
    object BluetoothLobby : CurrentScreen()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = QuizRepository(database)
    private val syncEngine = SyncEngine(application, database, viewModelScope)
    private val cloudDataSource = QuizCloudDataSource(database.questionDao(), database.categoryDao())
    private val firebaseService = FirebaseFirestoreService(database.questionDao(), database.categoryDao())
    private val bluetoothService = com.example.core.bluetooth.BluetoothManagerService(application)

    init {
        FirebaseManager.initialize(application)
        observeBluetoothMessages()
    }

    // Bluetooth Local Multiplayer States
    val bluetoothConnectionState = bluetoothService.connectionState
    val bluetoothDiscoveredDevices = bluetoothService.discoveredDevices
    val bluetoothPairedDevices = bluetoothService.pairedDevices
    val isBluetoothEnabled = bluetoothService.isBluetoothEnabled
    val lastReceivedBtMessage = bluetoothService.lastReceivedMessage

    // 1v1 Duel Peer-to-Peer State
    private val _isDuelMode = MutableStateFlow(false)
    val isDuelMode: StateFlow<Boolean> = _isDuelMode.asStateFlow()

    private val _opponentName = MutableStateFlow("Adversaire")
    val opponentName: StateFlow<String> = _opponentName.asStateFlow()

    private val _opponentScore = MutableStateFlow(0)
    val opponentScore: StateFlow<Int> = _opponentScore.asStateFlow()

    private val _opponentQuestionIndex = MutableStateFlow(0)
    val opponentQuestionIndex: StateFlow<Int> = _opponentQuestionIndex.asStateFlow()

    private val _opponentLastAnswerCorrect = MutableStateFlow<Boolean?>(null)
    val opponentLastAnswerCorrect: StateFlow<Boolean?> = _opponentLastAnswerCorrect.asStateFlow()

    private val _opponentFinished = MutableStateFlow(false)
    val opponentFinished: StateFlow<Boolean> = _opponentFinished.asStateFlow()

    private val _duelWinnerMessage = MutableStateFlow<String?>(null)
    val duelWinnerMessage: StateFlow<String?> = _duelWinnerMessage.asStateFlow()

    fun startBluetoothDiscovery() = bluetoothService.startDiscovery()
    fun stopBluetoothDiscovery() = bluetoothService.stopDiscovery()
    fun startBluetoothHosting() = bluetoothService.startHosting()
    fun connectToBluetoothDevice(address: String) = bluetoothService.connectToDevice(address)
    fun disconnectBluetooth() {
        bluetoothService.disconnect()
        _isDuelMode.value = false
    }
    fun sendBluetoothMessage(msg: String) = bluetoothService.sendMessage(msg)

    private fun observeBluetoothMessages() {
        viewModelScope.launch {
            bluetoothService.lastReceivedMessage.collect { rawMsg ->
                if (!rawMsg.isNullOrBlank()) {
                    handleIncomingBluetoothMessage(rawMsg)
                }
            }
        }
    }

    private fun handleIncomingBluetoothMessage(rawMsg: String) {
        try {
            val json = JSONObject(rawMsg)
            when (json.optString("action")) {
                "START_DUEL" -> {
                    val catTitle = json.optString("categoryTitle", "Duel 1v1 Bluetooth")
                    val hostName = json.optString("hostNickname", "Hôte")
                    val questionsArray = json.optJSONArray("questions") ?: return
                    val list = mutableListOf<QuestionEntity>()
                    for (i in 0 until questionsArray.length()) {
                        val qObj = questionsArray.getJSONObject(i)
                        list.add(jsonToQuestion(qObj))
                    }
                    if (list.isNotEmpty()) {
                        startDuelAsClient(list, catTitle, hostName)
                    }
                }
                "PROGRESS" -> {
                    val score = json.optInt("score", 0)
                    val qIndex = json.optInt("qIndex", 0)
                    val isCorrect = json.optBoolean("isCorrect", false)
                    val isFinished = json.optBoolean("isFinished", false)
                    _opponentScore.value = score
                    _opponentQuestionIndex.value = qIndex
                    _opponentLastAnswerCorrect.value = isCorrect
                    _opponentFinished.value = isFinished

                    if (isFinished && _currentScreen.value is CurrentScreen.QuizResult) {
                        computeDuelWinner()
                    }
                }
                "REMATCH_REQUEST" -> {
                    startBluetoothDuel()
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("MainViewModel", "Error parsing BT message: ${e.message}")
        }
    }

    fun startBluetoothDuel(categoryId: String = "all") {
        viewModelScope.launch {
            val connectedDevice = (bluetoothService.connectionState.value as? com.example.core.bluetooth.BluetoothConnectionState.Connected)?.device
            val oppName = connectedDevice?.name ?: "Adversaire"
            _opponentName.value = oppName
            _opponentScore.value = 0
            _opponentQuestionIndex.value = 0
            _opponentFinished.value = false
            _opponentLastAnswerCorrect.value = null
            _duelWinnerMessage.value = null

            val count = 10
            val loadedQuestions = repository.getQuestionsForQuiz(categoryId, "DUEL", count)
            if (loadedQuestions.isEmpty()) return@launch

            val jsonArray = JSONArray()
            for (q in loadedQuestions) {
                jsonArray.put(questionToJson(q))
            }

            val payload = JSONObject().apply {
                put("action", "START_DUEL")
                put("categoryTitle", "Duel 1v1 Bluetooth")
                put("hostNickname", playerProfile.value?.nickname ?: "Joueur 1")
                put("questions", jsonArray)
            }

            bluetoothService.sendMessage(payload.toString())

            // Initialize host local game
            _isDuelMode.value = true
            _activeCategoryId.value = categoryId
            _activeMode.value = "DUEL"
            _activeCategoryTitle.value = "Duel 1v1 Bluetooth"
            _activeQuestions.value = loadedQuestions
            _currentQuestionIndex.value = 0
            _currentScore.value = 0
            _currentCombo.value = 0
            _bestComboInSession.value = 0
            _selectedOption.value = null
            _isAnswerConfirmed.value = false
            _eliminatedOptionIndices.value = emptySet()
            _hasWatchedDoubleAd.value = false
            questionResultsMap.clear()

            _currentScreen.value = CurrentScreen.QuizPlay
            GameShowAudioEngine.playBgm(com.example.core.audio.BgmTrackType.QUIZ_PLATEAU_SUSPENSE)
            startQuestionTimer()
        }
    }

    private fun startDuelAsClient(questions: List<QuestionEntity>, categoryTitle: String, hostName: String) {
        _isDuelMode.value = true
        _opponentName.value = hostName
        _opponentScore.value = 0
        _opponentQuestionIndex.value = 0
        _opponentFinished.value = false
        _opponentLastAnswerCorrect.value = null
        _duelWinnerMessage.value = null

        _activeCategoryId.value = "all"
        _activeMode.value = "DUEL"
        _activeCategoryTitle.value = categoryTitle
        _activeQuestions.value = questions
        _currentQuestionIndex.value = 0
        _currentScore.value = 0
        _currentCombo.value = 0
        _bestComboInSession.value = 0
        _selectedOption.value = null
        _isAnswerConfirmed.value = false
        _eliminatedOptionIndices.value = emptySet()
        _hasWatchedDoubleAd.value = false
        questionResultsMap.clear()

        _currentScreen.value = CurrentScreen.QuizPlay
        GameShowAudioEngine.playBgm(com.example.core.audio.BgmTrackType.QUIZ_PLATEAU_SUSPENSE)
        startQuestionTimer()
    }

    fun requestDuelRematch() {
        if (_isDuelMode.value) {
            val isHost = (bluetoothService.connectionState.value as? com.example.core.bluetooth.BluetoothConnectionState.Connected)?.isHost == true
            if (isHost) {
                startBluetoothDuel()
            } else {
                val rematchPayload = JSONObject().apply {
                    put("action", "REMATCH_REQUEST")
                }
                bluetoothService.sendMessage(rematchPayload.toString())
            }
        }
    }

    private fun computeDuelWinner() {
        val myScore = _currentScore.value
        val oppScore = _opponentScore.value
        _duelWinnerMessage.value = when {
            myScore > oppScore -> "Victoire écrasante en Duel 1v1 !"
            myScore < oppScore -> "Défaite en Duel... Revanche ?"
            else -> "Égalité parfaite en Duel 1v1 !"
        }
    }

    private fun questionToJson(q: QuestionEntity): JSONObject {
        return JSONObject().apply {
            put("id", q.id)
            put("catId", q.categoryId)
            put("subcat", q.subcategory)
            put("question", q.question)
            put("optA", q.optionA)
            put("optB", q.optionB)
            put("optC", q.optionC)
            put("optD", q.optionD)
            put("correct", q.correctAnswer)
            put("explanation", q.explanation)
            put("diff", q.difficulty)
        }
    }

    private fun jsonToQuestion(json: JSONObject): QuestionEntity {
        return QuestionEntity(
            id = json.optLong("id", 0L),
            categoryId = json.optString("catId", "all"),
            subcategory = json.optString("subcat", "Général"),
            question = json.optString("question", ""),
            optionA = json.optString("optA", ""),
            optionB = json.optString("optB", ""),
            optionC = json.optString("optC", ""),
            optionD = json.optString("optD", ""),
            correctAnswer = json.optString("correct", "A"),
            explanation = json.optString("explanation", ""),
            difficulty = json.optString("diff", "medium")
        )
    }

    // Firebase Auth & Cloud States
    val currentFirebaseUser: StateFlow<FirebaseUser?> = FirebaseManager.currentUser
    val isAuthenticatingFirebase: StateFlow<Boolean> = FirebaseManager.isAuthenticating
    val firebaseAuthError: StateFlow<String?> = FirebaseManager.authError

    // Cloud and Multiplayer States
    val availableCloudPacks: List<CloudQuestionPack> = cloudDataSource.getAvailableCloudPacks()
    val activeOnlineRoom: StateFlow<OnlineMatchRoom?> = cloudDataSource.activeRoom
    val isSyncingCloud: StateFlow<Boolean> = cloudDataSource.isSyncingCloud
    val cloudSyncMessage: StateFlow<String?> = cloudDataSource.syncMessage

    // Reactive StateFlows
    val playerProfile: StateFlow<PlayerProfileEntity?> = repository.playerProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val achievements: StateFlow<List<AchievementEntity>> = repository.allAchievements.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val pendingSyncCount: StateFlow<Int> = repository.pendingSyncCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val syncStatus: StateFlow<SyncStatus> = syncEngine.syncStatus

    // Navigation & UI States
    private val _currentScreen = MutableStateFlow<CurrentScreen>(CurrentScreen.Home)
    val currentScreen: StateFlow<CurrentScreen> = _currentScreen.asStateFlow()

    private val _showRewardedAdDialog = MutableStateFlow(false)
    val showRewardedAdDialog: StateFlow<Boolean> = _showRewardedAdDialog.asStateFlow()

    // Active Quiz Session States
    private val _activeQuestions = MutableStateFlow<List<QuestionEntity>>(emptyList())
    val activeQuestions: StateFlow<List<QuestionEntity>> = _activeQuestions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _currentScore = MutableStateFlow(0)
    val currentScore: StateFlow<Int> = _currentScore.asStateFlow()

    private val _currentCombo = MutableStateFlow(0)
    val currentCombo: StateFlow<Int> = _currentCombo.asStateFlow()

    private val _bestComboInSession = MutableStateFlow(0)
    val bestComboInSession: StateFlow<Int> = _bestComboInSession.asStateFlow()

    private val _selectedOption = MutableStateFlow<Int?>(null)
    val selectedOption: StateFlow<Int?> = _selectedOption.asStateFlow()

    private val _isAnswerConfirmed = MutableStateFlow(false)
    val isAnswerConfirmed: StateFlow<Boolean> = _isAnswerConfirmed.asStateFlow()

    private val _eliminatedOptionIndices = MutableStateFlow<Set<Int>>(emptySet())
    val eliminatedOptionIndices: StateFlow<Set<Int>> = _eliminatedOptionIndices.asStateFlow()

    private val _timeRemainingSeconds = MutableStateFlow(QuizConstants.SECONDS_PER_QUESTION)
    val timeRemainingSeconds: StateFlow<Int> = _timeRemainingSeconds.asStateFlow()

    private val _activeCategoryTitle = MutableStateFlow("Quiz")
    val activeCategoryTitle: StateFlow<String> = _activeCategoryTitle.asStateFlow()

    private val _activeCategoryId = MutableStateFlow("all")
    private val _activeMode = MutableStateFlow("STANDARD")

    private val _quizSubmissionResult = MutableStateFlow<QuizSubmissionResult?>(null)
    val quizSubmissionResult: StateFlow<QuizSubmissionResult?> = _quizSubmissionResult.asStateFlow()

    private val _hasWatchedDoubleAd = MutableStateFlow(false)
    val hasWatchedDoubleAd: StateFlow<Boolean> = _hasWatchedDoubleAd.asStateFlow()

    private val questionResultsMap = mutableMapOf<Long, Boolean>()
    private var timerJob: Job? = null

    val isMusicEnabled: StateFlow<Boolean> = GameShowAudioEngine.isMusicEnabled
    val isMenuMusicEnabled: StateFlow<Boolean> = GameShowAudioEngine.isMenuMusicEnabled
    val isSfxEnabled: StateFlow<Boolean> = GameShowAudioEngine.isSfxEnabled
    val bgmVolume: StateFlow<Float> = GameShowAudioEngine.bgmVolume
    val sfxVolume: StateFlow<Float> = GameShowAudioEngine.sfxVolume

    fun toggleMusic() {
        GameShowAudioEngine.toggleMusic()
    }

    fun setMusicEnabled(enabled: Boolean) {
        GameShowAudioEngine.setMusicEnabled(enabled)
    }

    fun setMenuMusicEnabled(enabled: Boolean) {
        GameShowAudioEngine.setMenuMusicEnabled(enabled)
    }

    fun setSfxEnabled(enabled: Boolean) {
        GameShowAudioEngine.setSfxEnabled(enabled)
    }

    fun setBgmVolume(volume: Float) {
        GameShowAudioEngine.setBgmVolume(volume)
    }

    fun setSfxVolume(volume: Float) {
        GameShowAudioEngine.setSfxVolume(volume)
    }

    fun navigateTo(screen: CurrentScreen) {
        GameShowAudioEngine.stopTension()
        when (screen) {
            is CurrentScreen.QuizPlay -> GameShowAudioEngine.playBgm(com.example.core.audio.BgmTrackType.QUIZ_PLATEAU_SUSPENSE)
            is CurrentScreen.QuizResult -> GameShowAudioEngine.playBgm(com.example.core.audio.BgmTrackType.RESULTS_TRIUMPH)
            else -> GameShowAudioEngine.playBgm(com.example.core.audio.BgmTrackType.MENU_AMBIENT)
        }
        _currentScreen.value = screen
    }

    fun openRewardedAdDialog() {
        _showRewardedAdDialog.value = true
    }

    fun closeRewardedAdDialog() {
        _showRewardedAdDialog.value = false
    }

    fun startQuiz(categoryId: String, mode: String = "STANDARD") {
        viewModelScope.launch {
            val hasLife = repository.useLife()
            if (!hasLife) {
                openRewardedAdDialog()
                return@launch
            }

            _isDuelMode.value = false
            _activeCategoryId.value = categoryId
            _activeMode.value = mode

            val category = repository.getCategoryById(categoryId)
            _activeCategoryTitle.value = when {
                categoryId == "all" && mode == "CHRONO" -> "Mode Chrono (60s)"
                categoryId == "daily" -> "Défi du Jour"
                category != null -> category.name
                else -> "Quiz Mixte"
            }

            val count = if (mode == "CHRONO") 15 else 10
            val loadedQuestions = repository.getQuestionsForQuiz(categoryId, mode, count)
            _activeQuestions.value = loadedQuestions
            _currentQuestionIndex.value = 0
            _currentScore.value = 0
            _currentCombo.value = 0
            _bestComboInSession.value = 0
            _selectedOption.value = null
            _isAnswerConfirmed.value = false
            _eliminatedOptionIndices.value = emptySet()
            _hasWatchedDoubleAd.value = false
            questionResultsMap.clear()

            _currentScreen.value = CurrentScreen.QuizPlay
            GameShowAudioEngine.playBgm(com.example.core.audio.BgmTrackType.QUIZ_PLATEAU_SUSPENSE)
            startQuestionTimer()
        }
    }

    private fun startQuestionTimer() {
        timerJob?.cancel()
        val totalSecs = if (_activeMode.value == "CHRONO") QuizConstants.CHRONO_MODE_SECONDS else QuizConstants.SECONDS_PER_QUESTION
        _timeRemainingSeconds.value = totalSecs
        GameShowAudioEngine.startTension(totalSecs, totalSecs)

        timerJob = viewModelScope.launch {
            while (_timeRemainingSeconds.value > 0 && !_isAnswerConfirmed.value) {
                delay(1000)
                val remaining = _timeRemainingSeconds.value - 1
                _timeRemainingSeconds.value = remaining
                if (remaining > 0) {
                    GameShowAudioEngine.startTension(remaining, totalSecs)
                }
            }
            if (_timeRemainingSeconds.value <= 0 && !_isAnswerConfirmed.value) {
                // Time up! Count as wrong answer
                selectOption(-1)
            }
        }
    }

    fun selectOption(optionIndex: Int) {
        if (_isAnswerConfirmed.value) return
        if (optionIndex >= 0) {
            GameShowAudioEngine.playOptionClick()
        }
        timerJob?.cancel()
        GameShowAudioEngine.stopTension()

        _selectedOption.value = optionIndex
        _isAnswerConfirmed.value = true

        val currentQ = _activeQuestions.value.getOrNull(_currentQuestionIndex.value) ?: return
        val isCorrect = optionIndex == currentQ.correctOptionIndex
        questionResultsMap[currentQ.id] = isCorrect

        if (isCorrect) {
            _currentScore.value += 1
            val newCombo = _currentCombo.value + 1
            _currentCombo.value = newCombo
            if (newCombo > _bestComboInSession.value) {
                _bestComboInSession.value = newCombo
            }
            GameShowAudioEngine.playCorrectAnswer(newCombo)
        } else {
            _currentCombo.value = 0
            GameShowAudioEngine.playWrongAnswer()
        }

        if (_isDuelMode.value) {
            try {
                val progressPayload = JSONObject().apply {
                    put("action", "PROGRESS")
                    put("score", _currentScore.value)
                    put("qIndex", _currentQuestionIndex.value + 1)
                    put("isCorrect", isCorrect)
                    put("combo", _currentCombo.value)
                    put("isFinished", false)
                }
                bluetoothService.sendMessage(progressPayload.toString())
            } catch (e: Exception) {
                android.util.Log.w("MainViewModel", "Error sending progress: ${e.message}")
            }
        }
    }

    fun nextQuestion() {
        val nextIdx = _currentQuestionIndex.value + 1
        if (nextIdx < _activeQuestions.value.size) {
            _currentQuestionIndex.value = nextIdx
            _selectedOption.value = null
            _isAnswerConfirmed.value = false
            _eliminatedOptionIndices.value = emptySet()
            startQuestionTimer()
        } else {
            finishQuizSession()
        }
    }

    private fun finishQuizSession() {
        GameShowAudioEngine.stopTension()
        GameShowAudioEngine.playVictoryFanfare()
        
        if (_isDuelMode.value) {
            try {
                val finishPayload = JSONObject().apply {
                    put("action", "PROGRESS")
                    put("score", _currentScore.value)
                    put("qIndex", _activeQuestions.value.size)
                    put("isCorrect", true)
                    put("combo", _bestComboInSession.value)
                    put("isFinished", true)
                }
                bluetoothService.sendMessage(finishPayload.toString())
            } catch (e: Exception) {
                android.util.Log.w("MainViewModel", "Error sending finish BT progress: ${e.message}")
            }
            computeDuelWinner()
        }

        viewModelScope.launch {
            val result = repository.submitQuiz(
                categoryId = _activeCategoryId.value,
                mode = _activeMode.value,
                score = _currentScore.value,
                totalQuestions = _activeQuestions.value.size,
                bestCombo = _bestComboInSession.value,
                questionResults = questionResultsMap
            )
            _quizSubmissionResult.value = result
            _currentScreen.value = CurrentScreen.QuizResult
            GameShowAudioEngine.playBgm(com.example.core.audio.BgmTrackType.RESULTS_TRIUMPH)
        }
    }

    fun use5050Hint() {
        viewModelScope.launch {
            val canAfford = repository.deductCoins(QuizConstants.COST_HINT_5050)
            if (!canAfford) return@launch

            val currentQ = _activeQuestions.value.getOrNull(_currentQuestionIndex.value) ?: return@launch
            val correctIdx = currentQ.correctOptionIndex
            val wrongIndices = listOf(0, 1, 2, 3).filter { it != correctIdx }.shuffled().take(2)
            _eliminatedOptionIndices.value = wrongIndices.toSet()
            GameShowAudioEngine.playLifeline()
        }
    }

    fun applyFree5050Hint() {
        val currentQ = _activeQuestions.value.getOrNull(_currentQuestionIndex.value) ?: return
        val correctIdx = currentQ.correctOptionIndex
        val wrongIndices = listOf(0, 1, 2, 3).filter { it != correctIdx }.shuffled().take(2)
        _eliminatedOptionIndices.value = wrongIndices.toSet()
        GameShowAudioEngine.playLifeline()
    }

    fun useSkipHint() {
        viewModelScope.launch {
            val canAfford = repository.deductCoins(QuizConstants.COST_HINT_SKIP)
            if (!canAfford) return@launch
            GameShowAudioEngine.playLifeline()
            nextQuestion()
        }
    }

    fun applyFreeSkipHint() {
        GameShowAudioEngine.playLifeline()
        nextQuestion()
    }

    /**
     * Shows a Rewarded Video Ad via AdManager to grant the user a free Hint (50/50 or Skip).
     */
    fun watchAdForRewardedHint(
        activity: Activity,
        hintType: AdRewardType = AdRewardType.HINT_5050,
        onRewardCallback: (AdRewardResult) -> Unit = {}
    ) {
        AdManager.showRewardedAdForHint(
            activity = activity,
            hintType = hintType,
            onHintGranted = { result ->
                if (result.isSuccess) {
                    when (result.rewardType) {
                        AdRewardType.HINT_5050 -> applyFree5050Hint()
                        AdRewardType.HINT_SKIP -> applyFreeSkipHint()
                        else -> applyFree5050Hint()
                    }
                }
                onRewardCallback(result)
            }
        )
    }

    /**
     * Shows a Rewarded Video Ad via AdManager to grant the user bonus points / XP.
     */
    fun watchAdForBonusPoints(
        activity: Activity,
        bonusCoins: Int = QuizConstants.REWARD_WATCH_AD_BONUS_POINTS,
        bonusXp: Int = QuizConstants.REWARD_WATCH_AD_XP,
        onRewardCallback: (AdRewardResult) -> Unit = {}
    ) {
        AdManager.showRewardedAdForBonusPoints(
            activity = activity,
            bonusPoints = bonusCoins,
            onBonusGranted = { result ->
                if (result.isSuccess) {
                    viewModelScope.launch {
                        repository.addBonusPointsAndXp(bonusCoins = result.amount, bonusXp = bonusXp)
                    }
                }
                onRewardCallback(result)
            }
        )
    }

    fun reportQuestion(reason: String, comment: String) {
        viewModelScope.launch {
            val currentQ = _activeQuestions.value.getOrNull(_currentQuestionIndex.value) ?: return@launch
            repository.reportQuestion(currentQ.id, reason, comment)
        }
    }

    fun watchAdForLife() {
        viewModelScope.launch {
            repository.addLifeFromReward()
        }
    }

    fun buyLifeWithCoins() {
        viewModelScope.launch {
            val success = repository.deductCoins(QuizConstants.COST_EXTRA_LIFE)
            if (success) {
                repository.addLifeFromReward()
            }
        }
    }

    fun watchAdForCoins() {
        viewModelScope.launch {
            repository.addCoins(QuizConstants.REWARD_WATCH_AD_COINS)
            GameShowAudioEngine.playCoinCollect()
        }
    }

    fun watchDoubleRewardAd(activity: Activity? = null) {
        viewModelScope.launch {
            val result = _quizSubmissionResult.value ?: return@launch
            if (_hasWatchedDoubleAd.value) return@launch

            if (activity != null) {
                AdManager.showRewardedAd(
                    activity = activity,
                    rewardType = AdRewardType.COINS,
                    defaultAmount = result.coinsEarned,
                    onRewardEarned = {
                        viewModelScope.launch {
                            repository.addCoins(result.coinsEarned)
                            _hasWatchedDoubleAd.value = true
                            GameShowAudioEngine.playCoinCollect()
                        }
                    }
                )
            } else {
                repository.addCoins(result.coinsEarned)
                _hasWatchedDoubleAd.value = true
                GameShowAudioEngine.playCoinCollect()
            }
        }
    }

    fun showInterstitialOnExitQuiz(activity: Activity, onDone: () -> Unit) {
        AdManager.showInterstitialIfEligible(activity, onDone)
    }

    fun claimAchievement(id: String) {
        viewModelScope.launch {
            repository.claimAchievementReward(id)
            GameShowAudioEngine.playCoinCollect()
        }
    }

    fun updateProfile(nickname: String, avatarId: String) {
        viewModelScope.launch {
            repository.updateCustomization(nickname, avatarId)
        }
    }

    fun forceSync() {
        viewModelScope.launch {
            syncEngine.processSyncQueue()
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            val result = FirebaseManager.signInWithGoogle(context)
            result.onSuccess { user ->
                playerProfile.value?.let { profile ->
                    val updated = profile.copy(
                        nickname = user.displayName ?: profile.nickname,
                        isGuest = false
                    )
                    database.playerDao().insertOrUpdate(updated)
                    firebaseService.syncPlayerProfileToCloud(updated)
                }
            }
        }
    }

    fun signOutFromFirebase(context: Context) {
        viewModelScope.launch {
            FirebaseManager.signOut(context)
        }
    }

    fun syncWithFirebaseCloud() {
        viewModelScope.launch {
            playerProfile.value?.let { profile ->
                firebaseService.syncPlayerProfileToCloud(profile)
            }
        }
    }

    fun joinOnlineMatchmaking(roomCode: String) {
        viewModelScope.launch {
            val player = playerProfile.value
            cloudDataSource.joinMultiplayerRoom(
                roomCode = roomCode,
                playerNickname = player?.nickname ?: "Joueur_Cloud",
                playerAvatar = player?.avatarId ?: "avatar_1",
                playerLevel = player?.level ?: 1
            )
        }
    }

    fun installCloudPack(pack: CloudQuestionPack) {
        viewModelScope.launch {
            cloudDataSource.installCloudPack(pack)
        }
    }

    fun createCustomQuestion(question: QuestionEntity) {
        viewModelScope.launch {
            cloudDataSource.createCustomQuestion(question)
            firebaseService.publishQuestionToCloud(question)
        }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            database.clearAllTables()
            database.playerDao().insertOrUpdate(
                PlayerProfileEntity(
                    id = 1,
                    playerId = AppDatabase.generatePlayerId(),
                    deviceUuid = java.util.UUID.randomUUID().toString(),
                    nickname = "Joueur FULLQUIZZ",
                    avatarId = "avatar_1",
                    level = 1,
                    xp = 0,
                    coins = 100,
                    lives = 5,
                    maxLives = 5,
                    lastLifeRegenTimestamp = System.currentTimeMillis(),
                    streakCount = 1,
                    lastPlayedDate = "",
                    totalGamesPlayed = 0,
                    totalCorrectAnswers = 0,
                    totalQuestionsAnswered = 0,
                    bestStreak = 0,
                    isGuest = true
                )
            )
            database.categoryDao().insertAll(SeedData.categories)
            database.achievementDao().insertAll(SeedData.achievements)
            database.questionDao().insertAll(SeedData.questions)
            database.categoryDao().refreshQuestionCounts()
            navigateTo(CurrentScreen.Home)
        }
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothService.release()
    }
}
