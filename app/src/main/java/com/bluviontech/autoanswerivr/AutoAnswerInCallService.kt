package com.bluviontech.autoanswerivr

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.telecom.Call
import android.telecom.InCallService
import android.telecom.VideoProfile
import java.util.Locale

class AutoAnswerInCallService : InCallService(), TextToSpeech.OnInitListener {

    private lateinit var prefs: Prefs
    private var tts: TextToSpeech? = null
    private var latestCall: Call? = null

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        tts = TextToSpeech(this, this)
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        latestCall = call

        if (!prefs.enabled) return
        if (call.details.callDirection != Call.Details.DIRECTION_INCOMING) return

        Handler(Looper.getMainLooper()).postDelayed({
            try {
                call.answer(VideoProfile.STATE_AUDIO_ONLY)

                Handler(Looper.getMainLooper()).postDelayed({
                    if (prefs.speakerOn) {
                        setSpeakerphone(true)
                    }
                    speakScript()
                }, 1200)

            } catch (_: Exception) {
                // Some devices may block or behave differently
            }
        }, prefs.delaySeconds * 1000L)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)

        if (latestCall == call) {
            latestCall = null
        }

        stopSpeech()
        setSpeakerphone(false)
    }

    private fun setSpeakerphone(enabled: Boolean) {
        try {
            val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            @Suppress("DEPRECATION")
            run {
                am.mode = AudioManager.MODE_IN_COMMUNICATION
                am.isSpeakerphoneOn = enabled
            }
        } catch (_: Exception) {
        }
    }

    private fun speakScript() {
        val message = prefs.script.trim()
        if (message.isBlank()) return

        val utteranceId = "auto_answer_message"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } else {
            @Suppress("DEPRECATION")
            tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

    private fun stopSpeech() {
        try {
            tts?.stop()
        } catch (_: Exception) {
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            tts?.setSpeechRate(0.95f)
        }
    }

    override fun onDestroy() {
        stopSpeech()
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }
}