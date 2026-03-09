package com.bluviontech.autoanswerivr

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var switchEnable: Switch

    private val roleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            Toast.makeText(this, "Default phone app request finished", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnSetDialer = findViewById<Button>(R.id.btnSetDialer)
        switchEnable = findViewById(R.id.switchEnable)

        val sp = getSharedPreferences("app", MODE_PRIVATE)
        switchEnable.isChecked = sp.getBoolean("enabled", false)

        switchEnable.setOnCheckedChangeListener { _, isChecked ->
            sp.edit().putBoolean("enabled", isChecked).apply()
        }

        btnSetDialer.setOnClickListener {
            requestDefaultDialer()
        }

        requestPermissionsNow()
    }

    private fun requestPermissionsNow() {
        val list = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            list.add(Manifest.permission.READ_PHONE_STATE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            list.add(Manifest.permission.ANSWER_PHONE_CALLS)
        }

        if (list.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, list.toTypedArray(), 100)
        }
    }

    private fun requestDefaultDialer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
            roleLauncher.launch(intent)
        } else {
            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
            intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
            roleLauncher.launch(intent)
        }
    }
}
