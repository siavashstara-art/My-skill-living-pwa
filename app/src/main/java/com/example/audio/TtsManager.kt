package com.example.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.local.VoiceProviderConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

class TtsManager(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _availableLanguages = MutableStateFlow<Set<String>>(emptySet())
    val availableLanguages: StateFlow<Set<String>> = _availableLanguages.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            _isInitialized.value = true
            checkLanguages()
        } else {
            Log.e("TtsManager", "TTS initialization failed with code: $status")
        }
    }

    private fun checkLanguages() {
        val supported = mutableSetOf<String>()
        val locales = listOf(
            "fa" to Locale("fa"),
            "en" to Locale.US,
            "ar" to Locale("ar")
        )
        for ((code, locale) in locales) {
            val availability = tts?.isLanguageAvailable(locale) ?: TextToSpeech.LANG_NOT_SUPPORTED
            if (availability >= TextToSpeech.LANG_AVAILABLE) {
                supported.add(code)
            }
        }
        _availableLanguages.value = supported
    }

    /**
     * Speaks text using configured external provider if valid, or Android TTS fallback
     */
    suspend fun speak(
        text: String,
        config: VoiceProviderConfig,
        onDone: () -> Unit = {}
    ) = withContext(Dispatchers.Main) {
        if (config.isConfigured && config.providerType == "ELEVENLABS") {
            // Synthesize to temp file and play
            val tempFile = File(context.cacheDir, "tts_preview_${System.currentTimeMillis()}.mp3")
            val success = synthesizeWithElevenLabs(text, config, tempFile)
            if (success && tempFile.exists()) {
                val player = AudioPlayer(context)
                player.playTrack(tempFile.absolutePath, "پیش‌نمایش صدای AI")
                return@withContext
            }
        }

        // Fallback to Android TextToSpeech
        val engine = tts ?: return@withContext
        val locale = when (config.selectedLanguage) {
            "fa" -> Locale("fa")
            "ar" -> Locale("ar")
            else -> Locale.US
        }

        val result = engine.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // If Persian voice is not installed on system, use English/fallback
            Log.w("TtsManager", "Language $locale not supported, attempting default locale")
            engine.language = Locale.getDefault()
        }

        engine.setSpeechRate(config.speechRate)
        engine.setPitch(config.pitch)

        val utteranceId = UUID.randomUUID().toString()
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) { onDone() }
            override fun onError(utteranceId: String?) { onDone() }
        })

        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    /**
     * Synthesizes audio to a file for saving in library or audiobook chapter
     */
    suspend fun synthesizeToFile(
        text: String,
        targetFile: File,
        config: VoiceProviderConfig
    ): Boolean = withContext(Dispatchers.IO) {
        if (config.isConfigured && config.providerType == "ELEVENLABS") {
            val success = synthesizeWithElevenLabs(text, config, targetFile)
            if (success) return@withContext true
        }

        // Android TTS synthesizeToFile
        val engine = tts ?: return@withContext false
        val locale = when (config.selectedLanguage) {
            "fa" -> Locale("fa")
            "ar" -> Locale("ar")
            else -> Locale.US
        }
        withContext(Dispatchers.Main) {
            engine.setLanguage(locale)
            engine.setSpeechRate(config.speechRate)
            engine.setPitch(config.pitch)
        }

        val utteranceId = UUID.randomUUID().toString()
        val params = Bundle()
        var completed = false
        var error = false

        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {}
            override fun onDone(id: String?) { completed = true }
            override fun onError(id: String?) { error = true }
        })

        val res = engine.synthesizeToFile(text, params, targetFile, utteranceId)
        if (res == TextToSpeech.SUCCESS) {
            // Wait for file writing
            var waitCount = 0
            while (!completed && !error && waitCount < 100) {
                delay(100)
                waitCount++
            }
            return@withContext targetFile.exists() && targetFile.length() > 0
        }
        return@withContext false
    }

    private suspend fun synthesizeWithElevenLabs(
        text: String,
        config: VoiceProviderConfig,
        targetFile: File
    ): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = "https://api.elevenlabs.io/v1/text-to-speech/${config.voiceId}"
            val json = JSONObject().apply {
                put("text", text)
                put("model_id", "eleven_multilingual_v2")
                val voiceSettings = JSONObject().apply {
                    put("stability", 0.5)
                    put("similarity_boost", 0.8)
                }
                put("voice_settings", voiceSettings)
            }
            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .addHeader("xi-api-key", config.apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful && response.body != null) {
                val bytes = response.body!!.bytes()
                FileOutputStream(targetFile).use { it.write(bytes) }
                true
            } else {
                Log.e("TtsManager", "ElevenLabs error: ${response.code} ${response.message}")
                false
            }
        } catch (e: Exception) {
            Log.e("TtsManager", "ElevenLabs API exception: ${e.message}", e)
            false
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
