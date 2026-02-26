package com.yoki.zarqaproduction.data.repository

import com.google.firebase.*
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore

import com.yoki.zarqaproduction.data.model.BarangKeluar
import com.yoki.zarqaproduction.data.model.StokBarangJadi
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class BarangJadiRepository {

    private val firestore = Firebase.firestore

    suspend fun getStokBarangJadi(): Result<List<StokBarangJadi>> {
        return try {
            val snapshot = firestore.collection("stok_barang_jadi").get().await()
            val stok = snapshot.documents.mapNotNull { doc ->
                doc.toObject(StokBarangJadi::class.java)?.copy(id = doc.id)
            }
            Result.success(stok)
        } catch (e: Exception) {
            Timber.e(e, "Gagal mengambil stok barang jadi")
            Result.failure(e)
        }
    }

    /**
     * [stokIdPerUkuran] = Map dari stokId ke jumlah yang dikeluarkan
     */
    suspend fun catatBarangKeluar(
        barangKeluar: BarangKeluar,
        stokIdPerUkuran: Map<String, Int>
    ): Result<Unit> {
        return try {
            firestore.collection("barang_keluar").add(mapOf(
                "model_id"       to barangKeluar.model_id,
                "nama_model"     to barangKeluar.nama_model,
                "detail_keluar"  to barangKeluar.detail_keluar.map {
                    mapOf("ukuran" to it.ukuran, "jumlah_pcs" to it.jumlah_pcs)
                },
                "total_pcs"      to barangKeluar.total_pcs,
                "tujuan"         to barangKeluar.tujuan,
                "keterangan"     to barangKeluar.keterangan,
                "dicatat_oleh"   to barangKeluar.dicatat_oleh,
                "tanggal_keluar" to FieldValue.serverTimestamp()
            )).await()

            for ((stokId, jumlah) in stokIdPerUkuran) {
                firestore.collection("stok_barang_jadi").document(stokId)
                    .update(mapOf(
                        "stok_tersedia" to FieldValue.increment(-jumlah.toLong()),
                        "total_keluar"  to FieldValue.increment(jumlah.toLong())
                    )).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Gagal catat barang keluar")
            Result.failure(e)
        }
    }
}
