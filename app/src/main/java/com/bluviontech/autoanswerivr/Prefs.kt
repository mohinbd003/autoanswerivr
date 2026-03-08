package com.bluviontech.autoanswerivr

import android.content.Context

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("auto_answer_ivr", Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = sp.getBoolean("enabled", false)
        set(value) = sp.edit().putBoolean("enabled", value).apply()

    var speakerOn: Boolean
        get() = sp.getBoolean("speakerOn", true)
        set(value) = sp.edit().putBoolean("speakerOn", value).apply()

    var delaySeconds: Int
        get() = sp.getInt("delaySeconds", 2)
        set(value) = sp.edit().putInt("delaySeconds", value.coerceIn(0, 30)).apply()

    var script: String
        get() = sp.getString(
            "script",
            "Assalamu alaikum. This phone is using an automatic response. Please leave a short message after the beep."
        ) ?: ""
        set(value) = sp.edit().putString("script", value).apply()
}