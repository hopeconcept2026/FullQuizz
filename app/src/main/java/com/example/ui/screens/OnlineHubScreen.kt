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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.PlayerProfileEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.remote.CloudQuestionPack
import com.example.data.remote.OnlineMatchRoom
import com.example.ui.theme.CleanMinGold
import com.example.ui.theme.CleanMinGreen
import com.example.ui.theme.CleanMinGreenBg
import com.example.ui.theme.CleanMinOutline
import com.example.ui.theme.CleanMinOutlineVariant
import com.example.ui.theme.CleanMinPrimary
import com.example.ui.theme.CleanMinPrimaryContainer
import com.example.ui.theme.CleanMinSecondaryContainer

@Composable
fun OnlineHubScreen(
    playerProfile: PlayerProfileEntity?,
    categories: List<CategoryEntity>,
    availablePacks: List<CloudQuestionPack>,
    activeRoom: OnlineMatchRoom?,
    isSyncing: Boolean,
    syncMessage: String?,
    onBack: () -> Unit,
    onJoinMatchmaking: (roomCode: String) -> Unit,
    onStartOnlineDuel: (categoryId: String) -> Unit,
    onInstallPack: (CloudQuestionPack) -> Unit,
    onCreateCustomQuestion: (QuestionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Multijoueur En Ligne", "Packs Cloud", "Ajout Question")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("online_hub_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("online_hub_back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Retour",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Centre En Ligne & Cloud",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "Multijoueur, Nouveaux Packs & Créateur",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // Tab Selector
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = CleanMinPrimary,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Notification Banner if any
        if (syncMessage != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = CleanMinGreenBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, CleanMinGreen.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = CleanMinGreen
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = CleanMinGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = syncMessage,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CleanMinGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> OnlineMultiplayerTab(
                playerProfile = playerProfile,
                categories = categories,
                activeRoom = activeRoom,
                isSyncing = isSyncing,
                onJoinMatchmaking = onJoinMatchmaking,
                onStartOnlineDuel = onStartOnlineDuel
            )
            1 -> CloudPacksTab(
                packs = availablePacks,
                isSyncing = isSyncing,
                onInstallPack = onInstallPack
            )
            2 -> AddQuestionTab(
                categories = categories,
                onCreateQuestion = onCreateCustomQuestion
            )
        }
    }
}

@Composable
private fun OnlineMultiplayerTab(
    playerProfile: PlayerProfileEntity?,
    categories: List<CategoryEntity>,
    activeRoom: OnlineMatchRoom?,
    isSyncing: Boolean,
    onJoinMatchmaking: (roomCode: String) -> Unit,
    onStartOnlineDuel: (categoryId: String) -> Unit
) {
    var inputRoomCode by remember { mutableStateOf("") }
    var selectedCategoryForDuel by remember { mutableStateOf("all") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Matchmaking Hero Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CleanMinPrimaryContainer.copy(alpha = 0.5f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, CleanMinPrimary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CleanMinPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Duel Matchmaking Rapide",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CleanMinPrimary
                                )
                            )
                            Text(
                                text = "Trouvez un joueur en ligne instantanément",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onJoinMatchmaking("") },
                        enabled = !isSyncing,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CleanMinPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("quick_matchmaking_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSyncing) "Recherche en cours..." else "Lancer un Duel Aléatoire (1v1)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Active Room details if found
        if (activeRoom != null) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanMinGreenBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CleanMinGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Salon : ${activeRoom.roomId}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CleanMinGreen
                                )
                            )
                            Text(
                                text = if (activeRoom.guest != null) "Adversaire connecté" else "En attente d'un joueur",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeRoom.guest != null) CleanMinGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = activeRoom.host.nickname,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(text = "Niv. ${activeRoom.host.level}", style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                text = "VS",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = CleanMinPrimary
                                )
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = activeRoom.guest?.nickname ?: "En attente d'un ami...",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (activeRoom.guest != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Text(
                                    text = if (activeRoom.guest != null) "Niv. ${activeRoom.guest?.level ?: 1}" else "Donnez le code ${activeRoom.roomId}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (activeRoom.guest != null) {
                            Button(
                                onClick = { onStartOnlineDuel("all") },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CleanMinGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Lancer le Duel en Ligne ⚔️", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Invitez un autre joueur à ouvrir l'appli et entrer le code ${activeRoom.roomId} ci-dessous !",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Join Room by Code Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CleanMinOutlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Rejoindre un Salon Privé",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "Entrez le code partagé par un ami (ex: QZ-4821)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputRoomCode,
                            onValueChange = { inputRoomCode = it.uppercase() },
                            placeholder = { Text("Code QZ-XXXX") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("room_code_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onJoinMatchmaking(inputRoomCode) },
                            enabled = inputRoomCode.isNotBlank() && !isSyncing,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("join_room_button")
                        ) {
                            Text("Rejoindre")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CloudPacksTab(
    packs: List<CloudQuestionPack>,
    isSyncing: Boolean,
    onInstallPack: (CloudQuestionPack) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Packs de Questions Téléchargeables",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = "Ajoutez de nouveaux quiz et niveaux à votre base de données sans mise à jour du Play Store.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        items(packs) { pack ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CleanMinOutlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = pack.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CleanMinPrimaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "+${pack.questionCount} questions",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CleanMinPrimary
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = pack.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { onInstallPack(pack) },
                        enabled = !isSyncing,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("install_pack_${pack.packId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Installer dans la Base Locale", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddQuestionTab(
    categories: List<CategoryEntity>,
    onCreateQuestion: (QuestionEntity) -> Unit
) {
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: "culture_generale") }
    var questionText by remember { mutableStateOf("") }
    var optA by remember { mutableStateOf("") }
    var optB by remember { mutableStateOf("") }
    var optC by remember { mutableStateOf("") }
    var optD by remember { mutableStateOf("") }
    var correctAnswer by remember { mutableStateOf("A") }
    var explanationText by remember { mutableStateOf("") }
    var referenceText by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("medium") }
    var submittedMessage by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Créateur de Question & Niveau",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = "Créez une question. Elle sera intégrée immédiatement au jeu et ajoutée à la file de synchronisation Cloud.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // Category Selector Chips
        item {
            Text(
                text = "1. Choisir la catégorie",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val previewCats = categories.take(4)
                previewCats.forEach { cat ->
                    val isSelected = selectedCategoryId == cat.id
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) CleanMinPrimary else CleanMinSecondaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier
                            .clickable { selectedCategoryId = cat.id }
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = cat.name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Question Input
        item {
            OutlinedTextField(
                value = questionText,
                onValueChange = { questionText = it },
                label = { Text("Énoncé de la question") },
                placeholder = { Text("Ex: Quel est le plus long fleuve d'Afrique ?") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Options A, B, C, D
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = optA,
                    onValueChange = { optA = it },
                    label = { Text("Option A") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = optB,
                    onValueChange = { optB = it },
                    label = { Text("Option B") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = optC,
                    onValueChange = { optC = it },
                    label = { Text("Option C") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = optD,
                    onValueChange = { optD = it },
                    label = { Text("Option D") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // Correct Answer Selector
        item {
            Text(
                text = "Bonne réponse :",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("A", "B", "C", "D").forEach { opt ->
                    val isSelected = correctAnswer == opt
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) CleanMinGreen else CleanMinSecondaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { correctAnswer = opt }
                    ) {
                        Text(
                            text = "Option $opt",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }
            }
        }

        // Explanation and Reference
        item {
            OutlinedTextField(
                value = explanationText,
                onValueChange = { explanationText = it },
                label = { Text("Explication pédagogique") },
                placeholder = { Text("Détaillez pourquoi cette réponse est exacte...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            OutlinedTextField(
                value = referenceText,
                onValueChange = { referenceText = it },
                label = { Text("Référence / Source (Optionnelle)") },
                placeholder = { Text("Ex: Luc 10:25, Traité de Versailles, etc.") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Submit Button
        item {
            val isFormValid = questionText.isNotBlank() && optA.isNotBlank() && optB.isNotBlank() && optC.isNotBlank() && optD.isNotBlank()
            Button(
                onClick = {
                    val newQ = QuestionEntity(
                        categoryId = selectedCategoryId,
                        question = questionText.trim(),
                        optionA = optA.trim(),
                        optionB = optB.trim(),
                        optionC = optC.trim(),
                        optionD = optD.trim(),
                        correctAnswer = correctAnswer,
                        explanation = if (explanationText.isBlank()) "Explication validée." else explanationText.trim(),
                        reference = referenceText.ifBlank { null },
                        difficulty = difficulty
                    )
                    onCreateQuestion(newQ)
                    questionText = ""
                    optA = ""
                    optB = ""
                    optC = ""
                    optD = ""
                    explanationText = ""
                    referenceText = ""
                    submittedMessage = true
                },
                enabled = isFormValid,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CleanMinPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_custom_question_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Enregistrer la Question dans le Jeu", fontWeight = FontWeight.Bold)
            }
        }
    }
}
