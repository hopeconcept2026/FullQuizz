package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CleanMinOutline
import com.example.ui.theme.CleanMinPrimary
import com.example.ui.theme.CleanMinRed
import com.example.ui.theme.CleanMinRedBg
import com.example.ui.theme.CleanMinSecondaryContainer

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onResetProgress: () -> Unit,
    isMusicEnabled: Boolean = true,
    isMenuMusicEnabled: Boolean = false,
    isSfxEnabled: Boolean = true,
    bgmVolume: Float = 0.35f,
    sfxVolume: Float = 0.85f,
    onToggleMusic: (Boolean) -> Unit = {},
    onToggleMenuMusic: (Boolean) -> Unit = {},
    onToggleSfx: (Boolean) -> Unit = {},
    onBgmVolumeChange: (Float) -> Unit = {},
    onSfxVolumeChange: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var vibrationEnabled by remember { mutableStateOf(true) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(text = "Réinitialiser la progression ?") },
            text = { Text(text = "Toutes vos statistiques, niveaux, pièces et succès locaux seront remis à zéro. Cette action est irréversible.") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetProgress()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CleanMinRed)
                ) {
                    Text(text = "Réinitialiser")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(text = "Annuler")
                }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text(text = "Politique de Confidentialité") },
            text = {
                Text(
                    text = "FULLQUIZZ fonctionne selon une architecture 'Offline-First'. Aucune donnée personnelle nominative n'est collectée sans votre consentement. Les identifiants générés sont anonymes. Les publicités intégrées respectent les directives du Règlement Général sur la Protection des Données (RGPD)."
                )
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CleanMinPrimary)
                ) {
                    Text(text = "Compris")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("settings_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Retour")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Paramètres",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Audio & Haptics
            item {
                Text(
                    text = "Expérience de Jeu",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CleanMinOutline, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Music Master Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = CleanMinPrimary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Musique du Quiz", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                                    Text(text = "Tension plateau & chrono dynamique", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = isMusicEnabled,
                                onCheckedChange = onToggleMusic,
                                colors = SwitchDefaults.colors(checkedThumbColor = CleanMinPrimary)
                            )
                        }

                        if (isMusicEnabled) {
                            Spacer(modifier = Modifier.height(10.dp))
                            // Music in Menus Switch
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 36.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Musique dans les menus", style = MaterialTheme.typography.bodyMedium)
                                    Text(text = "Ambiance discrète sur l'accueil", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = isMenuMusicEnabled,
                                    onCheckedChange = onToggleMenuMusic,
                                    colors = SwitchDefaults.colors(checkedThumbColor = CleanMinPrimary)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            // Music Volume Slider
                            Column(modifier = Modifier.padding(start = 36.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Volume Musique", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = "${(bgmVolume * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = CleanMinPrimary
                                    )
                                }
                                Slider(
                                    value = bgmVolume,
                                    onValueChange = onBgmVolumeChange,
                                    valueRange = 0.05f..1.0f,
                                    colors = SliderDefaults.colors(thumbColor = CleanMinPrimary, activeTrackColor = CleanMinPrimary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // SFX Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = CleanMinPrimary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Jingles & Effets Dopamine", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                                    Text(text = "Victoires, clics, pièces et jokers", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = isSfxEnabled,
                                onCheckedChange = onToggleSfx,
                                colors = SwitchDefaults.colors(checkedThumbColor = CleanMinPrimary)
                            )
                        }

                        if (isSfxEnabled) {
                            Spacer(modifier = Modifier.height(10.dp))
                            // SFX Volume Slider
                            Column(modifier = Modifier.padding(start = 36.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Volume Effets", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = "${(sfxVolume * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = CleanMinPrimary
                                    )
                                }
                                Slider(
                                    value = sfxVolume,
                                    onValueChange = onSfxVolumeChange,
                                    valueRange = 0.1f..1.0f,
                                    colors = SliderDefaults.colors(thumbColor = CleanMinPrimary, activeTrackColor = CleanMinPrimary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Vibration, contentDescription = null, tint = CleanMinPrimary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = "Vibrations Haptiques", style = MaterialTheme.typography.bodyLarge)
                            }
                            Switch(
                                checked = vibrationEnabled,
                                onCheckedChange = { vibrationEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = CleanMinPrimary)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = CleanMinPrimary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = "Rappels Défi du Jour", style = MaterialTheme.typography.bodyLarge)
                            }
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { notificationsEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = CleanMinPrimary)
                            )
                        }
                    }
                }
            }

            // About & Legal
            item {
                Text(
                    text = "À Propos & Confidentialité",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CleanMinOutline, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.fullquizz_logo_1786978399430),
                                    contentDescription = "FULLQUIZZ Logo",
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(text = "FULLQUIZZ v1.0.0", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(text = "Développé avec Jetpack Compose & SQLite", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = { showPrivacyDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Politique de Confidentialité")
                        }
                    }
                }
            }

            // Danger Zone
            item {
                Text(
                    text = "Données & Progression",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanMinRedBg.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CleanMinRed.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Zone de Réinitialisation",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = CleanMinRed
                            )
                        )
                        Text(
                            text = "Effacer les données locales de quiz et restaurer la configuration de bienvenue.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                        Button(
                            onClick = { showResetDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CleanMinRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Effacer toute la progression")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
