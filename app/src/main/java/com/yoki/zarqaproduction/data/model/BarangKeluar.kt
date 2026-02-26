package com.yoki.zarqaproduction.data.model

import com.google.firebase.Timestamp

data class BarangKeluar(
    val model_id: String = "",
    val nama_model: String = "",
    val detail_keluar: List<DetailKeluar> = emptyList(),
    val total_pcs: Int = 0,
    val tujuan: String = "",
    val keterangan: String? = null,
    val dicatat_oleh: String = "",
    val tanggal_keluar: Timestamp? = null
)

data class DetailKeluar(
    val ukuran: String = "",
    val jumlah_pcs: Int = 0
)
