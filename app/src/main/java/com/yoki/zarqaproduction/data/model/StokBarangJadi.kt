package com.yoki.zarqaproduction.data.model

data class StokBarangJadi(
    val id: String = "",
    val model_id: String = "",
    val nama_model: String = "",
    val nama_warna: String? = null,
    val kode_hex_warna: String? = null,
    val ukuran: String = "",
    val stok_tersedia: Int = 0,
    val total_masuk: Int = 0,
    val total_keluar: Int = 0
)
