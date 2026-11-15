package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class VoiceProviderConfig(
    val providerType: String = "SYSTEM_TTS", // "ELEVENLABS", "AZURE", "CUSTOM_HTTP", "SYSTEM_TTS"
    val apiKey: String = "",
    val endpointUrl: String = "",
    val voiceId: String = "",
    val selectedLanguage: String = "fa", // "fa", "en", "ar"
    val speechRate: Float = 1.0f,
    val pitch: Float = 1.0f
) {
    val isConfigured: Boolean
        get() = when (providerType) {
            "ELEVENLABS" -> apiKey.isNotBlank() && voiceId.isNotBlank()
            "AZURE" -> apiKey.isNotBlank() && endpointUrl.isNotBlank()
            "CUSTOM_HTTP" -> endpointUrl.isNotBlank()
            else -> false // SYSTEM_TTS is always available on device
        }

    val providerDisplayName: String
        get() = when (providerType) {
            "ELEVENLABS" -> "ElevenLabs Personal Voice"
            "AZURE" -> "Microsoft Azure Custom Neural Voice"
            "CUSTOM_HTTP" -> "External Voice API"
            else -> "موتور صوتی داخلی دستگاه (TTS محلی)"
        }
}

class VoiceSettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("afrina_voice_prefs", Context.MODE_PRIVATE)

    private val _configFlow = MutableStateFlow(loadConfig())
    val configFlow: StateFlow<VoiceProviderConfig> = _configFlow.asStateFlow()

    private fun loadConfig(): VoiceProviderConfig {
        return VoiceProviderConfig(
            providerType = prefs.getString("provider_type", "SYSTEM_TTS") ?: "SYSTEM_TTS",
            apiKey = prefs.getString("api_key", "") ?: "",
            endpointUrl = prefs.getString("endpoint_url", "") ?: "",
            voiceId = prefs.getString("voice_id", "") ?: "",
            selectedLanguage = prefs.getString("selected_lang", "fa") ?: "fa",
            speechRate = prefs.getFloat("speech_rate", 1.0f),
            pitch = prefs.getFloat("pitch", 1.0f)
        )
    }

    fun saveConfig(config: VoiceProviderConfig) {
        prefs.edit()
            .putString("provider_type", config.providerType)
            .putString("api_key", config.apiKey)
            .putString("endpoint_url", config.endpointUrl)
            .putString("voice_id", config.voiceId)
            .putString("selected_lang", config.selectedLanguage)
            .putFloat("speech_rate", config.speechRate)
            .putFloat("pitch", config.pitch)
            .apply()
        _configFlow.value = config
    }
}
