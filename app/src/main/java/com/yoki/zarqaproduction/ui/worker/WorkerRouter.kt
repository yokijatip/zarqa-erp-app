package com.yoki.zarqaproduction.ui.worker

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.yoki.zarqaproduction.data.model.UserProfile
import com.yoki.zarqaproduction.ui.worker.admin.AdminScreen
import com.yoki.zarqaproduction.ui.worker.cutting.CuttingScreen
import com.yoki.zarqaproduction.ui.worker.jahit.JahitScreen
import com.yoki.zarqaproduction.ui.worker.steam.SteamScreen

@Composable
fun WorkerRouter(userProfile: UserProfile) {
    when (userProfile.role) {
        "kepala_cutting"                     -> CuttingScreen(userProfile = userProfile)
        "kepala_jahit"                       -> JahitScreen(userProfile = userProfile)
        "kepala_steam"                       -> SteamScreen(userProfile = userProfile)
        "admin_gudang", "kepala_keluar"      -> AdminScreen(userProfile = userProfile)
        else                                 -> UnauthorizedScreen()
    }
}

@Composable
private fun UnauthorizedScreen() {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Akses tidak tersedia",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Role Anda tidak memiliki akses ke aplikasi ini.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                Firebase.auth.signOut()
                (context as? WorkerActivity)?.finish()
            }) {
                Text("Keluar")
            }
        }
    }
}
