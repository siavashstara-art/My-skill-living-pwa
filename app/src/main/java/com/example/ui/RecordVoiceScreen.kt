package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.audio.RecorderState
import com.example.data.local.AudioItem
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordVoiceScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val context = LocalContext.current
    val recorderState by viewModel.audioRecorder.recorderState.collectAsState()
    val currentAmp by viewModel.audioRecorder.currentAmplitude.collectAsState()
    val durationMs by viewModel.audioRecorder.recordingDurationMs.collectAsState()
    val playbackStatus by viewModel.audioPlayer.status.collectAsState()
    val selectedItem by viewModel.selectedAudioItem.collectAsState()

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
    }

    var recordTitle by remember {
        val sdf = SimpleDateFormat("HH:mm - yyyy/MM/dd", Locale.getDefault())
        mutableStateOf("ضبط صدا ${sdf.format(Date())}")
    }

    // Keep amplitude history for live waveform
    val amplitudeHistory = remember { mutableStateListOf<Float>() }
    if (recorderState == RecorderState.RECORDING) {
        val norm = (currentAmp.toFloat() / 25000f).coerceIn(0.05f, 1f)
        if (amplitudeHistory.size > 28) {
            amplitudeHistory.removeAt(0)
        }
        amplitudeHistory.add(norm)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ضبط صدای من",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("record_back_btn")
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!hasMicPermission) {
                // Permission Request Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "نیاز به دسترسی میکروفون",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "برای ضبط صدای باکیفیت و شفاف در آفرینا، دسترسی به میکروفون دستگاه الزامی است.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("grant_mic_btn")
                        ) {
                            Text("اعطای دسترسی میکروفون", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            } else {
                // Waveform & Recording Canvas
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Status badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (recorderState) {
                                RecorderState.RECORDING -> DangerRed.copy(alpha = 0.15f)
                                RecorderState.PAUSED -> AmberGold.copy(alpha = 0.15f)
                                else -> SuccessGreen.copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = when (recorderState) {
                                    RecorderState.RECORDING -> "● در حال ضبط زنده..."
                                    RecorderState.PAUSED -> "❚❚ ضبط متوقف موقت شد"
                                    RecorderState.STOPPED -> "✓ ضبط با موفقیت ذخیره شد"
                                    else -> "آماده برای ضبط استودیویی"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = when (recorderState) {
                                    RecorderState.RECORDING -> DangerRed
                                    RecorderState.PAUSED -> AmberGold
                                    else -> SuccessGreen
                                },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Large Timer
                        val totalSeconds = (durationMs / 1000).toInt()
                        val minutes = totalSeconds / 60
                        val seconds = totalSeconds % 60
                        Text(
                            text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
                            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 48.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Dynamic Waveform Visualizer
                        WaveformVisualizer(
                            amplitudes = amplitudeHistory,
                            isRecording = recorderState == RecorderState.RECORDING,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Controls: Large Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (recorderState) {
                        RecorderState.IDLE, RecorderState.STOPPED -> {
                            // Big Start Recording Button
                            Button(
                                onClick = {
                                    amplitudeHistory.clear()
                                    viewModel.startRecording()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp)
                                    .testTag("start_record_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "شروع ضبط صدا",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        RecorderState.RECORDING -> {
                            // Pause Button
                            OutlinedButton(
                                onClick = { viewModel.pauseRecording() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .testTag("pause_record_btn"),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Filled.Pause, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("مکث", style = MaterialTheme.typography.bodyLarge)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Stop & Save Button
                            Button(
                                onClick = {
                                    val item = viewModel.stopAndSaveRecording(recordTitle)
                                },
                                modifier = Modifier
                                    .weight(1.4f)
                                    .height(56.dp)
                                    .testTag("stop_record_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Filled.Stop, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "توقف و ذخیره",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        RecorderState.PAUSED -> {
                            // Resume Button
                            Button(
                                onClick = { viewModel.resumeRecording() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .testTag("resume_record_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = AmberDark),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Filled.Mic, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ادامه ضبط", style = MaterialTheme.typography.bodyLarge)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Stop & Save Button
                            Button(
                                onClick = {
                                    val item = viewModel.stopAndSaveRecording(recordTitle)
                                },
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(56.dp)
                                    .testTag("stop_paused_record_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ذخیره نهایی", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Post-Recording Panel
                if (selectedItem != null && recorderState == RecorderState.STOPPED) {
                    val item = selectedItem!!
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "فایل ذخیره‌شده آماده است",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                                Text(
                                    text = "${item.durationMs / 1000} ثانیه",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Playback Preview Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (playbackStatus.isPlaying) {
                                            viewModel.audioPlayer.pause()
                                        } else {
                                            viewModel.audioPlayer.playTrack(item.filePath, item.title)
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberDark),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = if (playbackStatus.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (playbackStatus.isPlaying) "توقف پخش" else "پخش صدا")
                                }

                                // Share / Export
                                OutlinedButton(
                                    onClick = { viewModel.shareAudio(context, item) },
                                    modifier = Modifier.height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.Share, contentDescription = "اشتراک")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("دانلود / اشتراک")
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Direct Improvement Shortcut
                            Button(
                                onClick = {
                                    viewModel.selectAudioItem(item)
                                    viewModel.navigateTo(StudioScreen.IMPROVE_AUDIO)
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StudioCyan),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ارتقای کیفیت این ضبط (نویزگیری و شفاف‌سازی)", color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WaveformVisualizer(
    amplitudes: List<Float>,
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val barCount = 30
            val barWidth = (size.width / (barCount * 1.5f))
            val spacing = barWidth * 0.5f
            val maxHeight = size.height * 0.85f

            for (i in 0 until barCount) {
                val amp = if (i < amplitudes.size && isRecording) {
                    amplitudes[i]
                } else if (isRecording) {
                    0.1f
                } else {
                    0.05f
                }
                val barHeight = (amp * maxHeight).coerceIn(4.dp.toPx(), maxHeight)
                val x = i * (barWidth + spacing)
                val y = (size.height - barHeight) / 2

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(AmberGold, StudioCyan)
                    ),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }
        }
    }
}
