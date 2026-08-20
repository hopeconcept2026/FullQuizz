package com.example.data.remote

import com.example.data.local.entity.QuestionEntity

/**
 * Data models for Online Multiplayer lobbies, Duels, and Cloud Question Packs.
 */

enum class MatchStatus {
    WAITING_FOR_OPPONENT,
    OPPONENT_FOUND,
    IN_PROGRESS,
    FINISHED
}

data class OnlinePlayer(
    val id: String,
    val nickname: String,
    val avatarId: String = "avatar_1",
    val level: Int = 1,
    val score: Int = 0,
    val currentQuestionIndex: Int = 0,
    val isReady: Boolean = false
)

data class OnlineMatchRoom(
    val roomId: String,
    val categoryId: String,
    val categoryName: String,
    val host: OnlinePlayer,
    val guest: OnlinePlayer? = null,
    val status: MatchStatus = MatchStatus.WAITING_FOR_OPPONENT,
    val questions: List<QuestionEntity> = emptyList(),
    val winnerNickname: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class CloudQuestionPack(
    val packId: String,
    val title: String,
    val categoryId: String,
    val description: String,
    val questionCount: Int,
    val version: Int = 1,
    val isInstalled: Boolean = false,
    val questions: List<QuestionEntity> = emptyList()
)

data class OnlineLeaderboardEntry(
    val rank: Int,
    val nickname: String,
    val avatarId: String,
    val country: String,
    val score: Int,
    val level: Int,
    val isOnline: Boolean = false
)
