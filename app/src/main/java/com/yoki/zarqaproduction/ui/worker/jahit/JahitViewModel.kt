package com.yoki.zarqaproduction.ui.worker.jahit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoki.zarqaproduction.data.model.BatchProduksi
import com.yoki.zarqaproduction.data.model.DetailUkuran
import com.yoki.zarqaproduction.data.repository.BatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class JahitViewModel : ViewModel() {

    private val repository = BatchRepository()
    private var currentUid: String = ""

    private val _batches = MutableStateFlow<List<BatchProduksi>>(emptyList())
    val batches: StateFlow<List<BatchProduksi>> = _batches

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

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
            repository.getBatchByStatuses(listOf("CUTTING_DONE", "JAHIT_IN_PROGRESS"))
                .onSuccess { all ->
                    _batches.value = all.filter { batch ->
                        when (batch.status) {
                            // Hanya tampil jika sudah diassign ke saya oleh admin
                            "CUTTING_DONE"     -> batch.penugasan?.jahit?.uid == currentUid
                            "JAHIT_IN_PROGRESS" -> batch.penugasan?.jahit?.uid == currentUid
                            else -> false
                        }
                    }
                }
                .onFailure {
                    Timber.e(it, "Gagal load jahit batches")
                    _error.value = "Gagal memuat data. Coba refresh."
                }
            _isLoading.value = false
        }
    }

    fun startJahit(batchId: String, uid: String, nama: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.startProcess(
                batchId      = batchId,
                statusDari   = "CUTTING_DONE",
                statusBaru   = "JAHIT_IN_PROGRESS",
                penugasanKey = "jahit",
                uid          = uid,
                nama         = nama
            ).onSuccess { loadBatches(); onResult(true) }
             .onFailure { Timber.e(it, "Gagal mulai jahit"); onResult(false) }
        }
    }

    fun finishJahit(
        batchId: String, uid: String, nama: String,
        pcsBerhasil: Int, pcsReject: Int,
        detailRejectUkuran: List<DetailUkuran>,
        catatan: String?,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            repository.finishProcess(
                batchId            = batchId,
                statusDari         = "JAHIT_IN_PROGRESS",
                statusBaru         = "JAHIT_DONE",
                uid                = uid,
                nama               = nama,
                pcsBerhasil        = pcsBerhasil,
                pcsReject          = pcsReject,
                detailRejectUkuran = detailRejectUkuran,
                catatan            = catatan
            ).onSuccess { loadBatches(); onResult(true) }
             .onFailure { Timber.e(it, "Gagal selesai jahit"); onResult(false) }
        }
    }

    fun clearError() { _error.value = null }
}
