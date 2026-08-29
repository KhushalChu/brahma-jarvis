package com.brahma.jarvis.commands

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager

/**
 * Handles simple on-device actions locally (no AI round-trip needed) so the
 * assistant feels instant for common commands. Returns null if the text
 * doesn't match a known local command, so the caller can fall back to Gemini.
 */
class DeviceCommandHandler(private val context: Context) {

    private var flashlightOn = false

    fun tryHandle(rawText: String): String? {
        val text = rawText.trim().lowercase()

        return when {
            containsAny(text, "flashlight on", "torch on", "light on") -> {
                setFlashlight(true)
                "Flashlight on kar diya."
            }
            containsAny(text, "flashlight off", "torch off", "light off") -> {
                setFlashlight(false)
                "Flashlight off kar diya."
            }
            containsAny(text, "volume up", "awaaz badhao", "volume badhao") -> {
                adjustVolume(AudioManager.ADJUST_RAISE)
                "Volume badha diya."
            }
            containsAny(text, "volume down", "awaaz kam", "volume kam") -> {
                adjustVolume(AudioManager.ADJUST_LOWER)
                "Volume kam kar diya."
            }
            containsAny(text, "mute") -> {
                adjustVolume(AudioManager.ADJUST_MUTE)
                "Mute kar diya."
            }
            containsAny(text, "battery", "battery status", "charge kitna") -> {
                "Battery abhi ${batteryPercent()}% hai."
            }
            text.startsWith("open ") -> {
                val target = text.removePrefix("open ").trim()
                openUrlOrSearch(target)
                "$target khol raha hoon."
            }
            else -> null
        }
    }

    private fun containsAny(text: String, vararg needles: String): Boolean =
        needles.any { text.contains(it) }

    private fun setFlashlight(on: Boolean) {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return
            cameraManager.setTorchMode(cameraId, on)
            flashlightOn = on
        } catch (_: Exception) {
            // Device may not have a flash; fail silently and let the reply stand.
        }
    }

    private fun adjustVolume(direction: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
    }

    private fun batteryPercent(): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private fun openUrlOrSearch(target: String) {
        val looksLikeUrl = target.contains(".") && !target.contains(" ")
        val uri = if (looksLikeUrl) {
            Uri.parse(if (target.startsWith("http")) target else "https://$target")
        } else {
            Uri.parse("https://www.google.com/search?q=" + Uri.encode(target))
        }
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
