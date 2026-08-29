package com.brahma.jarvis.voice

import android.content.Context
import android.content.Intent
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

/**
 * Wraps Android's built-in on-device/cloud SpeechRecognizer for
 * simple "listen once, get text back" voice input.
 */
class SpeechInputManager(private val context: Context) {

    private var recognizer: SpeechRecognizer? = null

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening(
        onResult: (String) -> Unit,
        onError: (String) -> Unit,
        onListeningStateChanged: (Boolean) -> Unit = {}
    ) {
        if (!isAvailable()) {
            onError("Is device pe speech recognition available nahi hai.")
            return
        }

        stopListening()
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: android.os.Bundle?) {
                    onListeningStateChanged(true)
                }

                override fun onResults(results: android.os.Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull().orEmpty()
                    onListeningStateChanged(false)
                    if (text.isNotBlank()) onResult(text) else onError("Kuch samajh nahi aaya, dobara try karo.")
                }

                override fun onError(error: Int) {
                    onListeningStateChanged(false)
                    onError("Voice input error code: $error")
                }

                override fun onEndOfSpeech() {
                    onListeningStateChanged(false)
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onPartialResults(partialResults: android.os.Bundle?) {}
                override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }

        recognizer?.startListening(intent)
    }

    fun stopListening() {
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
    }
}
