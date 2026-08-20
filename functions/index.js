/**
 * FullQuizz - Firebase Cloud Functions Backend (Node.js)
 * Manages multiplayer game rooms, real-time score synchronization,
 * server-side answer validation, and leaderboard updates.
 */

const { onRequest, onCall, HttpsError } = require("firebase-functions/v2/https");
const { onDocumentUpdated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

// Helper to generate a random 6-character room PIN code
function generateRoomCode(length = 6) {
  const chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  let code = "";
  for (let i = 0; i < length; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return code;
}

/**
 * 1. Callable Function: createGameRoom
 * Creates a multiplayer room with selected category, fetches questions,
 * and sets up the room in Firestore.
 */
exports.createGameRoom = onCall(
  { cors: true },
  async (request) => {
    const { categoryId = "all", hostPlayerName = "Joueur 1", hostPlayerAvatar = "avatar_1", questionsCount = 10 } = request.data || {};
    const authUid = request.auth ? request.auth.uid : `guest_${Date.now()}`;

    try {
      const roomCode = generateRoomCode(6);
      const roomRef = db.collection("game_rooms").doc(roomCode);

      // Fetch randomized questions from Firestore "questions" collection
      let questionsQuery = db.collection("questions");
      if (categoryId && categoryId !== "all") {
        questionsQuery = questionsQuery.where("categoryId", "==", categoryId);
      }

      const questionsSnapshot = await questionsQuery.limit(50).get();
      let questionsList = [];

      if (!questionsSnapshot.empty) {
        questionsSnapshot.forEach((doc) => {
          const qData = doc.data();
          questionsList.push({
            id: doc.id,
            prompt: qData.prompt || qData.questionText,
            options: [
              qData.optionA || (qData.options && qData.options[0]),
              qData.optionB || (qData.options && qData.options[1]),
              qData.optionC || (qData.options && qData.options[2]),
              qData.optionD || (qData.options && qData.options[3]),
            ].filter(Boolean),
            correctIndex: qData.correctAnswerIndex ?? qData.correctIndex ?? 0,
            explanation: qData.explanation || "",
            difficulty: qData.difficulty || "MEDIUM",
            timeLimitSeconds: qData.timeLimitSeconds || 15,
          });
        });

        // Shuffle questions
        questionsList = questionsList.sort(() => 0.5 - Math.random()).slice(0, questionsCount);
      } else {
        // Fallback sample questions if database is empty
        questionsList = [
          {
            id: "sample_q1",
            prompt: "Quelle est la capitale historique du Royaume du Kongo ?",
            options: ["Mbanza-Kongo", "Nairobi", "Dakar", "Tombouctou"],
            correctIndex: 0,
            explanation: "Mbanza-Kongo était la capitale politique et spirituelle du Royaume du Kongo.",
            difficulty: "MEDIUM",
            timeLimitSeconds: 15
          },
          {
            id: "sample_q2",
            prompt: "Combien de livres composent le Nouveau Testament biblique ?",
            options: ["27 livres", "39 livres", "66 livres", "12 livres"],
            correctIndex: 0,
            explanation: "Le Nouveau Testament compte 27 livres.",
            difficulty: "EASY",
            timeLimitSeconds: 15
          }
        ];
      }

      // Prepare Room Object
      const newRoom = {
        roomCode: roomCode,
        categoryId: categoryId,
        status: "WAITING", // WAITING, PLAYING, FINISHED, CANCELLED
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        startedAt: null,
        currentQuestionIndex: 0,
        totalQuestions: questionsList.length,
        questions: questionsList,
        players: {
          [authUid]: {
            uid: authUid,
            name: hostPlayerName,
            avatar: hostPlayerAvatar,
            isHost: true,
            score: 0,
            correctCount: 0,
            combo: 0,
            currentAnswerIndex: null,
            finished: false,
            joinedAt: Date.now()
          }
        }
      };

      await roomRef.set(newRoom);

      return {
        success: true,
        roomCode: roomCode,
        totalQuestions: questionsList.length,
        status: "WAITING"
      };
    } catch (error) {
      console.error("Error creating game room:", error);
      throw new HttpsError("internal", "Impossible de créer la salle de jeu: " + error.message);
    }
  }
);

/**
 * 2. Callable Function: joinGameRoom
 * Joins an existing game room using the 6-character room PIN.
 */
exports.joinGameRoom = onCall(
  { cors: true },
  async (request) => {
    const { roomCode, playerName = "Joueur 2", playerAvatar = "avatar_2" } = request.data || {};
    const authUid = request.auth ? request.auth.uid : `guest_${Date.now()}`;

    if (!roomCode) {
      throw new HttpsError("invalid-argument", "Le code de la salle est obligatoire.");
    }

    const cleanCode = roomCode.trim().toUpperCase();
    const roomRef = db.collection("game_rooms").doc(cleanCode);

    try {
      const result = await db.runTransaction(async (transaction) => {
        const roomDoc = await transaction.get(roomRef);

        if (!roomDoc.exists) {
          throw new HttpsError("not-found", "Salle introuvable avec le code: " + cleanCode);
        }

        const roomData = roomDoc.data();

        if (roomData.status === "FINISHED" || roomData.status === "CANCELLED") {
          throw new HttpsError("failed-precondition", "Cette partie est déjà terminée.");
        }

        const currentPlayers = roomData.players || {};
        const playerKeys = Object.keys(currentPlayers);

        // Allow reconnect if player is already inside
        if (!currentPlayers[authUid] && playerKeys.length >= 2) {
          throw new HttpsError("resource-exhausted", "Cette salle est déjà complète (2/2 joueurs).");
        }

        // Add player 2
        currentPlayers[authUid] = {
          uid: authUid,
          name: playerName,
          avatar: playerAvatar,
          isHost: false,
          score: 0,
          correctCount: 0,
          combo: 0,
          currentAnswerIndex: null,
          finished: false,
          joinedAt: Date.now()
        };

        // Transition room to PLAYING when 2 players are present
        const updates = {
          players: currentPlayers,
          status: "PLAYING",
          startedAt: admin.firestore.FieldValue.serverTimestamp()
        };

        transaction.update(roomRef, updates);

        return {
          success: true,
          roomCode: cleanCode,
          status: "PLAYING",
          totalQuestions: roomData.totalQuestions || (roomData.questions ? roomData.questions.length : 0),
          questions: roomData.questions || []
        };
      });

      return result;
    } catch (error) {
      console.error("Error joining game room:", error);
      if (error instanceof HttpsError) throw error;
      throw new HttpsError("internal", error.message);
    }
  }
);

/**
 * 3. Callable Function: submitAnswer
 * Validates player answer server-side, calculates points + combo bonus,
 * and updates Firestore in real-time.
 */
exports.submitAnswer = onCall(
  { cors: true },
  async (request) => {
    const { roomCode, questionIndex, selectedOptionIndex, timeRemainingSeconds = 10 } = request.data || {};
    const authUid = request.auth ? request.auth.uid : request.data.userId;

    if (!roomCode || questionIndex === undefined || selectedOptionIndex === undefined) {
      throw new HttpsError("invalid-argument", "Paramètres manquants pour soumettre la réponse.");
    }

    const cleanCode = roomCode.trim().toUpperCase();
    const roomRef = db.collection("game_rooms").doc(cleanCode);

    try {
      const outcome = await db.runTransaction(async (transaction) => {
        const roomDoc = await transaction.get(roomRef);
        if (!roomDoc.exists) {
          throw new HttpsError("not-found", "Salle introuvable.");
        }

        const room = roomDoc.data();
        const question = room.questions && room.questions[questionIndex];

        if (!question) {
          throw new HttpsError("invalid-argument", "Question introuvable à l'index: " + questionIndex);
        }

        const isCorrect = (selectedOptionIndex === question.correctIndex);
        const player = (room.players && room.players[authUid]) || {
          uid: authUid,
          name: "Joueur",
          score: 0,
          correctCount: 0,
          combo: 0
        };

        // Score Calculation: Base points (100) + speed bonus (up to 50) + combo multiplier
        let pointsEarned = 0;
        let newCombo = 0;

        if (isCorrect) {
          newCombo = (player.combo || 0) + 1;
          const speedBonus = Math.max(0, Math.min(50, Math.round(timeRemainingSeconds * 3.5)));
          const comboMultiplier = newCombo >= 5 ? 2.0 : newCombo >= 3 ? 1.5 : 1.0;
          pointsEarned = Math.round((100 + speedBonus) * comboMultiplier);
        } else {
          newCombo = 0;
        }

        const updatedPlayer = {
          ...player,
          score: (player.score || 0) + pointsEarned,
          correctCount: (player.correctCount || 0) + (isCorrect ? 1 : 0),
          combo: newCombo,
          currentAnswerIndex: selectedOptionIndex,
          lastAnswerCorrect: isCorrect,
          lastAnswerTimestamp: Date.now(),
          finished: (questionIndex >= (room.totalQuestions - 1))
        };

        // Update player data in room
        const updatedPlayers = {
          ...room.players,
          [authUid]: updatedPlayer
        };

        // Check if all players have completed all questions
        const allFinished = Object.values(updatedPlayers).every(p => p.finished);
        const newStatus = allFinished ? "FINISHED" : room.status;

        transaction.update(roomRef, {
          players: updatedPlayers,
          status: newStatus,
          lastActivity: admin.firestore.FieldValue.serverTimestamp()
        });

        return {
          isCorrect: isCorrect,
          correctAnswerIndex: question.correctIndex,
          pointsEarned: pointsEarned,
          newTotalScore: updatedPlayer.score,
          combo: newCombo,
          explanation: question.explanation || "",
          gameFinished: allFinished
        };
      });

      return outcome;
    } catch (error) {
      console.error("Error submitting answer:", error);
      if (error instanceof HttpsError) throw error;
      throw new HttpsError("internal", error.message);
    }
  }
);

/**
 * 4. Firestore Trigger: onRoomUpdated
 * Triggered whenever a game room document changes.
 * Automatically archives scores to Leaderboard when status transitions to "FINISHED".
 */
exports.onRoomUpdated = onDocumentUpdated(
  "game_rooms/{roomCode}",
  async (event) => {
    const beforeData = event.data.before.data();
    const afterData = event.data.after.data();

    // Trigger only on transition to FINISHED
    if (beforeData.status !== "FINISHED" && afterData.status === "FINISHED") {
      const players = Object.values(afterData.players || {});
      const batch = db.batch();

      // Update global leaderboard and match logs for each player
      for (const p of players) {
        if (p.uid && !p.uid.startsWith("guest_")) {
          const userRef = db.collection("players").doc(p.uid);
          batch.set(
            userRef,
            {
              xp: admin.firestore.FieldValue.increment(p.score || 0),
              totalGamesPlayed: admin.firestore.FieldValue.increment(1),
              totalCorrectAnswers: admin.firestore.FieldValue.increment(p.correctCount || 0),
              lastMatchTimestamp: admin.firestore.FieldValue.serverTimestamp()
            },
            { merge: true }
          );
        }
      }

      await batch.commit();
      console.log(`Game room ${event.params.roomCode} finished. Leaderboard updated successfully.`);
    }
  }
);

/**
 * 5. HTTP Endpoint: Health Check
 */
exports.healthCheck = onRequest({ cors: true }, (req, res) => {
  res.status(200).json({
    status: "ok",
    service: "FullQuizz Multiplayer Functions",
    timestamp: new Date().toISOString()
  });
});
