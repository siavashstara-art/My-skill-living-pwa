package com.example.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AudioItem
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioTeal
import com.example.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val context = LocalContext.current
    val allAudioItems by viewModel.allAudioItems.collectAsState()
    val playbackStatus by viewModel.audioPlayer.status.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "VOICE", "TTS", "ENHANCED"

    val filteredItems = when (selectedFilter) {
        "VOICE" -> allAudioItems.filter { it.category == "VOICE" }
        "TTS" -> allAudioItems.filter { it.category == "TTS" }
        "ENHANCED" -> allAudioItems.filter { it.isEnhanced || it.category == "ENHANCED" }
        else -> allAudioItems
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "آرشیو صوتی آفرینا",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("library_back_btn")
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
                .padding(16.dp)
        ) {
            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = (selectedFilter == "ALL"),
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("همه (${allAudioItems.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AmberDark.copy(alpha = 0.25f),
                        selectedLabelColor = AmberGold
                    )
                )
                FilterChip(
                    selected = (selectedFilter == "VOICE"),
                    onClick = { selectedFilter = "VOICE" },
                    label = { Text("ضبط‌های من") }
                )
                FilterChip(
                    selected = (selectedFilter == "TTS"),
                    onClick = { selectedFilter = "TTS" },
                    label = { Text("تولید صوت") }
                )
                FilterChip(
                    selected = (selectedFilter == "ENHANCED"),
                    onClick = { selectedFilter = "ENHANCED" },
                    label = { Text("ارتقا یافته") }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredItems.isEmpty()) {
                // Empty State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FolderOpen,
                            contentDescription = null,
                            tint = AmberGold.copy(alpha = 0.6f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "آرشیو صوتی در این بخش خالی است",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "از ابزارهای استودیو برای ضبط صدا یا خلاصه‌سازی و تبدیل به صوت استفاده کنید.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        val isPlaying = playbackStatus.isPlaying && playbackStatus.currentTrackTitle == item.title

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (isPlaying) viewModel.audioPlayer.pause()
                                                else viewModel.audioPlayer.playTrack(item.filePath, item.title)
                                            },
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                                contentDescription = null,
                                                tint = AmberGold
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Column {
                                            Text(
                                                text = item.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
                                            Text(
                                                text = sdf.format(Date(item.createdAt)),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Category Badge
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when {
                                            item.isEnhanced -> SuccessGreen.copy(alpha = 0.2f)
                                            item.category == "VOICE" -> AmberDark.copy(alpha = 0.2f)
                                            else -> StudioCyan.copy(alpha = 0.2f)
                                        }
                                    ) {
                                        Text(
                                            text = when {
                                                item.isEnhanced -> "ارتقا یافته"
                                                item.category == "VOICE" -> "صدای من"
                                                item.category == "TTS" -> "تولید صوت"
                                                else -> "کتاب صوتی"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = when {
                                                item.isEnhanced -> SuccessGreen
                                                item.category == "VOICE" -> AmberGold
                                                else -> StudioCyan
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Send to Enhance
                                        if (!item.isEnhanced) {
                                            Surface(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        viewModel.selectAudioItem(item)
                                                        viewModel.navigateTo(StudioScreen.IMPROVE_AUDIO)
                                                    },
                                                color = StudioCyan.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = StudioCyan, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("ارتقای کیفیت", style = MaterialTheme.typography.labelSmall, color = StudioCyan)
                                                }
                                            }
                                        }

                                        // Send to Audiobook
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    viewModel.selectAudioItem(item)
                                                    viewModel.navigateTo(StudioScreen.CREATE_AUDIOBOOK)
                                                },
                                            color = AmberGold.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Filled.AutoStories, contentDescription = null, tint = AmberGold, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("کتاب صوتی", style = MaterialTheme.typography.labelSmall, color = AmberGold)
                                            }
                                        }
                                    }

                                    Row {
                                        IconButton(
                                            onClick = { viewModel.shareAudio(context, item) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Filled.Share, contentDescription = "اشتراک", modifier = Modifier.size(20.dp))
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteAudioItem(item) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = DangerRed, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
