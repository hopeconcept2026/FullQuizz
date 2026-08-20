package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val playerId: String, // e.g. "QZ-8F42K7"
    val deviceUuid: String,
    val nickname: String = "Joueur FULLQUIZZ",
    val avatarId: String = "avatar_1",
    val level: Int = 1,
    val xp: Int = 0,
    val coins: Int = 100, // starting bonus coins
    val lives: Int = 5,
    val maxLives: Int = 5,
    val lastLifeRegenTimestamp: Long = System.currentTimeMillis(),
    val streakCount: Int = 1,
    val lastPlayedDate: String = "", // "YYYY-MM-DD"
    val totalGamesPlayed: Int = 0,
    val totalCorrectAnswers: Int = 0,
    val totalQuestionsAnswered: Int = 0,
    val bestStreak: Int = 0,
    val isGuest: Boolean = true,
    val accountEmail: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
