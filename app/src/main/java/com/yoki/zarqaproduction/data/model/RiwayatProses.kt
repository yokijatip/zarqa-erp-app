package com.yoki.zarqaproduction.data.model

import com.google.firebase.Timestamp

data class RiwayatProses(
    val status_dari: String = "",
    val status_ke: String = "",
    val updated_by_uid: String = "",
    val updated_by_nama: String = "",
    val pcs_berhasil: Int = 0,
    val pcs_reject: Int = 0,
    val catatan: String? = null,
    val timestamp: Timestamp? = null
)
