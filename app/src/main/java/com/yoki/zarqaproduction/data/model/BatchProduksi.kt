package com.yoki.zarqaproduction.data.model

import com.google.firebase.Timestamp

data class BatchProduksi(
    val id: String = "",
    val model_id: String = "",
    val nama_model: String = "",
    val nama_warna: String? = null,
    val kode_hex_warna: String? = null,
    val detail_ukuran: List<DetailUkuran> = emptyList(),
    val total_pcs: Int = 0,
    val kain_digunakan: List<KainDigunakan> = emptyList(),
    val status: String = "",
    val dibuat_oleh: String = "",
    val catatan_admin: String? = null,
    val penugasan: Penugasan? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class DetailUkuran(
    val ukuran: String = "",
    val jumlah_pcs: Int = 0
)

data class KainDigunakan(
    val kain_id: String = "",
    val nama_kain: String = "",
    val satuan: String = "",
    val jumlah_dipakai: Double = 0.0
)

data class Penugasan(
    val cutting: PenugasanWorker? = null,
    val jahit: PenugasanWorker? = null,
    val steam: PenugasanWorker? = null
)

data class PenugasanWorker(
    val uid: String = "",
    val nama: String = ""
)
