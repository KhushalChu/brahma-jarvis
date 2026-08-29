package com.brahma.jarvis

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.brahma.jarvis.ui.ChatScreen
import com.brahma.jarvis.ui.SettingsScreen
import com.brahma.jarvis.ui.theme.BrahmaJarvisTheme

private enum class Screen { CHAT, SETTINGS }

class MainActivity : ComponentActivity() {

    private val viewModel: JarvisViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* results not individually needed; mic button will just fail gracefully if denied */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNeededPermissions()

        setContent {
            BrahmaJarvisTheme {
                var screen by remember { mutableStateOf(Screen.CHAT) }
                val state by viewModel.uiState.collectAsState()

                when (screen) {
                    Screen.CHAT -> ChatScreen(
                        state = state,
                        onInputChanged = viewModel::onInputTextChanged,
                        onSend = viewModel::sendCurrentInput,
                        onMicClick = {
                            if (state.isListening) viewModel.stopVoiceInput() else viewModel.startVoiceInput()
                        },
                        onOpenSettings = { screen = Screen.SETTINGS },
                        onDismissError = viewModel::dismissError
                    )
                    Screen.SETTINGS -> SettingsScreen(
                        initialApiKey = viewModel.apiKeyValue(),
                        initialAssistantName = viewModel.assistantNameValue(),
                        onBack = { screen = Screen.CHAT },
                        onSave = { key, name ->
                            viewModel.saveApiKey(key)
                            viewModel.saveAssistantName(name)
                            screen = Screen.CHAT
                        }
                    )
                }
            }
        }
    }

    private fun requestNeededPermissions() {
        val needed = mutableListOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            needed.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val toRequest = needed.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (toRequest.isNotEmpty()) {
            permissionLauncher.launch(toRequest.toTypedArray())
        }
    }
}
