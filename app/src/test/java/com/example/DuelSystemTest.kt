package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.entity.QuestionEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DuelSystemTest {

    private fun questionToJson(q: QuestionEntity): JSONObject {
        return JSONObject().apply {
            put("id", q.id)
            put("catId", q.categoryId)
            put("subcat", q.subcategory)
            put("question", q.question)
            put("optA", q.optionA)
            put("optB", q.optionB)
            put("optC", q.optionC)
            put("optD", q.optionD)
            put("correct", q.correctAnswer)
            put("explanation", q.explanation)
            put("diff", q.difficulty)
        }
    }

    private fun jsonToQuestion(json: JSONObject): QuestionEntity {
        return QuestionEntity(
            id = json.optLong("id", 0L),
            categoryId = json.optString("catId", "all"),
            subcategory = json.optString("subcat", "Général"),
            question = json.optString("question", ""),
            optionA = json.optString("optA", ""),
            optionB = json.optString("optB", ""),
            optionC = json.optString("optC", ""),
            optionD = json.optString("optD", ""),
            correctAnswer = json.optString("correct", "A"),
            explanation = json.optString("explanation", ""),
            difficulty = json.optString("diff", "medium")
        )
    }

    @Test
    fun `test question serialization and deserialization for Bluetooth Duel`() {
        val originalQuestion = QuestionEntity(
            id = 42L,
            categoryId = "science",
            subcategory = "Physique",
            question = "Quelle est l'unité de mesure de la force ?",
            optionA = "Newton",
            optionB = "Joule",
            optionC = "Pascal",
            optionD = "Watt",
            correctAnswer = "A",
            explanation = "Le newton (N) est l'unité de force du Système international.",
            difficulty = "medium"
        )

        val json = questionToJson(originalQuestion)
        val deserialized = jsonToQuestion(json)

        assertEquals(originalQuestion.id, deserialized.id)
        assertEquals(originalQuestion.categoryId, deserialized.categoryId)
        assertEquals(originalQuestion.question, deserialized.question)
        assertEquals(originalQuestion.optionA, deserialized.optionA)
        assertEquals(originalQuestion.optionB, deserialized.optionB)
        assertEquals(originalQuestion.optionC, deserialized.optionC)
        assertEquals(originalQuestion.optionD, deserialized.optionD)
        assertEquals(originalQuestion.correctAnswer, deserialized.correctAnswer)
        assertEquals(originalQuestion.explanation, deserialized.explanation)
    }

    @Test
    fun `test START_DUEL payload packet integrity`() {
        val questionsList = listOf(
            QuestionEntity(
                id = 1L,
                categoryId = "hist",
                subcategory = "Afrique",
                question = "Qui était l'empereur du Mali au 14e siècle ?",
                optionA = "Kankan Moussa",
                optionB = "Soundiata Keïta",
                optionC = "Samory Touré",
                optionD = "Béhanzin",
                correctAnswer = "A",
                explanation = "Mansa Moussa était empereur.",
                difficulty = "easy"
            ),
            QuestionEntity(
                id = 2L,
                categoryId = "geo",
                subcategory = "Fleuves",
                question = "Quel est le plus long fleuve d'Afrique ?",
                optionA = "Le Nil",
                optionB = "Le Congo",
                optionC = "Le Niger",
                optionD = "Le Zambèze",
                correctAnswer = "A",
                explanation = "Le Nil mesure plus de 6600 km.",
                difficulty = "easy"
            )
        )

        val jsonArray = JSONArray()
        for (q in questionsList) {
            jsonArray.put(questionToJson(q))
        }

        val startPayload = JSONObject().apply {
            put("action", "START_DUEL")
            put("categoryTitle", "Duel 1v1 Bluetooth")
            put("hostNickname", "Champion_99")
            put("questions", jsonArray)
        }

        val serializedString = startPayload.toString()
        assertNotNull(serializedString)

        // Parse on client receiver side
        val parsedJson = JSONObject(serializedString)
        assertEquals("START_DUEL", parsedJson.optString("action"))
        assertEquals("Duel 1v1 Bluetooth", parsedJson.optString("categoryTitle"))
        assertEquals("Champion_99", parsedJson.optString("hostNickname"))

        val receivedQuestionsArray = parsedJson.optJSONArray("questions")
        assertNotNull(receivedQuestionsArray)
        assertEquals(2, receivedQuestionsArray!!.length())

        val q1 = jsonToQuestion(receivedQuestionsArray.getJSONObject(0))
        val q2 = jsonToQuestion(receivedQuestionsArray.getJSONObject(1))

        assertEquals("Qui était l'empereur du Mali au 14e siècle ?", q1.question)
        assertEquals("Le Nil", q2.optionA)
    }

    @Test
    fun `test PROGRESS live broadcast update packet`() {
        val progressPayload = JSONObject().apply {
            put("action", "PROGRESS")
            put("score", 150)
            put("qIndex", 3)
            put("isCorrect", true)
            put("combo", 3)
            put("isFinished", false)
        }

        val jsonStr = progressPayload.toString()
        val received = JSONObject(jsonStr)

        assertEquals("PROGRESS", received.optString("action"))
        assertEquals(150, received.optInt("score"))
        assertEquals(3, received.optInt("qIndex"))
        assertTrue(received.optBoolean("isCorrect"))
        assertEquals(3, received.optInt("combo"))
        assertFalse(received.optBoolean("isFinished"))
    }

    @Test
    fun `test duel winner calculation logic`() {
        // Case 1: Victory
        val myScore1 = 200
        val oppScore1 = 150
        val outcome1 = when {
            myScore1 > oppScore1 -> "VICTORY"
            myScore1 < oppScore1 -> "DEFEAT"
            else -> "DRAW"
        }
        assertEquals("VICTORY", outcome1)

        // Case 2: Defeat
        val myScore2 = 80
        val oppScore2 = 120
        val outcome2 = when {
            myScore2 > oppScore2 -> "VICTORY"
            myScore2 < oppScore2 -> "DEFEAT"
            else -> "DRAW"
        }
        assertEquals("DEFEAT", outcome2)

        // Case 3: Draw
        val myScore3 = 100
        val oppScore3 = 100
        val outcome3 = when {
            myScore3 > oppScore3 -> "VICTORY"
            myScore3 < oppScore3 -> "DEFEAT"
            else -> "DRAW"
        }
        assertEquals("DRAW", outcome3)
    }
}
