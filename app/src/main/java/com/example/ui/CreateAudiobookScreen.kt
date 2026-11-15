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
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.data.local.AudioItem
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberGold
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioTeal
import com.example.ui.theme.SuccessGreen
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAudiobookScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val context = LocalContext.current
    val allAudioItems by viewModel.allAudioItems.collectAsState()
    val selectedItem by viewModel.selectedAudioItem.collectAsState()
    val playbackStatus by viewModel.audioPlayer.status.collectAsState()

    var chapterTitle by remember { mutableStateOf("فصل اول: شازده کوچولو") }
    var selectedAudioTrack by remember(selectedItem, allAudioItems) {
        mutableStateOf(selectedItem ?: allAudioItems.firstOrNull())
    }

    var selectedMusic by remember { mutableStateOf("calm") } // "calm", "rain", "none"
    var musicVolume by remember { mutableFloatStateOf(0.25f) }
    var narrationVolume by remember { mutableFloatStateOf(1.0f) }
    var isDuckingEnabled by remember { mutableStateOf(true) }
    var isFadeInOutEnabled by remember { mutableStateOf(true) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "تولید کتاب صوتی آفرینا",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("audiobook_back_btn")
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
            // Chapter Title
            OutlinedTextField(
                value = chapterTitle,
                onValueChange = { chapterTitle = it },
                label = { Text("عنوان فصل یا کتاب") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Voice Narration Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "صدای گویندگی فصل (ضبط واقعی یا صوت AI):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AmberGold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (allAudioItems.isEmpty()) {
                        Text(
                            text = "هنوز فایل گویندگی وجود ندارد. می‌توانید ابتدا صدای خود را ضبط کنید یا از بخش «تولید صوت» گویندگی بسازید.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.navigateTo(StudioScreen.RECORD_VOICE) },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ضبط صدای من", style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedButton(
                                onClick = { viewModel.navigateTo(StudioScreen.GENERATE_AUDIO) },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Filled.AutoStories, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تولید صوت با هوش مصنوعی", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    } else {
                        allAudioItems.take(4).forEach { item ->
                            val isSelected = selectedAudioTrack?.id == item.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { selectedAudioTrack = item },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) StudioCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.background,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, StudioCyan) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = if (item.category == "VOICE") "ضبط صدای من" else if (item.isEnhanced) "کیفیت استودیویی ارتقا یافته" else "صوت تولید شده",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = StudioCyan, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Background Music & Ambient Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "موسیقی زمینه و فضای صوتی (Royalty-Free):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AmberGold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = (selectedMusic == "calm"),
                            onClick = { selectedMusic = "calm" },
                            label = { Text("آرامش هارمونیک") },
                            leadingIcon = { Icon(Icons.Filled.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StudioTeal.copy(alpha = 0.25f),
                                selectedLabelColor = StudioCyan
                            )
                        )
                        FilterChip(
                            selected = (selectedMusic == "rain"),
                            onClick = { selectedMusic = "rain" },
                            label = { Text("باران و طبیعت") },
                            leadingIcon = { Icon(Icons.Filled.GraphicEq, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        FilterChip(
                            selected = (selectedMusic == "none"),
                            onClick = { selectedMusic = "none" },
                            label = { Text("بدون موسیقی") }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sound Effects Preview
                    Text(
                        text = "افکت‌های صوتی فواصل کتاب:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.audioPlayer.playSoundEffect("chime") },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("زنگ آغاز فصل (Chime)")
                        }
                        OutlinedButton(
                            onClick = { viewModel.audioPlayer.playSoundEffect("page") },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("ورق زدن کتاب (Page Turn)")
                        }
                    }
                }
            }

            // Volume Sliders & Audio Ducking
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "تنظیمات تراز و میکس صوتی:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Narration Volume
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("بلندی صدای گوینده: ${(narrationVolume * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                    }
                    Slider(
                        value = narrationVolume,
                        onValueChange = {
                            narrationVolume = it
                            viewModel.audioPlayer.narrationVolume = it
                        },
                        colors = SliderDefaults.colors(thumbColor = AmberGold, activeTrackColor = AmberDark)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Music Volume
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("بلندی موسیقی زمینه: ${(musicVolume * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                    }
                    Slider(
                        value = musicVolume,
                        onValueChange = {
                            musicVolume = it
                            viewModel.audioPlayer.musicVolume = it
                        },
                        colors = SliderDefaults.colors(thumbColor = StudioCyan, activeTrackColor = StudioTeal)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Audio Ducking Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "کاهش خودکار موسیقی حین کلام (Audio Ducking)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "به‌صورت خودکار در حین صحبت گوینده صدای موسیقی ۶۵٪ کم شده و پس از پایان به حالت عادی برمی‌گردد.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isDuckingEnabled,
                            onCheckedChange = {
                                isDuckingEnabled = it
                                viewModel.audioPlayer.isDuckingEnabled = it
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = AmberGold)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Fade in/out
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "محو تدریجی ابتدا و انتها (Fade In / Out)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "شروع و پایان نرم موسیقی بدون قطع ناگهانی",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isFadeInOutEnabled,
                            onCheckedChange = { isFadeInOutEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = StudioCyan)
                        )
                    }
                }
            }

            // Preview and Export Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Play Combined Audiobook Preview
                Button(
                    onClick = {
                        if (playbackStatus.isPlaying) {
                            viewModel.audioPlayer.stop()
                        } else {
                            viewModel.audioPlayer.playAudiobookProject(
                                narrationPath = selectedAudioTrack?.filePath,
                                musicType = selectedMusic,
                                narrationVol = narrationVolume,
                                musicVol = musicVolume,
                                ducking = isDuckingEnabled
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(54.dp)
                        .testTag("play_audiobook_preview_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioTeal),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = if (playbackStatus.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (playbackStatus.isPlaying) "توقف پخش کتاب صوتی" else "پخش میکس نهایی کتاب صوتی",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Save / Export
                if (selectedAudioTrack != null) {
                    OutlinedButton(
                        onClick = {
                            viewModel.shareAudio(context, selectedAudioTrack!!)
                        },
                        modifier = Modifier.height(54.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("خروجی نهایی")
                    }
                }
            }
        }
    }
}
