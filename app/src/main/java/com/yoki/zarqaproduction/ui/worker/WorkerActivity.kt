package com.yoki.zarqaproduction.ui.worker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.yoki.zarqaproduction.data.model.UserProfile
import com.yoki.zarqaproduction.ui.theme.ZarqaProductionTheme

class WorkerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
}
