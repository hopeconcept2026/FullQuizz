package com.example.data.repository

import com.example.core.constants.QuizConstants
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AchievementEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.DailyChallengeEntity
import com.example.data.local.entity.PlayerProfileEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.local.entity.QuizSessionEntity
import com.example.data.local.entity.SyncQueueEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class QuizSubmissionResult(
    val sessionId: Long,
    val score: Int,
    val totalQuestions: Int,
    val xpEarned: Int,
    val coinsEarned: Int,
    val bestCombo: Int,
    val didLevelUp: Boolean,
    val newLevel: Int,
    val newTitle: String,
    val unlockedAchievements: List<AchievementEntity>
)

class QuizRepository(private val database: AppDatabase) {

    private val playerDao = database.playerDao()
    private val categoryDao = database.categoryDao()
    private val questionDao = database.questionDao()
    private val quizSessionDao = database.quizSessionDao()
    private val achievementDao = database.achievementDao()
    private val dailyChallengeDao = database.dailyChallengeDao()
    private val syncQueueDao = database.syncQueueDao()

    // 1. Reactive Player Profile with Life Regeneration Check
    val playerProfile: Flow<PlayerProfileEntity?> = playerDao.getPlayerProfileFlow().map { player ->
        if (player != null) {
            checkAndRegenerateLives(player)
        } else {
            null
        }
    }.flowOn(Dispatchers.IO)

    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategoriesFlow()
    val allAchievements: Flow<List<AchievementEntity>> = achievementDao.getAllAchievementsFlow()
    val pendingSyncCount: Flow<Int> = syncQueueDao.getPendingCountFlow()

    private suspend fun checkAndRegenerateLives(player: PlayerProfileEntity): PlayerProfileEntity {
        if (player.lives >= player.maxLives) return player

        val now = System.currentTimeMillis()
        val elapsed = now - player.lastLifeRegenTimestamp
        val regenUnits = (elapsed / QuizConstants.LIFE_REGEN_INTERVAL_MS).toInt()

        if (regenUnits > 0) {
            val newLives = (player.lives + regenUnits).coerceAtMost(player.maxLives)
            val newTimestamp = if (newLives >= player.maxLives) now else player.lastLifeRegenTimestamp + (regenUnits * QuizConstants.LIFE_REGEN_INTERVAL_MS)
            playerDao.updateLives(newLives, newTimestamp)
            return player.copy(lives = newLives, lastLifeRegenTimestamp = newTimestamp)
        }
        return player
    }

    suspend fun getQuestionsForQuiz(categoryId: String, mode: String, count: Int = 10): List<QuestionEntity> = withContext(Dispatchers.IO) {
        if (categoryId == "all" || categoryId == "mixed") {
            questionDao.getRandomQuestionsAll(count)
        } else {
            val questions = questionDao.getRandomQuestionsByCategory(categoryId, count)
            if (questions.size < count) {
                val extras = questionDao.getRandomQuestionsAll(count - questions.size)
                (questions + extras).distinctBy { it.id }.take(count)
            } else {
                questions
            }
        }
    }

    suspend fun getCategoryById(categoryId: String): CategoryEntity? = withContext(Dispatchers.IO) {
        categoryDao.getCategoryById(categoryId)
    }

    suspend fun useLife(): Boolean = withContext(Dispatchers.IO) {
        val player = playerDao.getPlayerProfile() ?: return@withContext false
        if (player.lives > 0) {
            val newLives = player.lives - 1
            val timestamp = if (player.lives == player.maxLives) System.currentTimeMillis() else player.lastLifeRegenTimestamp
            playerDao.updateLives(newLives, timestamp)
            true
        } else {
            false
        }
    }

    suspend fun addLifeFromReward(): Unit = withContext(Dispatchers.IO) {
        val player = playerDao.getPlayerProfile() ?: return@withContext
        val newLives = (player.lives + 1).coerceAtMost(player.maxLives)
        playerDao.updateLives(newLives, System.currentTimeMillis())
    }

    suspend fun addCoins(amount: Int): Unit = withContext(Dispatchers.IO) {
        playerDao.addCoins(amount)
    }

    suspend fun addBonusPointsAndXp(bonusCoins: Int, bonusXp: Int): Unit = withContext(Dispatchers.IO) {
        if (bonusCoins > 0) {
            playerDao.addCoins(bonusCoins)
        }
        if (bonusXp > 0) {
            val player = playerDao.getPlayerProfile() ?: return@withContext
            val totalXp = player.xp + bonusXp
            var newLevel = player.level
            while (totalXp >= QuizConstants.getXpRequiredForLevel(newLevel)) {
                newLevel++
            }
            playerDao.updateLevelAndXp(newLevel, totalXp)
        }
    }

