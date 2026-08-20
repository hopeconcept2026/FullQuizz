@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
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
import com.example.data.local.entity.PlayerProfileEntity
import com.example.ui.theme.CleanMinGold
import com.example.ui.theme.CleanMinOutline
import com.example.ui.theme.CleanMinPinkBg
import com.example.ui.theme.CleanMinPrimary
import com.example.ui.theme.CleanMinPrimaryContainer
import com.example.ui.theme.CleanMinPurpleBg
import com.example.ui.theme.CleanMinSecondaryContainer

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val level: Int,
    val xp: Int,
    val avatarEmoji: String,
    val isCurrentPlayer: Boolean = false
)

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun LeaderboardScreen(
    player: PlayerProfileEntity?,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Global", "Hebdo", "Mensuel")

    val playerXp = player?.xp ?: 450
    val playerLevel = player?.level ?: 3
    val playerName = player?.nickname ?: "Joueur FULLQUIZZ"

    // Seeded realistic leaderboard
    val topPlayers = remember(selectedTabIndex, playerXp) {
        listOf(
            LeaderboardEntry(1, "Ephraïm K.", 28, 14250, "👑"),
            LeaderboardEntry(2, "Esther B.", 24, 11800, "🦁"),
            LeaderboardEntry(3, "Jonathan M.", 21, 9950, "⚡"),
            LeaderboardEntry(4, "Grâce N.", 19, 8200, "🌟"),
            LeaderboardEntry(5, "David T.", 17, 7150, "🔥"),
            LeaderboardEntry(6, "Deborah L.", 16, 6400, "📚"),
            LeaderboardEntry(7, "Samuel K.", 14, 5300, "🎯"),
            LeaderboardEntry(8, "Sarah W.", 12, 4200, "💡"),
            LeaderboardEntry(9, "Moïse D.", 10, 3100, "🦅"),
            LeaderboardEntry(10, "Rebecca S.", 8, 2400, "🕊️"),
            LeaderboardEntry(142, "$playerName (Vous)", playerLevel, playerXp, "👤", true)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("leaderboard_screen")
    ) {
        // Tab Row
        SecondaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = CleanMinPrimary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Top 3 Podium
            item {
                PodiumSection(topPlayers.take(3))
            }

            item {
                Text(
                    text = "Classement Général",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Players List
            itemsIndexed(topPlayers) { _, entry ->
                LeaderboardItemRow(entry = entry)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PodiumSection(topThree: List<LeaderboardEntry>) {
    if (topThree.size < 3) return
    val first = topThree[0]
    val second = topThree[1]
    val third = topThree[2]

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        // 2nd Place
        PodiumCol(entry = second, rankColor = Color(0xFF9E9E9E), height = 110.dp, medal = "🥈")

        // 1st Place
        PodiumCol(entry = first, rankColor = CleanMinGold, height = 135.dp, medal = "🥇")

        // 3rd Place
        PodiumCol(entry = third, rankColor = Color(0xFFCD7F32), height = 95.dp, medal = "🥉")
    }
}

@Composable
private fun PodiumCol(
    entry: LeaderboardEntry,
    rankColor: Color,
    height: androidx.compose.ui.unit.Dp,
    medal: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(CleanMinSecondaryContainer)
                .border(2.dp, rankColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = entry.avatarEmoji, fontSize = 22.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = entry.name.split(" ").first(),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            maxLines = 1
        )

        Text(
            text = "${entry.xp} XP",
            style = MaterialTheme.typography.labelSmall.copy(color = CleanMinPrimary, fontSize = 10.sp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Podium Block
        Card(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = CleanMinPrimaryContainer),
            modifier = Modifier
                .width(88.dp)
                .height(height)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = medal, fontSize = 28.sp)
            }
        }
    }
}

@Composable
private fun LeaderboardItemRow(entry: LeaderboardEntry) {
    val isMe = entry.isCurrentPlayer
    val containerBg = if (isMe) CleanMinPrimaryContainer else MaterialTheme.colorScheme.surface
    val borderStroke = if (isMe) CleanMinPrimary else CleanMinOutline

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(if (isMe) 1.5.dp else 1.dp, borderStroke, RoundedCornerShape(16.dp))
            .testTag(if (isMe) "my_leaderboard_row" else "leaderboard_row_${entry.rank}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Rank Number
                Text(
                    text = "#${entry.rank}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isMe) CleanMinPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.width(36.dp)
                )

                // Avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CleanMinPurpleBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = entry.avatarEmoji, fontSize = 18.sp)
                }

                // Name & Level
                Column {
                    Text(
                        text = entry.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isMe) FontWeight.Bold else FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "Niveau ${entry.level}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            // XP Badge
            Text(
                text = "${entry.xp} XP",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = CleanMinPrimary
                )
            )
        }
    }
}
