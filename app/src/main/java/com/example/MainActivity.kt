package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CreateAudiobookScreen
import com.example.ui.GenerateAudioScreen
import com.example.ui.HomeScreen
import com.example.ui.ImproveAudioScreen
import com.example.ui.LibraryScreen
import com.example.ui.RecordVoiceScreen
import com.example.ui.SettingsScreen
import com.example.ui.StudioScreen
import com.example.ui.StudioViewModel
import com.example.ui.SummarizeBookScreen
import com.example.ui.theme.AfrinaAudioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AfrinaAudioTheme {
                val viewModel: StudioViewModel = viewModel()
                val currentScreen by viewModel.currentScreen.collectAsState()

                Surface(modifier = Modifier.fillMaxSize()) {
                    Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                        when (screen) {
                            StudioScreen.HOME -> HomeScreen(viewModel)
                            StudioScreen.RECORD_VOICE -> RecordVoiceScreen(viewModel)
                            StudioScreen.CREATE_AUDIOBOOK -> CreateAudiobookScreen(viewModel)
                            StudioScreen.SUMMARIZE_BOOK -> SummarizeBookScreen(viewModel)
                            StudioScreen.GENERATE_AUDIO -> GenerateAudioScreen(viewModel)
                            StudioScreen.IMPROVE_AUDIO -> ImproveAudioScreen(viewModel)
                            StudioScreen.LIBRARY -> LibraryScreen(viewModel)
                            StudioScreen.SETTINGS -> SettingsScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}
