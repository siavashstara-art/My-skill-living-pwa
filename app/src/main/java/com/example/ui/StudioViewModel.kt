package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioEnhancer
import com.example.audio.AudioPlayer
import com.example.audio.AudioRecorder
import com.example.audio.EnhancementSettings
import com.example.audio.PlaybackStatus
import com.example.audio.RecorderState
import com.example.audio.TtsManager
import com.example.data.local.AppDatabase
import com.example.data.local.AudioItem
import com.example.data.local.BookProject
import com.example.data.local.VoiceProviderConfig
import com.example.data.local.VoiceSettingsRepository
import com.example.summarizer.BookSummaryResult
import com.example.summarizer.BookSummarizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class StudioScreen {
    HOME,
    RECORD_VOICE,
    CREATE_AUDIOBOOK,
    SUMMARIZE_BOOK,
    GENERATE_AUDIO,
    IMPROVE_AUDIO,
    LIBRARY,
    SETTINGS
}

class StudioViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.audioStudioDao()
    private val voiceRepo = VoiceSettingsRepository(application)

    val audioRecorder = AudioRecorder(application)
    val audioPlayer = AudioPlayer(application)
    val audioEnhancer = AudioEnhancer(application)
    val ttsManager = TtsManager(application)

    // Current Screen
    private val _currentScreen = MutableStateFlow(StudioScreen.HOME)
    val currentScreen: StateFlow<StudioScreen> = _currentScreen.asStateFlow()

    // Screen Backstack
    private val screenStack = mutableListOf(StudioScreen.HOME)

    // Data Flows
    val allAudioItems: StateFlow<List<AudioItem>> = dao.getAllAudioItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBookProjects: StateFlow<List<BookProject>> = dao.getAllBookProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val voiceConfig: StateFlow<VoiceProviderConfig> = voiceRepo.configFlow

    // Active Selection States
    private val _selectedAudioItem = MutableStateFlow<AudioItem?>(null)
    val selectedAudioItem: StateFlow<AudioItem?> = _selectedAudioItem.asStateFlow()

    // Summarizer State
    private val _summaryResult = MutableStateFlow<BookSummaryResult?>(null)
    val summaryResult: StateFlow<BookSummaryResult?> = _summaryResult.asStateFlow()

    private val _isSummarizing = MutableStateFlow(false)
    val isSummarizing: StateFlow<Boolean> = _isSummarizing.asStateFlow()

    // Audio Enhancement State
    private val _enhancementProgress = MutableStateFlow<Float?>(null)
    val enhancementProgress: StateFlow<Float?> = _enhancementProgress.asStateFlow()

    private val _lastEnhancedItem = MutableStateFlow<AudioItem?>(null)
    val lastEnhancedItem: StateFlow<AudioItem?> = _lastEnhancedItem.asStateFlow()

    // TTS Generation State
    private val _isGeneratingAudio = MutableStateFlow(false)
    val isGeneratingAudio: StateFlow<Boolean> = _isGeneratingAudio.asStateFlow()

    // Text being transferred between screens (e.g. from Summarizer to TTS)
    private val _prefilledTtsText = MutableStateFlow("")
    val prefilledTtsText: StateFlow<String> = _prefilledTtsText.asStateFlow()

    fun navigateTo(screen: StudioScreen) {
        if (_currentScreen.value != screen) {
            screenStack.add(screen)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.size - 1)
            _currentScreen.value = screenStack.last()
            return true
        } else if (_currentScreen.value != StudioScreen.HOME) {
            _currentScreen.value = StudioScreen.HOME
            screenStack.clear()
            screenStack.add(StudioScreen.HOME)
            return true
        }
        return false
    }

    // --- Recording Actions ---
    fun startRecording(): Boolean {
        val dir = getAudioDir()
        val file = File(dir, "afrina_voice_${System.currentTimeMillis()}.m4a")
        return audioRecorder.startRecording(file)
    }

    fun pauseRecording() = audioRecorder.pauseRecording()
    fun resumeRecording() = audioRecorder.resumeRecording()

    fun stopAndSaveRecording(title: String = "ضبط صدای من"): AudioItem? {
        val duration = audioRecorder.recordingDurationMs.value
        val file = audioRecorder.stopRecording() ?: return null

        val item = AudioItem(
            title = if (title.isBlank()) "ضبط صدای جدید" else title,
            filePath = file.absolutePath,
            originalFilePath = file.absolutePath,
            durationMs = duration,
            category = "VOICE",
            isEnhanced = false
        )

        viewModelScope.launch {
            val id = dao.insertAudioItem(item)
            _selectedAudioItem.value = item.copy(id = id)
        }
        return item
    }

    // --- Book Summarization ---
    fun summarizeBook(text: String, title: String) {
        viewModelScope.launch {
            _isSummarizing.value = true
            val result = BookSummarizer.summarize(text, title)
            _summaryResult.value = result

            // Save to BookProject
            val project = BookProject(
                title = title,
                originalText = text,
                compactSummary = result.fullCompact,
                standardSummary = result.fullStandard,
                detailedSummary = result.fullDetailed,
                narrationText = result.fullNarrationReady
            )
            dao.insertBookProject(project)
            _isSummarizing.value = false
        }
    }

    fun sendSummaryToTts(text: String) {
        _prefilledTtsText.value = text
        navigateTo(StudioScreen.GENERATE_AUDIO)
    }

    // --- TTS Generation ---
    fun generateSpeechAudio(text: String, title: String, onComplete: (AudioItem?) -> Unit) {
        viewModelScope.launch {
            _isGeneratingAudio.value = true
            val dir = getAudioDir()
            val outFile = File(dir, "afrina_tts_${System.currentTimeMillis()}.wav")

            val success = ttsManager.synthesizeToFile(text, outFile, voiceConfig.value)
            _isGeneratingAudio.value = false

            if (success && outFile.exists()) {
                val item = AudioItem(
                    title = if (title.isBlank()) "تولید صوت خودکار" else title,
                    filePath = outFile.absolutePath,
                    originalFilePath = outFile.absolutePath,
                    durationMs = 0L,
                    category = "TTS",
                    isEnhanced = false
                )
                val id = dao.insertAudioItem(item)
                val saved = item.copy(id = id)
                _selectedAudioItem.value = saved
                onComplete(saved)
            } else {
                // If direct file synthesis is not supported on some virtual audio hardware, test live playback
                ttsManager.speak(text, voiceConfig.value)
                onComplete(null)
            }
        }
    }

    // --- Audio Enhancement ---
    fun enhanceSelectedAudio(
        item: AudioItem,
        settings: EnhancementSettings,
        onSuccess: (AudioItem) -> Unit
    ) {
        viewModelScope.launch {
            _enhancementProgress.value = 0.1f
            val sourceFile = File(item.filePath)
            val dir = getAudioDir()
            val enhancedFile = File(dir, "enhanced_${System.currentTimeMillis()}.wav")

            val success = audioEnhancer.enhanceAudioFile(
                sourceFile = sourceFile,
                targetFile = enhancedFile,
                settings = settings,
                onProgress = { p -> _enhancementProgress.value = p }
            )

            _enhancementProgress.value = null
            if (success && enhancedFile.exists()) {
                val enhancedItem = AudioItem(
                    title = "${item.title} (کیفیت ارتقا یافته)",
                    filePath = enhancedFile.absolutePath,
                    originalFilePath = item.filePath, // Original is strictly preserved!
                    durationMs = item.durationMs,
                    category = "ENHANCED",
                    isEnhanced = true
                )
                val id = dao.insertAudioItem(enhancedItem)
                val saved = enhancedItem.copy(id = id)
                _lastEnhancedItem.value = saved
                onSuccess(saved)
            }
        }
    }

    fun selectAudioItem(item: AudioItem?) {
        _selectedAudioItem.value = item
    }

    fun deleteAudioItem(item: AudioItem) {
        viewModelScope.launch {
            dao.deleteAudioItem(item)
            try {
                File(item.filePath).delete()
            } catch (ignored: Exception) {}
            if (_selectedAudioItem.value?.id == item.id) {
                _selectedAudioItem.value = null
            }
        }
    }

    fun updateVoiceConfig(config: VoiceProviderConfig) {
        voiceRepo.saveConfig(config)
    }

    fun speakVoice(text: String, config: VoiceProviderConfig = voiceConfig.value, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            ttsManager.speak(text, config, onDone)
        }
    }

    fun shareAudio(context: Context, item: AudioItem) {
        try {
            val file = File(item.filePath)
            if (!file.exists()) {
                Toast.makeText(context, "فایل صوتی یافت نشد", Toast.LENGTH_SHORT).show()
                return
            }
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "اشتراک فایل صوتی آفرینا"))
        } catch (e: Exception) {
            Toast.makeText(context, "خطا در اشتراک فایل: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAudioDir(): File {
        val dir = File(getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_MUSIC), "AfrinaStudio")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
        audioRecorder.reset()
        ttsManager.shutdown()
    }
}
