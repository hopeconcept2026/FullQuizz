package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.window.Dialog
import com.example.core.constants.QuizConstants
import com.example.core.sync.SyncStatus
import com.example.data.local.entity.PlayerProfileEntity
import com.example.ui.theme.CleanMinGold
import com.example.ui.theme.CleanMinGreen
import com.example.ui.theme.CleanMinGreenBg
import com.example.ui.theme.CleanMinOutline
import com.example.ui.theme.CleanMinOutlineVariant
import com.example.ui.theme.CleanMinPinkBg
import com.example.ui.theme.CleanMinPrimary
import com.example.ui.theme.CleanMinPrimaryContainer
import com.example.ui.theme.CleanMinPurpleBg
import com.example.ui.theme.CleanMinRed
import com.example.ui.theme.CleanMinSecondaryContainer
import com.google.firebase.auth.FirebaseUser

@Composable
fun ProfileScreen(
    player: PlayerProfileEntity?,
    pendingSyncCount: Int,
    syncStatus: SyncStatus,
    firebaseUser: FirebaseUser? = null,
    isAuthenticatingFirebase: Boolean = false,
    onUpdateProfile: (nickname: String, avatarId: String) -> Unit,
    onForceSync: () -> Unit,
    onOpenSettings: () -> Unit,
    onSignInGoogle: () -> Unit = {},
    onSignOutGoogle: () -> Unit = {},
    onSyncFirebase: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isEditingName by remember { mutableStateOf(false) }
    var editedName by remember(player?.nickname) { mutableStateOf(player?.nickname ?: "Joueur FULLQUIZZ") }
    var showAvatarPicker by remember { mutableStateOf(false) }

    val level = player?.level ?: 1
    val xp = player?.xp ?: 0
    val coins = player?.coins ?: 100
    val lives = player?.lives ?: 5
    val streak = player?.streakCount ?: 1
    val bestStreak = player?.bestStreak ?: 1
    val totalGames = player?.totalGamesPlayed ?: 0
    val totalCorrect = player?.totalCorrectAnswers ?: 0
    val totalAnswered = player?.totalQuestionsAnswered ?: 0
    val accuracy = if (totalAnswered > 0) ((totalCorrect.toFloat() / totalAnswered.toFloat()) * 100).toInt() else 0

    val availableAvatars = listOf(
        "avatar_1" to "🦁",
        "avatar_2" to "🦅",
        "avatar_3" to "👑",
        "avatar_4" to "⚡",
        "avatar_5" to "🕊️",
        "avatar_6" to "🌟",
        "avatar_7" to "💡",
        "avatar_8" to "🔥"
    )

    val currentAvatarEmoji = availableAvatars.find { it.first == player?.avatarId }?.second ?: "🦁"

    if (showAvatarPicker) {
        Dialog(onDismissRequest = { showAvatarPicker = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Choisir un Avatar",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val chunked = availableAvatars.chunked(4)
                    chunked.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            row.forEach { (id, emoji) ->
                                val isSelected = player?.avatarId == id
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) CleanMinPrimaryContainer else CleanMinSecondaryContainer)
                                        .border(
                                            if (isSelected) 2.dp else 1.dp,
                                            if (isSelected) CleanMinPrimary else CleanMinOutline,
                                            CircleShape
                                        )
                                        .clickable {
                                            onUpdateProfile(player?.nickname ?: "Joueur", id)
                                            showAvatarPicker = false
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 26.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Profile Identity Card (Clean Minimalism)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CleanMinOutline, RoundedCornerShape(24.dp))
                    .testTag("profile_identity_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = onOpenSettings, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Paramètres",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Avatar Circle with Edit Badge
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(CleanMinPurpleBg)
                            .border(2.dp, CleanMinPrimary, CircleShape)
                            .clickable { showAvatarPicker = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = currentAvatarEmoji, fontSize = 40.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Nickname with edit icon
                    if (isEditingName) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            OutlinedTextField(
                                value = editedName,
                                onValueChange = { editedName = it },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            IconButton(
                                onClick = {
                                    if (editedName.isNotBlank()) {
                                        onUpdateProfile(editedName.trim(), player?.avatarId ?: "avatar_1")
                                    }
                                    isEditingName = false
                                }
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = "Valider", tint = CleanMinPrimary)
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { isEditingName = true }
                        ) {
                            Text(
                                text = player?.nickname ?: "Joueur FULLQUIZZ",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifier nom",
                                tint = CleanMinPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = "ID: ${player?.playerId ?: "QZ-884920"} • Niveau $level (${QuizConstants.getLevelTitle(level)})",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // Statistics Grid
        item {
            Text(
                text = "Statistiques de Jeu",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = "Parties Jouées",
                        value = "$totalGames",
                        emoji = "🎮",
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Précision",
                        value = "$accuracy%",
                        emoji = "🎯",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = "Série Actuelle",
                        value = "$streak j",
                        emoji = "🔥",
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Meilleure Série",
                        value = "$bestStreak j",
                        emoji = "⚡",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Firebase Cloud & Authentication Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (firebaseUser != null) CleanMinGreenBg.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (firebaseUser != null) CleanMinGreen.copy(alpha = 0.4f) else CleanMinOutline
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("firebase_account_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (firebaseUser != null) CleanMinGreen else CleanMinPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (firebaseUser != null) Icons.Default.Check else Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (firebaseUser != null) "Compte Firebase Connecté" else "Synchronisation Firebase Cloud",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (firebaseUser != null) (firebaseUser.email ?: firebaseUser.displayName ?: "Compte Google") else "Sauvegardez vos scores et jouez en ligne",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (firebaseUser == null) {
                        Button(
                            onClick = onSignInGoogle,
                            enabled = !isAuthenticatingFirebase,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CleanMinPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("firebase_sign_in_button")
                        ) {
                            Text(
                                text = if (isAuthenticatingFirebase) "Connexion en cours..." else "Se connecter avec Google (Firebase)",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onSyncFirebase,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Sauvegarder", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = onSignOutGoogle,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Déconnexion", fontSize = 12.sp, color = CleanMinRed)
                            }
                        }
                    }
                }
            }
        }

        // Offline / Sync Status Card (Sync Engine)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CleanMinSecondaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sync_engine_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (pendingSyncCount == 0) Icons.Default.CloudDone else Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = if (pendingSyncCount == 0) CleanMinGreen else CleanMinPrimary
                            )
                        }

                        Column {
                            Text(
                                text = if (pendingSyncCount == 0) "Synchronisation à jour" else "$pendingSyncCount élément(s) en attente",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Mode Hors-ligne 100% opérationnel",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onForceSync,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Forcer la synchronisation",
                            tint = CleanMinPrimary
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatMetricCard(
    title: String,
    value: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .border(1.dp, CleanMinOutline, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(text = emoji, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}
