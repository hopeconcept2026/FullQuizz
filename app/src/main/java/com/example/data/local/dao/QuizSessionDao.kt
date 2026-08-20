package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.QuizSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizSessionDao {
    @Query("SELECT * FROM quiz_sessions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSessionsFlow(limit: Int = 20): Flow<List<QuizSessionEntity>>

    @Query("SELECT * FROM quiz_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Long): QuizSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: QuizSessionEntity): Long

    @Query("SELECT COUNT(*) FROM quiz_sessions")
    suspend fun getTotalSessionsCount(): Int

    @Query("SELECT SUM(score) FROM quiz_sessions")
    suspend fun getTotalScoreSum(): Int?

    @Query("SELECT * FROM quiz_sessions WHERE isSynced = 0")
    suspend fun getUnsyncedSessions(): List<QuizSessionEntity>

    @Query("UPDATE quiz_sessions SET isSynced = 1 WHERE id IN (:sessionIds)")
    suspend fun markSessionsSynced(sessionIds: List<Long>)
}
