package com.example.core.firebase

import android.util.Log
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.QuestionDao
import com.example.data.local.entity.PlayerProfileEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.remote.CloudQuestionPack
import com.example.data.remote.MatchStatus
import com.example.data.remote.OnlineLeaderboardEntry
import com.example.data.remote.OnlineMatchRoom
import com.example.data.remote.OnlinePlayer
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Cloud Firestore Service managing Real-time Multiplayer Duels,
 * Online Leaderboards, and Question/Level Sync.
 */
class FirebaseFirestoreService(
    private val questionDao: QuestionDao,
    private val categoryDao: CategoryDao
) {
    private val TAG = "FirebaseFirestoreService"
    private val db: FirebaseFirestore? get() = FirebaseManager.getFirestore()

    /**
     * Syncs local player profile to Firestore 'users' collection.
     */
    suspend fun syncPlayerProfileToCloud(profile: PlayerProfileEntity): Boolean = withContext(Dispatchers.IO) {
        val firestore = db ?: return@withContext false
        val currentUser = FirebaseManager.currentUser.value
        val userId = currentUser?.uid ?: "local_player_${profile.id}"

        try {
            val userMap = hashMapOf(
                "uid" to userId,
                "playerId" to profile.playerId,
                "nickname" to profile.nickname,
                "avatarId" to profile.avatarId,
                "coins" to profile.coins,
                "level" to profile.level,
                "xp" to profile.xp,
                "totalGamesPlayed" to profile.totalGamesPlayed,
                "totalCorrectAnswers" to profile.totalCorrectAnswers,
                "totalQuestionsAnswered" to profile.totalQuestionsAnswered,
                "bestStreak" to profile.bestStreak,
                "isGuest" to profile.isGuest,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            firestore.collection("users").document(userId).set(userMap, SetOptions.merge()).await()

            // Also update global leaderboard entry
            val leaderboardEntry = hashMapOf(
                "userId" to userId,
                "nickname" to profile.nickname,
                "avatarId" to profile.avatarId,
                "score" to (profile.xp * 10 + profile.totalCorrectAnswers * 50),
                "level" to profile.level,
                "country" to "RDC / Afrique",
                "isOnline" to true,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("leaderboard").document(userId).set(leaderboardEntry, SetOptions.merge()).await()
            Log.i(TAG, "Profile successfully synced to Cloud Firestore for $userId")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync profile to cloud: ${e.message}")
            false
        }
    }

    /**
     * Fetches top global players from Firestore 'leaderboard'.
     */
    suspend fun fetchGlobalLeaderboard(): List<OnlineLeaderboardEntry> = withContext(Dispatchers.IO) {
        val firestore = db ?: return@withContext emptyList()
        try {
            val snapshot = firestore.collection("leaderboard")
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(25)
                .get()
                .await()

            var rank = 1
            snapshot.documents.mapNotNull { doc ->
                val nickname = doc.getString("nickname") ?: return@mapNotNull null
                val avatarId = doc.getString("avatarId") ?: "avatar_1"
                val score = doc.getLong("score")?.toInt() ?: 0
                val level = doc.getLong("level")?.toInt() ?: 1
                val country = doc.getString("country") ?: "RDC"
                val isOnline = doc.getBoolean("isOnline") ?: true

                OnlineLeaderboardEntry(
                    rank = rank++,
                    nickname = nickname,
                    avatarId = avatarId,
                    country = country,
                    score = score,
                    level = level,
                    isOnline = isOnline
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not fetch global leaderboard from Firestore: ${e.message}")
            emptyList()
        }
    }

    /**
     * Publishes a custom user question to Cloud Firestore so other players can play it.
     */
    suspend fun publishQuestionToCloud(question: QuestionEntity): Boolean = withContext(Dispatchers.IO) {
        val firestore = db ?: return@withContext false
        try {
            val qMap = hashMapOf(
                "categoryId" to question.categoryId,
                "subcategory" to question.subcategory,
                "question" to question.question,
                "optionA" to question.optionA,
                "optionB" to question.optionB,
                "optionC" to question.optionC,
                "optionD" to question.optionD,
                "correctAnswer" to question.correctAnswer,
                "explanation" to question.explanation,
                "reference" to (question.reference ?: ""),
                "difficulty" to question.difficulty,
                "author" to (FirebaseManager.currentUser.value?.displayName ?: "Anonyme"),
                "createdAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("cloud_questions").add(qMap).await()
            Log.i(TAG, "Question published to cloud successfully.")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Error publishing question: ${e.message}")
            false
        }
    }

    /**
     * Real-time Multiplayer: Creates a new duel match room in Firestore.
     */
    suspend fun createMultiplayerRoom(
        roomCode: String,
        hostPlayer: OnlinePlayer,
        categoryId: String,
        categoryName: String
    ): OnlineMatchRoom = withContext(Dispatchers.IO) {
        val firestore = db
        val questions = questionDao.getRandomQuestionsByCategory(categoryId, 5).ifEmpty {
            questionDao.getRandomQuestionsAll(5)
        }

        val room = OnlineMatchRoom(
            roomId = roomCode,
            categoryId = categoryId,
            categoryName = categoryName,
            host = hostPlayer,
            status = MatchStatus.WAITING_FOR_OPPONENT,
            questions = questions
        )

        if (firestore != null) {
            try {
                val roomMap = hashMapOf(
                    "roomId" to roomCode,
                    "categoryId" to categoryId,
                    "categoryName" to categoryName,
                    "status" to MatchStatus.WAITING_FOR_OPPONENT.name,
                    "hostId" to hostPlayer.id,
                    "hostNickname" to hostPlayer.nickname,
                    "hostAvatar" to hostPlayer.avatarId,
                    "hostLevel" to hostPlayer.level,
                    "hostScore" to 0,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                firestore.collection("multiplayer_rooms").document(roomCode).set(roomMap).await()
                Log.i(TAG, "Firestore Room $roomCode created.")
            } catch (e: Exception) {
                Log.w(TAG, "Firestore room creation error (fallback to local): ${e.message}")
            }
        }
        room
    }

    /**
     * Real-time Multiplayer: Joins an existing duel match room in Firestore.
     */
    suspend fun joinMultiplayerRoom(
        roomCode: String,
        guestPlayer: OnlinePlayer
    ): OnlineMatchRoom? = withContext(Dispatchers.IO) {
        val firestore = db ?: return@withContext null
        try {
            val docRef = firestore.collection("multiplayer_rooms").document(roomCode)
            val snapshot = docRef.get().await()

            if (!snapshot.exists()) return@withContext null

            val hostId = snapshot.getString("hostId") ?: "host_1"
            val hostNickname = snapshot.getString("hostNickname") ?: "Hôte"
            val hostAvatar = snapshot.getString("hostAvatar") ?: "avatar_1"
            val hostLevel = snapshot.getLong("hostLevel")?.toInt() ?: 1
            val categoryId = snapshot.getString("categoryId") ?: "all"
            val categoryName = snapshot.getString("categoryName") ?: "Duel Général"

            val updateMap = hashMapOf<String, Any>(
                "guestId" to guestPlayer.id,
                "guestNickname" to guestPlayer.nickname,
                "guestAvatar" to guestPlayer.avatarId,
                "guestLevel" to guestPlayer.level,
                "guestScore" to 0,
                "status" to MatchStatus.OPPONENT_FOUND.name,
                "startedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(updateMap).await()

            val questions = questionDao.getRandomQuestionsByCategory(categoryId, 5).ifEmpty {
                questionDao.getRandomQuestionsAll(5)
            }

            OnlineMatchRoom(
                roomId = roomCode,
                categoryId = categoryId,
                categoryName = categoryName,
                host = OnlinePlayer(id = hostId, nickname = hostNickname, avatarId = hostAvatar, level = hostLevel),
                guest = guestPlayer,
                status = MatchStatus.OPPONENT_FOUND,
                questions = questions
            )
        } catch (e: Exception) {
            Log.w(TAG, "Join room error: ${e.message}")
            null
        }
    }

    /**
     * Observes real-time updates for a multiplayer room via Firestore snapshot listener.
     */
    fun observeRoomUpdates(roomId: String): Flow<OnlineMatchRoom?> = callbackFlow {
        val firestore = db
        if (firestore == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val registration: ListenerRegistration = firestore.collection("multiplayer_rooms")
            .document(roomId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Room listener error: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val statusStr = snapshot.getString("status") ?: MatchStatus.WAITING_FOR_OPPONENT.name
                    val status = try { MatchStatus.valueOf(statusStr) } catch (_: Exception) { MatchStatus.WAITING_FOR_OPPONENT }
                    val categoryId = snapshot.getString("categoryId") ?: "all"
                    val categoryName = snapshot.getString("categoryName") ?: "Duel"

                    val host = OnlinePlayer(
                        id = snapshot.getString("hostId") ?: "",
                        nickname = snapshot.getString("hostNickname") ?: "Hôte",
                        avatarId = snapshot.getString("hostAvatar") ?: "avatar_1",
                        level = snapshot.getLong("hostLevel")?.toInt() ?: 1,
                        score = snapshot.getLong("hostScore")?.toInt() ?: 0
                    )

                    val guestNickname = snapshot.getString("guestNickname")
                    val guest = if (guestNickname != null) {
                        OnlinePlayer(
                            id = snapshot.getString("guestId") ?: "",
                            nickname = guestNickname,
                            avatarId = snapshot.getString("guestAvatar") ?: "avatar_2",
                            level = snapshot.getLong("guestLevel")?.toInt() ?: 1,
                            score = snapshot.getLong("guestScore")?.toInt() ?: 0
                        )
                    } else null

                    val room = OnlineMatchRoom(
                        roomId = roomId,
                        categoryId = categoryId,
                        categoryName = categoryName,
                        host = host,
                        guest = guest,
                        status = status
                    )
                    trySend(room)
                }
            }

        awaitClose { registration.remove() }
    }
}
