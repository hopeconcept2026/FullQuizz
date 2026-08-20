package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.WifiTethering
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.core.bluetooth.BluetoothConnectionState
import com.example.core.bluetooth.BluetoothDeviceModel
import com.example.ui.theme.CleanMinGold
import com.example.ui.theme.CleanMinGreen
import com.example.ui.theme.CleanMinGreenBg
import com.example.ui.theme.CleanMinOutline
import com.example.ui.theme.CleanMinOutlineVariant
import com.example.ui.theme.CleanMinPrimary
import com.example.ui.theme.CleanMinPrimaryContainer
import com.example.ui.theme.CleanMinRed
import com.example.ui.theme.CleanMinRedBg
import com.example.ui.theme.CleanMinSecondaryContainer

@Composable
fun BluetoothLobbyScreen(
    connectionState: BluetoothConnectionState,
    discoveredDevices: List<BluetoothDeviceModel>,
    pairedDevices: List<BluetoothDeviceModel>,
    isBluetoothEnabled: Boolean,
    lastReceivedMessage: String?,
    onBack: () -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onStartHosting: () -> Unit,
    onConnectToDevice: (String) -> Unit,
    onDisconnect: () -> Unit,
    onSendMessage: (String) -> Unit,
    onStartGame: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasPermissions by remember {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            listOf(Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION)
        }
        mutableStateOf(permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        })
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasPermissions = result.values.all { it }
        if (hasPermissions) {
            onStartScan()
        }
    }

    var customMessageText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("bluetooth_lobby_screen")
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("bt_back_btn")
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Retour")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Duel Bluetooth Local",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Affrontez un ami sans connexion Internet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (connectionState is BluetoothConnectionState.Discovering) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp,
                    color = CleanMinPrimary
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Connection Status Banner
            item {
                BluetoothStatusCard(
                    state = connectionState,
                    isBluetoothEnabled = isBluetoothEnabled,
                    onDisconnect = onDisconnect
                )
            }

            // 2. Permission Banner if needed
            if (!hasPermissions) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CleanMinSecondaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Sensors, contentDescription = null, tint = CleanMinPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Autorisation Bluetooth Requise",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Permet de détecter les appareils proches pour lancer un duel.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
                                    } else {
                                        arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                    permissionLauncher.launch(perms)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CleanMinPrimary)
                            ) {
                                Text(text = "Autoriser", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 3. Control Action Buttons (Host / Scan)
            if (connectionState !is BluetoothConnectionState.Connected) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (hasPermissions) onStartHosting()
                                else {
                                    val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE)
                                    } else {
                                        arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                    permissionLauncher.launch(perms)
                                }
                            },
                            enabled = connectionState !is BluetoothConnectionState.Hosting,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (connectionState is BluetoothConnectionState.Hosting) CleanMinGold else CleanMinPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("bt_host_btn")
                        ) {
                            Icon(imageVector = Icons.Default.WifiTethering, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (connectionState is BluetoothConnectionState.Hosting) "En Attente..." else "Héberger",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                if (hasPermissions) {
                                    if (connectionState is BluetoothConnectionState.Discovering) onStopScan() else onStartScan()
                                } else {
                                    val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
                                    } else {
                                        arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                    permissionLauncher.launch(perms)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("bt_scan_btn")
                        ) {
                            Icon(
                                imageVector = if (connectionState is BluetoothConnectionState.Discovering) Icons.Default.Close else Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (connectionState is BluetoothConnectionState.Discovering) "Arrêter" else "Rechercher",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 4. Connected Session Card (Active Socket)
            if (connectionState is BluetoothConnectionState.Connected) {
                item {
                    ConnectedDuelCard(
                        connectedState = connectionState,
                        lastReceivedMessage = lastReceivedMessage,
                        customMessageText = customMessageText,
                        onMessageChange = { customMessageText = it },
                        onSendMessage = {
                            if (customMessageText.isNotBlank()) {
                                onSendMessage(customMessageText)
                                customMessageText = ""
                            }
                        },
                        onStartGame = onStartGame,
                        onDisconnect = onDisconnect
                    )
                }
            }

            // 5. Discovered Devices List
            if (connectionState !is BluetoothConnectionState.Connected) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Appareils Détectés (${discoveredDevices.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (connectionState is BluetoothConnectionState.Discovering) {
                            Text(
                                text = "Scan actif...",
                                style = MaterialTheme.typography.labelSmall,
                                color = CleanMinPrimary
                            )
                        }
                    }
                }

                if (discoveredDevices.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CleanMinOutline, RoundedCornerShape(14.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BluetoothSearching,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (connectionState is BluetoothConnectionState.Discovering)
                                        "Recherche d'adversaires proches..."
                                    else
                                        "Aucun appareil détecté pour le moment.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Cliquez sur 'Héberger' sur l'autre téléphone ou lancez la recherche.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                } else {
                    items(discoveredDevices) { device ->
                        DeviceItemCard(
                            device = device,
                            isConnecting = connectionState is BluetoothConnectionState.Connecting && (connectionState as BluetoothConnectionState.Connecting).deviceName == device.name,
                            onConnect = { onConnectToDevice(device.address) }
                        )
                    }
                }

                // 6. Paired Devices List
                if (pairedDevices.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Appareils Déjà Appairés (${pairedDevices.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    items(pairedDevices) { device ->
                        DeviceItemCard(
                            device = device,
                            isConnecting = connectionState is BluetoothConnectionState.Connecting && (connectionState as BluetoothConnectionState.Connecting).deviceName == device.name,
                            onConnect = { onConnectToDevice(device.address) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

/**
 * Visual Banner Card summarizing current Bluetooth socket status.
 */
@Composable
private fun BluetoothStatusCard(
    state: BluetoothConnectionState,
    isBluetoothEnabled: Boolean,
    onDisconnect: () -> Unit
) {
    val (statusTitle, statusDesc, badgeColor, badgeBg) = when {
        !isBluetoothEnabled -> Quadruple(
            "Bluetooth Désactivé",
            "Veuillez activer le Bluetooth sur votre smartphone.",
            CleanMinRed,
            CleanMinRedBg
        )
        state is BluetoothConnectionState.Connected -> Quadruple(
            "Connecté au Duel !",
            "Socket actif avec ${state.device.name} (${if (state.isHost) "Hôte" else "Client"})",
            CleanMinGreen,
            CleanMinGreenBg
        )
        state is BluetoothConnectionState.Hosting -> Quadruple(
            "En Attente d'un Joueur...",
            "Votre appareil est prêt. Votre ami peut vous rejoindre dans la liste.",
            CleanMinGold,
            CleanMinGold.copy(alpha = 0.15f)
        )
        state is BluetoothConnectionState.Connecting -> Quadruple(
            "Connexion en cours...",
            "Établissement de la liaison socket avec ${state.deviceName}...",
            CleanMinPrimary,
            CleanMinPrimaryContainer
        )
        state is BluetoothConnectionState.Discovering -> Quadruple(
            "Recherche d'Appareils...",
            "Scan des appareils Bluetooth à proximité en cours.",
            CleanMinPrimary,
            CleanMinPrimaryContainer
        )
        state is BluetoothConnectionState.Error -> Quadruple(
            "Statut / Erreur",
            state.message,
            CleanMinRed,
            CleanMinRedBg
        )
        else -> Quadruple(
            "Déconnecté (En attente)",
            "Prêt pour héberger une partie ou rechercher un adversaire.",
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.surfaceVariant
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = badgeBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .testTag("bt_status_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (state) {
                        is BluetoothConnectionState.Connected -> Icons.Default.BluetoothConnected
                        is BluetoothConnectionState.Hosting -> Icons.Default.WifiTethering
                        is BluetoothConnectionState.Discovering -> Icons.Default.BluetoothSearching
                        else -> Icons.Default.Bluetooth
                    },
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = statusTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = badgeColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = statusDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (state is BluetoothConnectionState.Hosting || state is BluetoothConnectionState.Connected || state is BluetoothConnectionState.Connecting) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onDisconnect,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.7f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Annuler",
                        tint = CleanMinRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Device Item Row Card for discovered or bonded devices.
 */
@Composable
private fun DeviceItemCard(
    device: BluetoothDeviceModel,
    isConnecting: Boolean,
    onConnect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CleanMinOutline, RoundedCornerShape(14.dp))
            .clickable(enabled = !isConnecting) { onConnect() }
            .testTag("device_item_${device.address}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CleanMinSecondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (device.isPaired) Icons.Default.PhoneAndroid else Icons.Default.DeviceUnknown,
                    contentDescription = null,
                    tint = CleanMinPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "${device.address} ${if (device.isPaired) "• Appairé" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isConnecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = CleanMinPrimary
                )
            } else {
                Button(
                    onClick = onConnect,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CleanMinPrimary)
                ) {
                    Text(text = "Rejoindre", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Card displayed once two players are connected in Bluetooth socket.
 */
@Composable
private fun ConnectedDuelCard(
    connectedState: BluetoothConnectionState.Connected,
    lastReceivedMessage: String?,
    customMessageText: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onStartGame: () -> Unit,
    onDisconnect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, CleanMinGreen, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = CleanMinGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Salon Prêt à Jouer",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CleanMinGreen
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CleanMinGreenBg
                ) {
                    Text(
                        text = if (connectedState.isHost) "Rôle : Hôte (J1)" else "Rôle : Invité (J2)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = CleanMinGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Adversaire connecté : ${connectedState.device.name}",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "Adresse Bluetooth : ${connectedState.device.address}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Test Socket Message Exchange
            if (!lastReceivedMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanMinSecondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Dernier message socket reçu : \"$lastReceivedMessage\"",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Message Ping Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customMessageText,
                    onValueChange = onMessageChange,
                    placeholder = { Text("Tester un message socket (ex: Ping)") },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CleanMinPrimary,
                        unfocusedBorderColor = CleanMinOutline
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onSendMessage,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CleanMinPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Envoyer", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Start Game Button
            Button(
                onClick = onStartGame,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CleanMinGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("bt_start_duel_btn")
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Lancer le Duel 1v1", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onDisconnect,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CleanMinRed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Quitter le salon", fontWeight = FontWeight.Medium)
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
