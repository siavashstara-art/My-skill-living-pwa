package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberGold
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioDarkCard
import com.example.ui.theme.StudioTeal
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WaveformPurple

@Composable
fun HomeScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val voiceConfig by viewModel.voiceConfig.collectAsState()
    val playbackStatus by viewModel.audioPlayer.status.collectAsState()
    val allAudioItems by viewModel.allAudioItems.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                // Mini Player Bar if something is playing
                AnimatedVisibility(visible = playbackStatus.isPlaying || playbackStatus.currentPositionMs > 0) {
                    MiniPlayerBar(
                        title = playbackStatus.currentTrackTitle.ifBlank { "در حال پخش صوت" },
                        isPlaying = playbackStatus.isPlaying,
                        currentPos = playbackStatus.currentPositionMs,
                        duration = playbackStatus.durationMs,
                        onPlayPause = {
                            if (playbackStatus.isPlaying) viewModel.audioPlayer.pause()
                            else viewModel.audioPlayer.resume()
                        },
                        onStop = { viewModel.audioPlayer.stop() }
                    )
                }

                // Bottom Nav Bar
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = true,
                        onClick = { viewModel.navigateTo(StudioScreen.HOME) },
                        icon = { Icon(Icons.Outlined.Home, contentDescription = "استودیو") },
                        label = { Text("استودیو", style = MaterialTheme.typography.labelMedium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AmberGold,
                            indicatorColor = AmberDark.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("nav_home")
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { viewModel.navigateTo(StudioScreen.LIBRARY) },
                        icon = { Icon(Icons.Outlined.Folder, contentDescription = "آرشیو") },
                        label = { Text("آرشیو صوتی", style = MaterialTheme.typography.labelMedium) },
                        modifier = Modifier.testTag("nav_library")
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { viewModel.navigateTo(StudioScreen.SETTINGS) },
                        icon = { Icon(Icons.Outlined.Settings, contentDescription = "تنظیمات") },
                        label = { Text("تنظیمات صدا", style = MaterialTheme.typography.labelMedium) },
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Studio Header
            item {
                StudioHeader(
                    isVoiceConfigured = voiceConfig.isConfigured,
                    onSettingsClick = { viewModel.navigateTo(StudioScreen.SETTINGS) }
                )
            }

            // Five Main Actions Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "اقدامات اصلی استودیو",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "۵ ابزار کاربردی",
                        style = MaterialTheme.typography.labelMedium,
                        color = AmberGold
                    )
                }
            }

            // ACTION 1: Record My Voice
            item {
                MainActionCard(
                    title = "ضبط صدای من",
                    subtitle = "ضبط واقعی از میکروفون، مکث، پخش و ذخیره فایل اصلی",
                    badge = "میکروفون استودیویی",
                    icon = Icons.Filled.Mic,
                    gradientColors = listOf(AmberDark, AmberGold),
                    testTag = "action_record_voice",
                    onClick = { viewModel.navigateTo(StudioScreen.RECORD_VOICE) }
                )
            }

            // ACTION 2: Create Audiobook
            item {
                MainActionCard(
                    title = "تولید کتاب صوتی",
                    subtitle = "ترکیب گویندگی، موسیقی زمینه ملایم، داکینگ هوشمند و افکت‌ها",
                    badge = "تولید و میکس",
                    icon = Icons.Filled.AutoStories,
                    gradientColors = listOf(StudioTeal, StudioCyan),
                    testTag = "action_create_audiobook",
                    onClick = { viewModel.navigateTo(StudioScreen.CREATE_AUDIOBOOK) }
                )
            }

            // ACTION 3: Summarize a Book
            item {
                MainActionCard(
                    title = "خلاصه‌سازی کتاب",
                    subtitle = "خلاصه فشرده، استاندارد و تفصیلی فصل‌به‌فصل، آماده برای گویندگی",
                    badge = "هوشمند و روان",
                    icon = Icons.Filled.AutoAwesome,
                    gradientColors = listOf(Color(0xFF7C3AED), WaveformPurple),
                    testTag = "action_summarize_book",
                    onClick = { viewModel.navigateTo(StudioScreen.SUMMARIZE_BOOK) }
                )
            }

            // ACTION 4: Generate Audio
            item {
                MainActionCard(
                    title = "تولید صوت (متن به گفتار)",
                    subtitle = "تبدیل خلاصه و فصول به صدا (فارسی، انگلیسی، عربی یا صدای شخصی)",
                    badge = "TTS و صدای شخصی",
                    icon = Icons.Filled.RecordVoiceOver,
                    gradientColors = listOf(Color(0xFF0284C7), Color(0xFF38BDF8)),
                    testTag = "action_generate_audio",
                    onClick = { viewModel.navigateTo(StudioScreen.GENERATE_AUDIO) }
                )
            }

            // ACTION 5: Improve My Audio
            item {
                MainActionCard(
                    title = "بهبود کیفیت صدا",
                    subtitle = "حذف نویز، یکسان‌سازی، شفاف‌سازی EQ، فشرده‌سازی با حفظ فایل اصلی",
                    badge = "پردازش DSP",
                    icon = Icons.Filled.Equalizer,
                    gradientColors = listOf(Color(0xFFD97706), Color(0xFFFBBF24)),
                    testTag = "action_improve_audio",
                    onClick = { viewModel.navigateTo(StudioScreen.IMPROVE_AUDIO) }
                )
            }

            // Quick Recent Audio List
            if (allAudioItems.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "آخرین پروژه‌ها و ضبط‌ها",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "مشاهده همه (${allAudioItems.size})",
                            style = MaterialTheme.typography.labelMedium,
                            color = AmberGold,
                            modifier = Modifier.clickable { viewModel.navigateTo(StudioScreen.LIBRARY) }
                        )
                    }
                }

                items(minOf(allAudioItems.size, 3)) { index ->
                    val item = allAudioItems[index]
                    QuickAudioItemRow(
                        item = item,
                        isPlaying = playbackStatus.isPlaying && playbackStatus.currentTrackTitle == item.title,
                        onPlayClick = {
                            if (playbackStatus.isPlaying && playbackStatus.currentTrackTitle == item.title) {
                                viewModel.audioPlayer.pause()
                            } else {
                                viewModel.audioPlayer.playTrack(item.filePath, item.title)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StudioHeader(
    isVoiceConfigured: Boolean,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Logo Image
                    Image(
                        painter = painterResource(id = R.drawable.ic_afrina_logo),
                        contentDescription = "آرم آفرینا",
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.5.dp, AmberGold.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "استودیوی صوتی آفرینا",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "AFRINA AUDIO STUDIO — LEAN MVP",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // AI Voice Provider Status Indicator
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSettingsClick() },
                shape = RoundedCornerShape(12.dp),
                color = if (isVoiceConfigured) SuccessGreen.copy(alpha = 0.12f) else AmberGold.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isVoiceConfigured) SuccessGreen.copy(alpha = 0.4f) else AmberGold.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isVoiceConfigured) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                            contentDescription = null,
                            tint = if (isVoiceConfigured) SuccessGreen else AmberGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isVoiceConfigured) "صدای شخصی AI: فعال و متصل" else "صدای شخصی: پیکربندی نشده (استفاده از TTS داخلی)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "تنظیمات >",
                        style = MaterialTheme.typography.labelMedium,
                        color = AmberGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MainActionCard(
    title: String,
    subtitle: String,
    badge: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with colorful gradient background
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = gradientColors.first().copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = gradientColors.first(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MiniPlayerBar(
    title: String,
    isPlaying: Boolean,
    currentPos: Int,
    duration: Int,
    onPlayPause: () -> Unit,
    onStop: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Equalizer,
                        contentDescription = null,
                        tint = AmberGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "مکث" else "پخش",
                            tint = AmberGold
                        )
                    }
                }
            }

            if (duration > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (currentPos.toFloat() / duration.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(CircleShape),
                    color = AmberGold,
                    trackColor = AmberGold.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
fun QuickAudioItemRow(
    item: com.example.data.local.AudioItem,
    isPlaying: Boolean,
    onPlayClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onPlayClick() },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = AmberGold,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = when (item.category) {
                            "VOICE" -> "صدای ضبط شده"
                            "TTS" -> "تولید صوت"
                            "ENHANCED" -> "ارتقا یافته"
                            else -> "کتاب صوتی"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
