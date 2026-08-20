package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.PlayerProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    fun getPlayerProfileFlow(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    suspend fun getPlayerProfile(): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(player: PlayerProfileEntity)

    @Update
    suspend fun update(player: PlayerProfileEntity)

    @Query("UPDATE player_profile SET coins = coins + :amount WHERE id = 1")
    suspend fun addCoins(amount: Int)

    @Query("UPDATE player_profile SET lives = :lives, lastLifeRegenTimestamp = :timestamp WHERE id = 1")
    suspend fun updateLives(lives: Int, timestamp: Long)

    @Query("UPDATE player_profile SET nickname = :nickname, avatarId = :avatarId WHERE id = 1")
    suspend fun updateProfileCustomization(nickname: String, avatarId: String)

    @Query("UPDATE player_profile SET xp = :newXp, level = :newLevel WHERE id = 1")
    suspend fun updateLevelAndXp(newLevel: Int, newXp: Int)
}
