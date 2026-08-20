package com.example.core.sharing

import android.content.Context
import android.content.Intent

object ShareHelper {
    fun shareQuizResult(
        context: Context,
        categoryName: String,
        score: Int,
        totalQuestions: Int,
        xpEarned: Int,
        playerLevel: Int,
        playerTitle: String
    ) {
        val percentage = (score * 100) / (if (totalQuestions > 0) totalQuestions else 1)
        val emoji = when {
            percentage == 100 -> "🏆👑"
            percentage >= 80 -> "🔥🎯"
            percentage >= 50 -> "⚡👏"
            else -> "💪📚"
        }

        val shareMessage = """
            $emoji FULLQUIZZ — Quiz Culture & Savoir

            J'ai obtenu $score / $totalQuestions ($percentage%) dans la catégorie « $categoryName » !
            Niveau $playerLevel ($playerTitle) • +$xpEarned XP

            Peux-tu faire mieux ? Teste tes connaissances sur FULLQUIZZ !
            #FULLQUIZZ #Quiz #CultureGenerale #Afrique #Bible #RDC
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Mon score sur FULLQUIZZ !")
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val chooser = Intent.createChooser(intent, "Partager mon score").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(chooser)
    }
}
