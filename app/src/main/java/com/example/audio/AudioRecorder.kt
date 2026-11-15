package com.example.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
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
import java.io.File
import java.io.IOException

enum class RecorderState {
    IDLE,
    RECORDING,
    PAUSED,
    STOPPED
}

class AudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null
    private var startTimeMs: Long = 0
    private var pausedDurationMs: Long = 0
    private var pauseStartTimeMs: Long = 0
    private var amplitudeJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _recorderState = MutableStateFlow(RecorderState.IDLE)
    val recorderState: StateFlow<RecorderState> = _recorderState.asStateFlow()

    private val _currentAmplitude = MutableStateFlow(0)
    val currentAmplitude: StateFlow<Int> = _currentAmplitude.asStateFlow()

    private val _recordingDurationMs = MutableStateFlow(0L)
    val recordingDurationMs: StateFlow<Long> = _recordingDurationMs.asStateFlow()

    fun startRecording(targetFile: File): Boolean {
        return try {
            currentFile = targetFile
            val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            mr.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(targetFile.absolutePath)
                prepare()
                start()
            }
            recorder = mr
            startTimeMs = System.currentTimeMillis()
            pausedDurationMs = 0L
            _recorderState.value = RecorderState.RECORDING

            startAmplitudePolling()
            true
        } catch (e: IOException) {
            Log.e("AudioRecorder", "Failed to start recording: ${e.message}", e)
            _recorderState.value = RecorderState.IDLE
            false
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Unexpected error starting recording: ${e.message}", e)
            _recorderState.value = RecorderState.IDLE
            false
        }
    }

    fun pauseRecording() {
        if (_recorderState.value == RecorderState.RECORDING && recorder != null) {
            try {
                recorder?.pause()
                pauseStartTimeMs = System.currentTimeMillis()
                _recorderState.value = RecorderState.PAUSED
            } catch (e: Exception) {
                Log.e("AudioRecorder", "Pause error: ${e.message}", e)
            }
        }
    }

    fun resumeRecording() {
        if (_recorderState.value == RecorderState.PAUSED && recorder != null) {
            try {
                recorder?.resume()
                pausedDurationMs += (System.currentTimeMillis() - pauseStartTimeMs)
                _recorderState.value = RecorderState.RECORDING
            } catch (e: Exception) {
                Log.e("AudioRecorder", "Resume error: ${e.message}", e)
            }
        }
    }

    fun stopRecording(): File? {
        amplitudeJob?.cancel()
        amplitudeJob = null
        val recordedFile = currentFile
        try {
            if (_recorderState.value == RecorderState.RECORDING || _recorderState.value == RecorderState.PAUSED) {
                recorder?.stop()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Stop recording error: ${e.message}", e)
        } finally {
            try {
                recorder?.release()
            } catch (ignored: Exception) {}
            recorder = null
            _recorderState.value = RecorderState.STOPPED
        }
        return recordedFile
    }

    private fun startAmplitudePolling() {
        amplitudeJob?.cancel()
        amplitudeJob = scope.launch {
            while (isActive && (_recorderState.value == RecorderState.RECORDING || _recorderState.value == RecorderState.PAUSED)) {
                if (_recorderState.value == RecorderState.RECORDING) {
                    val amp = try {
                        recorder?.maxAmplitude ?: 0
                    } catch (e: Exception) { 0 }
                    _currentAmplitude.value = amp
                    val elapsed = System.currentTimeMillis() - startTimeMs - pausedDurationMs
                    _recordingDurationMs.value = maxOf(0L, elapsed)
                }
                delay(100)
            }
        }
    }

    fun reset() {
        amplitudeJob?.cancel()
        amplitudeJob = null
        try {
            recorder?.release()
        } catch (ignored: Exception) {}
        recorder = null
        _recorderState.value = RecorderState.IDLE
        _currentAmplitude.value = 0
        _recordingDurationMs.value = 0L
    }
}
