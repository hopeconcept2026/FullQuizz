package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE categoryId = :categoryId ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuestionsByCategory(categoryId: String, limit: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuestionsAll(limit: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE id IN (:ids)")
    suspend fun getQuestionsByIds(ids: List<Long>): List<QuestionEntity>

    @Query("SELECT COUNT(*) FROM questions")
    fun getTotalQuestionCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getTotalQuestionCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuestionEntity>)

    @Query("UPDATE questions SET reportedCount = reportedCount + 1 WHERE id = :questionId")
    suspend fun incrementReportCount(questionId: Long)

    @Query("UPDATE questions SET timesAsked = timesAsked + 1, timesCorrect = timesCorrect + :wasCorrect WHERE id = :questionId")
    suspend fun updateQuestionStats(questionId: Long, wasCorrect: Int)
}
