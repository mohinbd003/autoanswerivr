package com.bluviontech.autoanswerivr

import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.telecom.Call
import android.telecom.InCallService
import android.telecom.VideoProfile
import java.util.Locale

class MyInCallService : InCallService(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)

        val sp = getSharedPreferences("app", Context.MODE_PRIVATE)
        val enabled = sp.getBoolean("enabled", false)

        if (!enabled) return
        if (call.details.callDirection != Call.Details.DIRECTION_INCOMING) return

        Handler(Looper.getMainLooper()).postDelayed({
            try {
                call.answer(VideoProfile.STATE_AUDIO_ONLY)

                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                        am.mode = AudioManager.MODE_IN_COMMUNICATION
                        @Suppress("DEPRECATION")
                        run {
                            am.isSpeakerphoneOn = true
                        }

                        tts?.speak(
                            "Assalamu alaikum. This phone is using automatic answer.",
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "msg1"
                        )
                    } catch (_: Exception) {
                    }
                }, 1000)

            } catch (_: Exception) {
            }
        }, 2000)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
