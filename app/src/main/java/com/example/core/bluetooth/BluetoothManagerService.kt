package com.example.core.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.util.UUID

/**
 * Representation of a Bluetooth device.
 */
data class BluetoothDeviceModel(
    val name: String,
    val address: String,
    val isPaired: Boolean = false
)

/**
 * State of the Bluetooth connection.
 */
sealed class BluetoothConnectionState {
    object Idle : BluetoothConnectionState()
    object Discovering : BluetoothConnectionState()
    object Hosting : BluetoothConnectionState()
    data class Connecting(val deviceName: String) : BluetoothConnectionState()
    data class Connected(val device: BluetoothDeviceModel, val isHost: Boolean) : BluetoothConnectionState()
    data class Error(val message: String) : BluetoothConnectionState()
}

/**
 * Core Bluetooth Manager Service for Local Peer-to-Peer multiplayer duels.
 * Supports device discovery, RFCOMM Server/Client sockets, and real-time message streaming.
 */
class BluetoothManagerService(private val context: Context) {

    companion object {
        private const val TAG = "BluetoothManager"
        private const val SERVICE_NAME = "FullQuizzDuelService"
        // Standard SPP UUID for RFCOMM serial Bluetooth connections
        val SERVICE_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var serverJob: Job? = null
    private var clientJob: Job? = null
    private var readJob: Job? = null

    private var activeSocket: BluetoothSocket? = null
    private var serverSocket: BluetoothServerSocket? = null
    private var socketWriter: PrintWriter? = null

    private val _connectionState = MutableStateFlow<BluetoothConnectionState>(BluetoothConnectionState.Idle)
    val connectionState: StateFlow<BluetoothConnectionState> = _connectionState.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDeviceModel>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDeviceModel>> = _discoveredDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceModel>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDeviceModel>> = _pairedDevices.asStateFlow()

    private val _lastReceivedMessage = MutableStateFlow<String?>(null)
    val lastReceivedMessage: StateFlow<String?> = _lastReceivedMessage.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(adapter?.isEnabled == true)
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    val isBluetoothSupported: Boolean = adapter != null

    // Broadcast receiver for discovery and adapter changes
    private val discoveryReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(c: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }

                    device?.let { dev ->
                        val name = dev.name ?: "Appareil Inconnu"
                        val address = dev.address
                        val model = BluetoothDeviceModel(name = name, address = address, isPaired = false)

                        val currentList = _discoveredDevices.value.toMutableList()
                        if (currentList.none { it.address == address }) {
                            currentList.add(model)
                            _discoveredDevices.value = currentList
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    if (_connectionState.value is BluetoothConnectionState.Discovering) {
                        _connectionState.value = BluetoothConnectionState.Idle
                    }
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    _isBluetoothEnabled.value = (state == BluetoothAdapter.STATE_ON)
                }
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }
        context.registerReceiver(discoveryReceiver, filter)
        refreshPairedDevices()
    }

    /**
     * Checks if necessary runtime permissions are granted.
     */
    fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Refreshes list of already paired devices.
     */
    @SuppressLint("MissingPermission")
    fun refreshPairedDevices() {
        if (!hasRequiredPermissions() || adapter == null || !adapter.isEnabled) return

        try {
            val bonded = adapter.bondedDevices ?: emptySet()
            _pairedDevices.value = bonded.map { dev ->
                BluetoothDeviceModel(
                    name = dev.name ?: "Appareil Associé",
                    address = dev.address,
                    isPaired = true
                )
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException while getting bonded devices: ${e.message}")
        }
    }

    /**
     * Starts scanning for nearby Bluetooth devices.
     */
    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        if (!hasRequiredPermissions() || adapter == null || !adapter.isEnabled) {
            _connectionState.value = BluetoothConnectionState.Error("Permissions Bluetooth manquantes ou Bluetooth désactivé.")
            return
        }

        try {
            refreshPairedDevices()
            _discoveredDevices.value = emptyList()
            if (adapter.isDiscovering) {
                adapter.cancelDiscovery()
            }
            val started = adapter.startDiscovery()
            if (started) {
                _connectionState.value = BluetoothConnectionState.Discovering
            } else {
                _connectionState.value = BluetoothConnectionState.Error("Impossible de démarrer la recherche.")
            }
        } catch (e: SecurityException) {
            _connectionState.value = BluetoothConnectionState.Error("Permission refusée: ${e.message}")
        }
    }