    suspend fun deductCoins(amount: Int): Boolean = withContext(Dispatchers.IO) {
        val player = playerDao.getPlayerProfile() ?: return@withContext false
        if (player.coins >= amount) {
            playerDao.addCoins(-amount)
            true
        } else {
            false
        }
    }

    suspend fun updateCustomization(nickname: String, avatarId: String) = withContext(Dispatchers.IO) {
        playerDao.updateProfileCustomization(nickname, avatarId)
        // Queue profile update sync
        enqueueSyncEvent("PROFILE_UPDATED", """{"nickname":"$nickname","avatar_id":"$avatarId"}""")
    }

    // 2. Submit Quiz Engine
    suspend fun submitQuiz(
        categoryId: String,
        mode: String,
        score: Int,
        totalQuestions: Int,
        bestCombo: Int,
        questionResults: Map<Long, Boolean>
    ): QuizSubmissionResult = withContext(Dispatchers.IO) {
        var player = playerDao.getPlayerProfile() ?: PlayerProfileEntity(
            playerId = AppDatabase.generatePlayerId(),
            deviceUuid = UUID.randomUUID().toString()
        )

        // Calculate XP and Coins with combos
        val comboMultiplier = QuizConstants.getComboMultiplier(bestCombo)
        val earnedXp = (score * QuizConstants.BASE_XP_PER_CORRECT * comboMultiplier).coerceAtLeast(5)
        val earnedCoins = (score * QuizConstants.BASE_COINS_PER_CORRECT + (if (bestCombo >= 5) 10 else 0))

        // Level check
        val currentLevel = player.level
        val totalXp = player.xp + earnedXp
        var newLevel = currentLevel
        while (totalXp >= QuizConstants.getXpRequiredForLevel(newLevel)) {
            newLevel++
        }
        val didLevelUp = newLevel > currentLevel

        // Streak check
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        var newStreak = player.streakCount
        if (player.lastPlayedDate.isNotEmpty()) {
            val isConsecutive = isConsecutiveDay(player.lastPlayedDate, todayStr)
            val isSameDay = player.lastPlayedDate == todayStr
            if (!isSameDay) {
                newStreak = if (isConsecutive) player.streakCount + 1 else 1
            }
        } else {
            newStreak = 1
        }

        // Update Question stats
        questionResults.forEach { (qId, isCorrect) ->
            questionDao.updateQuestionStats(qId, if (isCorrect) 1 else 0)
        }

        // Save session
        val sessionId = quizSessionDao.insertSession(
            QuizSessionEntity(
                mode = mode,
                categoryId = categoryId,
                score = score,
                totalQuestions = totalQuestions,
                xpEarned = earnedXp,
                coinsEarned = earnedCoins,
                bestCombo = bestCombo,
                timestamp = System.currentTimeMillis()
            )
        )

        // Update player profile
        val updatedPlayer = player.copy(
            level = newLevel,
            xp = totalXp,
            coins = player.coins + earnedCoins,
            streakCount = newStreak,
            lastPlayedDate = todayStr,
            totalGamesPlayed = player.totalGamesPlayed + 1,
            totalCorrectAnswers = player.totalCorrectAnswers + score,
            totalQuestionsAnswered = player.totalQuestionsAnswered + totalQuestions,
            bestStreak = maxOf(player.bestStreak, newStreak)
        )
        playerDao.insertOrUpdate(updatedPlayer)

        // Check & unlock achievements
        val newlyUnlocked = checkAchievements(updatedPlayer, score, totalQuestions, bestCombo, categoryId)

        // Enqueue Sync Event (Offline -> Online Sync Engine)
        val payload = """
            {
                "session_id": $sessionId,
                "category_id": "$categoryId",
                "mode": "$mode",
                "score": $score,
                "total_questions": $totalQuestions,
                "xp_earned": $earnedXp,
                "coins_earned": $earnedCoins,
                "best_combo": $bestCombo,
                "player_id": "${updatedPlayer.playerId}",
                "level": $newLevel,
                "timestamp": ${System.currentTimeMillis()}
            }
        """.trimIndent()
        enqueueSyncEvent("QUIZ_COMPLETED", payload)

        QuizSubmissionResult(
            sessionId = sessionId,
            score = score,
            totalQuestions = totalQuestions,
            xpEarned = earnedXp,
            coinsEarned = earnedCoins,
            bestCombo = bestCombo,
            didLevelUp = didLevelUp,
            newLevel = newLevel,
            newTitle = QuizConstants.getLevelTitle(newLevel),
            unlockedAchievements = newlyUnlocked
        )
    }

