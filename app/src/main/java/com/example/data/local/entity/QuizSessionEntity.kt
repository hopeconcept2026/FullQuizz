package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_sessions")
data class QuizSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: String, // "STANDARD", "CHRONO", "SURVIVAL", "DAILY_CHALLENGE"
    val categoryId: String,
    val score: Int,
    val totalQuestions: Int,
    val xpEarned: Int,
    val coinsEarned: Int,
    val bestCombo: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
