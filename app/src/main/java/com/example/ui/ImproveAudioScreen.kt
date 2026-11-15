package com.example.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.EnhancementSettings
import com.example.data.local.AudioItem
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberGold
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImproveAudioScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val context = LocalContext.current
    val allAudioItems by viewModel.allAudioItems.collectAsState()
    val selectedItem by viewModel.selectedAudioItem.collectAsState()
    val enhancementProgress by viewModel.enhancementProgress.collectAsState()
    val lastEnhancedItem by viewModel.lastEnhancedItem.collectAsState()
    val playbackStatus by viewModel.audioPlayer.status.collectAsState()

    var activeAudioItem by remember(selectedItem, allAudioItems) {
        mutableStateOf(selectedItem ?: allAudioItems.firstOrNull())
    }

    // Enhancement toggles
    var noiseReduction by remember { mutableStateOf(true) }
    var normalization by remember { mutableStateOf(true) }
    var clarityEq by remember { mutableStateOf(true) }
    var compression by remember { mutableStateOf(true) }
    var reverb by remember { mutableStateOf(false) }

    // A/B test active playback mode: true = Enhanced, false = Original
    var isPlayingEnhancedVersion by remember { mutableStateOf(true) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "بهبود خودکار کیفیت صدا",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("improve_back_btn")
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
            // Original Preservation Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = StudioCyan.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCyan.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = StudioCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "فایل ضبط شده اصلی شما همواره دست‌نخورده و بدون تغییر باقی می‌ماند. نسخه بهبودیافته به‌صورت مجزا ذخیره می‌شود.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Audio Track Selector Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "انتخاب فایل صوتی جهت پردازش و بهبود:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AmberGold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (allAudioItems.isEmpty()) {
                        Text(
                            text = "هنوز فایلی در آرشیو وجود ندارد. ابتدا از بخش «ضبط صدای من» صدایی ضبط کنید.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        allAudioItems.take(4).forEach { item ->
                            val isSelected = activeAudioItem?.id == item.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { activeAudioItem = item },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) AmberDark.copy(alpha = 0.2f) else MaterialTheme.colorScheme.background,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, AmberGold) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            text = if (item.isEnhanced) "نسخه ارتقا یافته" else "نسخه اصلی",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (item.isEnhanced) StudioCyan else AmberGold
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = AmberGold, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Processing Options / Toggles
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ماژول‌های ارتقای هوشمند استودیویی:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    EnhancementToggleRow(
                        title = "حذف نویز محیطی (Noise Gate)",
                        subtitle = "حذف صدای پس‌زمینه و هیس میکروفون در فواصل سکوت کلمات",
                        checked = noiseReduction,
                        onCheckedChange = { noiseReduction = it }
                    )

                    EnhancementToggleRow(
                        title = "یکسان‌سازی بلندی صدا (Volume Normalization)",
                        subtitle = "تنظیم و رساندن اوج بلندی صدا به تراز استاندارد گویندگی کتاب صوتی (-0.5 dB)",
                        checked = normalization,
                        onCheckedChange = { normalization = it }
                    )

                    EnhancementToggleRow(
                        title = "شفاف‌سازی و اکولایزر صدا (Voice Clarity EQ)",
                        subtitle = "حذف بم‌های زائد زیر ۱۰۰ هرتز و تقویت حضور فرکانس‌های کلامی",
                        checked = clarityEq,
                        onCheckedChange = { clarityEq = it }
                    )

                    EnhancementToggleRow(
                        title = "فشرده‌سازی داینامیک ملایم (Gentle Compression)",
                        subtitle = "کاهش اختلاف بلندی بین زمزمه‌ها و صدای رسا جهت شنیداری نرم و یکدست",
                        checked = compression,
                        onCheckedChange = { compression = it }
                    )

                    EnhancementToggleRow(
                        title = "طنین محیطی ملایم (Subtle Reverb)",
                        subtitle = "شبیه‌سازی بازتاب اتاق آکوستیک جهت افزودن عمق به صدا",
                        checked = reverb,
                        onCheckedChange = { reverb = it }
                    )
                }
            }

            // Apply Action Button
            if (activeAudioItem != null) {
                val item = activeAudioItem!!
                Button(
                    onClick = {
                        val settings = EnhancementSettings(
                            noiseReductionEnabled = noiseReduction,
                            normalizationEnabled = normalization,
                            clarityEqEnabled = clarityEq,
                            compressionEnabled = compression,
                            reverbEnabled = reverb
                        )
                        viewModel.enhanceSelectedAudio(item, settings) { enhanced ->
                            Toast.makeText(context, "نسخه بهبودیافته با موفقیت ذخیره شد", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = enhancementProgress == null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("apply_enhancement_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberDark),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (enhancementProgress != null) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("در حال بهینه‌سازی صدا و فیلترینگ نویز...")
                    } else {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "اعمال بهبود کیفیت و ایجاد نسخه استودیویی",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Enhancement progress bar
            if (enhancementProgress != null) {
                LinearProgressIndicator(
                    progress = { enhancementProgress ?: 0f },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = AmberGold
                )
            }

            // A/B Comparison Player
            if (lastEnhancedItem != null || activeAudioItem?.isEnhanced == true) {
                val enhancedItem = lastEnhancedItem ?: activeAudioItem!!
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, SuccessGreen.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "مقایسه زنده صدای اصلی و بهبود یافته (A/B Test)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // A/B Toggle Switcher
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    isPlayingEnhancedVersion = false
                                    val originalPath = enhancedItem.originalFilePath ?: enhancedItem.filePath
                                    viewModel.audioPlayer.playTrack(originalPath, "صدای اصلی")
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!isPlayingEnhancedVersion) AmberDark else MaterialTheme.colorScheme.background
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "صدای اصلی",
                                    color = if (!isPlayingEnhancedVersion) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Button(
                                onClick = {
                                    isPlayingEnhancedVersion = true
                                    viewModel.audioPlayer.playTrack(enhancedItem.filePath, "صدای بهبود یافته استودیویی")
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isPlayingEnhancedVersion) SuccessGreen else MaterialTheme.colorScheme.background
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "صدای بهبود یافته ✓",
                                    color = if (isPlayingEnhancedVersion) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Share / Export Enhanced Audio
                        OutlinedButton(
                            onClick = { viewModel.shareAudio(context, enhancedItem) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("دانلود و اشتراک فایل استودیویی بهبود یافته")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancementToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AmberGold,
                checkedTrackColor = AmberDark.copy(alpha = 0.4f)
            )
        )
    }
}
