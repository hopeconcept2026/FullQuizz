package com.example.core.audio

import kotlinx.coroutines.flow.StateFlow

/**
 * Dynamic TV Game Show Audio Engine (Facade to AudioPlayerManager).
 */
object GameShowAudioEngine {
    private val manager = AudioPlayerManager.getInstance()

    val isMusicEnabled: StateFlow<Boolean> = manager.isMusicEnabled
    val isMenuMusicEnabled: StateFlow<Boolean> = manager.isMenuMusicEnabled
    val isSfxEnabled: StateFlow<Boolean> = manager.isSfxEnabled
    val bgmVolume: StateFlow<Float> = manager.bgmVolume
    val sfxVolume: StateFlow<Float> = manager.sfxVolume
    val currentBgmTrack: StateFlow<BgmTrackType> = manager.currentBgmTrack

    fun setMusicEnabled(enabled: Boolean) {
        manager.setMusicEnabled(enabled)
    }

    fun setMenuMusicEnabled(enabled: Boolean) {
        manager.setMenuMusicEnabled(enabled)
    }

    fun setSfxEnabled(enabled: Boolean) {
        manager.setSfxEnabled(enabled)
    }

    fun setBgmVolume(volume: Float) {
        manager.setBgmVolume(volume)
    }

    fun setSfxVolume(volume: Float) {
        manager.setSfxVolume(volume)
    }

    fun toggleMusic() {
        manager.toggleMusic()
    }

    fun toggleSfx() {
        manager.toggleSfx()
    }

    fun playBgm(trackType: BgmTrackType, fadeInDurationMs: Long = 400) {
        manager.playBgm(trackType, fadeInDurationMs)
    }

    fun stopBgm(fadeOutDurationMs: Long = 250) {
        manager.stopBgm(fadeOutDurationMs)
    }

    fun startTension(timeRemaining: Int, maxTime: Int = 15) {
        manager.updateChronoTension(timeRemaining, maxTime)
    }

    fun stopTension() {
        manager.stopTension()
    }

    fun playOptionClick() {
        manager.playOptionClick()
    }

    fun playCoinCollect() {
        manager.playCoinCollect()
    }

    fun playCorrectAnswer(combo: Int = 0) {
        manager.playCorrectAnswerReveal(combo)
    }

    fun playWrongAnswer() {
        manager.playWrongAnswerReveal()
    }

    fun playLifeline() {
        manager.playLifelineUsed()
    }

    fun playVictoryFanfare() {
        manager.playVictoryFanfare()
    }

    fun duckBgm(targetVolume: Float = 0.10f, durationMs: Long = 120) {
        manager.duckBgm(targetVolume, durationMs)
    }

    fun unduckBgm(durationMs: Long = 250) {
        manager.unduckBgm(durationMs)
    }
}