    /**
     * Stops discovering devices.
     */
    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        if (hasRequiredPermissions() && adapter?.isDiscovering == true) {
            try {
                adapter.cancelDiscovery()
            } catch (_: SecurityException) {}
        }
        if (_connectionState.value is BluetoothConnectionState.Discovering) {
            _connectionState.value = BluetoothConnectionState.Idle
        }
    }

    /**
     * Starts RFCOMM Server Socket to host a multiplayer duel game.
     */
    @SuppressLint("MissingPermission")
    fun startHosting(onConnected: ((BluetoothDeviceModel) -> Unit)? = null) {
        if (!hasRequiredPermissions() || adapter == null || !adapter.isEnabled) {
            _connectionState.value = BluetoothConnectionState.Error("Bluetooth non disponible ou non autorisé.")
            return
        }

        stopDiscovery()
        disconnect()

        _connectionState.value = BluetoothConnectionState.Hosting

        serverJob = serviceScope.launch {
            try {
                serverSocket = adapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, SERVICE_UUID)
                Log.d(TAG, "Server socket listening on UUID: $SERVICE_UUID")

                val socket = serverSocket?.accept() // Blocking call until client connects
                if (socket != null) {
                    serverSocket?.close()
                    serverSocket = null

                    val remoteDevice = socket.remoteDevice
                    val deviceModel = BluetoothDeviceModel(
                        name = remoteDevice.name ?: "Joueur 2",
                        address = remoteDevice.address
                    )

                    activeSocket = socket
                    setupSocketStreams(socket)

                    withContext(Dispatchers.Main) {
                        _connectionState.value = BluetoothConnectionState.Connected(deviceModel, isHost = true)
                        onConnected?.invoke(deviceModel)
                    }
                }
            } catch (e: IOException) {
                Log.w(TAG, "Server socket closed or failed: ${e.message}")
                if (_connectionState.value is BluetoothConnectionState.Hosting) {
                    withContext(Dispatchers.Main) {
                        _connectionState.value = BluetoothConnectionState.Error("Hébergement interrompu: ${e.message}")
                    }
                }
            } catch (e: SecurityException) {
                withContext(Dispatchers.Main) {
                    _connectionState.value = BluetoothConnectionState.Error("Permission refusée: ${e.message}")
                }
            }
        }
    }

    /**
     * Connects as a Client to a remote hosting device via RFCOMM Socket.
     */
    @SuppressLint("MissingPermission")
    fun connectToDevice(deviceAddress: String, onConnected: ((BluetoothDeviceModel) -> Unit)? = null) {
        if (!hasRequiredPermissions() || adapter == null || !adapter.isEnabled) {
            _connectionState.value = BluetoothConnectionState.Error("Bluetooth désactivé ou non autorisé.")
            return
        }

        stopDiscovery()
        disconnect()

        val device = try {
            adapter.getRemoteDevice(deviceAddress)
        } catch (e: IllegalArgumentException) {
            _connectionState.value = BluetoothConnectionState.Error("Adresse d'appareil invalide.")
            return
        }

        val deviceName = device.name ?: "Hôte"
        _connectionState.value = BluetoothConnectionState.Connecting(deviceName)

        clientJob = serviceScope.launch {
            try {
                val socket = device.createRfcommSocketToServiceRecord(SERVICE_UUID)
                socket.connect() // Blocking connect

                val deviceModel = BluetoothDeviceModel(name = deviceName, address = deviceAddress)
                activeSocket = socket
                setupSocketStreams(socket)

                withContext(Dispatchers.Main) {
                    _connectionState.value = BluetoothConnectionState.Connected(deviceModel, isHost = false)
                    onConnected?.invoke(deviceModel)
                }
            } catch (e: IOException) {
                Log.w(TAG, "Failed connecting to socket: ${e.message}")
                withContext(Dispatchers.Main) {
                    _connectionState.value = BluetoothConnectionState.Error("Échec de connexion: ${e.message}")
                }
            } catch (e: SecurityException) {
                withContext(Dispatchers.Main) {
                    _connectionState.value = BluetoothConnectionState.Error("Permission refusée: ${e.message}")
                }
            }
        }
    }

    /**
     * Sets up IO reader/writer loops on connected socket.
     */
    private fun setupSocketStreams(socket: BluetoothSocket) {
        try {
            socketWriter = PrintWriter(OutputStreamWriter(socket.outputStream), true)

            readJob?.cancel()
            readJob = serviceScope.launch {
                val reader = BufferedReader(InputStreamReader(socket.inputStream))
                try {
                    while (isActive) {
                        val line = reader.readLine() ?: break
                        Log.d(TAG, "Received BT message: $line")
                        _lastReceivedMessage.value = line
                    }
                } catch (e: IOException) {
                    Log.d(TAG, "Socket read stream ended: ${e.message}")
                } finally {
                    disconnect()
                }
            }
        } catch (e: IOException) {
            Log.w(TAG, "Error setting up socket streams: ${e.message}")
            disconnect()
        }
    }

    /**
     * Sends a text message or JSON payload through the connected Bluetooth socket.
     */
    fun sendMessage(message: String): Boolean {
        val writer = socketWriter
        return if (writer != null && activeSocket?.isConnected == true) {
            serviceScope.launch {
                try {
                    writer.println(message)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to send message: ${e.message}")
                }
            }
            true
        } else {
            false
        }
    }

    /**
     * Disconnects current socket and terminates server/client jobs.
     */
    fun disconnect() {
        try {
            readJob?.cancel()
            serverJob?.cancel()
            clientJob?.cancel()

            socketWriter = null
            serverSocket?.close()
            serverSocket = null

            activeSocket?.close()
            activeSocket = null
        } catch (e: Exception) {
            Log.w(TAG, "Error during disconnect: ${e.message}")
        } finally {
            _connectionState.value = BluetoothConnectionState.Idle
        }
    }

    /**
     * Unregisters broadcast receiver and frees all resources.
     */
    fun release() {
        try {
            context.unregisterReceiver(discoveryReceiver)
        } catch (_: Exception) {}
        disconnect()
    }
}
