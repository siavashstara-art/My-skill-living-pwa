package com.example.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.data.local.AudioItem
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberGold
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioTeal
import com.example.ui.theme.SuccessGreen
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateAudioScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val context = LocalContext.current
    val prefilledText by viewModel.prefilledTtsText.collectAsState()
    val voiceConfig by viewModel.voiceConfig.collectAsState()
    val isGenerating by viewModel.isGeneratingAudio.collectAsState()
    val playbackStatus by viewModel.audioPlayer.status.collectAsState()

    var textToSpeak by remember {
        mutableStateOf(
            if (prefilledText.isNotBlank()) prefilledText
            else "به استودیوی صوتی آفرینا خوش آمدید. ما کتاب‌های صوتی و پادکست‌های شما را با بالاترین کیفیت آماده می‌کنیم."
        )
    }

    var trackTitle by remember { mutableStateOf("گویندگی بخش اول") }
    var selectedLang by remember { mutableStateOf(voiceConfig.selectedLanguage) }
    var speechRate by remember { mutableFloatStateOf(voiceConfig.speechRate) }
    var speechPitch by remember { mutableFloatStateOf(voiceConfig.pitch) }
    var generatedItem by remember { mutableStateOf<AudioItem?>(null) }
    var isLiveSpeaking by remember { mutableStateOf(false) }

    LaunchedEffect(prefilledText) {
        if (prefilledText.isNotBlank()) {
            textToSpeak = prefilledText
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "تولید صوت (تبدیل متن به گفتار)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("gen_audio_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Voice Provider Status Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (voiceConfig.isConfigured) SuccessGreen.copy(alpha = 0.1f) else AmberGold.copy(alpha = 0.1f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (voiceConfig.isConfigured) SuccessGreen.copy(alpha = 0.4f) else AmberGold.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (voiceConfig.isConfigured) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                            contentDescription = null,
                            tint = if (voiceConfig.isConfigured) SuccessGreen else AmberGold
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (voiceConfig.isConfigured) "سرویس صدای شخصی متصل است" else "سرویس خارجی هنوز پیکربندی نشده است",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (voiceConfig.isConfigured) voiceConfig.providerDisplayName else "استفاده از موتور صوتی استاندارد دستگاه",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.navigateTo(StudioScreen.SETTINGS) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("تنظیمات", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Language Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "انتخاب زبان گویندگی:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = (selectedLang == "fa"),
                            onClick = {
                                selectedLang = "fa"
                                viewModel.updateVoiceConfig(voiceConfig.copy(selectedLanguage = "fa"))
                            },
                            label = { Text("فارسی (Persian First)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberDark.copy(alpha = 0.25f),
                                selectedLabelColor = AmberGold
                            )
                        )
                        FilterChip(
                            selected = (selectedLang == "en"),
                            onClick = {
                                selectedLang = "en"
                                viewModel.updateVoiceConfig(voiceConfig.copy(selectedLanguage = "en"))
                            },
                            label = { Text("English") }
                        )
                        FilterChip(
                            selected = (selectedLang == "ar"),
                            onClick = {
                                selectedLang = "ar"
                                viewModel.updateVoiceConfig(voiceConfig.copy(selectedLanguage = "ar"))
                            },
                            label = { Text("العربية") }
                        )
                    }
                }
            }

            // Track Title
            OutlinedTextField(
                value = trackTitle,
                onValueChange = { trackTitle = it },
                label = { Text("عنوان فایل صوتی خروجی") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Text Input
            OutlinedTextField(
                value = textToSpeak,
                onValueChange = { textToSpeak = it },
                label = { Text("متن جهت تبدیل به گفتار") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Speed & Pitch Sliders
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("سرعت گویندگی: ${String.format(Locale.US, "%.1fx", speechRate)}", style = MaterialTheme.typography.labelMedium)
                        Text(if (speechRate == 1.0f) "عادی" else if (speechRate > 1f) "سریع" else "آرام", style = MaterialTheme.typography.labelSmall, color = AmberGold)
                    }
                    Slider(
                        value = speechRate,
                        onValueChange = {
                            speechRate = it
                            viewModel.updateVoiceConfig(voiceConfig.copy(speechRate = it))
                        },
                        valueRange = 0.6f..1.6f,
                        steps = 9,
                        colors = SliderDefaults.colors(thumbColor = AmberGold, activeTrackColor = AmberDark)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("زیر و بمی صدا (Pitch): ${String.format(Locale.US, "%.1fx", speechPitch)}", style = MaterialTheme.typography.labelMedium)
                        Text(if (speechPitch == 1.0f) "طبیعی" else if (speechPitch > 1f) "زیر" else "بم", style = MaterialTheme.typography.labelSmall, color = AmberGold)
                    }
                    Slider(
                        value = speechPitch,
                        onValueChange = {
                            speechPitch = it
                            viewModel.updateVoiceConfig(voiceConfig.copy(pitch = it))
                        },
                        valueRange = 0.7f..1.4f,
                        steps = 6,
                        colors = SliderDefaults.colors(thumbColor = StudioCyan, activeTrackColor = StudioTeal)
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Live preview speech
                OutlinedButton(
                    onClick = {
                        if (isLiveSpeaking) {
                            viewModel.ttsManager.stop()
                            isLiveSpeaking = false
                        } else {
                            isLiveSpeaking = true
                            viewModel.speakVoice(textToSpeak, voiceConfig) {
                                isLiveSpeaking = false
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("live_tts_preview_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isLiveSpeaking) Icons.Filled.Pause else Icons.Filled.Hearing,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isLiveSpeaking) "توقف خواندن" else "پخش زنده")
                }

                // Synthesize & Save to File
                Button(
                    onClick = {
                        viewModel.generateSpeechAudio(textToSpeak, trackTitle) { item ->
                            generatedItem = item
                            if (item != null) {
                                Toast.makeText(context, "فایل صوتی ذخیره شد", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = textToSpeak.isNotBlank() && !isGenerating,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(52.dp)
                        .testTag("synthesize_to_file_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("در حال تولید فایل...")
                    } else {
                        Icon(Icons.Filled.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ذخیره به فایل صوتی", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Post-Generation Panel
            if (generatedItem != null) {
                val item = generatedItem!!
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "✓ فایل صوتی با موفقیت تولید شد",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = item.title, style = MaterialTheme.typography.bodyMedium)

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (playbackStatus.isPlaying) viewModel.audioPlayer.pause()
                                    else viewModel.audioPlayer.playTrack(item.filePath, item.title)
                                },
                                modifier = Modifier.weight(1f).height(46.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AmberDark),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(if (playbackStatus.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (playbackStatus.isPlaying) "توقف" else "پخش")
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.selectAudioItem(item)
                                    viewModel.navigateTo(StudioScreen.CREATE_AUDIOBOOK)
                                },
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("افزودن به کتاب صوتی")
                            }
                        }
                    }
                }
            }
        }
    }
}
