package com.yoki.zarqaproduction.ui.auth

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.yoki.zarqaproduction.data.repository.AuthRepository
import com.yoki.zarqaproduction.ui.worker.WorkerActivity
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = AuthRepository()

    LaunchedEffect(Unit) {
        delay(1500)
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            val profile = repository.getUserProfile(currentUser.uid)
            val intent = Intent(context, WorkerActivity::class.java).apply {
                putExtra("USER_ROLE", profile.role)
                putExtra("USER_NAME", profile.name)
                putExtra("USER_UID", profile.uid)
            }
            context.startActivity(intent)
            (context as? AuthActivity)?.finish()
        } else {
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
