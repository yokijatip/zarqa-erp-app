package com.yoki.zarqaproduction.data.repository


import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.yoki.zarqaproduction.data.model.UserProfile
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class AuthRepository {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    val currentUser get() = auth.currentUser

    suspend fun login(email: String, password: String): Result<UserProfile> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("UID tidak ditemukan"))
            val profile = getUserProfile(uid)
            Result.success(profile)
        } catch (e: Exception) {
            Timber.e(e, "Login gagal")
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(uid: String): UserProfile {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            doc.toObject(UserProfile::class.java)?.copy(uid = uid) ?: UserProfile(uid = uid)
        } catch (e: Exception) {
            Timber.e(e, "Gagal ambil profil user")
            UserProfile(uid = uid)
        }
    }

    fun logout() {
        auth.signOut()
    }
}
