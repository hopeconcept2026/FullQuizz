package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AchievementDao
import com.example.data.local.dao.AppSettingsDao
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.DailyChallengeDao
import com.example.data.local.dao.PlayerDao
import com.example.data.local.dao.QuestionDao
import com.example.data.local.dao.QuizSessionDao
import com.example.data.local.dao.SyncQueueDao
import com.example.data.local.entity.AchievementEntity
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.DailyChallengeEntity
import com.example.data.local.entity.PlayerProfileEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.local.entity.QuizSessionEntity
import com.example.data.local.entity.SyncQueueEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

@Database(
    entities = [
        PlayerProfileEntity::class,
        CategoryEntity::class,
        QuestionEntity::class,
        QuizSessionEntity::class,
        AchievementEntity::class,
        DailyChallengeEntity::class,
        SyncQueueEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun categoryDao(): CategoryDao
    abstract fun questionDao(): QuestionDao
    abstract fun quizSessionDao(): QuizSessionDao
    abstract fun achievementDao(): AchievementDao
    abstract fun dailyChallengeDao(): DailyChallengeDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fullquizz_database.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun generatePlayerId(): String {
            val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
            val randomSuffix = (1..6).map { chars[Random.nextInt(chars.length)] }.joinToString("")
            return "QZ-$randomSuffix"
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    database.categoryDao().insertAll(SeedData.categories)
                    database.questionDao().insertAll(SeedData.questions)
                    database.categoryDao().refreshQuestionCounts()
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val playerDao = database.playerDao()
            val categoryDao = database.categoryDao()
            val questionDao = database.questionDao()
            val achievementDao = database.achievementDao()

            // 1. Initialize anonymous local player identity
            val defaultPlayer = PlayerProfileEntity(
                id = 1,
                playerId = generatePlayerId(),
                deviceUuid = UUID.randomUUID().toString(),
                nickname = "Joueur FULLQUIZZ",
                avatarId = "avatar_1",
                level = 1,
                xp = 0,
                coins = 100, // Welcome gift
                lives = 5,
                maxLives = 5,
                lastLifeRegenTimestamp = System.currentTimeMillis(),
                streakCount = 1,
                lastPlayedDate = "",
                totalGamesPlayed = 0,
                totalCorrectAnswers = 0,
                totalQuestionsAnswered = 0,
                bestStreak = 0,
                isGuest = true
            )
            playerDao.insertOrUpdate(defaultPlayer)

            // 2. Populate Categories
            categoryDao.insertAll(SeedData.categories)

            // 3. Populate Achievements
            achievementDao.insertAll(SeedData.achievements)

            // 4. Populate Questions
            questionDao.insertAll(SeedData.questions)

            // Refresh category counts
            categoryDao.refreshQuestionCounts()
        }
    }
}
