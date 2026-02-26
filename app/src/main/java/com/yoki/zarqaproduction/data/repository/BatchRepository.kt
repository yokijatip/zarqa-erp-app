package com.yoki.zarqaproduction.data.repository

import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.yoki.zarqaproduction.data.model.BatchProduksi
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class BatchRepository {

    private val firestore = Firebase.firestore

    suspend fun getBatchByStatuses(statuses: List<String>): Result<List<BatchProduksi>> {
        return try {
            val snapshot = firestore.collection("batch_produksi")
                .whereIn("status", statuses)
                .get()
                .await()
            val batches = snapshot.documents.mapNotNull { doc ->
                doc.toObject(BatchProduksi::class.java)?.copy(id = doc.id)
            }
            Result.success(batches)
        } catch (e: Exception) {
            Timber.e(e, "Gagal mengambil batch")
            Result.failure(e)
        }
    }

    /**
     * Mulai proses: update status + simpan penugasan + tulis riwayat
     * [penugasanKey] = "cutting" | "jahit" | "steam"
     */
    suspend fun startProcess(
        batchId: String,
        statusDari: String,
        statusBaru: String,
        penugasanKey: String,
        uid: String,
        nama: String
    ): Result<Unit> {
        return try {
            val ref = firestore.collection("batch_produksi").document(batchId)
            ref.update(mapOf(
                "status" to statusBaru,
                "penugasan.$penugasanKey" to mapOf("uid" to uid, "nama" to nama),
                "updatedAt" to FieldValue.serverTimestamp()
            )).await()
            ref.collection("riwayat_proses").add(mapOf(
                "status_dari"     to statusDari,
                "status_ke"       to statusBaru,
                "updated_by_uid"  to uid,
                "updated_by_nama" to nama,
                "pcs_berhasil"    to 0,
                "pcs_reject"      to 0,
                "timestamp"       to FieldValue.serverTimestamp()
            )).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Gagal mulai proses $statusBaru")
            Result.failure(e)
        }
    }

    /**
     * Selesai proses: update status + tulis riwayat dengan pcs hasil
     */
    suspend fun finishProcess(
        batchId: String,
        statusDari: String,
        statusBaru: String,
        uid: String,
        nama: String,
        pcsBerhasil: Int,
        pcsReject: Int,
        catatan: String?
    ): Result<Unit> {
        return try {
            val ref = firestore.collection("batch_produksi").document(batchId)
            ref.update(mapOf(
                "status"    to statusBaru,
                "total_pcs" to pcsBerhasil,
                "updatedAt" to FieldValue.serverTimestamp()
            )).await()
            ref.collection("riwayat_proses").add(mapOf(
                "status_dari"     to statusDari,
                "status_ke"       to statusBaru,
                "updated_by_uid"  to uid,
                "updated_by_nama" to nama,
                "pcs_berhasil"    to pcsBerhasil,
                "pcs_reject"      to pcsReject,
                "catatan"         to catatan,
                "timestamp"       to FieldValue.serverTimestamp()
            )).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Gagal selesai proses $statusBaru")
            Result.failure(e)
        }
    }
}
