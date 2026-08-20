package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiObjects
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Forward
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.constants.QuizConstants
import com.example.data.local.entity.QuestionEntity
import com.example.ui.components.QuizBottomAdBanner
import com.example.ui.components.ReportQuestionDialog
import com.example.ui.theme.CleanMinGold
import com.example.ui.theme.CleanMinGreen
import com.example.ui.theme.CleanMinGreenBg
import com.example.ui.theme.CleanMinOutline
import com.example.ui.theme.CleanMinOutlineVariant
import com.example.ui.theme.CleanMinPrimary
import com.example.ui.theme.CleanMinRed
import com.example.ui.theme.CleanMinRedBg
import com.example.ui.theme.CleanMinSecondaryContainer

private data class OptionStyle(
    val cardBg: Color,
    val borderColor: Color,
    val textColor: Color,
    val letterBg: Color,
    val letterColor: Color
)

@Composable
fun QuizPlayScreen(
    questions: List<QuestionEntity>,
    currentIndex: Int,
    currentScore: Int,
    currentCombo: Int,
    selectedOption: Int?,
    isAnswerConfirmed: Boolean,
    eliminatedOptionIndices: Set<Int>,
    timeRemainingSeconds: Int,
    playerCoins: Int,
    categoryTitle: String,
    onSelectOption: (Int) -> Unit,
    onNextQuestion: () -> Unit,
    onUse5050Hint: () -> Unit,
    onUseSkipHint: () -> Unit,
    onReportQuestion: (reason: String, comment: String) -> Unit,
    onQuitQuiz: () -> Unit,
    onWatchAdForHint: (() -> Unit)? = null,
    isMusicEnabled: Boolean = true,
    onToggleMusic: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showReportDialog by remember { mutableStateOf(false) }

    if (questions.isEmpty() || currentIndex >= questions.size) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Chargement des questions...", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val currentQuestion = questions[currentIndex]
    val options = listOf(
        currentQuestion.optionA,
        currentQuestion.optionB,
        currentQuestion.optionC,
        currentQuestion.optionD
    )
    val optionLetters = listOf("A", "B", "C", "D")

    val progress = ((currentIndex + 1).toFloat() / questions.size.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "quiz_progress"
    )

    if (showReportDialog) {
        ReportQuestionDialog(
            isOpen = true,
            questionId = currentQuestion.id,
            onDismiss = { showReportDialog = false },
            onSubmitReport = { reason, comment ->
                onReportQuestion(reason, comment)
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            QuizBottomAdBanner()
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("quiz_play_screen")
        ) {
            // Top Navigation Header: Quit, Category Title & Question Counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onQuitQuiz,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("quit_quiz_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Quitter",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = categoryTitle,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    text = "Question ${currentIndex + 1} / ${questions.size}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleMusic,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("toggle_music_button")
                ) {
                    Icon(
                        imageVector = if (isMusicEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = if (isMusicEnabled) "Couper le son" else "Activer le son",
                        tint = if (isMusicEnabled) CleanMinPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }

                IconButton(
                    onClick = { showReportDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("report_question_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = "Signaler",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress Bar
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = CleanMinPrimary,
            trackColor = CleanMinOutlineVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subheader: Timer & Combo Status Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Timer Badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (timeRemainingSeconds <= 5) CleanMinRedBg else CleanMinSecondaryContainer
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Chrono",
                        tint = if (timeRemainingSeconds <= 5) CleanMinRed else CleanMinPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${timeRemainingSeconds}s",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (timeRemainingSeconds <= 5) CleanMinRed else CleanMinPrimary
                        )
                    )
                }
            }

            // Combo Pill
            if (currentCombo >= 2) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CleanMinSecondaryContainer,
                    modifier = Modifier.testTag("combo_badge")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Combo",
                            tint = CleanMinGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Combo x${currentCombo}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CleanMinGold
                            )
                        )
                    }
                }
            } else {
                Text(
                    text = "Score : $currentScore",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Question Card & Options (Scrollable)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Question Prompt Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CleanMinOutline, RoundedCornerShape(20.dp))
                        .testTag("question_prompt_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = currentQuestion.question,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                lineHeight = 28.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        if (currentQuestion.subcategory.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CleanMinSecondaryContainer
                            ) {
                                Text(
                                    text = currentQuestion.subcategory,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CleanMinPrimary
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Lifeline Hint Buttons (50:50, Skip, and Free Video Hint)
            item {
                if (!isAnswerConfirmed) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onUse5050Hint,
                                shape = RoundedCornerShape(16.dp),
                                enabled = playerCoins >= QuizConstants.COST_HINT_5050 && eliminatedOptionIndices.isEmpty(),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hint_5050_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiObjects,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "50:50 (${QuizConstants.COST_HINT_5050}p)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }

                            OutlinedButton(
                                onClick = onUseSkipHint,
                                shape = RoundedCornerShape(16.dp),
                                enabled = playerCoins >= QuizConstants.COST_HINT_SKIP,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hint_skip_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Forward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Passer (${QuizConstants.COST_HINT_SKIP}p)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }

                        // Rewarded Video Free Hint (Accessible anytime or when coins are low)
                        if (eliminatedOptionIndices.isEmpty() && onWatchAdForHint != null) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = CleanMinGold.copy(alpha = 0.1f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CleanMinGold.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onWatchAdForHint() }
                                    .testTag("rewarded_video_hint_button")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = CleanMinGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Regarder une vidéo pour 1 Indice 50:50 Gratuit",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = CleanMinGold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Options List (A, B, C, D)
            items(options.size) { index ->
                val isEliminated = eliminatedOptionIndices.contains(index)
                if (!isEliminated) {
                    val isSelected = selectedOption == index
                    val isCorrectAnswer = currentQuestion.correctOptionIndex == index

                    val style = when {
                        isAnswerConfirmed && isCorrectAnswer -> OptionStyle(
                            cardBg = CleanMinGreenBg,
                            borderColor = CleanMinGreen,
                            textColor = CleanMinGreen,
                            letterBg = CleanMinGreen,
                            letterColor = Color.White
                        )
                        isAnswerConfirmed && isSelected && !isCorrectAnswer -> OptionStyle(
                            cardBg = CleanMinRedBg,
                            borderColor = CleanMinRed,
                            textColor = CleanMinRed,
                            letterBg = CleanMinRed,
                            letterColor = Color.White
                        )
                        isSelected -> OptionStyle(
                            cardBg = CleanMinSecondaryContainer,
                            borderColor = CleanMinPrimary,
                            textColor = MaterialTheme.colorScheme.onSurface,
                            letterBg = CleanMinPrimary,
                            letterColor = Color.White
                        )
                        else -> OptionStyle(
                            cardBg = MaterialTheme.colorScheme.surface,
                            borderColor = CleanMinOutline,
                            textColor = MaterialTheme.colorScheme.onSurface,
                            letterBg = CleanMinSecondaryContainer,
                            letterColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = style.cardBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, style.borderColor, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(enabled = !isAnswerConfirmed) {
                                onSelectOption(index)
                            }
                            .testTag("option_button_$index")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(style.letterBg),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isAnswerConfirmed && isCorrectAnswer) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Correct",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else if (isAnswerConfirmed && isSelected && !isCorrectAnswer) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Incorrect",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Text(
                                        text = optionLetters[index],
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = style.letterColor
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Text(
                                text = options[index],
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = style.textColor
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Explanation & Next Button
            item {
                AnimatedVisibility(
                    visible = isAnswerConfirmed,
                    enter = fadeIn() + slideInVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (currentQuestion.explanation.isNotEmpty()) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = CleanMinSecondaryContainer),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("explanation_card")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "💡 Explication & Référence",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = CleanMinPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currentQuestion.explanation,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    )
                                    if (!currentQuestion.reference.isNullOrEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Réf: ${currentQuestion.reference}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = CleanMinPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = onNextQuestion,
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CleanMinPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("next_question_button")
                        ) {
                            Text(
                                text = if (currentIndex + 1 >= questions.size) "VOIR LES RÉSULTATS" else "QUESTION SUIVANTE",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
}