    private suspend fun checkAchievements(
        player: PlayerProfileEntity,
        score: Int,
        total: Int,
        bestCombo: Int,
        categoryId: String
    ): List<AchievementEntity> {
        val unlockedList = mutableListOf<AchievementEntity>()
        val now = System.currentTimeMillis()

        // 1. First quiz
        achievementDao.getAchievementById("first_quiz")?.let { ach ->
            if (!ach.isUnlocked && player.totalGamesPlayed >= 1) {
                achievementDao.unlock(ach.id, now)
                unlockedList.add(ach.copy(isUnlocked = true))
            }
        }

        // 2. Games count
        checkThresholdAchievement("quiz_5", player.totalGamesPlayed, unlockedList, now)
        checkThresholdAchievement("quiz_25", player.totalGamesPlayed, unlockedList, now)
        checkThresholdAchievement("quiz_100", player.totalGamesPlayed, unlockedList, now)

        // 3. Correct answers
        checkThresholdAchievement("correct_10", player.totalCorrectAnswers, unlockedList, now)
        checkThresholdAchievement("correct_50", player.totalCorrectAnswers, unlockedList, now)
        checkThresholdAchievement("correct_200", player.totalCorrectAnswers, unlockedList, now)
        checkThresholdAchievement("correct_500", player.totalCorrectAnswers, unlockedList, now)

        // 4. Combos
        if (bestCombo >= 3) checkThresholdAchievement("combo_3", bestCombo, unlockedList, now)
        if (bestCombo >= 5) checkThresholdAchievement("combo_5", bestCombo, unlockedList, now)
        if (bestCombo >= 10 && score == total) checkThresholdAchievement("combo_10", bestCombo, unlockedList, now)

        // 5. Streaks
        checkThresholdAchievement("streak_3", player.streakCount, unlockedList, now)
        checkThresholdAchievement("streak_7", player.streakCount, unlockedList, now)
        checkThresholdAchievement("streak_14", player.streakCount, unlockedList, now)
        checkThresholdAchievement("streak_30", player.streakCount, unlockedList, now)

        // 6. Level milestones
        checkThresholdAchievement("level_5", player.level, unlockedList, now)
        checkThresholdAchievement("level_10", player.level, unlockedList, now)
        checkThresholdAchievement("level_20", player.level, unlockedList, now)
        checkThresholdAchievement("level_50", player.level, unlockedList, now)

        // 7. Category specific
        if (categoryId == "bible") checkThresholdAchievement("expert_bible", player.totalCorrectAnswers, unlockedList, now)
        if (categoryId == "afrique") checkThresholdAchievement("expert_afrique", player.totalCorrectAnswers, unlockedList, now)
        if (categoryId == "rdc") checkThresholdAchievement("expert_rdc", player.totalCorrectAnswers, unlockedList, now)

        return unlockedList
    }

    private suspend fun checkThresholdAchievement(
        id: String,
        currentVal: Int,
        unlockedList: MutableList<AchievementEntity>,
        now: Long
    ) {
        val ach = achievementDao.getAchievementById(id) ?: return
        if (!ach.isUnlocked) {
            achievementDao.updateProgress(id, currentVal)
            if (currentVal >= ach.targetValue) {
                achievementDao.unlock(id, now)
                unlockedList.add(ach.copy(isUnlocked = true, currentProgress = currentVal))
            }
        }
    }

    suspend fun claimAchievementReward(id: String) = withContext(Dispatchers.IO) {
        val ach = achievementDao.getAchievementById(id) ?: return@withContext
        if (ach.isUnlocked && !ach.isClaimed) {
            achievementDao.claimReward(id)
            val player = playerDao.getPlayerProfile() ?: return@withContext
            playerDao.insertOrUpdate(
                player.copy(
                    xp = player.xp + ach.xpReward,
                    coins = player.coins + ach.coinsReward
                )
            )
            enqueueSyncEvent("ACHIEVEMENT_UNLOCKED", """{"achievement_id":"$id","claimed_at":${System.currentTimeMillis()}}""")
        }
    }

    suspend fun reportQuestion(questionId: Long, reason: String, comment: String = "") = withContext(Dispatchers.IO) {
        questionDao.incrementReportCount(questionId)
        val payload = """
            {
                "question_id": $questionId,
                "reason": "$reason",
                "comment": "$comment",
                "reported_at": ${System.currentTimeMillis()}
            }
        """.trimIndent()
        enqueueSyncEvent("QUESTION_REPORTED", payload)
    }

    private suspend fun enqueueSyncEvent(type: String, payloadJson: String) {
        syncQueueDao.enqueue(
            SyncQueueEntity(
                eventUuid = UUID.randomUUID().toString(),
                eventType = type,
                payloadJson = payloadJson,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    private fun isConsecutiveDay(prevDateStr: String, todayStr: String): Boolean {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val prev = format.parse(prevDateStr) ?: return false
            val today = format.parse(todayStr) ?: return false
            val diffMs = today.time - prev.time
            val diffDays = diffMs / (1000 * 60 * 60 * 24)
            diffDays == 1L
        } catch (e: Exception) {
            false
        }
    }
}
