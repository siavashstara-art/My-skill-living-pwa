package com.example.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.VoiceProviderConfig
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberGold
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val context = LocalContext.current
    val voiceConfig by viewModel.voiceConfig.collectAsState()

    var providerType by remember(voiceConfig) { mutableStateOf(voiceConfig.providerType) }
    var apiKey by remember(voiceConfig) { mutableStateOf(voiceConfig.apiKey) }
    var endpointUrl by remember(voiceConfig) { mutableStateOf(voiceConfig.endpointUrl) }
    var voiceId by remember(voiceConfig) { mutableStateOf(voiceConfig.voiceId) }
    var isTestingVoice by remember { mutableStateOf(false) }

    val isCurrentConfigured = when (providerType) {
        "ELEVENLABS" -> apiKey.isNotBlank() && voiceId.isNotBlank()
        "AZURE" -> apiKey.isNotBlank() && endpointUrl.isNotBlank()
        "CUSTOM_HTTP" -> endpointUrl.isNotBlank()
        else -> false
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "تنظیمات صدای شخصی AI و TTS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("settings_back_btn")
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
            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrentConfigured) SuccessGreen.copy(alpha = 0.12f) else AmberGold.copy(alpha = 0.12f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isCurrentConfigured) SuccessGreen.copy(alpha = 0.4f) else AmberGold.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isCurrentConfigured) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                        contentDescription = null,
                        tint = if (isCurrentConfigured) SuccessGreen else AmberGold,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isCurrentConfigured) "سرویس صدای شخصی هوش مصنوعی: فعال و متصل" else "سرویس خارجی هنوز پیکربندی نشده است",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isCurrentConfigured) "درخواست‌های گویندگی مستقیماً به سرویس شبیه‌سازی صدای شخصی شما ارسال خواهند شد."
                            else "سیستم به صورت خودکار و بدون اختلال از موتور استاندارد TTS دستگاه استفاده می‌کند.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Provider Type Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "انتخاب سرویس‌دهنده صدای شخصی / گویندگی:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AmberGold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = (providerType == "SYSTEM_TTS"),
                            onClick = { providerType = "SYSTEM_TTS" },
                            label = { Text("موتور صوتی داخلی دستگاه (TTS محلی)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberDark.copy(alpha = 0.25f),
                                selectedLabelColor = AmberGold
                            )
                        )
                        FilterChip(
                            selected = (providerType == "ELEVENLABS"),
                            onClick = { providerType = "ELEVENLABS" },
                            label = { Text("ElevenLabs (سرویس شبیه‌سازی صدای شخصی)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StudioCyan.copy(alpha = 0.25f),
                                selectedLabelColor = StudioCyan
                            )
                        )
                        FilterChip(
                            selected = (providerType == "AZURE"),
                            onClick = { providerType = "AZURE" },
                            label = { Text("Microsoft Azure Custom Neural Voice") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        FilterChip(
                            selected = (providerType == "CUSTOM_HTTP"),
                            onClick = { providerType = "CUSTOM_HTTP" },
                            label = { Text("External Custom Voice API (سرویس اختصاصی)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Provider Configuration Fields
            AnimatedVisibility(visible = providerType != "SYSTEM_TTS") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "مشخصات اتصال به سرویس شخصی:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        // API Key
                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            label = { Text("کلید دسترسی (API Key / Subscription Key)") },
                            leadingIcon = { Icon(Icons.Filled.Key, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        // Voice ID (for ElevenLabs)
                        if (providerType == "ELEVENLABS") {
                            OutlinedTextField(
                                value = voiceId,
                                onValueChange = { voiceId = it },
                                label = { Text("شناسه صدای شخصی (Voice ID)") },
                                leadingIcon = { Icon(Icons.Filled.RecordVoiceOver, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        // Endpoint URL (for Azure or Custom)
                        if (providerType == "AZURE" || providerType == "CUSTOM_HTTP") {
                            OutlinedTextField(
                                value = endpointUrl,
                                onValueChange = { endpointUrl = it },
                                label = { Text("آدرس سرور یا ریجن (Endpoint URL / Region)") },
                                leadingIcon = { Icon(Icons.Filled.Link, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            // Save and Test Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Test Sample Voice Button
                OutlinedButton(
                    onClick = {
                        isTestingVoice = true
                        val testConfig = voiceConfig.copy(
                            providerType = providerType,
                            apiKey = apiKey,
                            endpointUrl = endpointUrl,
                            voiceId = voiceId
                        )
                        viewModel.speakVoice(
                            "سلام، این صدای استودیوی آفرینا برای گویندگی کتاب صوتی است.",
                            testConfig
                        ) {
                            isTestingVoice = false
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("test_voice_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isTestingVoice) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.Filled.Hearing, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تست صدای نمونه")
                    }
                }

                // Save Configuration Button
                Button(
                    onClick = {
                        val newConfig = voiceConfig.copy(
                            providerType = providerType,
                            apiKey = apiKey.trim(),
                            endpointUrl = endpointUrl.trim(),
                            voiceId = voiceId.trim()
                        )
                        viewModel.updateVoiceConfig(newConfig)
                        Toast.makeText(context, "تنظیمات با موفقیت ذخیره شد", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(52.dp)
                        .testTag("save_voice_config_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ذخیره تنظیمات", fontWeight = FontWeight.Bold)
                }
            }

            // Reset to default
            OutlinedButton(
                onClick = {
                    providerType = "SYSTEM_TTS"
                    apiKey = ""
                    endpointUrl = ""
                    voiceId = ""
                    viewModel.updateVoiceConfig(VoiceProviderConfig())
                    Toast.makeText(context, "تنظیمات به حالت پیش‌فرض بازگشت", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("بازنشانی به موتور صوتی پیش‌فرض دستگاه")
            }
        }
    }
}
