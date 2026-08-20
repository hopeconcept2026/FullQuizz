package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.constants.QuizConstants
import com.example.data.local.entity.PlayerProfileEntity
import com.example.ui.theme.CleanMinGold
import com.example.ui.theme.CleanMinOutlineVariant
import com.example.ui.theme.CleanMinPrimary
import com.example.ui.theme.CleanMinRed
import com.example.ui.theme.CleanMinRedBg
import com.example.ui.theme.CleanMinSecondaryContainer

@Composable
fun TopBarHeader(
    player: PlayerProfileEntity?,
    onAvatarClick: () -> Unit = {},
    onLivesClick: () -> Unit = {},
    onCoinsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val level = player?.level ?: 1
    val xp = player?.xp ?: 0
    val coins = player?.coins ?: 100
    val lives = player?.lives ?: 5
    val streak = player?.streakCount ?: 1
    val nickname = player?.nickname ?: "Joueur"
    val initial = nickname.firstOrNull()?.uppercase() ?: "Q"

    val currentLevelXp = QuizConstants.getXpRequiredForLevel(level - 1)
    val nextLevelXp = QuizConstants.getXpRequiredForLevel(level)
    val xpInLevel = (xp - currentLevelXp).coerceAtLeast(0)
    val xpNeededInLevel = (nextLevelXp - currentLevelXp).coerceAtLeast(1)
    val progress = (xpInLevel.toFloat() / xpNeededInLevel.toFloat()).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "xp_progress"
    )

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier
            .fillMaxWidth()
            .testTag("top_bar_header")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Profile Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onAvatarClick() }
                    .padding(2.dp)
                    .testTag("profile_badge_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CleanMinPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "Niveau $level • ${QuizConstants.getLevelTitle(level)}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .width(96.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = CleanMinPrimary,
                        trackColor = CleanMinOutlineVariant
                    )
                }
            }

            // Status Badges (Streak, Coins, Lives)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Streak Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CleanMinSecondaryContainer,
                    modifier = Modifier.testTag("streak_pill")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = CleanMinGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$streak",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }

                // Coins Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CleanMinSecondaryContainer,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onCoinsClick() }
                        .testTag("coins_pill")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Pièces",
                            tint = CleanMinGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$coins",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }

                // Lives Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (lives <= 1) CleanMinRedBg else CleanMinRedBg,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onLivesClick() }
                        .testTag("lives_pill")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Vies",
                            tint = CleanMinRed,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$lives/5",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CleanMinRed
                            )
                        )
                    }
                }
            }
        }
    }
}
