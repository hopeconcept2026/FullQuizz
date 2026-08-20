package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_challenges")
data class DailyChallengeEntity(
    @PrimaryKey val dateString: String, // "YYYY-MM-DD"
    val title: String = "Défi Quotidien",
    val description: String = "10 questions variées pour tester ta culture",
    val categoryId: String = "mixed",
    val questionIdsJson: String, // CSV or JSON string of IDs
    val totalQuestions: Int = 10,
    val isCompleted: Boolean = false,
    val score: Int = 0,
    val xpBonus: Int = 50,
    val coinsBonus: Int = 30,
    val completedAt: Long? = null
)
