package com.yoki.zarqaproduction.ui.worker.steam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoki.zarqaproduction.data.model.BatchProduksi
import com.yoki.zarqaproduction.data.model.DetailUkuran
import com.yoki.zarqaproduction.data.repository.BatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class SteamViewModel : ViewModel() {

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
            repository.getBatchByStatuses(listOf("JAHIT_DONE", "STEAM_IN_PROGRESS"))
                .onSuccess { all ->
                    _batches.value = all.filter { batch ->
                        when (batch.status) {
                            // Hanya tampil jika sudah diassign ke saya oleh admin
                            "JAHIT_DONE"       -> batch.penugasan?.steam?.uid == currentUid
                            "STEAM_IN_PROGRESS" -> batch.penugasan?.steam?.uid == currentUid
                            else -> false
                        }
                    }
                }
                .onFailure {
                    Timber.e(it, "Gagal load steam batches")
                    _error.value = "Gagal memuat data. Coba refresh."
                }
            _isLoading.value = false
        }
    }

    fun startSteam(batchId: String, uid: String, nama: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.startProcess(
                batchId      = batchId,
                statusDari   = "JAHIT_DONE",
                statusBaru   = "STEAM_IN_PROGRESS",
                penugasanKey = "steam",
                uid          = uid,
                nama         = nama
            ).onSuccess { loadBatches(); onResult(true) }
             .onFailure { Timber.e(it, "Gagal mulai steam"); onResult(false) }
        }
    }

    fun finishSteam(
        batchId: String, uid: String, nama: String,
        pcsBerhasil: Int, pcsReject: Int,
        detailRejectUkuran: List<DetailUkuran>,
        catatan: String?,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            repository.finishProcess(
                batchId            = batchId,
                statusDari         = "STEAM_IN_PROGRESS",
                statusBaru         = "STEAM_DONE",
                uid                = uid,
                nama               = nama,
                pcsBerhasil        = pcsBerhasil,
                pcsReject          = pcsReject,
                detailRejectUkuran = detailRejectUkuran,
                catatan            = catatan
            ).onSuccess { loadBatches(); onResult(true) }
             .onFailure { Timber.e(it, "Gagal selesai steam"); onResult(false) }
        }
    }

    fun clearError() { _error.value = null }
}
