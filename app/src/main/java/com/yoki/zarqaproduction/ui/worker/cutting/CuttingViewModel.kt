package com.yoki.zarqaproduction.ui.worker.cutting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoki.zarqaproduction.data.model.BatchProduksi
import com.yoki.zarqaproduction.data.repository.BatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class CuttingViewModel : ViewModel() {

    private val repository = BatchRepository()
    private var currentUid: String = ""

    private val _batches = MutableStateFlow<List<BatchProduksi>>(emptyList())
    val batches: StateFlow<List<BatchProduksi>> = _batches

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /** Dipanggil dari Screen saat pertama kali compose. */
    fun initialize(uid: String) {
        if (currentUid == uid) return
        currentUid = uid
        loadBatches()
    }

    fun loadBatches() {
        if (currentUid.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getBatchByStatuses(listOf("PENDING_CUTTING", "CUTTING_IN_PROGRESS"))
                .onSuccess { all ->
                    _batches.value = all.filter { batch ->
                        when (batch.status) {
                            // Hanya tampil jika sudah diassign ke saya oleh admin
                            "PENDING_CUTTING"     -> batch.penugasan?.cutting?.uid == currentUid
                            // Hanya tampil jika saya yang sedang mengerjakan
                            "CUTTING_IN_PROGRESS" -> batch.penugasan?.cutting?.uid == currentUid
                            else -> false
                        }
                    }
                }
                .onFailure {
                    Timber.e(it, "Gagal load cutting batches")
                    _error.value = "Gagal memuat data. Coba refresh."
                }
            _isLoading.value = false
        }
    }

    fun startCutting(batchId: String, uid: String, nama: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.startProcess(
                batchId      = batchId,
                statusDari   = "PENDING_CUTTING",
                statusBaru   = "CUTTING_IN_PROGRESS",
                penugasanKey = "cutting",
                uid          = uid,
                nama         = nama
            ).onSuccess {
                loadBatches()
                onResult(true)
            }.onFailure {
                Timber.e(it, "Gagal mulai cutting")
                onResult(false)
            }
        }
    }

    fun finishCutting(
        batchId: String, uid: String, nama: String,
        pcsBerhasil: Int, pcsReject: Int, catatan: String?,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            repository.finishProcess(
                batchId     = batchId,
                statusDari  = "CUTTING_IN_PROGRESS",
                statusBaru  = "CUTTING_DONE",
                uid         = uid,
                nama        = nama,
                pcsBerhasil = pcsBerhasil,
                pcsReject   = pcsReject,
                catatan     = catatan
            ).onSuccess {
                loadBatches()
                onResult(true)
            }.onFailure {
                Timber.e(it, "Gagal selesai cutting")
                onResult(false)
            }
        }
    }

    fun clearError() { _error.value = null }
}
