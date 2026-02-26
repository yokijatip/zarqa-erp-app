package com.yoki.zarqaproduction.ui.worker.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoki.zarqaproduction.data.model.BarangKeluar
import com.yoki.zarqaproduction.data.model.StokBarangJadi
import com.yoki.zarqaproduction.data.repository.BarangJadiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class AdminViewModel : ViewModel() {

    private val repository = BarangJadiRepository()

    private val _stokList = MutableStateFlow<List<StokBarangJadi>>(emptyList())
    val stokList: StateFlow<List<StokBarangJadi>> = _stokList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init { loadStok() }

    fun loadStok() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getStokBarangJadi()
                .onSuccess { _stokList.value = it }
                .onFailure {
                    Timber.e(it, "Gagal load stok barang jadi")
                    _error.value = "Gagal memuat stok. Coba refresh."
                }
            _isLoading.value = false
        }
    }

    fun catatKeluar(
        barangKeluar: BarangKeluar,
        stokIdPerUkuran: Map<String, Int>,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.catatBarangKeluar(barangKeluar, stokIdPerUkuran)
                .onSuccess {
                    loadStok()
                    onResult(true)
                }.onFailure {
                    Timber.e(it, "Gagal catat barang keluar")
                    onResult(false)
                }
            _isSubmitting.value = false
        }
    }

    fun clearError() { _error.value = null }
}
