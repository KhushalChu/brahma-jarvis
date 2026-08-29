package com.brahma.jarvis

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.brahma.jarvis.ai.ChatMessage
import com.brahma.jarvis.ai.GeminiClient
import com.brahma.jarvis.ai.Sender
import com.brahma.jarvis.commands.DeviceCommandHandler
import com.brahma.jarvis.data.SettingsStore
import com.brahma.jarvis.voice.SpeechInputManager
import com.brahma.jarvis.voice.SpeechOutputManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JarvisUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isListening: Boolean = false,
    val isThinking: Boolean = false,
    val hasApiKey: Boolean = false,
    val inputText: String = "",
    val errorMessage: String? = null
)

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val settings = SettingsStore(application)
    private val geminiClient = GeminiClient()
    private val commandHandler = DeviceCommandHandler(application)
    private val speechInput = SpeechInputManager(application)
    private val speechOutput = SpeechOutputManager(application)

    private val _uiState = MutableStateFlow(
        JarvisUiState(
            hasApiKey = settings.hasApiKey(),
            messages = listOf(
                ChatMessage(
                    Sender.ASSISTANT,
                    "Namaste! Main ${settings.assistantName} hoon. Kuch bhi pucho ya bolo."
                )
            )
        )
    )
    val uiState: StateFlow<JarvisUiState> = _uiState.asStateFlow()

    fun onInputTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun refreshApiKeyState() {
        _uiState.update { it.copy(hasApiKey = settings.hasApiKey()) }
    }

    fun sendCurrentInput() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return
        _uiState.update { it.copy(inputText = "") }
        handleUserMessage(text)
    }

    fun startVoiceInput() {
        if (_uiState.value.isListening) return
        speechInput.startListening(
            onResult = { text -> handleUserMessage(text) },
            onError = { message -> _uiState.update { it.copy(isListening = false, errorMessage = message) } },
            onListeningStateChanged = { listening -> _uiState.update { it.copy(isListening = listening) } }
        )
    }

    fun stopVoiceInput() {
        speechInput.stopListening()
        _uiState.update { it.copy(isListening = false) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun saveApiKey(key: String) {
        settings.geminiApiKey = key.trim()
        refreshApiKeyState()
    }

    fun apiKeyValue(): String = settings.geminiApiKey

    fun assistantNameValue(): String = settings.assistantName

    fun saveAssistantName(name: String) {
        if (name.isNotBlank()) settings.assistantName = name.trim()
    }

    private fun handleUserMessage(text: String) {
        appendMessage(ChatMessage(Sender.USER, text))

        // Local, instant device commands first (flashlight, volume, open app...)
        val localReply = commandHandler.tryHandle(text)
        if (localReply != null) {
            appendMessage(ChatMessage(Sender.ASSISTANT, localReply))
            maybeSpeak(localReply)
            return
        }

        // Otherwise fall back to Gemini for a conversational reply.
        askGemini(text)
    }

    private fun askGemini(latestUserText: String) {
        val apiKey = settings.geminiApiKey
        if (apiKey.isBlank()) {
            val msg = "Pehle Settings mein jaake apna Gemini API key add karo."
            appendMessage(ChatMessage(Sender.ASSISTANT, msg))
            return
        }

        _uiState.update { it.copy(isThinking = true) }
        viewModelScope.launch {
            val systemPrompt = "Tum ${settings.assistantName} ho, ek helpful, concise voice assistant jo " +
                "Hindi-English mix (Hinglish) ya jis language mein user baat kare, usi mein jawab deta hai. " +
                "Jawab chhote aur natural rakho, jaise ek smart assistant baat karta hai."

            val result = geminiClient.sendMessage(
                apiKey = apiKey,
                systemPrompt = systemPrompt,
                history = _uiState.value.messages
            )

            _uiState.update { it.copy(isThinking = false) }

            when (result) {
                is GeminiClient.Result.Success -> {
                    appendMessage(ChatMessage(Sender.ASSISTANT, result.text))
                    maybeSpeak(result.text)
                }
                is GeminiClient.Result.Failure -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
            }
        }
    }

    private fun maybeSpeak(text: String) {
        if (settings.voiceReplyEnabled) speechOutput.speak(text)
    }

    private fun appendMessage(message: ChatMessage) {
        _uiState.update { it.copy(messages = it.messages + message) }
    }

    override fun onCleared() {
        super.onCleared()
        speechInput.stopListening()
        speechOutput.shutdown()
    }
}
