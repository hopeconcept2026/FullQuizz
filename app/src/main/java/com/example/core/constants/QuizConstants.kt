package com.example.core.constants

object QuizConstants {
    const val MAX_LIVES = 5
    const val LIFE_REGEN_INTERVAL_MINUTES = 15L
    const val LIFE_REGEN_INTERVAL_MS = LIFE_REGEN_INTERVAL_MINUTES * 60 * 1000L

    const val BASE_XP_PER_CORRECT = 10
    const val BASE_COINS_PER_CORRECT = 5

    const val COST_HINT_5050 = 15
    const val COST_HINT_SKIP = 20
    const val COST_EXTRA_LIFE = 50

    const val REWARD_WATCH_AD_COINS = 30
    const val REWARD_WATCH_AD_LIFE = 1
    const val REWARD_WATCH_AD_BONUS_POINTS = 50
    const val REWARD_WATCH_AD_XP = 75

    const val SECONDS_PER_QUESTION = 20
    const val CHRONO_MODE_SECONDS = 60
    const val STANDARD_QUIZ_QUESTIONS_COUNT = 10

    // AdMob Test IDs (Official Google Test Ad Unit IDs)
    const val ADMOB_TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val ADMOB_TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val ADMOB_TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val ADMOB_TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    // Interstitial Ad frequency limiting
    const val MIN_SECONDS_BETWEEN_INTERSTITIALS = 120L // 2 minutes
    const val MIN_QUIZZES_BETWEEN_INTERSTITIALS = 2

    // Level progression helper
    fun getLevelTitle(level: Int): String {
        return when {
            level < 5 -> "Débutant"
            level < 10 -> "Curieux"
            level < 20 -> "Connaisseur"
            level < 50 -> "Expert"
            level < 100 -> "Maître"
            else -> "Légende"
        }
    }

    fun getXpRequiredForLevel(level: Int): Int {
        return (level * 100) + ((level - 1) * (level - 1) * 35)
    }

    fun getComboMultiplier(streak: Int): Int {
        return when {
            streak >= 10 -> 5
            streak >= 5 -> 3
            streak >= 3 -> 2
            else -> 1
        }
    }
}
