package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

data class PlaybackStatus(
    val isPlaying: Boolean = false,
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0,
    val currentTrackTitle: String = "",
    val isMusicPlaying: Boolean = false
)

class AudioPlayer(private val context: Context) {
    private var narrationPlayer: MediaPlayer? = null
    private var musicPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _status = MutableStateFlow(PlaybackStatus())
    val status: StateFlow<PlaybackStatus> = _status.asStateFlow()

    // Configurable volumes
    var narrationVolume: Float = 1.0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            narrationPlayer?.setVolume(field, field)
        }

    var musicVolume: Float = 0.3f
        set(value) {
            field = value.coerceIn(0f, 1f)
            applyMusicDucking()
        }

    var isDuckingEnabled: Boolean = true
    var isFadeInOutEnabled: Boolean = true

    fun playTrack(filePath: String, title: String = "") {
        stop()
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Log.e("AudioPlayer", "File does not exist: $filePath")
                return
            }

            narrationPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                setOnPreparedListener { mp ->
                    mp.start()
                    mp.setVolume(narrationVolume, narrationVolume)
                    _status.value = _status.value.copy(
                        isPlaying = true,
                        durationMs = mp.duration,
                        currentPositionMs = 0,
                        currentTrackTitle = title
                    )
                    startProgressTracker()
                }
                setOnCompletionListener {
                    stop()
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error playing track: ${e.message}", e)
            stop()
        }
    }

    /**
     * Plays combined audiobook preview: narration track + background music with ducking
     */
    fun playAudiobookProject(
        narrationPath: String?,
        musicType: String, // "calm", "rain", "none"
        narrationVol: Float = 1.0f,
        musicVol: Float = 0.25f,
        ducking: Boolean = true
    ) {
        stop()
        narrationVolume = narrationVol
        musicVolume = musicVol
        isDuckingEnabled = ducking

        // Start background music if requested
        val musicResId = when (musicType) {
            "calm" -> R.raw.bg_calm_harmony
            "rain" -> R.raw.bg_gentle_rain
            else -> null
        }

        if (musicResId != null) {
            try {
                musicPlayer = MediaPlayer.create(context, musicResId)?.apply {
                    isLooping = true
                    // Start with ducked volume if narration is about to play
                    val targetVol = if (ducking && narrationPath != null) musicVolume * 0.35f else musicVolume
                    setVolume(targetVol, targetVol)
                    start()
                }
            } catch (e: Exception) {
                Log.e("AudioPlayer", "Error creating music player: ${e.message}", e)
            }
        }

        // Play narration if available
        if (!narrationPath.isNullOrBlank()) {
            val file = File(narrationPath)
            if (file.exists()) {
                try {
                    narrationPlayer = MediaPlayer().apply {
                        setDataSource(narrationPath)
                        setOnPreparedListener { mp ->
                            mp.start()
                            mp.setVolume(narrationVolume, narrationVolume)
                            _status.value = _status.value.copy(
                                isPlaying = true,
                                durationMs = mp.duration,
                                currentPositionMs = 0,
                                currentTrackTitle = "کتاب صوتی",
                                isMusicPlaying = musicPlayer?.isPlaying == true
                            )
                            startProgressTracker()
                        }
                        setOnCompletionListener {
                            // When narration completes, restore music volume smoothly
                            restoreMusicVolume()
                            _status.value = _status.value.copy(isPlaying = false)
                        }
                        prepareAsync()
                    }
                } catch (e: Exception) {
                    Log.e("AudioPlayer", "Error playing narration: ${e.message}", e)
                }
            }
        } else {
            // Only music is playing
            _status.value = _status.value.copy(
                isPlaying = true,
                durationMs = musicPlayer?.duration ?: 0,
                currentPositionMs = 0,
                currentTrackTitle = "موسیقی زمینه",
                isMusicPlaying = true
            )
            startProgressTracker()
        }
    }

    private fun applyMusicDucking() {
        val mp = musicPlayer ?: return
        val currentNarrationActive = narrationPlayer?.isPlaying == true
        val target = if (isDuckingEnabled && currentNarrationActive) {
            musicVolume * 0.35f // lower music by 65% during speech
        } else {
            musicVolume
        }
        mp.setVolume(target, target)
    }

    private fun restoreMusicVolume() {
        musicPlayer?.setVolume(musicVolume, musicVolume)
    }

    fun playSoundEffect(fxType: String) { // "chime", "page"
        val resId = when (fxType) {
            "chime" -> R.raw.fx_intro_chime
            "page" -> R.raw.fx_page_turn
            else -> return
        }
        try {
            MediaPlayer.create(context, resId)?.apply {
                setOnCompletionListener { release() }
                start()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Failed to play sound effect: ${e.message}", e)
        }
    }

    fun pause() {
        narrationPlayer?.pause()
        musicPlayer?.pause()
        _status.value = _status.value.copy(isPlaying = false)
    }

    fun resume() {
        narrationPlayer?.start()
        musicPlayer?.start()
        applyMusicDucking()
        _status.value = _status.value.copy(isPlaying = true)
    }

    fun seekTo(positionMs: Int) {
        narrationPlayer?.seekTo(positionMs)
        _status.value = _status.value.copy(currentPositionMs = positionMs)
    }

    fun stop() {
        progressJob?.cancel()
        progressJob = null

        try {
            narrationPlayer?.stop()
            narrationPlayer?.release()
        } catch (ignored: Exception) {}
        narrationPlayer = null

        try {
            musicPlayer?.stop()
            musicPlayer?.release()
        } catch (ignored: Exception) {}
        musicPlayer = null

        _status.value = PlaybackStatus()
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && (narrationPlayer?.isPlaying == true || musicPlayer?.isPlaying == true)) {
                val pos = narrationPlayer?.currentPosition ?: (musicPlayer?.currentPosition ?: 0)
                _status.value = _status.value.copy(currentPositionMs = pos)
                delay(200)
            }
        }
    }

    fun release() {
        stop()
    }
}
