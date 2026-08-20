package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val group: String, // "GENERAL", "BIBLE", "AFRIQUE", "RDC", "MASTERY", "STREAK"
    val iconName: String,
    val targetValue: Int,
    val currentProgress: Int = 0,
    val xpReward: Int,
    val coinsReward: Int,
    val isUnlocked: Boolean = false,
    val isClaimed: Boolean = false,
    val unlockedAt: Long? = null
) {
    val iconEmoji: String
        get() = when (iconName) {
            "trophy" -> "🏆"
            "fire" -> "🔥"
            "bible", "book" -> "📖"
            "globe", "africa" -> "🌍"
            "flag" -> "🇨🇩"
            "star", "stars" -> "🌟"
            "lightning", "bolt" -> "⚡"
            "crown" -> "👑"
            "target" -> "🎯"
            "brain", "scholar" -> "🧠"
            else -> "🎖️"
        }
}
