package com.example.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

/**
 * Type of background music tracks available.
 */
enum class BgmTrackType {
    NONE,
    MENU_AMBIENT,
    QUIZ_PLATEAU_SUSPENSE,
    RESULTS_TRIUMPH
}

/**
 * AudioPlayerManager:
 * - Smart, non-intrusive audio design inspired by top-chart mobile games (Trivia Crack, Duolingo, Kahoot).
 * - Balanced default volumes: BGM (35% volume) for discreet focus, SFX (85% volume) for rewarding dopamine feedback.
 * - Silent on app launch by default (or gentle soft fade), intense during Quiz gameplay.
 * - Addictive micro-SFX: crisp option click, rising combo streak chimes, satisfying coin drops, futuristic lifelines.
 */
class AudioPlayerManager private constructor() {

    companion object {
        private const val TAG = "AudioPlayerManager"
        private const val SAMPLE_RATE = 44100

        @Volatile
        private var INSTANCE: AudioPlayerManager? = null

        fun getInstance(): AudioPlayerManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AudioPlayerManager().also { INSTANCE = it }
            }
        }
    }

    private val audioScope = CoroutineScope(Dispatchers.Default)
    private val mediaMutex = Mutex()
    private var appContext: Context? = null

    // State flows
    private val _isMusicEnabled = MutableStateFlow(true)
    val isMusicEnabled: StateFlow<Boolean> = _isMusicEnabled.asStateFlow()

    private val _isMenuMusicEnabled = MutableStateFlow(false)
    val isMenuMusicEnabled: StateFlow<Boolean> = _isMenuMusicEnabled.asStateFlow()

    private val _isSfxEnabled = MutableStateFlow(true)
    val isSfxEnabled: StateFlow<Boolean> = _isSfxEnabled.asStateFlow()

    // Default calibrated volumes (Subtle background, punchy rewards)
    private val _bgmVolume = MutableStateFlow(0.35f)
    val bgmVolume: StateFlow<Float> = _bgmVolume.asStateFlow()

    private val _sfxVolume = MutableStateFlow(0.85f)
    val sfxVolume: StateFlow<Float> = _sfxVolume.asStateFlow()

    private val _currentBgmTrack = MutableStateFlow(BgmTrackType.NONE)
    val currentBgmTrack: StateFlow<BgmTrackType> = _currentBgmTrack.asStateFlow()

    // Media players & Jobs
    private var mediaPlayer: MediaPlayer? = null
    private var bgmLoopJob: Job? = null
    private var tensionJob: Job? = null
    private var volumeFadeJob: Job? = null
    private var isDucked: Boolean = false
    private var preDuckVolume: Float = 0.35f

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun setMusicEnabled(enabled: Boolean) {
        _isMusicEnabled.value = enabled
        if (!enabled) {
            stopBgm(fadeOutDurationMs = 150)
            stopTension()
        }
    }

    fun setMenuMusicEnabled(enabled: Boolean) {
        _isMenuMusicEnabled.value = enabled
        if (!enabled && _currentBgmTrack.value == BgmTrackType.MENU_AMBIENT) {
            stopBgm(fadeOutDurationMs = 200)
        } else if (enabled && _currentBgmTrack.value == BgmTrackType.NONE) {
            playBgm(BgmTrackType.MENU_AMBIENT, fadeInDurationMs = 800)
        }
    }

    fun setSfxEnabled(enabled: Boolean) {
        _isSfxEnabled.value = enabled
    }

    fun setBgmVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _bgmVolume.value = clamped
        try {
            mediaPlayer?.setVolume(clamped, clamped)
        } catch (_: Exception) {}
    }

    fun setSfxVolume(volume: Float) {
        _sfxVolume.value = volume.coerceIn(0f, 1f)
    }

    fun toggleMusic() {
        setMusicEnabled(!_isMusicEnabled.value)
    }

    fun toggleSfx() {
        setSfxEnabled(!_isSfxEnabled.value)
    }

    // -------------------------------------------------------------
    // 1. SMART BACKGROUND MUSIC & FADES
    // -------------------------------------------------------------

    /**
     * Starts playback of background music.
     * Menu music is only played if menu music is enabled.
     */
    fun playBgm(trackType: BgmTrackType, fadeInDurationMs: Long = 400) {
        if (!_isMusicEnabled.value) return

        // Respect menu music preference
        if (trackType == BgmTrackType.MENU_AMBIENT && !_isMenuMusicEnabled.value) {
            stopBgm(fadeOutDurationMs = 150)
            return
        }

        if (_currentBgmTrack.value == trackType && (mediaPlayer?.isPlaying == true || bgmLoopJob?.isActive == true)) return

        _currentBgmTrack.value = trackType

        audioScope.launch {
            mediaMutex.withLock {
                try {
                    mediaPlayer?.let { player ->
                        if (player.isPlaying) player.stop()
                        player.reset()
                        player.release()
                    }
                } catch (_: Exception) {}
                mediaPlayer = null

                bgmLoopJob?.cancel()
                bgmLoopJob = null

                // 1. Try playing from res/raw if present (Suno MP3s)
                val rawResName = when (trackType) {
                    BgmTrackType.QUIZ_PLATEAU_SUSPENSE -> "bgm_quiz"
                    BgmTrackType.MENU_AMBIENT -> "bgm_menu"
                    BgmTrackType.RESULTS_TRIUMPH -> "bgm_results"
                    BgmTrackType.NONE -> null
                }

                val context = appContext
                var rawResId = 0
                if (context != null && rawResName != null) {
                    rawResId = context.resources.getIdentifier(rawResName, "raw", context.packageName)
                }

                if (rawResId != 0 && context != null) {
                    try {
                        val player = MediaPlayer.create(context, rawResId)
                        if (player != null) {
                            player.isLooping = true
                            player.setOnErrorListener { mp, _, _ ->
                                try {
                                    mp.reset()
                                    mp.release()
                                } catch (_: Exception) {}
                                true
                            }
                            val targetVol = _bgmVolume.value
                            player.setVolume(0.02f, 0.02f)
                            player.start()
                            mediaPlayer = player
                            fadeVolume(from = 0.02f, to = targetVol, durationMs = fadeInDurationMs)
                            return@withLock
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed playing raw resource $rawResName: ${e.message}")
                    }
                }

                // 2. Fallback to ambient procedural synthesizer
                bgmLoopJob = audioScope.launch {
                    try {
                        when (trackType) {
                            BgmTrackType.QUIZ_PLATEAU_SUSPENSE -> runCalibratedQuizLoop()
                            BgmTrackType.MENU_AMBIENT -> runCalibratedMenuLoop()
                            BgmTrackType.RESULTS_TRIUMPH -> runCalibratedResultsLoop()
                            BgmTrackType.NONE -> {}
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Synth BGM loop exception: ${e.message}")
                    }
                }
            }
        }
    }

    /**
     * Stops current BGM smoothly.
     */
    fun stopBgm(fadeOutDurationMs: Long = 250) {
        audioScope.launch {
            mediaMutex.withLock {
                val player = mediaPlayer
                if (player != null) {
                    if (fadeOutDurationMs > 0) {
                        fadeVolume(from = _bgmVolume.value, to = 0f, durationMs = fadeOutDurationMs)
                    }
                    try {
                        if (player.isPlaying) player.stop()
                        player.reset()
                        player.release()
                    } catch (_: Exception) {}
                    mediaPlayer = null
                }

                val job = bgmLoopJob
                if (job != null && job.isActive) {
                    job.cancel()
                    bgmLoopJob = null
                }
                _currentBgmTrack.value = BgmTrackType.NONE
            }
        }
    }

    fun duckBgm(duckTargetVolume: Float = 0.10f, durationMs: Long = 120) {
        if (isDucked || !_isMusicEnabled.value) return
        isDucked = true
        preDuckVolume = _bgmVolume.value
        fadeVolume(from = preDuckVolume, to = duckTargetVolume, durationMs = durationMs)
    }

    fun unduckBgm(durationMs: Long = 250) {
        if (!isDucked || !_isMusicEnabled.value) return
        isDucked = false
        fadeVolume(from = _bgmVolume.value, to = preDuckVolume, durationMs = durationMs)
    }

    fun fadeVolume(from: Float, to: Float, durationMs: Long) {
        volumeFadeJob?.cancel()
        volumeFadeJob = audioScope.launch {
            val steps = 12
            val stepDelay = (durationMs / steps).coerceAtLeast(10)
            for (i in 0..steps) {
                if (!isActive) break
                val progress = i.toFloat() / steps
                val currentVol = from + (to - from) * progress
                try {
                    mediaPlayer?.setVolume(currentVol, currentVol)
                } catch (_: Exception) {}
                delay(stepDelay)
            }
            try {
                mediaPlayer?.setVolume(to, to)
            } catch (_: Exception) {}
        }
    }

    // -------------------------------------------------------------
    // 2. MICRO-INTERACTIONS & ADDICTIVE SFX (DOPAMINE LOOP)
    // -------------------------------------------------------------

    /**
     * Subtle, ultra-satisfying tactile pop when tapping an option card.
     */
    fun playOptionClick() {
        if (!_isSfxEnabled.value) return
        audioScope.launch {
            try {
                val durationMs = 35
                val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)
                val gain = _sfxVolume.value * 0.40f

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val freq = 520.0 + (300.0 * (1.0 - (i.toDouble() / numSamples)))
                    val env = exp(-t * 80.0)
                    val wave = sin(2 * PI * freq * t) * env
                    buffer[i] = (wave * Short.MAX_VALUE * gain).toInt().toShort()
                }
                playBuffer(buffer)
            } catch (_: Exception) {}
        }
    }

    /**
     * Rewarding coin collection sound when gaining coins/XP.
     */
    fun playCoinCollect() {
        if (!_isSfxEnabled.value) return
        audioScope.launch {
            try {
                val notes = listOf(987.77, 1318.51) // B5 -> E6
                val noteMs = 60
                val totalSamples = (SAMPLE_RATE * ((noteMs * notes.size + 150) / 1000.0)).toInt()
                val buffer = ShortArray(totalSamples)
                val gain = _sfxVolume.value * 0.65f

                var offset = 0
                for (freq in notes) {
                    val count = (SAMPLE_RATE * (noteMs / 1000.0)).toInt()
                    for (i in 0 until count) {
                        if (offset + i >= totalSamples) break
                        val t = i.toDouble() / SAMPLE_RATE
                        val env = exp(-t * 20.0)
                        val sample = (sin(2 * PI * freq * t) + 0.3 * sin(4 * PI * freq * t)) * env
                        buffer[offset + i] = (sample * Short.MAX_VALUE * gain).toInt().toShort()
                    }
                    offset += count
                }
                playBuffer(buffer)
            } catch (_: Exception) {}
        }
    }

    /**
     * Ascending harmonic major chime with progressive pitch per combo streak (+1 semitone per streak).
     */
    fun playCorrectAnswerReveal(combo: Int = 0) {
        if (!_isSfxEnabled.value) return
        stopTension()
        duckBgm(0.08f, 80)

        audioScope.launch {
            try {
                // Pitch rises with combo streak (max +6 semitones)
                val pitchMultiplier = Math.pow(2.0, (combo.coerceIn(0, 8) * 0.08))
                val baseFreq = 523.25 * pitchMultiplier // C5 base

                val notes = listOf(
                    baseFreq,
                    baseFreq * 1.2599, // Major 3rd
                    baseFreq * 1.4983, // 5th
                    baseFreq * 2.0000  // Octave
                )

                val noteMs = 70
                val totalSamples = (SAMPLE_RATE * ((noteMs * notes.size + 250) / 1000.0)).toInt()
                val buffer = ShortArray(totalSamples)
                val gain = _sfxVolume.value * 0.75f

                var offset = 0
                for (freq in notes) {
                    val count = (SAMPLE_RATE * (noteMs / 1000.0)).toInt()
                    for (i in 0 until count) {
                        if (offset + i >= totalSamples) break
                        val t = i.toDouble() / SAMPLE_RATE
                        val env = (1.0 - (i.toDouble() / count)).coerceAtLeast(0.0)
                        val sample = (sin(2 * PI * freq * t) * 0.55 + sin(4 * PI * freq * t) * 0.35 + sin(6 * PI * freq * t) * 0.10) * env
                        buffer[offset + i] = (sample * Short.MAX_VALUE * gain).toInt().toShort()
                    }
                    offset += count
                }

                // Shimmering chime sustain
                val lastFreq = notes.last()
                val remSamples = totalSamples - offset
                for (i in 0 until remSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val env = exp(-t * 5.0)
                    val sample = (sin(2 * PI * lastFreq * t) * 0.65 + sin(4 * PI * lastFreq * t) * 0.35) * env
                    buffer[offset + i] = (sample * Short.MAX_VALUE * gain).toInt().toShort()
                }

                playBuffer(buffer)
                delay(500)
                unduckBgm(250)
            } catch (e: Exception) {
                Log.w(TAG, "Correct jingle error: ${e.message}")
                unduckBgm(100)
            }
        }
    }

    /**
     * Clear, gentle but unequivocal game show buzzer.
     */
    fun playWrongAnswerReveal() {
        if (!_isSfxEnabled.value) return
        stopTension()
        duckBgm(0.08f, 80)

        audioScope.launch {
            try {
                val durationMs = 320
                val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)
                val gain = _sfxVolume.value * 0.70f

                val f1 = 220.00 // A3
                val f2 = 311.13 // Eb4 (Tritone)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val env = exp(-t * 6.0)

                    val wave1 = if (sin(2 * PI * f1 * t) > 0) 0.45 else -0.45
                    val wave2 = if (sin(2 * PI * f2 * t) > 0) 0.45 else -0.45
                    val wave = (wave1 + wave2) * 0.5

                    val sampleVal = (wave * env * Short.MAX_VALUE * gain).toInt()
                    buffer[i] = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playBuffer(buffer)
                delay(400)
                unduckBgm(200)
            } catch (e: Exception) {
                Log.w(TAG, "Wrong answer buzzer error: ${e.message}")
                unduckBgm(100)
            }
        }
    }

    /**
     * Futuristic laser whoosh for lifelines (50/50, Skip).
     */
    fun playLifelineUsed() {
        if (!_isSfxEnabled.value) return

        audioScope.launch {
            try {
                val durationMs = 240
                val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)
                val gain = _sfxVolume.value * 0.60f

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val progress = i.toDouble() / numSamples
                    val freq = 400.0 + (1200.0 * progress)
                    val env = sin(PI * progress)
                    val wave = sin(2 * PI * freq * t) * env
                    buffer[i] = (wave * Short.MAX_VALUE * gain).toInt().toShort()
                }

                playBuffer(buffer)
            } catch (e: Exception) {
                Log.w(TAG, "Lifeline sound error: ${e.message}")
            }
        }
    }

    /**
     * Triumphal victory fanfare.
     */
    fun playVictoryFanfare() {
        if (!_isSfxEnabled.value) return
        stopTension()

        audioScope.launch {
            try {
                val chords = listOf(
                    listOf(523.25, 659.25, 783.99),
                    listOf(587.33, 739.99, 880.00),
                    listOf(659.25, 830.61, 987.77),
                    listOf(783.99, 987.77, 1174.66, 1567.98)
                )

                for (chord in chords) {
                    val chordMs = 140
                    val numSamples = (SAMPLE_RATE * (chordMs / 1000.0)).toInt()
                    val buffer = ShortArray(numSamples)
                    val gain = _sfxVolume.value * 0.70f

                    for (i in 0 until numSamples) {
                        val t = i.toDouble() / SAMPLE_RATE
                        val env = (1.0 - (i.toDouble() / numSamples)).coerceAtLeast(0.0)
                        var wave = 0.0
                        for (freq in chord) {
                            wave += (sin(2 * PI * freq * t) + 0.3 * sin(4 * PI * freq * t)) / chord.size
                        }
                        buffer[i] = (wave * env * Short.MAX_VALUE * gain).toInt().toShort()
                    }
                    playBuffer(buffer)
                    delay(120)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Fanfare error: ${e.message}")
            }
        }
    }

    // -------------------------------------------------------------
    // 3. COUNTDOWN TENSION
    // -------------------------------------------------------------

    fun updateChronoTension(timeRemaining: Int, maxTime: Int = 15) {
        if (!_isMusicEnabled.value) return
        tensionJob?.cancel()

        // Only play pulse if under 10s to avoid annoying constant ticks
        if (timeRemaining > 8) return

        tensionJob = audioScope.launch {
            try {
                val isUrgent = timeRemaining <= 4
                val durationMs = if (isUrgent) 200 else 130
                val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
                val samples = ShortArray(numSamples)

                val pitch = if (isUrgent) 587.33 else 440.0
                val gain = _sfxVolume.value * (if (isUrgent) 0.60f else 0.40f)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val decay = exp(-t * (if (isUrgent) 18.0 else 28.0))
                    val wave = (sin(2 * PI * pitch * t) * 0.5 + sin(4 * PI * pitch * t) * 0.25)
                    samples[i] = (wave * decay * Short.MAX_VALUE * gain).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playBuffer(samples)
            } catch (e: Exception) {
                Log.w(TAG, "Chrono tension error: ${e.message}")
            }
        }
    }

    fun stopTension() {
        tensionJob?.cancel()
        tensionJob = null
    }

    // -------------------------------------------------------------
    // 4. CALIBRATED PROCEDURAL SYNTHESIS
    // -------------------------------------------------------------

    private suspend fun runCalibratedQuizLoop() {
        val loopSec = 3
        val totalSamples = SAMPLE_RATE * loopSec
        val buffer = ShortArray(totalSamples)

        val arpegNotes = doubleArrayOf(293.66, 440.00, 349.23, 523.25, 440.00, 349.23)
        val noteLengthSamples = totalSamples / arpegNotes.size

        for (n in arpegNotes.indices) {
            val freq = arpegNotes[n]
            val startIdx = n * noteLengthSamples
            for (i in 0 until noteLengthSamples) {
                val idx = startIdx + i
                if (idx >= totalSamples) break
                val t = i.toDouble() / SAMPLE_RATE
                val noteEnv = exp(-t * 9.0)
                val synth = (sin(2 * PI * freq * t) * 0.6 + sin(4 * PI * freq * t) * 0.4) * noteEnv
                buffer[idx] = (synth * Short.MAX_VALUE * 0.25f).toInt().toShort()
            }
        }

        while (audioScope.isActive && _currentBgmTrack.value == BgmTrackType.QUIZ_PLATEAU_SUSPENSE && mediaPlayer == null) {
            val vol = _bgmVolume.value
            if (vol > 0.01f) {
                val scaled = ShortArray(totalSamples)
                for (i in 0 until totalSamples) {
                    scaled[i] = (buffer[i] * vol).toInt().toShort()
                }
                playBuffer(scaled)
            }
            delay((loopSec * 1000L) - 80)
        }
    }

    private suspend fun runCalibratedMenuLoop() {
        val loopSec = 4
        val totalSamples = SAMPLE_RATE * loopSec
        val buffer = ShortArray(totalSamples)

        val notes = doubleArrayOf(392.00, 440.00, 523.25, 587.33)
        val noteLen = totalSamples / notes.size

        for (n in notes.indices) {
            val freq = notes[n]
            val start = n * noteLen
            for (i in 0 until noteLen) {
                val idx = start + i
                if (idx >= totalSamples) break
                val t = i.toDouble() / SAMPLE_RATE
                val env = 0.8 + 0.2 * sin(2 * PI * 2.0 * t)
                val wave = (sin(2 * PI * freq * t) * 0.5 + sin(4 * PI * freq * t) * 0.3) * env
                buffer[idx] = (wave * Short.MAX_VALUE * 0.20f).toInt().toShort()
            }
        }

        while (audioScope.isActive && _currentBgmTrack.value == BgmTrackType.MENU_AMBIENT && mediaPlayer == null) {
            val vol = _bgmVolume.value
            if (vol > 0.01f) {
                val scaled = ShortArray(totalSamples)
                for (i in 0 until totalSamples) {
                    scaled[i] = (buffer[i] * vol).toInt().toShort()
                }
                playBuffer(scaled)
            }
            delay((loopSec * 1000L) - 80)
        }
    }

    private suspend fun runCalibratedResultsLoop() {
        val loopSec = 3
        val totalSamples = SAMPLE_RATE * loopSec
        val buffer = ShortArray(totalSamples)

        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
        val noteLen = totalSamples / notes.size

        for (n in notes.indices) {
            val freq = notes[n]
            val start = n * noteLen
            for (i in 0 until noteLen) {
                val idx = start + i
                if (idx >= totalSamples) break
                val t = i.toDouble() / SAMPLE_RATE
                val wave = (sin(2 * PI * freq * t) * 0.5 + sin(4 * PI * freq * t) * 0.35) * exp(-t * 5.0)
                buffer[idx] = (wave * Short.MAX_VALUE * 0.25f).toInt().toShort()
            }
        }

        while (audioScope.isActive && _currentBgmTrack.value == BgmTrackType.RESULTS_TRIUMPH && mediaPlayer == null) {
            val vol = _bgmVolume.value
            if (vol > 0.01f) {
                val scaled = ShortArray(totalSamples)
                for (i in 0 until totalSamples) {
                    scaled[i] = (buffer[i] * vol).toInt().toShort()
                }
                playBuffer(scaled)
            }
            delay((loopSec * 1000L) - 80)
        }
    }

    private fun playBuffer(buffer: ShortArray) {
        var track: AudioTrack? = null
        try {
            val bufferSize = buffer.size * 2
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val format = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            track = AudioTrack.Builder()
                .setAudioAttributes(attributes)
                .setAudioFormat(format)
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()

            val durationMs = (buffer.size * 1000L) / SAMPLE_RATE
            audioScope.launch {
                delay(durationMs + 60)
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            Log.w(TAG, "playBuffer error: ${e.message}")
            try {
                track?.release()
            } catch (_: Exception) {}
        }
    }
}
