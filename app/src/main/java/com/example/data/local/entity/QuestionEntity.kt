package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: String,
    val subcategory: String = "Général",
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String, // "A", "B", "C", or "D"
    val explanation: String,
    val difficulty: String = "medium", // "easy", "medium", "hard", "expert"
    val language: String = "fr",
    val sourceName: String? = null,
    val sourceUrl: String? = null,
    val reference: String? = null, // e.g. "Jean 3:16", "Constitution RDC art. 1"
    val book: String? = null,
    val chapter: Int? = null,
    val verse: Int? = null,
    val reportedCount: Int = 0,
    val timesAsked: Int = 0,
    val timesCorrect: Int = 0,
    val status: String = "PUBLISHED" // DRAFT, REVIEW, APPROVED, PUBLISHED, ARCHIVED
) {
    val correctOptionIndex: Int
        get() = when (correctAnswer.trim().uppercase()) {
            "A" -> 0
            "B" -> 1
            "C" -> 2
            "D" -> 3
            else -> 0
        }
}
