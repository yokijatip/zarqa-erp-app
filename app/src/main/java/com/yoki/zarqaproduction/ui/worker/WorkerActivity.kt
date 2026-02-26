package com.yoki.zarqaproduction.ui.worker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.yoki.zarqaproduction.data.model.UserProfile
import com.yoki.zarqaproduction.ui.theme.ZarqaProductionTheme

class WorkerActivity : ComponentActivity() {

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* izin diberikan atau tidak, app tetap berjalan */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermissionIfNeeded()

        val userProfile = UserProfile(
            uid  = intent.getStringExtra("USER_UID")  ?: "",
            name = intent.getStringExtra("USER_NAME") ?: "",
            role = intent.getStringExtra("USER_ROLE") ?: ""
        )

        setContent {
            ZarqaProductionTheme {
                WorkerRouter(userProfile = userProfile)
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
