package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventUuid: String, // UUID for idempotent processing
    val eventType: String, // QUIZ_COMPLETED, ACHIEVEMENT_UNLOCKED, STREAK_UPDATED, QUESTION_REPORTED, PROFILE_UPDATED
    val payloadJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val attempts: Int = 0,
    val status: String = "PENDING", // PENDING, PROCESSING, COMPLETED, FAILED
    val lastError: String? = null
)
