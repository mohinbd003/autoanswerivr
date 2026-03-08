package com.bluviontech.autoanswerivr

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telecom.TelecomManager
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs

    private val requestRoleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            updateDialerStatusToast()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = Prefs(this)
        requestBasicPermissions()

        val btnSetDialer = findViewById<Button>(R.id.btnSetDialer)
        val switchEnable = findViewById<Switch>(R.id.switchEnable)
        val switchSpeaker = findViewById<Switch>(R.id.switchSpeaker)
        val etDelay = findViewById<EditText>(R.id.etDelay)
        val etScript = findViewById<EditText>(R.id.etScript)
        val btnSave = findViewById<Button>(R.id.btnSave)

        switchEnable.isChecked = prefs.enabled
        switchSpeaker.isChecked = prefs.speakerOn
        etDelay.setText(prefs.delaySeconds.toString())
        etScript.setText(prefs.script)

        btnSetDialer.setOnClickListener {
            requestDefaultDialerRole()
        }

        btnSave.setOnClickListener {
            prefs.enabled = switchEnable.isChecked
            prefs.speakerOn = switchSpeaker.isChecked
            prefs.delaySeconds = etDelay.text.toString().toIntOrNull() ?: 2
            prefs.script = etScript.text.toString().trim().ifEmpty {
                "Assalamu alaikum. This phone is using an automatic response."
            }
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestBasicPermissions() {
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.READ_PHONE_STATE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.ANSWER_PHONE_CALLS)
        }

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 101)
        }
    }

    private fun requestDefaultDialerRole() {
        val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager

        if (telecomManager.defaultDialerPackage == packageName) {
            Toast.makeText(this, "Already the default phone app", Toast.LENGTH_LONG).show()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                requestRoleLauncher.launch(intent)
            } else {
                openDefaultAppsSettings()
            }
        } else {
            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
            }
            requestRoleLauncher.launch(intent)
        }
    }

    private fun updateDialerStatusToast() {
        val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
        if (telecomManager.defaultDialerPackage == packageName) {
            Toast.makeText(this, "Default phone app set successfully", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Default phone app was not changed", Toast.LENGTH_LONG).show()
        }
    }

    private fun openDefaultAppsSettings() {
        try {
            startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
        } catch (_: Exception) {
            Toast.makeText(this, "Open system settings and set this app as Phone app", Toast.LENGTH_LONG).show()
        }
    }
}