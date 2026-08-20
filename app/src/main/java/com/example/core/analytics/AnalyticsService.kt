package com.example.core.analytics

import android.util.Log

/**
 * Abstract Analytics Service.
 * Allows effortless swapping or augmenting with Google Analytics / Firebase Analytics
 * or custom backend telemetry without coupling UI components.
 */
object AnalyticsService {
    private const val TAG = "FULLQUIZZAnalytics"

    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
        val paramStr = if (params.isNotEmpty()) " -> $params" else ""
        Log.d(TAG, "📊 [Event] $eventName$paramStr")
    }

    fun logQuizStarted(categoryId: String, mode: String) {
        logEvent("quiz_started", mapOf("category_id" to categoryId, "mode" to mode))
    }

    fun logQuizCompleted(categoryId: String, mode: String, score: Int, total: Int, xp: Int, coins: Int) {
        logEvent("quiz_completed", mapOf(
            "category_id" to categoryId,
            "mode" to mode,
            "score" to score,
            "total" to total,
            "xp_earned" to xp,
            "coins_earned" to coins
        ))
    }

    fun logQuestionAnswered(questionId: Long, isCorrect: Boolean, combo: Int) {
        logEvent("question_answered", mapOf(
            "question_id" to questionId,
            "is_correct" to isCorrect,
            "combo" to combo
        ))
    }

    fun logAdWatched(adType: String, rewardedType: String? = null) {
        logEvent("ad_watched", mapOf("ad_type" to adType, "reward" to (rewardedType ?: "none")))
    }

    fun logLevelUp(newLevel: Int, title: String) {
        logEvent("level_up", mapOf("new_level" to newLevel, "title" to title))
    }

    fun logAchievementUnlocked(achievementId: String) {
        logEvent("achievement_unlocked", mapOf("achievement_id" to achievementId))
    }
}
