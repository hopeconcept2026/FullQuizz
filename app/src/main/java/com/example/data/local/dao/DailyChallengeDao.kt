package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.DailyChallengeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyChallengeDao {
    @Query("SELECT * FROM daily_challenges WHERE dateString = :dateString LIMIT 1")
    fun getDailyChallengeFlow(dateString: String): Flow<DailyChallengeEntity?>

    @Query("SELECT * FROM daily_challenges WHERE dateString = :dateString LIMIT 1")
    suspend fun getDailyChallenge(dateString: String): DailyChallengeEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(challenge: DailyChallengeEntity)

    @Update
    suspend fun update(challenge: DailyChallengeEntity)

    @Query("UPDATE daily_challenges SET isCompleted = 1, score = :score, completedAt = :timestamp WHERE dateString = :dateString")
    suspend fun completeChallenge(dateString: String, score: Int, timestamp: Long)
}
