package com.example.aipocketassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aipocketassistant.ai.GeminiAiProvider
import com.example.aipocketassistant.data.SettingsRepository
import com.example.aipocketassistant.speech.AndroidSpeechProvider
import com.example.aipocketassistant.tts.AndroidTtsProvider
import com.example.aipocketassistant.ui.AssistantScreen
import com.example.aipocketassistant.ui.SettingsScreen
import com.example.aipocketassistant.ui.theme.AiPocketAssistantTheme
import com.example.aipocketassistant.utils.PermissionManager
import com.example.aipocketassistant.viewmodel.AssistantViewModel
import com.example.aipocketassistant.viewmodel.AssistantViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit permissionManager: PermissionManager
    private lateinit settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        settingsRepository = SettingsRepository(this)
        permissionManager = PermissionManager(this)

        if (!permissionManager.hasPermissions(this)) {
            permissionManager.requestPermissions {
                // Ignore result for now, simple implementation
            }
        }

        setContent {
            AiPocketAssistantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    var apiKey by remember { mutableStateOf("") }
                    
                    LaunchedEffect(Unit) {
                        apiKey = settingsRepository.geminiApiKeyFlow.first()
                    }

                    // Providers setup
                    val sttProvider = remember { AndroidSpeechProvider(this) }
                    val aiProvider = remember(apiKey) { GeminiAiProvider(apiKey) }
                    val ttsProvider = remember { AndroidTtsProvider(this) }

                    val viewModel: AssistantViewModel by viewModels {
                        AssistantViewModelFactory(sttProvider, aiProvider, ttsProvider)
                    }

                    NavHost(navController = navController, startDestination = "assistant") {
                        composable("assistant") {
                            AssistantScreen(
                                viewModel = viewModel,
                                onNavigateToSettings = { navController.navigate("settings") }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                settingsRepository = settingsRepository,
                                onNavigateBack = { 
                                    navController.popBackStack() 
                                    // Refresh the api key
                                    lifecycleScope.launch {
                                        apiKey = settingsRepository.geminiApiKeyFlow.first()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}



