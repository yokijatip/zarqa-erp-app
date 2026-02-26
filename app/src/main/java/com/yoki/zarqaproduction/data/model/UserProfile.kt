package com.yoki.zarqaproduction.data.model

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "",
    val tipe_akun: String = "permanent"
)
